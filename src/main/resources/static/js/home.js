/* =====================================================================
   HÖSÉG Web · Página principal
   - Carrusel de destacados, dos modos:
       · < 992 px, lineal: auto-avance cada 5 s, puntos y deslizamiento
         táctil. El ancho de cada tarjeta lo fija CSS (--visibles); aquí
         solo se desplaza la pista.
       · ≥ 992 px, arco: las tarjetas se reparten en arco alrededor de la
         central (home.css, "Carrusel en arco"). Flechas, arrastre con el
         puntero solo sobre el carrusel y teclado (← → Inicio Fin). Sin
         rueda. Es circular: la tarjeta que da la vuelta viaja oculta.
     En ambos, el auto-avance se pausa con el cursor encima, con el foco
     dentro o mientras se arrastra, y se reinicia tras cada acción manual.
   - Contadores de impacto: conteo animado al entrar en pantalla.
   - Scroll-spy: marca en la navegación la sección visible.
   Requiere navbar.js cargado antes (window.hsNavActivar).
   ===================================================================== */
(() => {
    'use strict';

    const menosMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const consultaArco = window.matchMedia('(min-width: 992px)');

    /* ---------- Carrusel de destacados ---------- */
    const iniciarCarrusel = (carrusel) => {
        const UMBRAL_DESLIZAR = 40;    // px de desplazamiento táctil para cambiar de tarjeta (lineal)
        const UMBRAL_ARRASTRE = 6;     // px antes de considerar que el puntero arrastra (arco)
        const SEPARACION_ARCO = 0.74;  // igual que el 74 % de translateX en home.css
        const seccion = carrusel.closest('section');
        const pista = carrusel.querySelector('.hs-carrusel__pista');
        const items = [...pista.children];
        const total = items.length;
        const puntos = seccion.querySelector('[data-carrusel-puntos]');
        const intervalo = parseInt(carrusel.dataset.intervalo, 10) || 5000;
        let indice = 0;
        let temporizador = null;
        let cursorEncima = false;
        let focoDentro = false;
        let modoArco = false;

        if (!total) {
            return;
        }

        items.forEach((item, i) => {
            item.setAttribute('role', 'group');
            item.setAttribute('aria-roledescription', 'diapositiva');
            item.setAttribute('aria-label', `${i + 1} de ${total}`);
        });

        /* ----- Modo lineal ----- */
        const visibles = () => parseFloat(getComputedStyle(carrusel).getPropertyValue('--visibles')) || 1;

        const separacion = () => {
            const estilo = getComputedStyle(pista);
            return parseFloat(estilo.columnGap || estilo.gap) || 16;
        };

        /* Lineal: última posición útil, sin dejar hueco a la derecha. Arco: cualquier tarjeta. */
        const indiceMaximo = () => (modoArco ? total - 1 : Math.max(0, Math.ceil(total - visibles())));

        const pintarLineal = () => {
            const paso = items[0].offsetWidth + separacion();
            pista.style.transform = `translateX(${-indice * paso}px)`;
        };

        /* ----- Modo arco ----- */
        /* Posición circular de la tarjeta i respecto del centro (puede tener decimales). */
        const posicionEnArco = (i, centro) => {
            let pos = (((i - centro) % total) + total) % total;
            if (pos > total / 2) {
                pos -= total;
            }
            return pos;
        };

        const pintarArco = (desplazamiento = 0) => {
            const centro = indice + desplazamiento;
            items.forEach((item, i) => {
                const pos = posicionEnArco(i, centro);
                const dist = Math.abs(pos);
                item.style.setProperty('--pos', pos.toFixed(3));
                item.style.setProperty('--dist', dist.toFixed(3));
                item.style.setProperty('--dist2', (dist * dist).toFixed(3));
                item.style.zIndex = String(100 - Math.round(dist * 10));
                item.classList.toggle('is-actual', dist < 0.5);
                item.classList.toggle('is-lejos', dist > 2.5);
                item.inert = dist > 2.5;   // lo oculto no recibe foco ni clics
            });
        };

        const limpiarArco = () => {
            items.forEach((item) => {
                ['--pos', '--dist', '--dist2'].forEach((v) => item.style.removeProperty(v));
                item.style.zIndex = '';
                item.classList.remove('is-actual', 'is-lejos');
                item.inert = false;
            });
        };

        /* ----- Común ----- */
        const pintar = () => {
            if (modoArco) {
                pintarArco();
            } else {
                pintarLineal();
            }
            [...puntos.children].forEach((boton, i) => {
                boton.classList.toggle('is-active', i === indice);
                boton.setAttribute('aria-selected', String(i === indice));
            });
        };

        const irA = (n) => {
            if (modoArco) {
                indice = ((n % total) + total) % total;
            } else {
                const maximo = indiceMaximo();
                indice = n > maximo ? 0 : (n < 0 ? maximo : n);
            }
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
                if (!cursorEncima && !focoDentro && !carrusel.classList.contains('is-arrastrando')) {
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
            const cantidad = indiceMaximo() + 1;
            if (cantidad <= 1) {
                return;
            }
            for (let i = 0; i < cantidad; i++) {
                const boton = document.createElement('button');
                boton.type = 'button';
                boton.setAttribute('role', 'tab');
                boton.setAttribute('aria-label', `Ir a la posición ${i + 1}`);
                boton.addEventListener('click', conReinicio(() => irA(i)));
                puntos.appendChild(boton);
            }
        };

        const aplicarModo = () => {
            modoArco = consultaArco.matches;
            carrusel.classList.toggle('hs-carrusel--arco', modoArco);
            if (modoArco) {
                pista.style.transform = '';
                carrusel.tabIndex = 0;
            } else {
                limpiarArco();
                carrusel.removeAttribute('tabindex');
                indice = Math.min(indice, indiceMaximo());
            }
            construirPuntos();
            pintar();
            reiniciarAutoavance();
        };

        seccion.querySelector('[data-carrusel-anterior]').addEventListener('click', conReinicio(anterior));
        seccion.querySelector('[data-carrusel-siguiente]').addEventListener('click', conReinicio(siguiente));

        carrusel.addEventListener('mouseenter', () => { cursorEncima = true; });
        carrusel.addEventListener('mouseleave', () => { cursorEncima = false; });
        carrusel.addEventListener('focusin', (e) => {
            focoDentro = true;
            // Arco: si el foco entra en una tarjeta lateral (Tab), esa pasa al centro
            if (modoArco) {
                const item = e.target.closest('.hs-carrusel__item');
                if (item && !item.classList.contains('is-actual')) {
                    irA(items.indexOf(item));
                }
            }
        });
        carrusel.addEventListener('focusout', (e) => {
            focoDentro = carrusel.contains(e.relatedTarget);
        });

        /* Teclado (arco): el carrusel es enfocable y responde a las flechas */
        carrusel.addEventListener('keydown', (e) => {
            if (!modoArco) {
                return;
            }
            const acciones = {
                ArrowLeft: anterior,
                ArrowRight: siguiente,
                Home: () => irA(0),
                End: () => irA(total - 1)
            };
            if (acciones[e.key]) {
                e.preventDefault();
                conReinicio(acciones[e.key])();
            }
        });

        /* Toque (lineal) */
        let toqueInicioX = null;
        carrusel.addEventListener('touchstart', (e) => {
            if (modoArco) {
                return;
            }
            toqueInicioX = e.touches[0].clientX;
        }, { passive: true });
        carrusel.addEventListener('touchend', (e) => {
            if (modoArco || toqueInicioX === null) {
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

        /* Arrastre con el puntero (arco). Solo se captura el puntero cuando de
           verdad arrastra: así un clic normal sigue llegando a "Ver detalle". */
        let punteroInicioX = null;
        let arrastrando = false;
        let ignorarClic = false;
        const pasoArco = () => items[0].offsetWidth * SEPARACION_ARCO;

        carrusel.addEventListener('pointerdown', (e) => {
            if (!modoArco || e.button !== 0) {
                return;
            }
            punteroInicioX = e.clientX;
            arrastrando = false;
        });

        carrusel.addEventListener('pointermove', (e) => {
            if (punteroInicioX === null) {
                return;
            }
            const dx = e.clientX - punteroInicioX;
            if (!arrastrando && Math.abs(dx) > UMBRAL_ARRASTRE) {
                arrastrando = true;
                carrusel.classList.add('is-arrastrando');
                carrusel.setPointerCapture(e.pointerId);
            }
            if (arrastrando) {
                pintarArco(-dx / pasoArco());
            }
        });

        const soltar = (e) => {
            if (punteroInicioX === null) {
                return;
            }
            const dx = e.clientX - punteroInicioX;
            punteroInicioX = null;
            if (!arrastrando) {
                return;
            }
            arrastrando = false;
            ignorarClic = true;
            carrusel.classList.remove('is-arrastrando');
            irA(indice + Math.round(-dx / pasoArco()));
            reiniciarAutoavance();
        };
        carrusel.addEventListener('pointerup', soltar);
        carrusel.addEventListener('pointercancel', soltar);

        /* Clic (arco): tras arrastrar no se abre nada; en una tarjeta lateral,
           primero se trae al centro. */
        carrusel.addEventListener('click', (e) => {
            if (!modoArco) {
                return;
            }
            if (ignorarClic) {
                ignorarClic = false;
                e.preventDefault();
                e.stopPropagation();
                return;
            }
            const item = e.target.closest('.hs-carrusel__item');
            if (item && !item.classList.contains('is-actual')) {
                e.preventDefault();
                conReinicio(() => irA(items.indexOf(item)))();
            }
        }, true);

        /* Las imágenes no se arrastran como archivos */
        carrusel.addEventListener('dragstart', (e) => {
            if (modoArco) {
                e.preventDefault();
            }
        });

        consultaArco.addEventListener('change', aplicarModo);
        window.addEventListener('resize', () => {
            if (modoArco) {
                return;
            }
            indice = Math.min(indice, indiceMaximo());
            construirPuntos();
            pintar();
            reiniciarAutoavance();
        });

        aplicarModo();
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
