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

    /* ---------- Tienda: aplicar filtros al cambiar una opción ---------- */
    document.querySelectorAll('form[data-autoenviar]').forEach((form) => {
        form.addEventListener('change', (e) => {
            if (e.target.matches('input[type=radio], input[type=checkbox], select')) {
                form.requestSubmit();
            }
        });
        // Con JavaScript el botón "Aplicar" sobra en escritorio
        form.querySelectorAll('[data-solo-sin-js]').forEach((b) => b.classList.add('d-lg-none'));
    });

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
