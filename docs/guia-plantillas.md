# Guía para crear páginas nuevas

Cómo hacer que Tienda, Detalle, Carrito, Login, Registro y Consulta de impacto se vean como la Home sin copiar CSS ni HTML. Lo que describe esta guía ya está implementado y en uso en `templates/index.html`.

**Punto de partida:** copiar `templates/ejemplo-pagina.html`. Con la app corriendo en desarrollo se ve en <http://localhost:8080/ejemplo>.

## 1. Esqueleto de una página

Toda página delega su estructura en `templates/layout/base.html`, que pone el `<head>` (fuentes, Bootstrap, Bootstrap Icons, `tokens.css`, `hoseg.css`), el navbar con el óvalo móvil, el footer y los scripts comunes. **La página solo escribe su `<main>`**: no se vuelve a escribir el navbar, el footer ni el `<head>` en ningún otro sitio.

```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org"
      th:replace="~{layout/base :: pagina(titulo='Tienda · HÖSÉG', activa='tienda',
                                          transparente=false, contenido=~{::main}, estilos=~{::link}, scripts=~{})}">
<head>
    <link rel="stylesheet" th:href="@{/css/tienda.css}">   <!-- solo si la página tiene CSS propio -->
</head>
<body>
<main class="hs-main--bajo-nav">
    <section class="hs-seccion">
        <div class="container">
            ...
        </div>
    </section>
</main>
<!-- <script th:src="@{/js/tienda.js}" defer></script>  solo con scripts=~{::script} -->
</body>
</html>
```

Parámetros de `layout/base :: pagina(...)` (hay que pasarlos todos):

| Parámetro | Qué es |
|---|---|
| `titulo` | Texto del `<title>` |
| `activa` | Destino que se marca en el navbar y el óvalo: `inicio` \| `tienda` \| `cafe` \| `impacto` \| `nosotros` \| `carrito` \| `cuenta`. El óvalo móvil tiene Inicio, Tienda, Café, Impacto y Cuenta; `carrito` no marca ninguno porque en móvil el carrito está en la barra superior |
| `transparente` | `true` solo si la página tiene un banner a pantalla completa (la Home); en todas las demás, `false` |
| `contenido` | Siempre `~{::main}`: el `<main>` de la página |
| `estilos` | `~{::link}` si la página trae su propio CSS en su `<head>`; `~{}` si no |
| `scripts` | `~{::script}` si la página trae su propio JS al final del `<body>`; `~{}` si no |

| Pieza que pone el layout | Qué hace |
|---|---|
| `fragments/navbar :: navbar(activa, transparente)` | Barra superior fija (desktop y móvil) **y** óvalo inferior móvil |
| `fragments/footer :: footer` | Footer; en móvil añade relleno inferior para que el óvalo no tape el copyright |
| `js/navbar.js` | Opacidad de la barra al hacer scroll y el indicador deslizante del óvalo |
| Bootstrap JS | Necesario para el menú desplegable de la cuenta |

`main.hs-main--bajo-nav` deja el hueco de la barra fija (56 px móvil / 64 px desktop); sin esa clase el contenido queda debajo de la barra. La Home no la usa porque su banner ocupa toda la pantalla. `js/home.js` (carrusel, contadores y scroll-spy) es **solo de la Home**, que lo pasa con `scripts=~{::script}`.

Lo que el navbar y el footer necesitan del modelo (`menuPrincipal`, `carritoUnidades`, `rutaConsulta`) lo pone `controller/AtributosGlobales` en todas las vistas; no hay que añadirlo en cada controlador. El estado de sesión (Ingresar / nombre del cliente) sale de Spring Security con `sec:authorize`.

**Agregar un destino al menú** (por ejemplo, una página nueva en la barra) es añadir un `EnlaceNavegacion` a `MENU_PRINCIPAL` en `AtributosGlobales`: aparece a la vez en la barra superior, en el footer y, si lleva icono, en el óvalo móvil. El óvalo admite cinco destinos en 360 px y ya los tiene (cuatro del menú más Cuenta), así que un destino nuevo va con icono `null`.

## 2. Fragmentos reutilizables

### Tarjeta de producto

```html
<div class="row g-3">
    <div class="col-6 col-md-4 col-xl-3" th:each="p : ${tarjetas}">
        <th:block th:replace="~{fragments/product-card :: card(${p})}"/>
    </div>
</div>
```

