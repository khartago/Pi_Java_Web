import { Controller } from '@hotwired/stimulus';
import { FavoritesStore } from '../favorites_store.js';

export default class extends Controller {
    static targets = ['count'];

    connect() {
        this._update();
        this._listener = () => this._update();
        window.addEventListener('favorites:changed', this._listener);
    }

    disconnect() {
        window.removeEventListener('favorites:changed', this._listener);
    }

    _update() {
        const n = FavoritesStore.count();
        if (this.hasCountTarget) {
            this.countTarget.textContent = n > 99 ? '99+' : n;
            this.countTarget.hidden = n === 0;
        }
        this.element.classList.toggle('has-favorites', n > 0);
    }
}
