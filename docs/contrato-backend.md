# Contrato backend ↔ plantillas

Qué datos recibe cada página y qué formularios envía. El backend (servicios, controladores y base de datos) ya está terminado: **cada bloque solo rediseña las plantillas de su carpeta**, sin tocar Java.

Cada plantilla ya existe como **esqueleto funcional**: muestra todos los datos y sus formularios funcionan, pero sin diseño. Reemplacen el HTML con su diseño de Figma y **conserven los nombres** de los atributos y de los campos de los formularios. El comentario al inicio de cada plantilla repite su parte de este contrato.

## Cómo trabajar

1. Copien `application-local.properties` (se los paso por privado) en `src/main/resources/` y ejecuten con `mvn spring-boot:run` o con el botón Run del IDE. Abran <http://localhost:8080>.
2. Creen su rama desde `main`: `feature/bloque-N-…`.
3. Antes de cada commit, corran `mvn test`. `VistasTest` renderiza **todas** las páginas con datos de prueba: si una plantilla queda rota (un atributo mal escrito, un `th:field` que no existe), falla ahí y no en la demo.

## Reglas comunes

- **Layout:** toda página pública usa `layout/base` (ver `docs/guia-plantillas.md`); las del panel usan `layout/admin`.
- **No llamen `contenido`, `titulo`, `activa`, `estilos` ni `scripts` a nada del modelo:** son parámetros del layout y lo taparían.
- **Avisos tras un POST:** `<th:block th:replace="~{fragments/avisos :: avisos}"/>` al inicio del `<main>`. Muestra los flash `exito` y `error` (el resultado del boletín lo muestra el propio footer).
- **Formularios:** siempre `th:action="@{...}"` (agrega el token CSRF) y `th:field="*{campo}"`; el error de cada campo va en `th:errors="*{campo}"`. Algunos formularios tienen errores "globales" con nombre propio (por ejemplo `*{contrasenasCoinciden}`), indicados en cada sección.
- **Formatos:** precio `'S/ ' + ${#numbers.formatDecimal(x, 1, 'COMMA', 2, 'POINT')}` → `S/ 259.90`; fecha `${#temporals.format(f, 'dd/MM/yyyy')}`.
- **Estados con texto listo:** `estado.etiqueta` en `EstadoVenta` (Pendiente, Pagado, Anulado), `EstadoDonacion` (En espera de pago → Pendiente de asignar → Lote asignado → Entregada, con `paso` de 0 a 3), `EstadoEnvio` y `TipoEnvio`. `TipoCompromiso.etiqueta` da "1 abrigo" / "1 árbol".
- **Impacto:** `ImpactoCompra.texto` ya viene armado: "2 abrigos + 1 árbol".
- **Sesión:** `sec:authorize="isAuthenticated()"` y `${#authentication.principal.nombre}` (cliente o administrador).
- **Rutas con sesión:** `/carrito/confirmar`, `/compra-exito`, `/cuenta/**` y `POST /tienda/{codigo}/resenas` exigen cliente; `/admin/**` exige administrador. Todo lo demás es público. Si agregan una ruta que necesite sesión, avísenme: hay que declararla en `SecurityConfig`.

---

## Bloque 1 · Tienda, catálogo y compra

| Pantalla | Ruta | Plantilla |
|---|---|---|
| 1.1 Tienda | `GET /tienda` | `tienda/lista.html` |
| 1.2 Detalle y reseñas | `GET /tienda/{codigo}` | `tienda/detalle.html` |
| 1.4 Carrito | `GET /carrito` | `carrito/carrito.html` |
| 1.5 Confirmar compra | `GET/POST /carrito/confirmar` | `carrito/confirmar.html` |
| 1.6 Compra registrada | `GET /compra-exito` | `carrito/compra-exito.html` |

