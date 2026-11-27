document.addEventListener('DOMContentLoaded', () => {
    // 1. Popovers de ayuda (CVV)
    document.querySelectorAll('[data-bs-toggle="popover"]').forEach(el => new bootstrap.Popover(el));

    // 2. Validación visual al pulsar Pagar + enfoque automático al primer campo inválido
    document.querySelectorAll('.needs-validation').forEach(form => {
        form.addEventListener('submit', e => {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
                form.classList.add('was-validated');
                const primeroInvalido = form.querySelector(':invalid');
                if (primeroInvalido) primeroInvalido.focus();
                return;
            }
            form.classList.add('was-validated');
        });
    });

    // 3. Formato automático de tarjeta (4 en 4) y vencimiento (MM/AA)
    const numTarjeta = document.getElementById('numeroTarjeta');
    if (numTarjeta) {
        numTarjeta.addEventListener('input', () => {
            const limpio = numTarjeta.value.replace(/\D/g, '').slice(0, 16);
            numTarjeta.value = limpio.replace(/(.{4})/g, '$1 ').trim();
        });
    }

    const vencimiento = document.getElementById('vencimiento');
    if (vencimiento) {
        vencimiento.addEventListener('input', () => {
            const limpio = vencimiento.value.replace(/\D/g, '').slice(0, 4);
            vencimiento.value = limpio.length > 2 ? limpio.slice(0, 2) + '/' + limpio.slice(2) : limpio;
        });
    }

    // 4. Autocompletado de titular por DNI (RENIEC)
    const dniTitular = document.getElementById('dniTitular');
    const titular = document.getElementById('titular');
    if (dniTitular && titular) {
        dniTitular.addEventListener('input', async () => {
            const dni = dniTitular.value.trim();
            if (dni.length === 8 && /^[0-9]{8}$/.test(dni)) {
                try {
                    const res = await fetch('/api/documento/DNI/' + dni);
                    if (res.ok) {
                        const data = await res.json();
                        if (data.nombreCompleto) titular.value = data.nombreCompleto;
                    }
                } catch (_) {}
            }
        });
    }
});
