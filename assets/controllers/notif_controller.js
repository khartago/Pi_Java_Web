import { Controller } from '@hotwired/stimulus';

const STORAGE_KEY = 'farmtech_seen_alerts';
const POLL_MS     = 30_000;

export default class extends Controller {
    static targets = ['badge', 'panel', 'list'];
    static values  = { url: String };

    connect() {
        this.open  = false;
        this.items = [];
        this.poll();
        this._interval = setInterval(() => this.poll(), POLL_MS);
        document.addEventListener('click', this._onOutsideClick.bind(this));
    }

    disconnect() {
        clearInterval(this._interval);
        document.removeEventListener('click', this._onOutsideClick.bind(this));
    }

    async poll() {
        try {
            const res  = await fetch(this.urlValue);
            const data = await res.json();
            this.items = this._buildItems(data);
            this._render();
        } catch { /* silent — network may be down */ }
    }

    toggle(e) {
        e.stopPropagation();
        this.open = !this.open;
        this.panelTarget.hidden = !this.open;
        if (this.open) this._renderList();
    }

    markAllRead() {
        const seen = this._loadSeen();
        this.items.forEach(item => seen.add(item.key));
        this._saveSeen(seen);
        this._render();
        if (this.open) this._renderList();
    }

    // ── private ──────────────────────────────────────────────

    _buildItems(data) {
        const items = [];
        (data.low_stock_products || []).forEach(p => items.push({
            key:   `p_${p.id}`,
            type:  'warning',
            title: p.nom,
            meta:  `Stock : ${p.quantite} ${p.unite}`,
        }));
        (data.broken_materiels || []).forEach(m => items.push({
            key:   `m_${m.id}`,
            type:  'danger',
            title: m.nom,
            meta:  `Panne${m.produit ? ' · ' + m.produit : ''}`,
        }));
        return items;
    }

    _unreadItems() {
        const seen = this._loadSeen();
        return this.items.filter(i => !seen.has(i.key));
    }

    _render() {
        const count = this._unreadItems().length;
        this.badgeTarget.textContent = count > 99 ? '99+' : count;
        this.badgeTarget.hidden      = count === 0;
        this.element.classList.toggle('has-alerts', count > 0);
    }

    _renderList() {
        const unread = this._unreadItems();
        if (!unread.length) {
            this.listTarget.innerHTML = '<p class="notif-empty">Tout est en ordre.</p>';
            return;
        }
        this.listTarget.innerHTML = unread.map(item => `
            <div class="notif-item notif-item--${item.type}">
                <span class="notif-item__dot"></span>
                <div class="notif-item__body">
                    <p class="notif-item__title">${this._esc(item.title)}</p>
                    <p class="notif-item__meta">${this._esc(item.meta)}</p>
                </div>
            </div>`).join('');
    }

    _onOutsideClick(e) {
        if (this.open && !this.element.contains(e.target)) {
            this.open = false;
            this.panelTarget.hidden = true;
        }
    }

    _loadSeen() {
        try { return new Set(JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')); }
        catch { return new Set(); }
    }

    _saveSeen(set) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify([...set]));
    }

    _esc(str) {
        return String(str ?? '').replace(/[&<>"']/g, c =>
            ({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c]));
    }
}
