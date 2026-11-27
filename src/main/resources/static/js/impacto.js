document.addEventListener('DOMContentLoaded', () => {
    // 1. Seleccionar / deseleccionar todas las donaciones pendientes
    const chkAll = document.getElementById('chkAllDon');
    if (chkAll) {
        chkAll.addEventListener('change', () => {
            document.querySelectorAll('.chk-don').forEach(c => c.checked = chkAll.checked);
        });
    }

    // 2. Validar y apuntar al lote destino seleccionado al asignar
    const formAsignar = document.getElementById('formAsignarLote');
    if (formAsignar) {
        formAsignar.addEventListener('submit', (e) => {
            const id = document.getElementById('selectLoteDestino').value;
            if (!id) {
                e.preventDefault();
                alert('Elige un lote destino.');
                return;
            }
            formAsignar.action = formAsignar.action.replace('/0/', '/' + id + '/');
        });
    }

    // 3. Filtro por estado y paginación de lotes registrados (8 por página)
    const filas = Array.from(document.querySelectorAll('.hs-lote-row'));
    const wrap = document.getElementById('lotes-paginacion');
    if (filas.length && wrap) {
        const info = document.getElementById('lotes-paginacion-info');
        const num = document.getElementById('lotes-paginacion-num');
        const btnPrev = document.getElementById('lotes-prev');
        const btnNext = document.getElementById('lotes-next');
        const tabs = document.querySelectorAll('[data-lote-filter]');
        const porPagina = 8;
        let estadoFiltro = 'TODOS';
        let pagina = 1;

        function render() {
            const visibles = filas.filter(tr => estadoFiltro === 'TODOS' || tr.dataset.estado === estadoFiltro);
            const totalPaginas = Math.max(1, Math.ceil(visibles.length / porPagina));
            if (pagina > totalPaginas) pagina = totalPaginas;

            const inicio = (pagina - 1) * porPagina;
            const fin = inicio + porPagina;

            filas.forEach(tr => tr.hidden = true);
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

        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.classList.remove('is-active'));
                tab.classList.add('is-active');
                estadoFiltro = tab.dataset.loteFilter;
                pagina = 1;
                render();
            });
        });

        if (btnPrev) btnPrev.addEventListener('click', () => { if (pagina > 1) { pagina--; render(); } });
        if (btnNext) btnNext.addEventListener('click', () => { pagina++; render(); });
        render();
    }
});
