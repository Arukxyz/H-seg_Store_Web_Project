# Del export de Figma Make a las plantillas Thymeleaf

Qué entregó el diseño, dónde quedó cada cosa y en qué se convierte al implementar la Home.

## Qué es el export

`design/figma-make/` es una aplicación **React 19 + Vite + Tailwind CSS v4** generada por Figma Make. No es un export de assets ni HTML estático: toda la Home está en un único archivo, `src/App.tsx` (990 líneas), con los estilos en clases de Tailwind y estilos en línea.

**No se compila ni se sirve desde Spring Boot.** Queda como referencia visual: de ahí se leen medidas, colores, textos, animaciones y la estructura de cada sección al escribir los fragmentos Thymeleaf. Para verlo funcionando: `cd design/figma-make && pnpm install && pnpm dev`.

## Reparto de archivos

| Origen en el export | Destino | Estado |
|---|---|---|
| Proyecto completo | `design/figma-make/` | Referencia, fuera del build de Maven |
| `src/index.css` (`@theme`, `:root`, animaciones) | `src/main/resources/static/css/tokens.css` | **Traducido a CSS estándar y en uso** |
| Copia literal del CSS original | `docs/diseno/tokens-figma.css` | Referencia, por si hay que comparar |
| `src/imports/brief-diseno-home.md` | — | Es nuestro propio brief (`docs/brief-diseno-home.md`), idéntico |
| `.gitattributes` (reglas Git LFS de Figma) | — | Eliminado: activaría LFS para todo png/jpg/pdf del repo |
| `CLAUDE.md` (una línea, `@AGENTS.md`) | — | Eliminado: inyectaba las instrucciones de Figma Make en este repo |
| `src/assets/attachment.png` | — | Eliminado: no era una imagen, era el brief con extensión `.png` |
| `AGENTS.md`, `package.json`, `vite.config.ts`, etc. | `design/figma-make/` | Se conservan tal cual |

## Lo que el export NO trae

- **Ninguna imagen propia.** Las 8 fotos de producto y del banner son URLs de Unsplash incrustadas en `App.tsx`. Hay que reemplazarlas por fotos reales de HÖSÉG en `static/images/`; para maquetar sirven temporalmente, pero no pueden quedar en la entrega.
- **Ningún logo.** "HÖSÉG" está puesto como texto con la tipografía Fraunces, no como SVG.
- **Ningún archivo de fuente.** Fraunces y Outfit se cargan desde Google Fonts (`@import` en `index.css`).
- **Ningún icono suelto.** Los 12 iconos son componentes React con SVG en línea (`HomeIcon`, `ShopIcon`, `ImpactNavIcon`, `CartIcon`, `AccountIcon`, `CoatIcon`, `TreeIcon`, `CommunityIcon`, `HeartIcon`, `ChevronLeft`, `ChevronRight`, `ArrowRight`). Se copian sus `<svg>` a los fragmentos, o se sustituyen por Bootstrap Icons.

## Componentes del export → fragmentos Thymeleaf

Todos los componentes del brief están implementados. Correspondencia para la implementación:

| Componente en `App.tsx` | Línea | Fragmento destino | Datos que consumirá |
|---|---|---|---|
| `Navbar` (barra superior + óvalo móvil) | 735 | `fragments/navbar.html` | Sesión del cliente, nº de ítems del carrito |
| Óvalo móvil (dentro de `Navbar`) | 851 | `fragments/navbar.html :: pill` | Página activa, contador del carrito |
| `HeroBanner` | 916 | `index.html` | Estático |
| `FeaturedCarousel` | 344 | `index.html` + `fragments/product-card.html` | Productos destacados (`activo` y `visible_web`) |
| `ProductCard` | 250 | `fragments/product-card.html` | `nombre`, `coleccion`, `precio`, `talla`, `tipo_compromiso`, `stock_comercial` |
| `ImpactBadge` | 229 | `fragments/product-card.html :: badge` | `tipo_compromiso` (ABRIGO / ARBOL) |
| `ImpactSteps` | 450 | `index.html` | Estático |
| `ImpactCounters` + `CounterCard` | 543 / 522 | `index.html` | Cifras reales desde la BD |
| `ImpactCTA` | 578 | `index.html` | Formulario que redirige a `/consulta-impacto` |
| `AboutStrip` | 622 | `index.html` | Estático |
| `Footer` | 673 | `fragments/footer.html` | Estático |

