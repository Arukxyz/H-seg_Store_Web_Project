# Guía para crear páginas nuevas

Cómo hacer que Tienda, Detalle, Carrito, Login, Registro y Consulta de impacto se vean como la Home sin copiar CSS ni HTML. Lo que describe esta guía ya está implementado y en uso en `templates/index.html`.

**Punto de partida:** copiar `templates/ejemplo-pagina.html`. Con la app corriendo en desarrollo se ve en <http://localhost:8080/ejemplo>.

## 1. Esqueleto de una página

```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <th:block th:replace="~{fragments/head :: comun('Tienda · HÖSÉG')}"/>
    <link rel="stylesheet" th:href="@{/css/tienda.css}">   <!-- solo si la página tiene CSS propio -->
</head>
<body>
<th:block th:replace="~{fragments/navbar :: navbar('tienda', false)}"/>

<main class="hs-main--bajo-nav">
    <section class="hs-seccion">
        <div class="container">
            ...
        </div>
    </section>
</main>

<th:block th:replace="~{fragments/footer :: footer}"/>
<script th:src="@{/webjars/bootstrap/js/bootstrap.bundle.min.js}" defer></script>
<script th:src="@{/js/navbar.js}" defer></script>
</body>
</html>
```

| Pieza | Qué hace | Parámetros |
|---|---|---|
| `fragments/head :: comun(titulo)` | `<meta>`, Google Fonts (Fraunces + Outfit), Bootstrap desde el WebJar, `tokens.css`, `hoseg.css` | `titulo`: texto del `<title>` |
| `fragments/navbar :: navbar(activa, transparente)` | Barra superior fija (desktop y móvil) **y** óvalo inferior móvil | `activa`: `inicio` \| `tienda` \| `impacto` \| `nosotros` \| `carrito` \| `cuenta`. `transparente`: `true` solo si la página tiene un banner a pantalla completa (la Home); en todas las demás, `false` |
| `main.hs-main--bajo-nav` | Deja el hueco de la barra fija (56 px móvil / 64 px desktop). Sin esta clase el contenido queda debajo de la barra | — |
| `fragments/footer :: footer` | Footer; en móvil añade relleno inferior para que el óvalo no tape el copyright | — |
| `js/navbar.js` | Opacidad de la barra al hacer scroll y el indicador deslizante del óvalo. **Siempre** | — |
| `js/home.js` | Carrusel, contadores y scroll-spy. **Solo la Home** | — |

Lo que el navbar y el footer necesitan del modelo (`carritoUnidades`, `rutaConsulta`) lo pone `controller/AtributosGlobales` en todas las vistas; no hay que añadirlo en cada controlador. El estado de sesión (Ingresar / nombre del cliente) sale de Spring Security con `sec:authorize`.

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

Recibe un `TipoCompromiso` (`ABRIGO` → "1 abrigo", `ARBOL` → "1 árbol"). Comprobar antes que no sea `null`.

### Iconos

```html
<th:block th:replace="~{fragments/icons :: carrito}"/>
```

Nombres: `casa`, `tienda`, `impacto`, `carrito`, `cuenta`, `abrigo`, `arbol`, `comunidad`, `corazon`, `badge-abrigo`, `badge-arbol`, `paso-compra`, `paso-registro`, `paso-entrega`, `chevron-izq`, `chevron-der`, `flecha-der`. Son SVG en línea que toman el color del texto (`currentColor`). No hay Bootstrap Icons en el proyecto; si hace falta uno nuevo, se añade a `fragments/icons.html`.

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

Para la grilla, espaciado y utilidades (`row`, `col-*`, `d-flex`, `gap-*`, `mb-*`, `d-none d-md-block`…) se usa **Bootstrap 5** tal cual. Los componentes de Bootstrap (`.btn`, `.card`, `.form-control`) también funcionan y toman la paleta por las variables `--bs-*` mapeadas en `tokens.css`, pero para que la página se vea igual que la Home es mejor usar las `hs-*` de arriba.

### Reglas

- **Nada de colores literales en las plantillas.** Todo sale de `tokens.css` (`var(--hs-terracotta)`, `var(--hs-muted)`…). Si falta un color, se agrega ahí.
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

La ruta de consulta se lee de la propiedad `hoseg.consulta.ruta` (modelo: `${rutaConsulta}`); no escribirla a mano.

## 5. Servicios que ya existen

- `CatalogoService`: `tarjetas()`, `destacados(n)`. Filtra `activo AND visible_web`.
- `ImpactoService`: `resumen()` (cifras sobre lo entregado) y `nombresComunidades()`.
- `CarritoService` (`@SessionScope`, un carrito por visitante): `agregar(Producto, cantidad)`, `quitar(codigo)`, `vaciar()`, `items()`, `estaVacio()`, `totalUnidades()`, `total()`. Cada talla es un `Producto` distinto, así que cada línea del carrito es un código.
- Repositorios: `ProductoRepository`, `ClienteRepository` (`findByEmailIgnoreCase`), `VentaRepository` (`findByCodigoComprobante`), `DonacionRepository`, `LoteDonacionRepository`, `ComunidadRepository`.

Los controladores **no** usan repositorios ni contienen reglas de negocio: llaman a servicios. Las validaciones de formulario van en DTOs de `dto/` con `@NotBlank`, `@Email`, `@Size`.

## 6. Probar sin base de datos

`HomeControllerTest` muestra cómo renderizar una vista con `@WebMvcTest` + `@MockitoBean` para los servicios: sirve para comprobar que una plantilla compila y muestra lo esperado sin conectarse a Supabase.
