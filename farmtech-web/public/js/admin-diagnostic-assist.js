(function () {
    function byId(id) {
        return document.getElementById(id);
    }

    /**
     * Champs du formulaire diagnostic (Symfony met souvent attr id sur le div form_row, pas sur le widget).
     */
    function diagField(form, name) {
        if (!form) return null;
        var sel = '[name="diagnostique[' + name + ']"]';
        var el = form.querySelector(sel);
        if (el && (el.tagName === 'TEXTAREA' || el.tagName === 'INPUT')) {
            return el;
        }
        return null;
    }

    document.addEventListener('click', function (e) {
        var btn = e.target && e.target.closest && e.target.closest('.js-copy-similaire');
        if (!btn) return;
        var cause = btn.getAttribute('data-cause') || '';
        var solution = btn.getAttribute('data-solution') || '';
        var med = btn.getAttribute('data-medicament') || '';
        var form = byId('diagnostic-form');
        var c = diagField(form, 'cause');
        var s = diagField(form, 'solutionProposee');
        var m = diagField(form, 'medicament');
        if (c) c.value = cause;
        if (s) s.value = solution;
        if (m) m.value = med;
        var r = diagField(form, 'resultat');
        if (r && !r.value) r.value = 'En attente';
    });

    document.addEventListener('DOMContentLoaded', function () {
        var aiBtn = byId('btn-ai-suggest');
        if (!aiBtn) return;
        var pidEl = byId('ai-probleme-id');
        var csrfEl = byId('ai-csrf');
        var status = byId('ai-status');
        aiBtn.addEventListener('click', function () {
            var pid = pidEl ? pidEl.value : '';
            var token = csrfEl ? csrfEl.value : '';
            if (!pid || !token) return;
            aiBtn.disabled = true;
            if (status) status.textContent = 'Génération en cours…';
            var body = new URLSearchParams();
            body.set('_token', token);
            body.set('probleme', pid);
            fetch(
                (typeof window.FARMTECH_DIAG_AI_URL === 'string' && window.FARMTECH_DIAG_AI_URL) ||
                    '/admin/diagnostics/ai-suggest',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        Accept: 'application/json',
                    },
                    body: body.toString(),
                    credentials: 'same-origin',
                }
            )
                .then(function (r) {
                    return r.json().then(function (j) {
                        return { ok: r.ok, j: j };
                    });
                })
                .then(function (x) {
                    if (!x.ok || !x.j || !x.j.ok) {
                        throw new Error((x.j && x.j.error) || 'Erreur IA');
                    }
                    var form = byId('diagnostic-form');
                    var c = diagField(form, 'cause');
                    var s = diagField(form, 'solutionProposee');
                    var m = diagField(form, 'medicament');
                    var res = diagField(form, 'resultat');
                    if (c) c.value = x.j.cause || '';
                    if (s) s.value = x.j.solutionProposee || '';
                    if (m) m.value = x.j.medicament || '';
                    if (res && !res.value) res.value = 'En attente';
                    if (status) status.textContent = 'Suggestion appliquée.';
                })
                .catch(function (err) {
                    if (status) status.textContent = err.message || 'Erreur';
                })
                .finally(function () {
                    aiBtn.disabled = false;
                });
        });
    });
})();
