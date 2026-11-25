/* =====================================================================
   HÖSÉG Web · Bloque 1 (tienda, detalle, confirmar, compra registrada)
   Mejora progresiva: todo funciona sin JavaScript (formularios normales);
   esto solo evita clics de más y actualiza cifras en pantalla. El precio,
   el stock y el costo de envío que valen son los que calcula el servidor.
   ===================================================================== */
(() => {
    'use strict';

    const formatoSoles = new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const soles = (monto) => 'S/ ' + formatoSoles.format(monto);

    const menosMovimiento = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /* ---------- Tienda: filtrar, ordenar y paginar sin recargar ----------
       El formulario sigue siendo un GET normal (sin JS funciona igual, recargando).
       Con JS se intercepta el envío, se pide solo el fragmento "resultados"
       (cabecera X-Hoseg-Parcial) y se reemplaza en pantalla; la URL se
       actualiza para que "atrás", recargar y compartir sigan funcionando. */
    const iniciarTienda = (form) => {
        const zona = form.querySelector('[data-tienda-zona]');
        const anuncio = form.querySelector('[data-tienda-anuncio]');
        const titulo = zona.querySelector('h2');
        const buscador = form.querySelector('input[name=q]');
        const ESPERA_BUSQUEDA_MS = 300;

        let controlador = null;      // aborta la petición anterior si llega otra
        let esperaBusqueda = null;
        let ultimaBusqueda = buscador ? buscador.value.trim() : '';
        let modoHistorial = 'push';  // 'replace' mientras se escribe, para no llenar el historial

        // Panel de filtros. En móvil (< 992 px) tapa los resultados, así que sus
        // cambios no se aplican al tocarlos: esperan a "Aplicar", que además
        // cierra el panel y baja a los resultados.
        const panel = form.querySelector('#panel-filtros');
        const esMovil = window.matchMedia('(max-width: 991.98px)');
        const enPanelMovil = (el) => esMovil.matches && panel && panel.contains(el);

        // Con JavaScript el botón "Aplicar" sobra en escritorio
        form.querySelectorAll('[data-solo-sin-js]').forEach((b) => b.classList.add('d-lg-none'));
        if (titulo) titulo.tabIndex = -1;

        const fueraDeResultados = (el) => !el.closest('[data-tienda-resultados]');

        // URL limpia: sin campos vacíos, sin página 1 ni valores por defecto
        const urlDe = (datos) => {
            const defectos = {};
            form.querySelectorAll('[data-defecto]').forEach((el) => { defectos[el.name] = el.dataset.defecto; });
            const params = new URLSearchParams();
            for (const [clave, valor] of datos) {
                if (valor === '' || (clave === 'pagina' && valor === '1') || defectos[clave] === valor) continue;
                params.append(clave, valor);
            }
            const consulta = params.toString();
            return form.action + (consulta ? '?' + consulta : '');
        };

        /* Lleva la vista al inicio de los resultados. Por defecto solo si se
           quedó por debajo (paginando); con siempre=true también baja (móvil,
           tras "Aplicar"). */
        const llevarArriba = (siempre = false) => {
            const y = zona.getBoundingClientRect().top + window.scrollY - 110;   // deja libre la barra fija
            if (siempre || window.scrollY > y) {
                window.scrollTo({ top: y, behavior: menosMovimiento() ? 'auto' : 'smooth' });
            }
            if (titulo) titulo.focus({ preventScroll: true });
        };

        const cerrarPanel = () => {
            if (!panel || !panel.classList.contains('show')) return;
            if (window.bootstrap && window.bootstrap.Collapse) {
                window.bootstrap.Collapse.getOrCreateInstance(panel, { toggle: false }).hide();
            } else {
                panel.classList.remove('show');
            }
        };

        const cargar = async (url, { historial = 'push', paginando = false, aplicando = false } = {}) => {
            const actual = zona.querySelector('[data-tienda-resultados]');
            if (controlador) controlador.abort();
            controlador = new AbortController();
            actual.classList.add('is-cargando');
            actual.setAttribute('aria-busy', 'true');
            try {
                const respuesta = await fetch(url, {
                    headers: { 'X-Hoseg-Parcial': 'true' },
                    signal: controlador.signal
                });
                if (!respuesta.ok) throw new Error('HTTP ' + respuesta.status);
                const plantilla = document.createElement('template');
                plantilla.innerHTML = (await respuesta.text()).trim();
                const nuevo = plantilla.content.querySelector('[data-tienda-resultados]');
                if (!nuevo) throw new Error('Respuesta sin resultados');

                actual.replaceWith(nuevo);
                if (window.hsRevelar) window.hsRevelar(nuevo);
                if (historial === 'push') history.pushState({ tienda: true }, '', url);
                if (historial === 'replace') history.replaceState({ tienda: true }, '', url);
                const resumen = nuevo.querySelector('.hs-tienda__resumen span');
                if (anuncio && resumen) anuncio.textContent = resumen.textContent;
                if (aplicando) {
                    cerrarPanel();
                    llevarArriba(true);
                } else if (paginando) {
                    llevarArriba();
                }
            } catch (error) {
                if (error.name === 'AbortError') return;
                window.location.href = url;   // si algo falla, navegación normal
            }
        };

        form.addEventListener('submit', (e) => {
            e.preventDefault();
            clearTimeout(esperaBusqueda);
            const datos = new FormData(form);
            // Los botones del paginador envían name="pagina"; sin ellos se vuelve a la página 1
            const paginando = !!(e.submitter && e.submitter.name === 'pagina');
            const aplicando = !!(e.submitter && e.submitter.matches('[data-aplicar-filtros]'));
            if (paginando) datos.set('pagina', e.submitter.value);
            if (buscador) ultimaBusqueda = buscador.value.trim();
            cargar(urlDe(datos), { historial: modoHistorial, paginando, aplicando });
            modoHistorial = 'push';
        });

        // Filtros, orden y "productos por página": se aplican al cambiar,
        // salvo los del panel en móvil, que esperan a "Aplicar"
        form.addEventListener('change', (e) => {
            if (e.target.matches('input[type=radio], input[type=checkbox], select') && !enPanelMovil(e.target)) {
                form.requestSubmit();
            }
        });

        // Búsqueda mientras se escribe, con espera para no pedir por cada letra
        if (buscador) {
            buscador.addEventListener('input', () => {
                clearTimeout(esperaBusqueda);
                esperaBusqueda = setTimeout(() => {
                    if (buscador.value.trim() === ultimaBusqueda) return;
                    modoHistorial = 'replace';
                    form.requestSubmit();
                }, ESPERA_BUSQUEDA_MS);
            });
        }

        // "Limpiar filtros" / "Ver todo el catálogo": quita filtros y búsqueda, conserva orden y tamaño
        form.addEventListener('click', (e) => {
            if (!e.target.closest('[data-limpiar]')) return;
            e.preventDefault();
            form.querySelectorAll('input[type=radio]').forEach((r) => { if (fueraDeResultados(r)) r.checked = r.value === ''; });
            form.querySelectorAll('input[type=checkbox]').forEach((c) => { c.checked = false; });
            if (buscador) buscador.value = '';
            form.requestSubmit();
        });

        // Atrás / adelante: los controles vuelven a reflejar la URL y se recargan los resultados
        window.addEventListener('popstate', () => {
            const params = new URLSearchParams(window.location.search);
            form.querySelectorAll('input[type=radio]').forEach((r) => {
                if (fueraDeResultados(r)) r.checked = (params.get(r.name) ?? '') === r.value;
            });
            form.querySelectorAll('input[type=checkbox]').forEach((c) => { c.checked = params.get(c.name) === c.value; });
            form.querySelectorAll('select[data-defecto]').forEach((s) => {
                if (fueraDeResultados(s)) s.value = params.get(s.name) ?? s.dataset.defecto;
            });
            if (buscador) buscador.value = ultimaBusqueda = params.get('q') ?? '';
            cargar(window.location.href, { historial: 'ninguno' });
        });
    };

    const formTienda = document.querySelector('form[data-tienda-filtros]');
    if (formTienda && formTienda.querySelector('[data-tienda-zona]') && window.fetch) {
        iniciarTienda(formTienda);
    }

    /* ---------- Selector de cantidad − n + ---------- */
    const ajustarCantidad = (grupo) => {
        const campo = grupo.querySelector('input');
        const max = parseInt(campo.max, 10) || 1;
        let valor = Math.min(Math.max(parseInt(campo.value, 10) || 1, 1), max);
        campo.value = valor;
        grupo.querySelector('[data-menos]').disabled = valor <= 1;
        grupo.querySelector('[data-mas]').disabled = valor >= max;
        campo.dispatchEvent(new Event('cantidad'));
    };

    document.querySelectorAll('[data-cantidad]').forEach((grupo) => {
        const campo = grupo.querySelector('input');
        grupo.querySelector('[data-menos]').addEventListener('click', () => {
            campo.value = (parseInt(campo.value, 10) || 1) - 1;
            ajustarCantidad(grupo);
        });
        grupo.querySelector('[data-mas]').addEventListener('click', () => {
            campo.value = (parseInt(campo.value, 10) || 1) + 1;
            ajustarCantidad(grupo);
        });
        campo.addEventListener('change', () => ajustarCantidad(grupo));
        ajustarCantidad(grupo);
    });

    /* ---------- Detalle: la talla elegida cambia precio, máximo, aviso e impacto ---------- */
    const detalle = document.querySelector('[data-detalle]');
    if (detalle) {
        const precio = detalle.querySelector('[data-precio]');
        const aviso = detalle.querySelector('[data-aviso-pocas]');
        const grupoCantidad = detalle.querySelector('[data-cantidad]');
        const campoCantidad = grupoCantidad && grupoCantidad.querySelector('input');
        const impacto = detalle.querySelector('[data-impacto-texto]');

        const actualizarImpacto = () => {
            if (!impacto || !campoCantidad) return;
            const n = parseInt(campoCantidad.value, 10) || 1;
            impacto.textContent = impacto.dataset.tipo === 'ABRIGO'
                ? `Esta compra abriga a ${n} ${n === 1 ? 'niño' : 'niños'} en Cusco.`
                : `Esta compra planta ${n} ${n === 1 ? 'árbol' : 'árboles'} en Cusco.`;
        };

        const elegirTalla = (radio) => {
            if (precio) precio.textContent = soles(parseFloat(radio.dataset.precio));
            if (aviso) aviso.hidden = radio.dataset.pocas !== 'true';
            if (campoCantidad) {
                campoCantidad.max = radio.dataset.maximo;
                ajustarCantidad(grupoCantidad);
            }
        };

        detalle.querySelectorAll('input[name=codigo][data-precio]').forEach((radio) => {
            radio.addEventListener('change', () => elegirTalla(radio));
            if (radio.checked) elegirTalla(radio);
        });
        if (campoCantidad) campoCantidad.addEventListener('cantidad', actualizarImpacto);
        actualizarImpacto();
    }

    /* ---------- Confirmar: campos de delivery y total a pagar ---------- */
    const confirmar = document.querySelector('[data-confirmar]');
    if (confirmar) {
        const camposDelivery = confirmar.querySelector('[data-campos-delivery]');
        const filaEnvio = document.querySelector('[data-envio]');
        const filaTotal = document.querySelector('[data-total-pagar]');
        const productos = parseFloat(filaTotal.dataset.productos);
        const costoDelivery = parseFloat(filaTotal.dataset.costoDelivery);

        const actualizar = () => {
            const elegido = confirmar.querySelector('input[name=tipo]:checked');
            const esDelivery = elegido && elegido.value === 'DELIVERY';
            camposDelivery.hidden = !esDelivery;
            const envio = esDelivery ? costoDelivery : 0;
            filaEnvio.textContent = envio === 0 ? 'Gratis' : soles(envio);
            filaTotal.textContent = soles(productos + envio);
        };
        confirmar.querySelectorAll('input[name=tipo]').forEach((r) => r.addEventListener('change', actualizar));
        actualizar();
    }

    /* ---------- Compra registrada: copiar el código ---------- */
    document.querySelectorAll('[data-copiar]').forEach((boton) => {
        if (!navigator.clipboard) {
            boton.hidden = true;
            return;
        }
        boton.addEventListener('click', async () => {
            await navigator.clipboard.writeText(boton.dataset.copiar);
            const etiqueta = boton.querySelector('[data-copiar-texto]');
            const original = etiqueta.textContent;
            etiqueta.textContent = 'Copiado';
            setTimeout(() => { etiqueta.textContent = original; }, 2000);
        });
    });
})();
