/* =====================================================================
   HÖSÉG Web · Nosotros: locales
   El abanico (escritorio) y las píldoras (móvil) eligen el mismo local;
   solo se ve la ficha del elegido. Abrir el abanico es CSS (:hover y
   :focus-within); aquí solo se marca la elección (aria-pressed) y se
   muestra la ficha. Sin JavaScript se ven todas las fichas, con su foto.
   ===================================================================== */
(() => {
    'use strict';

    const locales = document.querySelector('[data-locales]');
    if (!locales) {
        return;
    }

    const abanico = locales.querySelector('[data-locales-abanico]');
    const pildoras = locales.querySelector('[data-locales-pildoras]');
    const botones = [...locales.querySelectorAll('button[data-local]')];
    const fichas = [...locales.querySelectorAll('[data-ficha]')];
    if (!fichas.length) {
        return;
    }

    const elegir = (clave) => {
        botones.forEach((b) => b.setAttribute('aria-pressed', String(b.dataset.local === clave)));
        fichas.forEach((f) => { f.hidden = f.dataset.ficha !== clave; });
    };

    abanico.hidden = false;
    pildoras.hidden = false;
    locales.classList.add('is-interactivo');

    botones.forEach((boton) => boton.addEventListener('click', () => elegir(boton.dataset.local)));

    elegir(fichas[0].dataset.ficha);
})();