**`/tienda`** filtra por parámetros GET, todos opcionales: `q`, `categoria`, `coleccion`, `impacto` (`ABRIGO`/`ARBOL`), `disponibles=true`, `orden` (`DESTACADOS`, `PRECIO_ASC`, `PRECIO_DESC`, `NOMBRE`).
Modelo: `productos` (List<TarjetaProducto>, se pintan con `fragments/product-card :: card`), `total`, `filtro` (con `hayFiltros()`, `soloDisponibles()` y `ordenEfectivo()`), `categorias`, `colecciones`, `compromisos`, `ordenes`.

**`/tienda/{codigo}`**: el código de cualquier talla abre el modelo completo.
- `producto` (DetalleProducto): `nombre`, `categoria`, `coleccion`, `descripcion`, `urlImagen`, `precioDesde`, `tipoCompromiso`, `disponible`, `sinTallas` (café y accesorios: sin selector), `codigoElegido`, `relacionados`.
  - `variantes` (una por talla): `codigo`, `talla`, `precio`, `disponible`, `maximo` (stock, para limitar la cantidad), `quedanPocas`.
- `resumenResenas`: `promedio`, `total`, `estrellas` (redondeado a medio punto) y `tieneResenas()`.
- `resenas`: `calificacion`, `titulo`, `comentario`, `autor` ("María G."), `talla`, `fecha`, `editada`.
- `resenaForm`: **solo existe** si el cliente puede reseñar, es decir, si compró el modelo en un pedido pagado. Úsenlo con `th:if="${resenaForm != null}"`.
- `yaReseno`: si es true, el mismo formulario edita la reseña existente.

Formularios:
- **Agregar:** `POST /carrito/agregar` con `codigo` (la talla elegida) y `cantidad`.
- **Reseñar:** `POST /tienda/{codigo}/resenas` con `calificacion` (1 a 5), `titulo` y `comentario` (10 a 1000 caracteres). Al terminar vuelve a `#resenas` con flash.

**`/carrito`**
- `items`: `codigoProducto`, `nombre`, `talla`, `precioUnitario`, `cantidad`, `subtotal()`, `tipoCompromiso`, `urlImagen`.
- `total`, `impacto` y `vacio`.
- `problemasPorCodigo`: mensaje por línea, del tipo "Solo quedan 2 unidades…".
- `sePuedeConfirmar`.

Acciones: `POST /carrito/actualizar` (`codigo`, `cantidad`; 0 quita la línea) y `POST /carrito/quitar` (`codigo`).

**`/carrito/confirmar`**
- Recibe lo mismo que el carrito, y además:
  - `envioForm` (DatosEnvioForm): `tipo`, `nombreReceptor`, `telefonoReceptor`, `direccion`, `referencia`, `distrito`, `provincia`, `departamento`. Viene precargado con los datos de la cuenta. Error global: `*{direccionCompleta}`.
  - `tiposEnvio`, `costoDelivery` y `localesRecojo`.
- El costo lo calcula el servidor; el total a pagar es productos + envío.
- Si el carrito cambió (sin stock o producto retirado), vuelve a `/carrito` con flash `error`.
- La web **no cobra**: incluyan el bloque "Instrucciones de pago".

**`/compra-exito`**: `compra` (CompraRegistrada) con `codigoComprobante`, `totalProductos`, `costoEnvio`, `totalAPagar`, `tipoEnvio` e `impacto`. Si se recarga la página, redirige a `/cuenta`.

## Bloque 2 · Cuenta del cliente, autenticación y delivery

| Pantalla | Ruta | Plantilla |
|---|---|---|
| 2.1 Iniciar sesión | `GET /login` (el POST lo procesa Spring Security) | `cuenta/login.html` |
| 2.2 Registro | `GET/POST /registro` | `cuenta/registro.html` |
| 2.3 Recuperar contraseña | `GET/POST /recuperar` | `cuenta/recuperar.html` |
| 2.4 Nueva contraseña | `GET/POST /restablecer?token=…` | `cuenta/restablecer.html` |
| 2.5 y 2.7 Mi cuenta | `GET /cuenta?seccion=pedidos\|datos` | `cuenta/cuenta.html` |
| 2.6 Detalle de pedido y envío | `GET /cuenta/pedidos/{codigo}` | `cuenta/pedido-detalle.html` |

