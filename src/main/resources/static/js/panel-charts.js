document.addEventListener('DOMContentLoaded', () => {
    const canvasEstados = document.getElementById('chartEstadosPedidos');
    const canvasVentas = document.getElementById('chartVentasFecha');
    const canvasEnvios = document.getElementById('chartOperacionEnvios');
    if (!canvasEstados || !canvasVentas || !canvasEnvios || typeof Chart === 'undefined') return;

    const styles = getComputedStyle(document.documentElement);
    const cTerracotta = styles.getPropertyValue('--hs-terracotta').trim() || '#b55638';
    const cSage = styles.getPropertyValue('--hs-sage').trim() || '#586e56';
    const cAmber = styles.getPropertyValue('--hs-amber').trim() || '#b8832b';
    const cBark = styles.getPropertyValue('--hs-bark').trim() || '#2f241f';
    const cMuted = styles.getPropertyValue('--hs-muted').trim() || '#8a7a70';

    // La tabla de pedidos está en /admin/pedidos: aquí se leen los atributos data-* de los
    // elementos ocultos [data-pedido] que deja panel.html.
    const filas = Array.from(document.querySelectorAll('[data-pedido]'));
    let pagados = 0, pendientes = 0, anulados = 0;
    let delivery = 0, recojo = 0, entregados = 0, enProceso = 0;
    let sumaPagados = 0;
    const ventasPorDia = {};

    filas.forEach(el => {
        const estado = el.dataset.estado;
        const fechaTxt = (el.dataset.fecha || '').trim();
        const monto = parseFloat(el.dataset.total) || 0;
        const envioTxt = (el.dataset.envio || '').trim();

        if (estado === 'PAGADO') {
            pagados++;
            sumaPagados += monto;
            if (fechaTxt) ventasPorDia[fechaTxt] = (ventasPorDia[fechaTxt] || 0) + monto;
        } else if (estado === 'PENDIENTE') {
            pendientes++;
        } else if (estado === 'ANULADO') {
            anulados++;
        }

        if (envioTxt.includes('Delivery')) delivery++;
        if (envioTxt.includes('Recojo')) recojo++;
        if (envioTxt.includes('Entregado')) entregados++;
        else if (estado === 'PAGADO') enProceso++;
    });

    const elTicket = document.getElementById('kpi-ticket-promedio');
    if (elTicket) {
        const ticket = pagados > 0 ? (sumaPagados / pagados) : 0;
        elTicket.textContent = 'S/ ' + ticket.toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    // Gráfico 1: CFO & CRO · Estado de Pedidos (Dona)
    new Chart(canvasEstados, {
        type: 'doughnut',
        data: {
            labels: ['Pagados', 'Pendientes', 'Anulados'],
            datasets: [{
                data: [pagados, pendientes, anulados],
                backgroundColor: [cSage, cAmber, cMuted],
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom' } }
        }
    });

    // Gráfico 2: CEO & CRO · Ingresos Pagados por Fecha (Barras)
    // Toma máximo los últimos 7 días con ventas para que nunca se amontone
    const fechasOrdenadas = Object.keys(ventasPorDia).reverse().slice(-7);
    const montosOrdenados = fechasOrdenadas.map(f => Number(ventasPorDia[f].toFixed(2)));

    new Chart(canvasVentas, {
        type: 'bar',
        data: {
            labels: fechasOrdenadas.length ? fechasOrdenadas : ['Sin ventas pagadas'],
            datasets: [{
                label: 'Ventas pagadas (S/)',
                data: montosOrdenados.length ? montosOrdenados : Array.of(0),
                backgroundColor: cTerracotta,
                borderRadius: 8,
                maxBarThickness: 45 // Evita que la barra salga "gordita" cuando hay 1 o 2 días
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true } }
        }
    });

    // Gráfico 3: COO · Logística y Cumplimiento de Entregas
    new Chart(canvasEnvios, {
        type: 'bar',
        data: {
            labels: ['Delivery', 'Recojo tienda', 'Entregados', 'En proceso'],
            datasets: [{
                label: 'Pedidos',
                data: [delivery, recojo, entregados, enProceso],
                backgroundColor: [cBark, cTerracotta, cSage, cAmber],
                borderRadius: 8
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { x: { beginAtZero: true, ticks: { precision: 0 } } }
        }
    });
});
