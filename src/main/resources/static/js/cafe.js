/* =====================================================================
   HÖSÉG Web · Höség Café
   Carta con pestañas por categoría: se ve una categoría a la vez y se
   cambia con clic o con el teclado (← → Inicio Fin), según el patrón de
   pestañas de ARIA. Todo ocurre en el navegador: la carta completa ya
   viene en la página. Sin JavaScript se ven todas las categorías.
   ===================================================================== */
(() => {
    'use strict';

    const carta = document.querySelector('[data-carta]');
    const barra = carta?.querySelector('[data-carta-pestanas]');
    if (!carta || !barra) {
        return;
    }

    const pestanas = [...barra.querySelectorAll('[role=tab]')];
    const paneles = pestanas.map((p) => document.getElementById(p.getAttribute('aria-controls')));
    if (pestanas.length < 2) {
        return;   // con una sola categoría no hacen falta pestañas
    }

    const elegir = (indice, enfocar = false) => {
        pestanas.forEach((pestana, i) => {
            const activa = i === indice;
            pestana.setAttribute('aria-selected', String(activa));
            pestana.tabIndex = activa ? 0 : -1;
            paneles[i].hidden = !activa;
        });
        const actual = pestanas[indice];
        if (enfocar) {
            actual.focus({ preventScroll: true });
        }
    };

    /* En móvil la fila de pestañas se desliza: deja visible la elegida
       (solo en horizontal, la página no se mueve). */
    const mostrarPestana = (indice) => {
        const pestana = pestanas[indice];
        const izquierda = pestana.offsetLeft - (barra.clientWidth - pestana.offsetWidth) / 2;
        const menosMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        barra.scrollTo({ left: Math.max(0, izquierda), behavior: menosMovimiento ? 'auto' : 'smooth' });
    };

    barra.hidden = false;
    carta.classList.add('is-pestanas');
    paneles.forEach((panel) => panel.setAttribute('tabindex', '0'));

    barra.addEventListener('click', (e) => {
        const pestana = e.target.closest('[role=tab]');
        if (pestana) {
            elegir(pestanas.indexOf(pestana));
            mostrarPestana(pestanas.indexOf(pestana));
        }
    });

    barra.addEventListener('keydown', (e) => {
        const actual = pestanas.indexOf(document.activeElement);
        if (actual < 0) {
            return;
        }
        const destinos = {
            ArrowRight: (actual + 1) % pestanas.length,
            ArrowLeft: (actual - 1 + pestanas.length) % pestanas.length,
            Home: 0,
            End: pestanas.length - 1
        };
        if (e.key in destinos) {
            e.preventDefault();
            elegir(destinos[e.key], true);
            mostrarPestana(destinos[e.key]);
        }
    });

    elegir(0);
})();
