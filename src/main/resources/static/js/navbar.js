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
(function () {
    'use strict';

    /* ---------- Barra superior: opaca al hacer scroll ---------- */
    var barras = document.querySelectorAll('[data-nav-scroll]');

    function alDesplazar() {
        var bajado = window.scrollY > 60;
        barras.forEach(function (barra) {
            barra.classList.toggle('is-scrolled', bajado);
        });
    }

    if (barras.length) {
        window.addEventListener('scroll', alDesplazar, { passive: true });
        alDesplazar();
    }

    /* ---------- Óvalo móvil: indicador deslizante ---------- */
    var pill = document.querySelector('[data-pill]');
    var indicador = pill ? pill.querySelector('.hs-pill__indicador') : null;
    var itemsPill = pill ? Array.prototype.slice.call(pill.querySelectorAll('.hs-pill__item')) : [];

    function itemActivo() {
        return pill.querySelector('.hs-pill__item.is-active');
    }

    function colocarSobre(item) {
        if (!item) {
            indicador.style.opacity = '0';
            return;
        }
        indicador.style.left = item.offsetLeft + 'px';
        indicador.style.width = item.offsetWidth + 'px';
        indicador.style.opacity = '1';
    }

    /* Colocación inicial y tras redimensionar: sin animación. */
    function colocarSinAnimar() {
        indicador.classList.add('sin-transicion');
        colocarSobre(itemActivo());
        void indicador.offsetWidth; // fuerza el reflow antes de reactivar la transición
        indicador.classList.remove('sin-transicion');
    }

    /* Mueve el indicador en dos fases: estirar y encoger. */
    function moverA(item) {
        var anterior = itemActivo();
        itemsPill.forEach(function (i) { i.classList.remove('is-active'); });
        item.classList.add('is-active');

        if (!anterior || anterior === item) {
            colocarSobre(item);
            return;
        }
        var izquierda = Math.min(anterior.offsetLeft, item.offsetLeft);
        var derecha = Math.max(anterior.offsetLeft + anterior.offsetWidth, item.offsetLeft + item.offsetWidth);
        indicador.style.left = izquierda + 'px';
        indicador.style.width = (derecha - izquierda) + 'px';
        window.setTimeout(function () { colocarSobre(item); }, 220);
    }

    if (pill && indicador) {
        colocarSinAnimar();
        window.addEventListener('resize', colocarSinAnimar);
        if (document.fonts && document.fonts.ready) {
            document.fonts.ready.then(colocarSinAnimar); // el ancho cambia al cargar Outfit
        }
        itemsPill.forEach(function (item) {
            item.addEventListener('click', function () { moverA(item); });
        });
    }

    /* ---------- Marcar sección activa (barra y óvalo) ---------- */
    window.hsNavActivar = function (seccion) {
        document.querySelectorAll('.hs-nav__enlaces a[data-seccion]').forEach(function (enlace) {
            enlace.classList.toggle('is-active', enlace.dataset.seccion === seccion);
        });
        if (!pill || !indicador) {
            return;
        }
        var destino = itemsPill.filter(function (i) { return i.dataset.seccion === seccion; })[0];
        if (destino && !destino.classList.contains('is-active')) {
            moverA(destino);
        }
    };
})();
