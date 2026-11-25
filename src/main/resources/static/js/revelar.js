/* =====================================================================
   HÖSÉG Web · Aparición al entrar en pantalla (todo el sitio)
   Marca con .is-visible cada [data-revelar] la primera vez que entra en
   la ventana; el CSS (hoseg.css, "Aparición al entrar en pantalla") hace
   la transición. Un solo observador para toda la página.

   El script en línea del <head> de layout/base.html añade .hs-revelar al
   <html> antes de pintar, solo si hay IntersectionObserver y no se pidió
   menos movimiento. Sin esa clase el CSS no oculta nada, así que sin JS
   todo queda visible.

   Contenido que llega después (p. ej. resultados de la tienda): basta con
   llamar a window.hsRevelar(contenedor).
   ===================================================================== */
(() => {
    'use strict';

    const raiz = document.documentElement;
    if (!raiz.classList.contains('hs-revelar')) {
        return;
    }

    const observador = new IntersectionObserver((entradas) => {
        entradas
            .filter((entrada) => entrada.isIntersecting)
            .forEach((entrada) => {
                entrada.target.classList.add('is-visible');
                observador.unobserve(entrada.target);
            });
    }, { threshold: 0.12, rootMargin: '0px 0px -6% 0px' });

    const observar = (contenedor) => {
        contenedor.querySelectorAll('[data-revelar]:not(.is-visible)')
            .forEach((elemento) => observador.observe(elemento));
    };

    window.hsRevelar = observar;
    observar(document);
})();
