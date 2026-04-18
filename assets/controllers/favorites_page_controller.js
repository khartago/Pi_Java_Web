import { Controller } from '@hotwired/stimulus';
import { FavoritesStore } from '../favorites_store.js';

export default class extends Controller {
    static targets = ['grid', 'empty', 'count', 'collectionTabs', 'exportBtn', 'shareBtn', 'shareResult'];
    static values = { batchUrl: String };

    connect() {
        this._activeCollection = 'Toutes';
        this._items = {}; // id → product data

        // Import from URL if present (?fav=1,2,3)
        const urlParams = new URLSearchParams(window.location.search);
        const favParam = urlParams.get('fav');
        if (favParam) {
            const added = FavoritesStore.importFromUrl(favParam);
            if (added > 0) this._flash(`${added} favori(s) importé(s) depuis le lien partagé.`);
            history.replaceState(null, '', window.location.pathname);
        }

        this._listener = () => this._refresh();
        window.addEventListener('favorites:changed', this._listener);
        this._refresh();
    }

    disconnect() {
        window.removeEventListener('favorites:changed', this._listener);
    }

    filterCollection(event) {
        const target = event.currentTarget;
        this._activeCollection = target.dataset.collection;
        this.collectionTabsTarget.querySelectorAll('[data-collection]').forEach(el =>
            el.classList.toggle('is-active', el === target));
        this._renderGrid();
    }

    async remove(event) {
        const id = parseInt(event.currentTarget.dataset.productId, 10);
        if (id) FavoritesStore.remove(id);
    }

    changeCollection(event) {
        const id = parseInt(event.currentTarget.dataset.productId, 10);
        const newCol = event.currentTarget.value;
        FavoritesStore.update(id, { collection: newCol });
    }

    editNote(event) {
        const id = parseInt(event.currentTarget.dataset.productId, 10);
        const current = FavoritesStore.read()[String(id)]?.note || '';
        const note = prompt('Note personnelle :', current);
        if (note !== null) FavoritesStore.update(id, { note });
    }

    addCollection() {
        const name = prompt('Nom de la nouvelle collection :');
        if (name && FavoritesStore.addCollection(name)) {
            this._flash(`Collection « ${name} » créée.`);
        }
    }

    clearAll() {
        if (!FavoritesStore.count()) return;
        if (!confirm(`Retirer les ${FavoritesStore.count()} favoris ?`)) return;
        for (const id of FavoritesStore.ids()) FavoritesStore.remove(id);
    }

    export() {
        if (!FavoritesStore.count()) { this._flash('Aucun favori à exporter.'); return; }
        const csv = FavoritesStore.exportCsv();
        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `farmtech-favoris-${new Date().toISOString().slice(0,10)}.csv`;
        a.click();
        URL.revokeObjectURL(url);
        this._flash('Export téléchargé.');
    }

    async share() {
        if (!FavoritesStore.count()) { this._flash('Aucun favori à partager.'); return; }
        const shareUrl = FavoritesStore.shareUrl(window.location.origin + window.location.pathname);
        try {
            await navigator.clipboard.writeText(shareUrl);
            this.shareResultTarget.textContent = '✓ Lien copié : ' + shareUrl;
            this.shareResultTarget.hidden = false;
            setTimeout(() => { this.shareResultTarget.hidden = true; }, 6000);
        } catch {
            prompt('Copiez ce lien :', shareUrl);
        }
    }

    // ── private ──────────────────────────────────────────────

    async _refresh() {
        const ids = FavoritesStore.ids();
        this.countTarget.textContent = ids.length;

        if (ids.length === 0) {
            this.gridTarget.hidden  = true;
            this.emptyTarget.hidden = false;
            this._renderTabs([]);
            return;
        }

        this.emptyTarget.hidden = true;
        this.gridTarget.hidden  = false;

        try {
            const res  = await fetch(`${this.batchUrlValue}?ids=${ids.join(',')}`);
            const data = await res.json();
            this._items = Object.fromEntries((data.items || []).map(p => [p.id, p]));
            this._renderTabs(ids);
            this._renderGrid();
        } catch (e) {
            console.error('[favorites] fetch error:', e);
            this.gridTarget.innerHTML = '<p class="fav-error">Impossible de charger les favoris.</p>';
        }
    }