Recibe un `dto.TarjetaProducto`. Se obtiene de `CatalogoService.tarjetas()` (todo el catálogo visible) o `destacados(n)`. **Una tarjeta = un nombre de producto**, con sus tallas agrupadas; el enlace "Ver detalle" apunta a `/tienda/{codigo}` con el código de la primera talla. Muestra sola la variante "Agotado" y omite el badge si el producto no tiene compromiso.

### Badge de impacto

```html
<th:block th:replace="~{fragments/product-card :: badge(${producto.tipoCompromiso})}"/>
```

Recibe un `TipoCompromiso` (`ABRIGO` → "1 abrigo", `ARBOL` → "1 árbol"). Comprobar antes que no sea `null`. El texto, el icono (`getIcono()`) y el sufijo de la clase CSS (`getClave()`) salen del propio enum, así que la plantilla no tiene `if` por tipo.

### Paso y contador de impacto

```html
<th:block th:replace="~{fragments/impacto :: paso('01', 'bi-bag', 'Compras', 'Texto del paso.', true)}"/>
<th:block th:replace="~{fragments/impacto :: contador('bi-tree', ${impacto.arbolesPlantados}, 'Árboles plantados')}"/>
```

`paso(numero, icono, titulo, texto, flecha)`: `flecha` es `true` en todos menos el último. `contador(icono, valor, etiqueta)`: `home.js` anima la cifra al entrar en pantalla.

### Iconos

Se usa **Bootstrap Icons**, servido desde su WebJar (lo carga el layout). No hay SVG en las plantillas.

```html
<i class="bi bi-cart hs-icono" aria-hidden="true"></i>
```

Catálogo completo: <https://icons.getbootstrap.com>. Son una fuente: toman el color del texto y su tamaño es el `font-size`. `hs-icono` (22 px) y `hs-icono-sm` (18 px) dan las medidas del diseño; las cajas de icono de los componentes (`hs-paso__icono`, `hs-contador__icono`, `hs-btn-redondo`) ya fijan el suyo. Si el icono es decorativo lleva `aria-hidden="true"`; si es lo único que hay en un botón, el botón lleva `aria-label`.

Iconos que ya usa el portal: `bi-house` (Inicio), `bi-bag` (Tienda), `bi-cup-hot` (Café), `bi-activity` (Impacto), `bi-person` (Cuenta), `bi-cart` (Carrito), `bi-gift` (abrigo; Bootstrap Icons no tiene prenda), `bi-tree` (árbol), `bi-people`, `bi-heart`, `bi-chevron-left/right`, `bi-arrow-right`, `bi-journal-text` (Libro de Reclamaciones), `bi-shield-check` (sello B).

## 3. Clases `hs-*` (en `static/css/hoseg.css`)

| Uso | Clases |
|---|---|
| Sección de página | `hs-seccion`, `hs-seccion--alterna` (fondo crema oscuro), `hs-seccion--corta` |
| Ancho de lectura | `container hs-ancho-md` (1024 px), `container hs-ancho-sm` (672 px) |
| Titulares | `hs-kicker` (antetítulo en mayúsculas), `hs-h2` (titular de sección en Fraunces), `hs-texto-secundario` |
| Botones | `hs-btn hs-btn--primario`, `hs-btn--secundario`, `hs-btn--vidrio` (sobre foto), `hs-btn--deshabilitado`, `hs-btn-redondo` (circular con icono) |
| Chips | `hs-chip`, `hs-chip hs-chip--pequeno`, `hs-chip-talla`, `hs-chip-vidrio` (sobre foto) |
| Formularios | `hs-input` (campo de texto con foco terracota) |
| Enlaces | `hs-enlace-flecha` |
| Anchos sueltos | `hs-ancho-texto` (párrafo, 36 rem), `hs-ancho-form` (formulario corto, 24 rem) |
| Iconos | `hs-icono` (22 px), `hs-icono-sm` (18 px) |

Para la grilla, espaciado y utilidades (`row`, `col-*`, `d-flex`, `gap-*`, `mb-*`, `d-none d-md-block`…) se usa **Bootstrap 5** tal cual. Los componentes de Bootstrap (`.btn`, `.card`, `.form-control`) también funcionan y toman la paleta por las variables `--bs-*` mapeadas en `tokens.css`, pero para que la página se vea igual que la Home es mejor usar las `hs-*` de arriba.

### Reglas

