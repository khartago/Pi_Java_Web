import { Controller } from '@hotwired/stimulus';

export default class extends Controller {
    static values = {
        message: String,
    };

    submit(event) {
        const message = this.messageValue || 'Confirmer cette action ?';

        if (!window.confirm(message)) {
            event.preventDefault();
        }
    }
}
