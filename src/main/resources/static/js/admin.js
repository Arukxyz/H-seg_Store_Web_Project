/* Bloque 4: exclusivamente interacciones de interfaz; los POST siguen siendo del backend. */
(() => {
  'use strict';

  const scrollCafeGuardado = sessionStorage.getItem('hs_cafe_scroll');
  if (scrollCafeGuardado) {
    window.scrollTo(0, parseInt(scrollCafeGuardado, 10));
    sessionStorage.removeItem('hs_cafe_scroll');
  }

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



           // Buscador, pestañas y paginación en Catálogo web (10 por página)
    const buscadorCatalogo = document.getElementById('filtro-catalogo');
    const tabsCatalogo = [...document.querySelectorAll('[data-catalog-filter]')];
    const filasCatalogo = [...document.querySelectorAll('.hs-catalog-row')];
    const vacioCatalogo = document.querySelector('.hs-catalog-search-empty');
    const paginacionBox = document.getElementById('catalogo-paginacion');
    const paginacionInfo = document.getElementById('catalogo-paginacion-info');
    const paginacionNum = document.getElementById('catalogo-paginacion-num');
    const btnPrev = document.getElementById('catalogo-prev');
    const btnNext = document.getElementById('catalogo-next');

    if (filasCatalogo.length) {
      const POR_PAGINA = 10;
      let paginaActual = 1;
      let estadoFiltro = 'TODAS';

      const actualizarCatalogo = () => {
        const termino = (buscadorCatalogo?.value || '').trim().toLowerCase();
        const filtradas = filasCatalogo.filter(fila => {
          const coincideTexto = !termino || (fila.dataset.search || '').includes(termino);
          const esVisible = fila.dataset.visible === 'true';
          const esDestacado = fila.dataset.destacado === 'true';
          const coincideTab =
            estadoFiltro === 'TODAS' ||
            (estadoFiltro === 'VISIBLES' && esVisible) ||
            (estadoFiltro === 'OCULTAS' && !esVisible) ||
            (estadoFiltro === 'DESTACADAS' && esDestacado);
          return coincideTexto && coincideTab;
        });

        const totalPaginas = Math.max(1, Math.ceil(filtradas.length / POR_PAGINA));
        if (paginaActual > totalPaginas) paginaActual = totalPaginas;
        const inicio = (paginaActual - 1) * POR_PAGINA;
        const fin = inicio + POR_PAGINA;

        filasCatalogo.forEach(fila => { fila.hidden = true; });
        filtradas.slice(inicio, fin).forEach(fila => { fila.hidden = false; });

        if (vacioCatalogo) vacioCatalogo.hidden = filtradas.length > 0;
        if (paginacionBox) {
          paginacionBox.hidden = filtradas.length <= POR_PAGINA;
          if (paginacionInfo) {
            paginacionInfo.textContent = `Mostrando ${inicio + 1}–${Math.min(fin, filtradas.length)} de ${filtradas.length}`;
          }
          if (paginacionNum) paginacionNum.textContent = `${paginaActual} / ${totalPaginas}`;
          if (btnPrev) btnPrev.disabled = paginaActual <= 1;
          if (btnNext) btnNext.disabled = paginaActual >= totalPaginas;
        }
      };

      buscadorCatalogo?.addEventListener('input', () => {
        paginaActual = 1;
        actualizarCatalogo();
      });

      tabsCatalogo.forEach(tab => tab.addEventListener('click', () => {
        estadoFiltro = tab.dataset.catalogFilter;
        paginaActual = 1;
        tabsCatalogo.forEach(t => {
          const activo = t === tab;
          t.classList.toggle('is-active', activo);
          t.setAttribute('aria-pressed', String(activo));
        });
        actualizarCatalogo();
      }));

      btnPrev?.addEventListener('click', () => {
        if (paginaActual > 1) { paginaActual -= 1; actualizarCatalogo(); }
      });

      btnNext?.addEventListener('click', () => {
        paginaActual += 1;
        actualizarCatalogo();
      });

      actualizarCatalogo();
    }


        // Transición suave con spinner y conservación de scroll al cambiar interruptores en Catálogo
    const formsSwitch = [...document.querySelectorAll('.hs-catalog-table form')];
    if (formsSwitch.length) {
      // Restaurar posición de scroll si venimos de pulsar un switch
      const scrollGuardado = sessionStorage.getItem('hs_catalog_scroll');
      if (scrollGuardado) {
        window.scrollTo({ top: parseInt(scrollGuardado, 10), behavior: 'instant' });
        sessionStorage.removeItem('hs_catalog_scroll');
      }

      // Crear overlay de carga con spinner
      const loader = document.createElement('div');
      loader.className = 'hs-saving-overlay';
      loader.hidden = true;
      loader.innerHTML = '<div class="hs-saving-pill"><span class="hs-spinner" aria-hidden="true"></span><span>Actualizando catálogo...</span></div>';
      document.body.appendChild(loader);

      formsSwitch.forEach(form => {
        form.addEventListener('submit', () => {
          const btn = form.querySelector('.hs-switch');
          if (btn) {
            btn.classList.toggle('is-on');
            btn.disabled = true;
          }
          sessionStorage.setItem('hs_catalog_scroll', String(window.scrollY));
          loader.hidden = false;
        });
      });
    }


        // Buscador instantáneo en Carta de Höség Café
    const buscadorCafe = document.getElementById('filtro-cafe');
    const filasCafe = [...document.querySelectorAll('.hs-cafe-row')];
    const vacioCafe = document.querySelector('.hs-cafe-search-empty');
    if (buscadorCafe && filasCafe.length) {
      buscadorCafe.addEventListener('input', () => {
        const termino = buscadorCafe.value.trim().toLowerCase();
        let visibles = 0;
        filasCafe.forEach(fila => {
          const coincide = !termino || (fila.dataset.search || '').includes(termino);
          fila.hidden = !coincide;
          if (coincide) visibles += 1;
        });
        if (vacioCafe) vacioCafe.hidden = visibles > 0;
      });
    }


           // Conservar posición de scroll y mostrar velo suave al abrir "Editar" en Höség Café
   
    const enlacesEditarCafe = [...document.querySelectorAll('.hs-cafe-page a[href*="item="], .hs-cafe-page a[href*="local="]')];
    if (enlacesEditarCafe.length) {
      const loaderCafe = document.createElement('div');
      loaderCafe.className = 'hs-saving-overlay';
      loaderCafe.hidden = true;
      loaderCafe.innerHTML = '<div class="hs-saving-pill"><span class="hs-spinner" aria-hidden="true"></span><span>Cargando datos...</span></div>';
      document.body.appendChild(loaderCafe);

      enlacesEditarCafe.forEach(enlace => {
        enlace.addEventListener('click', () => {
          sessionStorage.setItem('hs_cafe_scroll', String(window.scrollY));
          loaderCafe.hidden = false;
        });
      });
    }


        // Buscador de bloques, contador de caracteres y vista previa de imagen en Contenido
    const buscadorContenido = document.getElementById('filtro-contenido');
    const filasContenido = [...document.querySelectorAll('.hs-content-row')];
    const vacioContenido = document.querySelector('.hs-content-search-empty');
    if (buscadorContenido && filasContenido.length) {
      buscadorContenido.addEventListener('input', () => {
        const termino = buscadorContenido.value.trim().toLowerCase();
        let visibles = 0;
        filasContenido.forEach(fila => {
          const coincide = !termino || (fila.dataset.search || '').includes(termino);
          fila.hidden = !coincide;
          if (coincide) visibles += 1;
        });
        if (vacioContenido) vacioContenido.hidden = visibles > 0;
      });
    }

    const textareaCuerpo = document.getElementById('contenido-cuerpo');
    const contadorCuerpo = document.getElementById('contenido-char-count');
    if (textareaCuerpo && contadorCuerpo) {
      const actualizarContador = () => {
        contadorCuerpo.textContent = `${textareaCuerpo.value.length.toLocaleString('es-PE')} / 20,000`;
      };
      textareaCuerpo.addEventListener('input', actualizarContador);
      actualizarContador();
    }

    const inputImgContenido = document.getElementById('contenido-imagen');
    const wrapImgContenido = document.getElementById('contenido-img-preview-wrap');
    const imgPreviewContenido = document.getElementById('contenido-img-preview');
    if (inputImgContenido && wrapImgContenido && imgPreviewContenido) {
      imgPreviewContenido.addEventListener('error', () => { wrapImgContenido.hidden = true; });
      imgPreviewContenido.addEventListener('load', () => { wrapImgContenido.hidden = false; });
      inputImgContenido.addEventListener('input', () => {
        const url = inputImgContenido.value.trim();
        if (!url) {
          wrapImgContenido.hidden = true;
          imgPreviewContenido.removeAttribute('src');
        } else {
          imgPreviewContenido.src = url;
        }
      });
    }


  });
})();