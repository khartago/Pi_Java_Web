import { Controller } from '@hotwired/stimulus';
import { FavoritesStore } from '../favorites_store.js';

export default class extends Controller {
    static values = { productId: Number };

    connect() {
        this._sync();
        this._listener = () => this._sync();
        window.addEventListener('favorites:changed', this._listener);
    }

    disconnect() {
        window.removeEventListener('favorites:changed', this._listener);
    }

    toggle(event) {
        event.preventDefault();
        event.stopPropagation();
        const nowFav = FavoritesStore.toggle(this.productIdValue);
        if (nowFav) {
            this.element.classList.add('is-pulsing');
            setTimeout(() => this.element.classList.remove('is-pulsing'), 600);
        }
    }

    _sync() {
        const isFav = FavoritesStore.has(this.productIdValue);
        this.element.classList.toggle('is-favorited', isFav);
        this.element.setAttribute('aria-pressed', isFav ? 'true' : 'false');
        this.element.setAttribute('aria-label', isFav ? 'Retirer des favoris' : 'Ajouter aux favoris');
    }
}
