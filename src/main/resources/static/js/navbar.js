/* =====================================================================
   HÖSÉG Web · Navbar y óvalo móvil
   - La barra superior recibe .is-scrolled al bajar más de 60 px (sobre el
     banner arranca transparente y aquí se vuelve opaca).
   - El indicador del óvalo se coloca sobre el destino activo y, al tocar
     otro, se estira hasta cubrir ambos y luego se encoge sobre el nuevo
     (dos fases de 0,22 s, como en el diseño).
   - Expone window.hsNavActivar(seccion) para que la Home marque la
     sección visible al hacer scroll.
   ===================================================================== */
(() => {
    'use strict';

    const UMBRAL_SCROLL = 60;
    const FASE_INDICADOR = 220; // ms, igual que --hs-t-pill

    /* ---------- Barra superior: opaca al hacer scroll ---------- */
    const barras = document.querySelectorAll('[data-nav-scroll]');

    const alDesplazar = () => {
        const bajado = window.scrollY > UMBRAL_SCROLL;
        barras.forEach((barra) => barra.classList.toggle('is-scrolled', bajado));
    };

    if (barras.length) {
        window.addEventListener('scroll', alDesplazar, { passive: true });
        alDesplazar();
    }

    /* ---------- Óvalo móvil: indicador deslizante ---------- */
    const pill = document.querySelector('[data-pill]');
    const indicador = pill?.querySelector('.hs-pill__indicador') ?? null;
    const itemsPill = pill ? [...pill.querySelectorAll('.hs-pill__item')] : [];

    const itemActivo = () => pill.querySelector('.hs-pill__item.is-active');

    const colocarSobre = (item) => {
        if (!item) {
            indicador.style.opacity = '0';
            return;
        }
        indicador.style.left = `${item.offsetLeft}px`;
        indicador.style.width = `${item.offsetWidth}px`;
        indicador.style.opacity = '1';
    };

    /* Colocación inicial y tras redimensionar: sin animación. */
    const colocarSinAnimar = () => {
        indicador.classList.add('sin-transicion');
        colocarSobre(itemActivo());
        void indicador.offsetWidth; // fuerza el reflow antes de reactivar la transición
        indicador.classList.remove('sin-transicion');
    };

    /* Mueve el indicador en dos fases: estirar y encoger. */
    const moverA = (item) => {
        const anterior = itemActivo();
        itemsPill.forEach((i) => i.classList.remove('is-active'));
        item.classList.add('is-active');

        if (!anterior || anterior === item) {
            colocarSobre(item);
            return;
        }
        const izquierda = Math.min(anterior.offsetLeft, item.offsetLeft);
        const derecha = Math.max(anterior.offsetLeft + anterior.offsetWidth, item.offsetLeft + item.offsetWidth);
        indicador.style.left = `${izquierda}px`;
        indicador.style.width = `${derecha - izquierda}px`;
        window.setTimeout(() => colocarSobre(item), FASE_INDICADOR);
    };

    if (indicador) {
        colocarSinAnimar();
        window.addEventListener('resize', colocarSinAnimar);
        document.fonts?.ready.then(colocarSinAnimar); // el ancho cambia al cargar Outfit
        itemsPill.forEach((item) => item.addEventListener('click', () => moverA(item)));
    }

    /* ---------- Marcar sección activa (barra y óvalo) ---------- */
    window.hsNavActivar = (seccion) => {
        document.querySelectorAll('.hs-nav__enlaces a[data-seccion]').forEach((enlace) => {
            enlace.classList.toggle('is-active', enlace.dataset.seccion === seccion);
        });
        if (!indicador) {
            return;
        }
        const destino = itemsPill.find((i) => i.dataset.seccion === seccion);
        if (destino && !destino.classList.contains('is-active')) {
            moverA(destino);
        }
    };
})();
