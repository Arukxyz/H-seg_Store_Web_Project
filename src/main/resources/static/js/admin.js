/* Bloque 4: exclusivamente interacciones de interfaz; los POST siguen siendo del backend. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    // Filtro visual de pedidos recibidos para el rango GET actual (no altera indicadores).
    const filtros = [...document.querySelectorAll('[data-order-filter]')];
    const pedidos = [...document.querySelectorAll('[data-order-status]')];
    const filaVacia = document.querySelector('.hs-filter-empty');
    if (filtros.length) {
      filtros.forEach(boton => boton.addEventListener('click', () => {
        const estado = boton.dataset.orderFilter;
        let visibles = 0;
        filtros.forEach(filtro => {
          filtro.classList.toggle('is-active', filtro === boton);
          filtro.setAttribute('aria-pressed', String(filtro === boton));
        });
        pedidos.forEach(pedido => {
          const coincide = estado === 'TODOS' || pedido.dataset.orderStatus === estado;
          pedido.hidden = !coincide;
          if (coincide) visibles += 1;
        });
        if (filaVacia) filaVacia.hidden = visibles > 0 || pedidos.length === 0;
      }));
    }

    // Modales de alta/edición existentes; nunca genera nuevas rutas.
    document.querySelectorAll('[data-open-modal]').forEach(boton => {
      boton.addEventListener('click', () => {
        const modalDestino = document.getElementById(boton.dataset.openModal);
        if (!modalDestino || !window.bootstrap?.Modal) return;
        // Cuando se pulsa "Agregar" desde una URL ?item= o ?local=, se va
        // al formulario de creación limpio para no sobrescribir un registro.
        const parametros = new URLSearchParams(location.search);
        if (parametros.has('item') || parametros.has('local')) {
          const direccionDestino = new URL(location.href);
          direccionDestino.searchParams.delete('item');
          direccionDestino.searchParams.delete('local');
          location.assign(direccionDestino.toString());
          return;
        }
        window.bootstrap.Modal.getOrCreateInstance(modalDestino).show();
      });
    });
    document.querySelectorAll('.hs-admin-modal[data-auto-open]').forEach(modal => {
      const hayErrorValidacion = [...modal.querySelectorAll('.hs-field-error')]
        .some(elemento => elemento.textContent.trim().length > 0);
      if ((modal.dataset.autoOpen === 'true' || hayErrorValidacion) && window.bootstrap?.Modal) {
        window.bootstrap.Modal.getOrCreateInstance(modal).show();
      }
    });


        // Menú flotante en arco para móvil
    const fabBtn = document.querySelector('.hs-admin-fab');
    const fabNav = document.querySelector('nav[aria-label="Panel"]');
    const fabBackdrop = document.querySelector('.hs-admin-fab-backdrop');
    if (fabBtn && fabNav) {
      const toggleFab = (abrir) => {
        const estado = typeof abrir === 'boolean' ? abrir : !fabNav.classList.contains('is-open');
        fabNav.classList.toggle('is-open', estado);
        fabBtn.classList.toggle('is-open', estado);
        fabBtn.setAttribute('aria-expanded', String(estado));
        if (fabBackdrop) fabBackdrop.hidden = !estado;
      };
      fabBtn.addEventListener('click', () => toggleFab());
      if (fabBackdrop) fabBackdrop.addEventListener('click', () => toggleFab(false));
    }

  });
})();