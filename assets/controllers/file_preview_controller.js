import { Controller } from '@hotwired/stimulus';

export default class extends Controller {
    static targets = ['input', 'preview', 'name'];
    static values = {
        placeholder: String,
    };

    connect() {
        if (this.hasNameTarget && !this.nameTarget.textContent.trim()) {
            this.nameTarget.textContent = 'Aucun nouveau fichier sélectionné';
        }
    }

    update() {
        const file = this.inputTarget.files[0];

        if (!file) {
            this.previewTarget.src = this.placeholderValue;
            this.nameTarget.textContent = 'Aucun nouveau fichier sélectionné';

            return;
        }

        this.nameTarget.textContent = `${file.name} (${Math.round(file.size / 1024)} Ko)`;

        const reader = new FileReader();
        reader.onload = (event) => {
            this.previewTarget.src = event.target?.result ?? this.placeholderValue;
        };
        reader.readAsDataURL(file);
    }
}