Los datos de `PRODUCTS`, `IMPACT_COUNTERS` y `COMMUNITIES` (líneas 7–78) son valores de maqueta: coinciden con el catálogo real, pero al implementar salen de la base de datos.

## Estado: Home implementada

La Home está traducida por completo (`templates/index.html` + `fragments/`, `static/css/hoseg.css` y `home.css`, `static/js/navbar.js` y `home.js`). Cómo reutilizar los fragmentos en las demás páginas: `docs/guia-plantillas.md`.

Diferencias deliberadas respecto al export:

- La `ProductCard` **no tiene botón "Agregar"**: el brief (§2.3 y §7) dice que desde la Home no se agrega al carrito, porque la talla se elige en el detalle. Solo "Ver detalle".
- La "Mochila artesanal" sale **sin badge**: en la base tiene `aplica_triple_impacto = FALSE`. Los datos mandan sobre la maqueta.
- Los destacados se **intercalan** ABRIGO / ARBOL (no hay columna "destacado" en el esquema) para que el carrusel muestre ambos compromisos desde la primera vista.
- Las cifras de `ImpactCounters` cuentan **lo entregado** (`donacion.estado = ENTREGADA`, lotes `ENTREGADO`), igual que el reporte del escritorio.
- El carrusel es JS propio (no el de Bootstrap) para poder mostrar 1,4 tarjetas en móvil.

Pendiente (solo assets):

1. **Fotografía propia.** El banner y la franja "Nosotros" siguen con URLs de Unsplash, marcadas con comentario en `index.html`. Las fotos de producto salen de `producto.url_imagen`, que en la semilla está en NULL (la tarjeta muestra un icono neutro); cargarlas es un script SQL del equipo de escritorio.
2. **Logo en SVG.** Hoy es texto "HÖSÉG" en Fraunces (`.hs-logo`). Si no habrá logo gráfico, queda así como decisión consciente.

## Decisión tomada: Bootstrap 5, no Tailwind

Las plantillas se escriben con la grilla y los componentes de Bootstrap 5 (ya incluido en el `pom.xml` como WebJar), tomando color, tipografía, radios y animaciones de `static/css/tokens.css`. Tailwind **no** se agrega al proyecto: obligaría a meter Node y un paso de compilación de CSS dentro del build de Maven.

Qué implica al implementar:

- Las clases utilitarias del export (`flex items-center gap-2`, `md:hidden`, `text-[11px]`…) **no se copian**; se traducen a utilidades de Bootstrap (`d-flex align-items-center gap-2`, `d-md-none`) o a CSS propio cuando no exista equivalente.
- Los estilos en línea del export (el óvalo móvil, la barra que se opaca al hacer scroll, los badges) sí se conservan como valores: son medidas y colores concretos, y pasan a `tokens.css` o a un `home.css` propio.
- El carrusel usa el componente de Bootstrap con `data-bs-ride="carousel"` en vez del `transform` manual del export, salvo que el deslizamiento de 1.5 tarjetas en móvil obligue a hacerlo a mano.
- Los breakpoints del export (`md:` = 768 px) coinciden con los de Bootstrap, así que el corte entre barra superior y óvalo móvil se mantiene igual.

## Comportamiento que el diseño ya resuelve

- Barra superior transparente sobre el banner que se vuelve opaca al hacer scroll.
- Óvalo móvil fijo abajo (`position: fixed; bottom: 20px`), fondo translúcido con desenfoque, **indicador deslizante** que se mueve al icono activo (transición de 0.22 s) y 5 destinos: Inicio, Tienda, Impacto, Carrito (con badge) y Cuenta.
- Carrusel con auto-avance, pausa al pasar el cursor y deslizamiento táctil.
- Contadores de impacto que se animan al entrar en pantalla (`IntersectionObserver`).
- Variante "Agotado" de la tarjeta de producto.