    _renderTabs(ids) {
        const favData = FavoritesStore.read();
        const colCounts = { 'Toutes': ids.length };
        for (const col of FavoritesStore.collections()) colCounts[col] = 0;
        for (const id of ids) {
            const c = favData[String(id)]?.collection;
            if (c && colCounts[c] !== undefined) colCounts[c]++;
        }

        const lowStockIds = ids.filter(id => this._items[id]?.isLowStock);
        const expiringIds = ids.filter(id => this._items[id]?.isExpiringSoon || this._items[id]?.isExpired);

        const tabsHtml = Object.entries(colCounts).map(([name, count]) => `
            <button type="button"
                    class="fav-tab ${name === this._activeCollection ? 'is-active' : ''}"
                    data-collection="${this._esc(name)}"
                    data-action="favorites-page#filterCollection">
                ${this._esc(name)} <span>${count}</span>
            </button>`).join('');

        const alertTabs = `
            <button type="button"
                    class="fav-tab fav-tab--warning ${this._activeCollection === '__lowStock' ? 'is-active' : ''}"
                    data-collection="__lowStock"
                    data-action="favorites-page#filterCollection">
                ⚠ Stock bas <span>${lowStockIds.length}</span>
            </button>
            <button type="button"
                    class="fav-tab fav-tab--warning ${this._activeCollection === '__expiring' ? 'is-active' : ''}"
                    data-collection="__expiring"
                    data-action="favorites-page#filterCollection">
                📅 Expirations <span>${expiringIds.length}</span>
            </button>`;

        this.collectionTabsTarget.innerHTML = tabsHtml + alertTabs;
    }

    _renderGrid() {
        const favData = FavoritesStore.read();
        const ids = FavoritesStore.ids();
        const collections = FavoritesStore.collections();

        const filtered = ids.filter(id => {
            if (this._activeCollection === 'Toutes')     return true;
            if (this._activeCollection === '__lowStock') return this._items[id]?.isLowStock;
            if (this._activeCollection === '__expiring') return this._items[id]?.isExpiringSoon || this._items[id]?.isExpired;
            return favData[String(id)]?.collection === this._activeCollection;
        });

        if (!filtered.length) {
            this.gridTarget.innerHTML = '<p class="fav-empty-filter">Aucun favori dans cette collection.</p>';
            return;
        }

        this.gridTarget.innerHTML = filtered.map(id => {
            const p   = this._items[id];
            const fav = favData[String(id)];
            if (!p) return '';
            const opts = collections.map(c =>
                `<option value="${this._esc(c)}" ${c === fav.collection ? 'selected' : ''}>${this._esc(c)}</option>`
            ).join('');

            const badges = [];
            if (p.isOutOfStock)       badges.push('<span class="fav-alert-badge fav-alert-badge--danger">🔴 Rupture</span>');
            else if (p.isLowStock)    badges.push('<span class="fav-alert-badge fav-alert-badge--warning">⚠ Stock bas</span>');
            if (p.isExpired)          badges.push('<span class="fav-alert-badge fav-alert-badge--danger">❌ Expiré</span>');
            else if (p.isExpiringSoon) badges.push('<span class="fav-alert-badge fav-alert-badge--warning">📅 Expire bientôt</span>');

            const cardClass = 'fav-card'
                + (p.isLowStock || p.isOutOfStock ? ' fav-card--low-stock' : '')
                + (p.isExpired || p.isExpiringSoon ? ' fav-card--expiring' : '');

            return `
                <article class="${cardClass}">
                    <figure class="fav-card__media">
                        <img src="/${this._esc(p.imagePath)}" alt="${this._esc(p.nom)}" loading="lazy">
                        ${badges.length ? `<div class="fav-card__badges">${badges.join('')}</div>` : ''}
                    </figure>
                    <div class="fav-card__body">
                        <h3 class="fav-card__title">${this._esc(p.nom)}</h3>
                        <p class="fav-card__meta">${p.quantite} ${this._esc(p.unite)} · ${p.materielCount} matériel(s)</p>
                        <p class="fav-card__expiry">Expire : ${this._esc(p.dateExpiration)}</p>

                        <label class="fav-card__col-label">Collection</label>
                        <select class="fav-card__select"
                                data-product-id="${id}"
                                data-action="change->favorites-page#changeCollection">
                            ${opts}
                        </select>

                        ${fav.note ? `<p class="fav-card__note">📝 ${this._esc(fav.note)}</p>` : ''}

                        <div class="fav-card__actions">
                            <button type="button" class="fav-card__btn" data-product-id="${id}" data-action="favorites-page#editNote">
                                ${fav.note ? 'Modifier note' : 'Ajouter note'}
                            </button>
                            <a class="fav-card__btn" href="/produits/${id}">Fiche</a>
                            <button type="button" class="fav-card__btn fav-card__btn--danger" data-product-id="${id}" data-action="favorites-page#remove">
                                Retirer
                            </button>
                        </div>
                    </div>
                </article>`;
        }).join('');
    }

    _flash(msg) {
        this.shareResultTarget.textContent = msg;
        this.shareResultTarget.hidden = false;
        setTimeout(() => { this.shareResultTarget.hidden = true; }, 4000);
    }

    _esc(str) {
        return String(str ?? '').replace(/[&<>"']/g,
            c => ({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c]));
    }
}
