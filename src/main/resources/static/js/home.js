/* =====================================================================
   HÖSÉG Web · Página principal
   - Carrusel de destacados: auto-avance cada 5 s, pausa al pasar el
     cursor, controles, puntos y deslizamiento táctil. El ancho de cada
     tarjeta lo fija CSS (--visibles); aquí solo se desplaza la pista.
   - Contadores de impacto: conteo animado al entrar en pantalla.
   - Scroll-spy: marca en la navegación la sección visible.
   Requiere navbar.js cargado antes (window.hsNavActivar).
   ===================================================================== */
(function () {
    'use strict';

    var menosMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /* ---------- Carrusel de destacados ---------- */
    var carrusel = document.querySelector('[data-carrusel]');

    if (carrusel) {
        var pista = carrusel.querySelector('.hs-carrusel__pista');
        var items = pista.children;
        var puntos = document.querySelector('[data-carrusel-puntos]');
        var intervalo = parseInt(carrusel.dataset.intervalo, 10) || 5000;
        var indice = 0;
        var temporizador = null;
        var pausado = false;
        var toqueInicioX = null;

        function visibles() {
            return parseFloat(getComputedStyle(carrusel).getPropertyValue('--visibles')) || 1;
        }

        /* Última posición útil: no se avanza hasta dejar hueco vacío a la derecha. */
        function indiceMaximo() {
            return Math.max(0, Math.ceil(items.length - visibles()));
        }

        function separacion() {
            var estilo = getComputedStyle(pista);
            return parseFloat(estilo.columnGap || estilo.gap) || 16;
        }

        function pintar() {
            if (!items.length) {
                return;
            }
            var paso = items[0].offsetWidth + separacion();
            pista.style.transform = 'translateX(' + (-indice * paso) + 'px)';
            Array.prototype.forEach.call(puntos.children, function (boton, i) {
                boton.classList.toggle('is-active', i === indice);
                boton.setAttribute('aria-selected', i === indice ? 'true' : 'false');
            });
        }

        function irA(n) {
            var maximo = indiceMaximo();
            indice = n > maximo ? 0 : (n < 0 ? maximo : n);
            pintar();
        }

        function siguiente() { irA(indice + 1); }
        function anterior()  { irA(indice - 1); }

        function reiniciarAutoavance() {
            window.clearInterval(temporizador);
            if (menosMovimiento || indiceMaximo() === 0) {
                return;
            }
            temporizador = window.setInterval(function () {
                if (!pausado) {
                    siguiente();
                }
            }, intervalo);
        }

        function construirPuntos() {
            puntos.innerHTML = '';
            var total = indiceMaximo() + 1;
            if (total <= 1) {
                return;
            }
            for (var i = 0; i < total; i++) {
                var boton = document.createElement('button');
                boton.type = 'button';
                boton.setAttribute('role', 'tab');
                boton.setAttribute('aria-label', 'Ir a la posición ' + (i + 1));
                boton.addEventListener('click', (function (destino) {
                    return function () { irA(destino); reiniciarAutoavance(); };
                })(i));
                puntos.appendChild(boton);
            }
        }

        carrusel.closest('section').querySelector('[data-carrusel-anterior]')
            .addEventListener('click', function () { anterior(); reiniciarAutoavance(); });
        carrusel.closest('section').querySelector('[data-carrusel-siguiente]')
            .addEventListener('click', function () { siguiente(); reiniciarAutoavance(); });

        carrusel.addEventListener('mouseenter', function () { pausado = true; });
        carrusel.addEventListener('mouseleave', function () { pausado = false; });

        carrusel.addEventListener('touchstart', function (e) {
            toqueInicioX = e.touches[0].clientX;
        }, { passive: true });
        carrusel.addEventListener('touchend', function (e) {
            if (toqueInicioX === null) {
                return;
            }
            var dx = e.changedTouches[0].clientX - toqueInicioX;
            if (dx < -40) {
                siguiente();
            } else if (dx > 40) {
                anterior();
            }
            toqueInicioX = null;
            reiniciarAutoavance();
        });

        window.addEventListener('resize', function () {
            indice = Math.min(indice, indiceMaximo());
            construirPuntos();
            pintar();
            reiniciarAutoavance();
        });

        construirPuntos();
        pintar();
        reiniciarAutoavance();
    }

    /* ---------- Contadores de impacto ---------- */
    function formatear(n) {
        return String(n).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }

    function animarContador(el) {
        var objetivo = parseInt(el.dataset.valor, 10) || 0;
        var duracion = 1600;
        var inicio = null;
        if (objetivo === 0) {
            return;
        }
        function paso(t) {
            if (inicio === null) {
                inicio = t;
            }
            var progreso = Math.min((t - inicio) / duracion, 1);
            var suavizado = 1 - Math.pow(1 - progreso, 3);
            el.textContent = formatear(Math.floor(objetivo * suavizado));
            if (progreso < 1) {
                window.requestAnimationFrame(paso);
            } else {
                el.textContent = formatear(objetivo);
            }
        }
        el.textContent = '0';
        window.requestAnimationFrame(paso);
    }

    var contadores = document.querySelectorAll('[data-contador]');
    if (contadores.length && !menosMovimiento && 'IntersectionObserver' in window) {
        var observadorContadores = new IntersectionObserver(function (entradas) {
            entradas.forEach(function (entrada) {
                if (entrada.isIntersecting) {
                    animarContador(entrada.target);
                    observadorContadores.unobserve(entrada.target);
                }
            });
        }, { threshold: 0.3 });
        contadores.forEach(function (c) { observadorContadores.observe(c); });
    }

    /* ---------- Scroll-spy ---------- */
    var secciones = document.querySelectorAll('[data-nav]');
    if (secciones.length && typeof window.hsNavActivar === 'function' && 'IntersectionObserver' in window) {
        var observadorSecciones = new IntersectionObserver(function (entradas) {
            entradas.forEach(function (entrada) {
                if (entrada.isIntersecting) {
                    window.hsNavActivar(entrada.target.dataset.nav);
                }
            });
        }, { rootMargin: '-40% 0px -55% 0px' }); // franja central de la ventana
        secciones.forEach(function (s) { observadorSecciones.observe(s); });
    }
})();
