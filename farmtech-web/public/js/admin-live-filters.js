/**
 * Filtres GET en direct (admin) — fichier statique dans public/, pas besoin d’importmap.
 */
(function () {
    function debounce(fn, ms) {
        var t;
        return function () {
            var a = arguments;
            clearTimeout(t);
            t = setTimeout(function () {
                fn.apply(null, a);
            }, ms);
        };
    }

    function submitForm(form) {
        if (typeof form.requestSubmit === 'function') {
            form.requestSubmit();
        } else {
            form.submit();
        }
    }

    function init() {
        var forms = document.querySelectorAll('form[data-live-filter="1"]');
        for (var i = 0; i < forms.length; i++) {
            var form = forms[i];
            if (form.dataset.liveFilterInit === '1') continue;
            form.dataset.liveFilterInit = '1';

            var q = form.querySelector('input[name="q"]');
            if (q) {
                q.addEventListener('input', debounce(function () {
                    submitForm(form);
                }, 380));
            }
            var selects = form.querySelectorAll('select');
            for (var j = 0; j < selects.length; j++) {
                selects[j].addEventListener('change', function () {
                    submitForm(form);
                });
            }

            var dateInputs = form.querySelectorAll('input[type="date"]');
            for (var d = 0; d < dateInputs.length; d++) {
                dateInputs[d].addEventListener('change', function () {
                    submitForm(form);
                });
            }

            var btns = form.querySelectorAll('.btn-filter-fallback');
            for (var k = 0; k < btns.length; k++) {
                btns[k].setAttribute('hidden', 'hidden');
            }
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
