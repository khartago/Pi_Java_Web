// Central favorites store — localStorage + cross-tab sync.
const STORAGE_KEY = 'farmtech_favorites_v1';
const COLLECTIONS_KEY = 'farmtech_fav_collections_v1';
const DEFAULT_COLLECTIONS = ['Essentiels', 'À commander', 'Saison'];

export const FavoritesStore = {
    read() {
        try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}'); }
        catch { return {}; }
    },

    _write(data) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
        this._emit();
    },

    has(id) {
        return Boolean(this.read()[String(id)]);
    },

    toggle(id) {
        const data = this.read();
        const key = String(id);
        if (data[key]) {
            delete data[key];
        } else {
            data[key] = {
                addedAt: Date.now(),
                collection: DEFAULT_COLLECTIONS[0],
                note: '',
            };
        }
        this._write(data);
        return Boolean(data[key]);
    },

    remove(id) {
        const data = this.read();
        delete data[String(id)];
        this._write(data);
    },

    update(id, patch) {
        const data = this.read();
        const key = String(id);
        if (data[key]) {
            data[key] = { ...data[key], ...patch };
            this._write(data);
        }
    },

    ids() {
        return Object.keys(this.read()).map(Number);
    },

    count() {
        return Object.keys(this.read()).length;
    },

    collections() {
        try {
            const raw = localStorage.getItem(COLLECTIONS_KEY);
            if (raw) return JSON.parse(raw);
        } catch { /* */ }
        return [...DEFAULT_COLLECTIONS];
    },

    addCollection(name) {
        const n = String(name || '').trim();
        if (!n) return false;
        const cols = this.collections();
        if (cols.includes(n)) return false;
        cols.push(n);
        localStorage.setItem(COLLECTIONS_KEY, JSON.stringify(cols));
        this._emit();
        return true;
    },

    removeCollection(name) {
        const cols = this.collections().filter(c => c !== name);
        localStorage.setItem(COLLECTIONS_KEY, JSON.stringify(cols));
        const fallback = cols[0] || DEFAULT_COLLECTIONS[0];
        const data = this.read();
        for (const id in data) {
            if (data[id].collection === name) data[id].collection = fallback;
        }
        this._write(data);
    },

    exportCsv() {
        const rows = [['id', 'collection', 'note', 'addedAt']];
        const data = this.read();
        for (const id in data) {
            rows.push([
                id,
                data[id].collection || '',
                (data[id].note || '').replace(/"/g, '""'),
                new Date(data[id].addedAt).toISOString(),
            ]);
        }
        return rows.map(r => r.map(c => `"${c}"`).join(',')).join('\n');
    },

    shareUrl(baseUrl) {
        const ids = this.ids().join(',');
        return `${baseUrl}?fav=${encodeURIComponent(ids)}`;
    },

    importFromUrl(ids) {
        const data = this.read();
        let added = 0;
        for (const raw of String(ids).split(',')) {
            const id = parseInt(raw, 10);
            if (id > 0 && !data[String(id)]) {
                data[String(id)] = {
                    addedAt: Date.now(),
                    collection: DEFAULT_COLLECTIONS[0],
                    note: '(importé)',
                };
                added++;
            }
        }
        if (added > 0) this._write(data);
        return added;
    },

    _emit() {
        window.dispatchEvent(new CustomEvent('favorites:changed', {
            detail: { count: this.count(), ids: this.ids() },
        }));
    },
};

// Cross-tab synchronization via storage event
window.addEventListener('storage', (e) => {
    if (e.key === STORAGE_KEY || e.key === COLLECTIONS_KEY) {
        FavoritesStore._emit();
    }
});