- **Nada de colores literales en las plantillas ni en los CSS de componentes.** Todo sale de `tokens.css` (`var(--hs-terracotta)`, `var(--hs-muted)`, transparencias y sombras incluidas). Si falta un color, se agrega ahí.
- **Nada de `style="..."` en las plantillas**: si hace falta una medida, se crea una clase.
- **Nada de SVG copiados**: los iconos son de Bootstrap Icons.
- Si un bloque se repite (dos o más veces con distinto texto), se convierte en un fragmento con parámetros en `fragments/`.
- **Nada de Tailwind** ni clases copiadas del export de Figma (`flex items-center`, `text-[11px]`): se traducen a Bootstrap o a CSS propio.
- CSS propio de una página va en `static/css/<pagina>.css` y se enlaza en su `<head>`. Si algo sirve para más de una página, va a `hoseg.css`.
- En móvil, evitar `row g-5` dentro de `.container`: sobresale 12 px y provoca scroll horizontal. Usar `g-4 g-sm-5`.

## 4. Rutas que la Home ya enlaza

Quien implemente cada página debe usar exactamente estas rutas, porque ya están en el navbar, el óvalo, las tarjetas, el CTA y el footer, y en `SecurityConfig`:

| Ruta | Página | Notas |
|---|---|---|
| `/tienda` | Listado de productos | Pública |
| `/tienda/{codigo}` | Detalle de producto con selector de tallas | `codigo` es el de **una** talla (`HSG-CAS-001`); la página agrupa por `nombre` y lista las demás |
| `/carrito` | Carrito | Pública (se puede llenar sin cuenta) |
| `/carrito/confirmar` | Checkout | **Exige sesión** (ya configurado) |
| `/login` | Inicio de sesión de clientes | Spring Security espera `POST /login` con `username` y `password` |
| `/registro` | Registro de clientes | Pública |
| `/cuenta` | Datos y pedidos del cliente | **Exige sesión** |
| `/consulta-impacto?boleta=WEB-000110` | Consulta pública | Además debe aceptar `?donacion=<id>`, que es lo que imprime el QR de la boleta del escritorio |
| `/logout` | Cierra sesión | Ya configurado, `POST` |
| `/cafe` | Höség Café (CU-05) | Pública |
| `/contacto` | Formulario de contacto (CU-08) | Pública |
| `/terminos`, `/politica-rsu` | Páginas legales (CU-06) | Públicas; una sola plantilla de texto largo |
| `/libro-reclamaciones` | Libro de Reclamaciones | Pública; obligatoria por ley para tiendas en línea en Perú |
| `/boletin` | Suscripción al boletín (CU-09) | Pública, `POST` con campo `email` desde el footer |
| `/cuenta?seccion=datos` | Pestaña "Mis datos" de la cuenta | La enlaza el menú de usuario del navbar |

La ruta de consulta se lee de la propiedad `hoseg.consulta.ruta` (modelo: `${rutaConsulta}`); no escribirla a mano.

## 5. Servicios que ya existen

Los más usados al armar una página:

- `CatalogoService`: `tarjetas()` y `tarjetas(FiltroCatalogo)`, `destacados(n)`, `detalle(codigo)`, `categorias()`, `colecciones()`. Filtra `activo AND visible_web`.
- `ImpactoService`: `resumen()` (cifras sobre lo entregado), `nombresComunidades()`, `nombresOng()`, `consultarBoleta(codigo)` y `consultarDonacion(id)`.
- `CarritoService` (`@SessionScope`, un carrito por visitante): `agregar(Producto, cantidad)`, `quitar(codigo)`, `vaciar()`, `items()`, `estaVacio()`, `totalUnidades()`, `total()`. Cada talla es un `Producto` distinto, así que cada línea del carrito es un código.
- `ContenidoService`: `bloque(clave)` y `bloques(...)`, los textos editables desde el panel; si un bloque no existe, la plantilla usa su texto por defecto.
- `ResenaService`, `VentaService`, `ClienteService`, `CafeService` y los demás: la lista completa, con qué controlador usa cada uno, está en el [README](../README.md#estructura); qué pone cada controlador en el modelo, en [contrato-backend.md](contrato-backend.md).

Los controladores **no** usan repositorios ni contienen reglas de negocio: llaman a servicios. Las validaciones de formulario van en DTOs de `dto/` con `@NotBlank`, `@Email`, `@Size`.

## 6. Probar sin base de datos

`HomeControllerTest` muestra cómo renderizar una vista con `@WebMvcTest` + `@MockitoBean` para los servicios: sirve para comprobar que una plantilla compila y muestra lo esperado sin conectarse a Supabase. `EjemploControllerTest` hace lo mismo con una página interior (perfil `dev`) y comprueba que los recursos de los WebJars se sirven.
