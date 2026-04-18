import { Controller } from '@hotwired/stimulus';

export default class extends Controller {
    static targets = [
        'lowCount', 'brokenCount',
        'lowList', 'brokenList',
        'sendBtn', 'feedback',
        'skeleton', 'content',
    ];

    static values = {
        summaryUrl: String,
        sendUrl: String,
    };

    connect() {
        this.loadSummary();
    }

    async loadSummary() {
        try {
            if (this.hasSkeletonTarget) this.skeletonTarget.hidden = false;
            if (this.hasContentTarget)  this.contentTarget.hidden  = true;

            const res  = await fetch(this.summaryUrlValue);
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            const data = await res.json();
            this._renderSummary(data);
        } catch (e) {
            console.error('[alerts] loadSummary error:', e);
            this._showFeedback('Impossible de charger le résumé : ' + e.message, 'error');
        } finally {
            if (this.hasSkeletonTarget) this.skeletonTarget.hidden = true;
            if (this.hasContentTarget)  this.contentTarget.hidden  = false;
        }
    }

    async send() {
        if (this.hasSendBtnTarget) this.sendBtnTarget.disabled = true;
        this._showFeedback('Envoi en cours…', 'loading');

        try {
            const res  = await fetch(this.sendUrlValue, { method: 'POST' });
            const data = await res.json();

            if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`);

            const msg = data.sent
                ? `Alertes envoyées — ${data.low_stock_count} stock bas, ${data.broken_count} pannes.`
                : 'Aucune alerte critique à envoyer.';

            this._showFeedback(msg, data.sent ? 'success' : 'info');
        } catch (e) {
            console.error('[alerts] send error:', e);
            this._showFeedback(e.message || "Echec de l'envoi.", 'error');
        } finally {
            if (this.hasSendBtnTarget) this.sendBtnTarget.disabled = false;
        }
    }

    // ── private ──────────────────────────────────────────────

    _renderSummary(data) {
        const lowCount    = data.low_stock_count  ?? 0;
        const brokenCount = data.broken_count     ?? 0;
        const lowProducts = data.low_stock_products ?? [];
        const brokenMats  = data.broken_materiels   ?? [];

        if (this.hasLowCountTarget) {
            this.lowCountTargets.forEach(el => { el.textContent = lowCount; });
        }
        if (this.hasBrokenCountTarget) {
            this.brokenCountTargets.forEach(el => { el.textContent = brokenCount; });
        }

        const lowStat = this.element.querySelector('.alert-stat--low');
        if (lowStat) lowStat.classList.toggle('is-alert', lowCount > 0);
        const brokenStat = this.element.querySelector('.alert-stat--broken');
        if (brokenStat) brokenStat.classList.toggle('is-alert', brokenCount > 0);

        if (this.hasLowListTarget)    this.lowListTarget.innerHTML    = this._renderProducts(lowProducts);
        if (this.hasBrokenListTarget) this.brokenListTarget.innerHTML = this._renderMateriels(brokenMats);
    }

    _renderProducts(items) {
        if (!items.length) return '<p class="alert-empty">Aucun produit en rupture de stock.</p>';
        return items.map(p => `
            <div class="alert-item">
                <span class="alert-item__name">${this._esc(p.nom)}</span>
                <span class="alert-item__badge alert-item__badge--warning">${p.quantite} ${this._esc(p.unite)}</span>
            </div>`).join('');
    }

    _renderMateriels(items) {
        if (!items.length) return '<p class="alert-empty">Aucun matériel en panne.</p>';
        return items.map(m => `
            <div class="alert-item">
                <span class="alert-item__name">${this._esc(m.nom)}</span>
                <span class="alert-item__badge alert-item__badge--danger">panne</span>
                ${m.produit ? `<span class="alert-item__sub">${this._esc(m.produit)}</span>` : ''}
            </div>`).join('');
    }

    _showFeedback(msg, type) {
        if (!this.hasFeedbackTarget) return;
        const el = this.feedbackTarget;
        el.textContent = msg;
        el.className   = `alerts-feedback alerts-feedback--${type}`;
        el.hidden      = false;
        if (type === 'success' || type === 'info') {
            setTimeout(() => { el.hidden = true; }, 5000);
        }
    }

    _esc(str) {
        return String(str ?? '').replace(/[&<>"']/g,
            c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
    }
}