**`/login`**: el formulario **debe** enviar `email` y `password` por POST a `/login`.
- Variantes por parámetro de la URL: `?error`, `?bloqueado=5` (minutos), `?registrado`, `?restablecida`.
- Modelo: `desdeCompra`, para el aviso "tu carrito se conserva".
- 3 intentos fallidos bloquean la cuenta 5 minutos.

**`/registro`**
- `registroForm`: `nombre`, `apellido`, `tipoDoc`, `numDoc`, `email`, `telefono`, `direccion`, `contrasena`, `confirmacion`, `aceptaTerminos`.
- Errores globales: `*{documentoValido}` y `*{contrasenasCoinciden}`. Los campos `email` y `numDoc` también reciben errores de negocio, como "ya registrado".
- Además recibe `tiposDocumento`.
- Si todo sale bien, va a `/login?registrado`. Quien ya compró sin cuenta se registra con su mismo email.

**`/recuperar`**: `POST` con `email`. La variante `?enviado` muestra el mismo mensaje exista o no el correo. **Sin SMTP, el enlace se imprime en la consola del servidor**: cópienlo de ahí para probar.

**`/restablecer`**: `vencido` (variante "Enlace vencido") y `restablecerForm` (`token` oculto, `nueva`, `confirmacion`; error global `*{contrasenasCoinciden}`).

**`/cuenta`**
- `seccion` (pestaña activa) y `nombre`.
- `impacto` (ImpactoCliente, es la "fidelidad"): `generado`, `entregado` y `enEsperaDePago`, cada uno con su `texto`.
- `pedidos`: `codigoComprobante`, `fecha`, `estado`, `totalAPagar`, `impacto`, `estadoEnvio` (puede ser null).
- `datosForm` → `POST /cuenta/datos`: `email` (solo lectura), `nombre`, `apellido`, `telefono`, `direccion`.
- `contrasenaForm` → `POST /cuenta/contrasena`: `actual`, `nueva`, `confirmacion`.

**`/cuenta/pedidos/{codigo}`**: `pedido` (DetallePedido). Un pedido de otro cliente responde 404.
- `lineas`: `codigoProducto`, `nombre`, `talla`, `cantidad`, `subtotal`.
- `envio` (null en pedidos antiguos): `tipo`, `estado`, dirección, `costo`, `courier`, `codigoSeguimiento` y fechas.
- `donaciones`: línea de tiempo con `estado.paso`, `comunidad`, `ong` y `fechaEntrega`.
- `puedeResenar`: si es true, el botón "Reseñar" lleva a `/tienda/{codigoProducto}#resenas`.

La línea de tiempo de la donación es **el mismo componente** que la consulta pública del Bloque 3: acuérdenlo entre ambos.

## Bloque 3 · Impacto social, Höség Café y fidelidad

| Pantalla | Ruta | Plantilla |
|---|---|---|
| 3.1 Consulta de impacto | `GET /consulta-impacto?boleta=…` o `?donacion=…` | `impacto/consulta.html` |
| 3.2 Höség Café | `GET /cafe` | `institucional/cafe.html` |
| 3.3 Nosotros | `GET /nosotros` | `institucional/nosotros.html` |
| 3.4 Legales | `GET /terminos`, `GET /politica-rsu` | `institucional/legal.html` |
| 3.5 Contacto | `GET/POST /contacto` | `institucional/contacto.html` |
| 3.6 Libro de Reclamaciones | `GET/POST /libro-reclamaciones` | `institucional/libro-reclamaciones.html` |

