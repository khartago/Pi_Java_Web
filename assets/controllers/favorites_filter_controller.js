import { Controller } from '@hotwired/stimulus';
import { FavoritesStore } from '../favorites_store.js';

const FILTER_KEY = 'farmtech_fav_filter_active';

export default class extends Controller {
    static targets = ['toggle'];

    connect() {
        this._active = localStorage.getItem(FILTER_KEY) === '1';
        this._listener = () => this._apply();
        window.addEventListener('favorites:changed', this._listener);
        this._syncToggle();
        this._apply();
    }

    disconnect() {
        window.removeEventListener('favorites:changed', this._listener);
    }

    toggle() {
        this._active = !this._active;
        localStorage.setItem(FILTER_KEY, this._active ? '1' : '0');
        this._syncToggle();
        this._apply();
    }

    _syncToggle() {
        this.toggleTarget.classList.toggle('is-active', this._active);
        this.toggleTarget.setAttribute('aria-pressed', this._active ? 'true' : 'false');
    }

    _apply() {
        const cards = this.element.querySelectorAll('[data-product-id]');
        const favIds = new Set(FavoritesStore.ids().map(String));

        cards.forEach(card => {
            const id = card.dataset.productId;
            const hide = this._active && !favIds.has(id);
            card.classList.toggle('is-hidden-by-filter', hide);
        });

        // Empty state
        const visible = Array.from(cards).filter(c => !c.classList.contains('is-hidden-by-filter'));
        const empty = this.element.querySelector('.market-empty-favs');
        if (empty) empty.hidden = !this._active || visible.length > 0;
    }
}
