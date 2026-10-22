/* =====================================================================
   HÖSÉG Web · Página principal
   - Carrusel de destacados: auto-avance cada 5 s, pausa al pasar el
     cursor, controles, puntos y deslizamiento táctil. El ancho de cada
     tarjeta lo fija CSS (--visibles); aquí solo se desplaza la pista.
   - Contadores de impacto: conteo animado al entrar en pantalla.
   - Scroll-spy: marca en la navegación la sección visible.
   Requiere navbar.js cargado antes (window.hsNavActivar).
   ===================================================================== */
(() => {
    'use strict';

    const menosMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /* ---------- Carrusel de destacados ---------- */
    const iniciarCarrusel = (carrusel) => {
        const UMBRAL_DESLIZAR = 40; // px de desplazamiento táctil para cambiar de tarjeta
        const seccion = carrusel.closest('section');
        const pista = carrusel.querySelector('.hs-carrusel__pista');
        const items = pista.children;
        const puntos = seccion.querySelector('[data-carrusel-puntos]');
        const intervalo = parseInt(carrusel.dataset.intervalo, 10) || 5000;
        let indice = 0;
        let temporizador = null;
        let pausado = false;
        let toqueInicioX = null;

        const visibles = () => parseFloat(getComputedStyle(carrusel).getPropertyValue('--visibles')) || 1;

        /* Última posición útil: no se avanza hasta dejar hueco vacío a la derecha. */
        const indiceMaximo = () => Math.max(0, Math.ceil(items.length - visibles()));

        const separacion = () => {
            const estilo = getComputedStyle(pista);
            return parseFloat(estilo.columnGap || estilo.gap) || 16;
        };

        const pintar = () => {
            if (!items.length) {
                return;
            }
            const paso = items[0].offsetWidth + separacion();
            pista.style.transform = `translateX(${-indice * paso}px)`;
            [...puntos.children].forEach((boton, i) => {
                boton.classList.toggle('is-active', i === indice);
                boton.setAttribute('aria-selected', String(i === indice));
            });
        };

        const irA = (n) => {
            const maximo = indiceMaximo();
            indice = n > maximo ? 0 : (n < 0 ? maximo : n);
            pintar();
        };

        const siguiente = () => irA(indice + 1);
        const anterior = () => irA(indice - 1);

        const reiniciarAutoavance = () => {
            window.clearInterval(temporizador);
            if (menosMovimiento || indiceMaximo() === 0) {
                return;
            }
            temporizador = window.setInterval(() => {
                if (!pausado) {
                    siguiente();
                }
            }, intervalo);
        };

        /* Toda acción manual reinicia la cuenta del auto-avance. */
        const conReinicio = (accion) => () => {
            accion();
            reiniciarAutoavance();
        };

        const construirPuntos = () => {
            puntos.replaceChildren();
            const total = indiceMaximo() + 1;
            if (total <= 1) {
                return;
            }
            for (let i = 0; i < total; i++) {
                const boton = document.createElement('button');
                boton.type = 'button';
                boton.setAttribute('role', 'tab');
                boton.setAttribute('aria-label', `Ir a la posición ${i + 1}`);
                boton.addEventListener('click', conReinicio(() => irA(i)));
                puntos.appendChild(boton);
            }
        };

        seccion.querySelector('[data-carrusel-anterior]').addEventListener('click', conReinicio(anterior));
        seccion.querySelector('[data-carrusel-siguiente]').addEventListener('click', conReinicio(siguiente));

        carrusel.addEventListener('mouseenter', () => { pausado = true; });
        carrusel.addEventListener('mouseleave', () => { pausado = false; });

        carrusel.addEventListener('touchstart', (e) => {
            toqueInicioX = e.touches[0].clientX;
        }, { passive: true });
        carrusel.addEventListener('touchend', (e) => {
            if (toqueInicioX === null) {
                return;
            }
            const dx = e.changedTouches[0].clientX - toqueInicioX;
            if (dx < -UMBRAL_DESLIZAR) {
                siguiente();
            } else if (dx > UMBRAL_DESLIZAR) {
                anterior();
            }
            toqueInicioX = null;
            reiniciarAutoavance();
        });

        window.addEventListener('resize', () => {
            indice = Math.min(indice, indiceMaximo());
            construirPuntos();
            pintar();
            reiniciarAutoavance();
        });

        construirPuntos();
        pintar();
        reiniciarAutoavance();
    };

    const carrusel = document.querySelector('[data-carrusel]');
    if (carrusel) {
        iniciarCarrusel(carrusel);
    }

    /* ---------- Contadores de impacto ---------- */
    /* Mismo formato que el servidor (#numbers.formatInteger con 'COMMA'): 1,248 */
    const formatear = (n) => String(n).replace(/\B(?=(\d{3})+(?!\d))/g, ',');

    const animarContador = (el) => {
        const objetivo = parseInt(el.dataset.valor, 10) || 0;
        const duracion = 1600;
        let inicio = null;
        if (objetivo === 0) {
            return;
        }
        const paso = (t) => {
            inicio ??= t;
            const progreso = Math.min((t - inicio) / duracion, 1);
            const suavizado = 1 - Math.pow(1 - progreso, 3);
            el.textContent = formatear(Math.floor(objetivo * suavizado));
            if (progreso < 1) {
                window.requestAnimationFrame(paso);
            } else {
                el.textContent = formatear(objetivo);
            }
        };
        el.textContent = '0';
        window.requestAnimationFrame(paso);
    };

    const contadores = document.querySelectorAll('[data-contador]');
    if (contadores.length && !menosMovimiento && 'IntersectionObserver' in window) {
        const observadorContadores = new IntersectionObserver((entradas) => {
            entradas
                .filter((entrada) => entrada.isIntersecting)
                .forEach((entrada) => {
                    animarContador(entrada.target);
                    observadorContadores.unobserve(entrada.target);
                });
        }, { threshold: 0.3 });
        contadores.forEach((c) => observadorContadores.observe(c));
    }

    /* ---------- Scroll-spy ---------- */
    const secciones = document.querySelectorAll('[data-nav]');
    if (secciones.length && typeof window.hsNavActivar === 'function' && 'IntersectionObserver' in window) {
        const observadorSecciones = new IntersectionObserver((entradas) => {
            entradas
                .filter((entrada) => entrada.isIntersecting)
                .forEach((entrada) => window.hsNavActivar(entrada.target.dataset.nav));
        }, { rootMargin: '-40% 0px -55% 0px' }); // franja central de la ventana
        secciones.forEach((s) => observadorSecciones.observe(s));
    }
})();