**`/consulta-impacto`**: acepta `WEB-000110`, `web-110` o solo `110`. `?donacion=` es lo que trae el QR de la boleta del escritorio.
- Modelo: `boleta`, `noEncontrado` y `consulta` (ConsultaImpacto o null).
  - ConsultaImpacto trae `codigoComprobante`, `fechaCompra`, `estadoVenta`, `porQr` y `donaciones`.
  - Cada donación: `unidades`, `producto`, `estado` (con `paso`), `comunidad`, `ong`, `fechaEntrega`.
- Versión JSON para buscar sin recargar: `GET /api/impacto?boleta=…` (404 si no existe).
- Es pública: no muestra datos del comprador.

**`/cafe`**
- `arboles`: el contador.
- `carta`: categorías, cada una con sus ítems (`nombre`, `descripcion`, `precio`, `disponible`).
- `locales`: `nombre`, `direccion`, `horario`, `telefono`, `urlMapa`.
- `cafesEnGrano`: tarjetas de la tienda.

**`/nosotros`**
- `bloques['nosotros.historia']` (`titulo`, `cuerpo`, `imagenUrl`). Puede faltar: usen siempre un texto por defecto con `?:`.
- `impacto` (ResumenImpacto), `comunidades` y `ongs`.

**Legales**: `bloque` (`titulo`, `cuerpo`, `actualizadoEn`; puede ser null) y `tituloPorDefecto`. El texto lo edita el panel.

**`/contacto`**: `mensajeForm` (`nombre`, `email`, `asunto`, `mensaje`). La variante `?enviado` se muestra tras enviarlo, y se manda un acuse de recibo por correo.

**Boletín (footer)**: `POST /boletin` con `email`. Vuelve a la misma página con flash `boletin` o `boletinError`, y el ancla `#boletin`.

**`/libro-reclamaciones`**
- `reclamoForm`: `tipo`, `nombre`, `tipoDoc`, `numDoc`, `domicilio`, `email`, `telefono`, `tipoBien`, `montoReclamado`, `descripcionBien`, `detalle`, `pedidoConsumidor`, `codigoComprobante` (opcional; si no existe, el error sale en ese campo). Error global: `*{documentoValido}`.
- Además recibe `tiposReclamo`, `tiposDocumento` y `tiposBien`.
- La variante `?registrado` muestra `numeroHoja` (flash, por ejemplo `LR-000001`).

## Bloque 4 · Panel de administración web

Se entra con los usuarios **ADMINISTRADOR** del escritorio (tabla `usuario`), con la misma contraseña. Un ENCARGADO recibe `?sinPermiso`.

| Pantalla | Ruta | Plantilla |
|---|---|---|
| 4.1 Login | `GET /admin/login` (POST con `username` y `password`) | `admin/login.html` |
| 4.2 Panel | `GET /admin/panel?desde=AAAA-MM-DD&hasta=…` | `admin/panel.html` |
| 4.3 Catálogo web | `GET /admin/catalogo` | `admin/catalogo.html` |
| 4.4 Höség Café | `GET /admin/cafe?pestana=carta\|local\|arboles` | `admin/cafe.html` |
| 4.5 Contenido | `GET /admin/contenido?clave=…` | `admin/contenido.html` |
| 4.6 Mensajes | `GET /admin/mensajes?pestana=bandeja\|reclamaciones\|suscriptores` | `admin/mensajes.html` |
| 4.7 Reseñas | `GET /admin/resenas?estado=PUBLICADA\|OCULTA` | `admin/resenas.html` |

