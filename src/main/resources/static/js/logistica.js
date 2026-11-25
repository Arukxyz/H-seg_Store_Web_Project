document.addEventListener('DOMContentLoaded', () => {
    function crearPaginador({ filasSelector, wrapId, infoId, numId, prevId, nextId, porPagina = 10, filtroFn = () => true }) {
        const todas = Array.from(document.querySelectorAll(filasSelector));
        const wrap = document.getElementById(wrapId);
        if (!todas.length || !wrap) return { reiniciar: () => { } };

        const info = document.getElementById(infoId);
        const num = document.getElementById(numId);
        const btnPrev = document.getElementById(prevId);
        const btnNext = document.getElementById(nextId);
        let pagina = 1;

        function render() {
            const visibles = todas.filter(filtroFn);
            const totalPaginas = Math.max(1, Math.ceil(visibles.length / porPagina));
            if (pagina > totalPaginas) pagina = totalPaginas;

            const inicio = (pagina - 1) * porPagina;
            const fin = inicio + porPagina;

            todas.forEach(tr => tr.hidden = true);
            visibles.slice(inicio, fin).forEach(tr => tr.hidden = false);

            wrap.hidden = visibles.length <= porPagina;
            if (info) {
                const desde = visibles.length ? inicio + 1 : 0;
                const hasta = Math.min(fin, visibles.length);
                info.textContent = `Mostrando ${desde}–${hasta} de ${visibles.length}`;
            }
            if (num) num.textContent = `${pagina} / ${totalPaginas}`;
            if (btnPrev) btnPrev.disabled = pagina <= 1;
            if (btnNext) btnNext.disabled = pagina >= totalPaginas;
        }

        if (btnPrev) btnPrev.addEventListener('click', () => { if (pagina > 1) { pagina--; render(); } });
        if (btnNext) btnNext.addEventListener('click', () => { pagina++; render(); });

        render();
        return { reiniciar: () => { pagina = 1; render(); } };
    }

    // 1. Inventario: filtro + paginación de 10 en 10
    let soloCritico = false;
    const pagInv = crearPaginador({
        filasSelector: '.hs-inv-row',
        wrapId: 'inv-paginacion',
        infoId: 'inv-paginacion-info',
        numId: 'inv-paginacion-num',
        prevId: 'inv-prev',
        nextId: 'inv-next',
        porPagina: 10,
        filtroFn: tr => !soloCritico || tr.dataset.critico === 'true'
    });

    const invTabs = document.querySelectorAll('[data-inv-filter]');
    invTabs.forEach(btn => {
        btn.addEventListener('click', () => {
            invTabs.forEach(t => t.classList.remove('is-active'));
            btn.classList.add('is-active');
            soloCritico = btn.dataset.invFilter === 'CRITICO';
            pagInv.reiniciar();
        });
    });

    // 2. Proveedores: paginación de órdenes (8 en 8)
    crearPaginador({
        filasSelector: '.hs-prov-row',
        wrapId: 'prov-paginacion',
        infoId: 'prov-paginacion-info',
        numId: 'prov-paginacion-num',
        prevId: 'prov-prev',
        nextId: 'prov-next',
        porPagina: 8
    });

    // 3. Autocompletado de RUC SUNAT al registrar taller/proveedor
    const rucInput = document.getElementById('provRuc');
    const nombreInput = document.getElementById('provNombre');
    const avisoRuc = document.getElementById('avisoRucProv');
    if (rucInput && nombreInput) {
        rucInput.addEventListener('input', async () => {
            const ruc = rucInput.value.trim();
            if (ruc.length === 11 && /^[0-9]{11}$/.test(ruc)) {
                if (avisoRuc) avisoRuc.textContent = 'Consultando SUNAT...';
                try {
                    const res = await fetch('/api/documento/RUC/' + ruc);
                    if (res.ok) {
                        const data = await res.json();
                        if (data.nombreCompleto) nombreInput.value = data.nombreCompleto;
                        if (avisoRuc) avisoRuc.textContent = 'RUC verificado (' + data.fuente + ').';
                    } else if (avisoRuc) {
                        avisoRuc.textContent = '';
                    }
                } catch (_) {
                    if (avisoRuc) avisoRuc.textContent = '';
                }
            }
        });
    }

    // >>> Aviso de stock crítico (Pedidos al iniciar sesión e Inventario)
    // 4. Aviso flotante de stock crítico: se muestra una vez por sesión de trabajo.
    //    Al cerrar sesión se borra la marca, para que vuelva a salir en el siguiente inicio.
    document.querySelectorAll('form[action$="/admin/logout"]').forEach(f => {
        f.addEventListener('submit', () => sessionStorage.removeItem('hs_stock_toast_visto'));
    });

    const toastStock = document.getElementById('toastStockCritico');
    if (toastStock) {
        if (!sessionStorage.getItem('hs_stock_toast_visto')) {
            toastStock.hidden = false;
            sessionStorage.setItem('hs_stock_toast_visto', '1');
        }
        const btnCerrar = document.getElementById('btnCerrarStockToast');
        if (btnCerrar) btnCerrar.addEventListener('click', () => toastStock.remove());

        const btnFiltrar = document.getElementById('btnVerSoloCriticos');
        if (btnFiltrar) {
            btnFiltrar.addEventListener('click', () => {
                const tabCritico = document.querySelector('[data-inv-filter="CRITICO"]');
                if (tabCritico) {
                    // Ya estamos en Inventario: se aplica el filtro aquí mismo
                    tabCritico.click();
                    toastStock.remove();
                } else if (toastStock.dataset.destino) {
                    // Estamos en Pedidos: se va a Inventario ya filtrado
                    window.location.href = toastStock.dataset.destino;
                }
            });
        }
    }

    // 5. Modal "Editar producto": un solo modal que se llena con los data-* del botón de la fila
    const modalEditar = document.getElementById('modal-editar-producto');
    if (modalEditar) {
        const form = modalEditar.querySelector('[data-form-editar]');
        const campo = (nombre) => form.elements.namedItem(nombre);
        const elegir = (select, valor) => {
            select.value = valor || '';
            // Valor heredado que ya no está en la lista: se ve vacío y "required" obliga a elegir
            if (select.value !== (valor || '')) select.value = '';
        };

        modalEditar.addEventListener('show.bs.modal', (e) => {
            const d = e.relatedTarget && e.relatedTarget.dataset;
            if (!d || !d.codigo) return;
            form.action = form.dataset.accionBase + encodeURIComponent(d.codigo) + '/editar';
            modalEditar.querySelector('[data-editar-codigo]').textContent = d.codigo;
            campo('nombre').value = d.nombre || '';
            campo('talla').value = d.talla || '';
            campo('precio').value = d.precio || '';
            campo('stockMinimo').value = d.minimo || '0';
            elegir(campo('categoria'), d.categoria);
            elegir(campo('coleccion'), d.coleccion);
            elegir(campo('tipoCompromiso'), d.compromiso);
        });
        modalEditar.addEventListener('shown.bs.modal', () => campo('nombre').focus());
    }

    // Si se llega a Inventario con ?filtro=critico, se activa la pestaña de stock crítico
    if (new URLSearchParams(window.location.search).get('filtro') === 'critico') {
        const tab = document.querySelector('[data-inv-filter="CRITICO"]');
        if (tab) tab.click();
    }
    // <<< Aviso de stock crítico
});