- **Layout** (`layout/admin`): `pagina(titulo, activa, contenido)`, con `activa` igual a `panel`, `catalogo`, `cafe`, `contenido`, `mensajes` o `resenas`. Ya muestra los flash `exito` y `error`.
- **Login:** variantes `?error`, `?bloqueado=5`, `?inactivo`, `?sinPermiso`, `?salir`.
- **Panel:** solo consulta, **sin botones de confirmar ni anular** (eso es del escritorio).
  - `desde` y `hasta`: por defecto, del 1 del mes a hoy.
  - `indicadores`: `pedidosHoy`, `pendientesDePago`, `montoVendido`, `impactoGenerado`, `mensajesNuevos`, `reclamosSinResponder`.
  - `pedidos`: `codigoComprobante`, `cliente`, `fecha`, `total`, `costoEnvio`, `estado`, `tipoEnvio`, `estadoEnvio`.
- **Catálogo:** `productos` (una fila por talla, con el stock en solo lectura), `destacados`, `maxDestacados` (8) y `vistaPrevia` (el carrusel).
  - `POST /admin/catalogo/{codigo}/visible?visible=…` cambia la visibilidad por talla.
  - `POST /admin/catalogo/{codigo}/destacado?destacado=…` marca o desmarca el modelo entero. Al pasar de 8 vuelve con flash `error`.
- **Café:**
  - Carta: `carta` e `itemForm` (`id` null para crear uno nuevo; `?item=5` abre ese ítem). Se guarda con `POST /admin/cafe/carta`; `POST /admin/cafe/carta/{id}/activo?activo=…` lo activa o desactiva.
  - Local: `locales` y `localForm`, que se guarda con `POST /admin/cafe/locales`.
  - Árboles: `arboles` → `POST /admin/cafe/arboles?arboles=…`.
- **Contenido:** `bloques`, `clave` y `contenidoForm` (`titulo`, `cuerpo`, `imagenUrl`, que debe ser `/images/…` o `https://…`) → `POST /admin/contenido/{clave}`.
- **Mensajes:**
  - Bandeja: `mensajes`; `POST /admin/mensajes/{id}/atendido` lo marca como atendido.
  - Reclamaciones: `reclamos` y `respuestaForm`; `POST /admin/reclamaciones/{id}/respuesta` responde y la respuesta se envía por correo.
  - Suscriptores: `suscriptores`; `GET /admin/suscriptores.csv` descarga la lista.
- **Reseñas:** `resenas` (`producto`, `talla`, `calificacion`, `comentario`, `cliente`, `estado`, `moderadaPor`), `estado` y `estados`.
  - `POST /admin/resenas/{id}/ocultar` y `POST /admin/resenas/{id}/publicar`.

## Bloque 5 · Arquitectura front-end y páginas auxiliares

- **Base compartida:** `layout/base.html`, `layout/admin.html`, `fragments/` (navbar, óvalo móvil, footer, product-card, impacto, avisos), `static/css/tokens.css` y `hoseg.css`. Cualquier cambio aquí afecta a todos los bloques: avisen en el grupo antes de cambiar un parámetro.
- **Errores:** `error/404.html` (ruta inexistente o recurso que no existe: producto oculto, pedido ajeno) y `error.html` (cualquier otro error; recibe `status`). Spring Boot los usa solos.
- **Avisos:** `fragments/avisos.html` define cómo se ven los flash (franja o toast).
- **Navbar con sesión:** ya usa `sec:authorize`. Para el nombre del cliente está `${#authentication.principal.nombre}`, y para el avatar, `getInicial()`.
- **Boletín del footer:** ya envía a `POST /boletin` y muestra `boletin` o `boletinError` sobre el formulario (`.hs-footer__aviso`, ancla `#boletin`).
- **Navbar:** tres estados ya resueltos: visitante (Ingresar), cliente (menú con su inicial) y personal del panel (enlace "Panel"); el óvalo móvil lleva a cuenta, panel o login según el caso.
- **Home** (coordinador): título y bajada del banner salen del bloque `home.banner` y "Quiénes somos" de `nosotros.historia`, editables en el panel (4.5); si faltan, la plantilla usa su texto. Imágenes por defecto: `static/images/hero-cusco.jpg` y `comunidad-cusco.jpg`; si el panel pone una `imagenUrl`, se usa esa.
