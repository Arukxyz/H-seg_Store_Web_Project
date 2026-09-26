# Casos de uso · Portal web HÖSÉG

Matriz de trazabilidad del portal web (Spring Boot + Thymeleaf + Supabase). Es la misma información de las notas de `casos-de-uso-web.drawio`, generada desde la misma fuente.

## Actores

| Actor | Tipo | Descripción |
|---|---|---|
| **Visitante** | Humano (principal) | Cualquier persona que entra al portal sin iniciar sesión. |
| **Cliente registrado** | Humano (principal) | Visitante con cuenta; hereda todo lo que puede hacer el Visitante (CU-01 a CU-10, página 2) y además puede confirmar la compra. |
| **Administrador web** | Humano (principal) | Personal de Höség con rol ADMINISTRADOR que mantiene el contenido del portal. |
| **Supabase Cloud DB** | Sistema externo (secundario) | PostgreSQL compartido: persiste los datos, ejecuta las transacciones y valida unicidad. |
| **Servicio de correo** | Sistema externo (secundario) | Servidor SMTP usado por JavaMailSender para las notificaciones. |

## Requerimientos funcionales cubiertos

| Código | Requerimiento | Casos de uso |
|---|---|---|
| RF01 | Gestión de autenticación | CU-07, CU-11, CU-12, CU-14, CU-16 |
| RF02 | Registro de ventas web | CU-04, CU-15, CU-15.2, CU-21 |
| RF03 | Control de stock (lectura de disponibilidad) | CU-02, CU-02.1, CU-03, CU-15.1 |
| RF04 | Registro de triple impacto | CU-01, CU-03, CU-05, CU-06, CU-10, CU-13, CU-15, CU-15.3, CU-21 |
| RF06 | Seguimiento de donaciones | CU-10, CU-10.1, CU-13 |
| RF11* | Gestión de contenido web y Höség Café (propuesto) | CU-05, CU-06, CU-17, CU-18, CU-19 |
| RF12* | Comunicación con el cliente: contacto, boletín y correos (propuesto) | CU-08, CU-09, CU-20 |

RF05 (lotes), RF07 (alertas de stock), RF08 (abastecimiento), RF09 (bitácora) y RF10 (reportes) no se atienden desde el portal web. RF02 dice «descontar stock comercial»: en el portal la compra queda PENDIENTE y el stock se descuenta al confirmarse el pago, no al confirmar el carrito.

## Matriz de casos de uso

| Código | Caso de uso | Actor | Relación | RF | Vista | Ruta | Controlador | Tablas | Lógica |
|---|---|---|---|---|---|---|---|---|---|
| CU-01 | Explorar página principal | Visitante · Supabase | — | RF04 | index.html (fragmentos navbar, footer, product-card) | GET / | HomeController → CatalogoService, ImpactoService | producto (lee), donacion (lee), lote_donacion (lee), comunidad (lee), venta (lee) | Ve el banner de identidad, el carrusel de productos destacados con su badge «1 abrigo» / «1 árbol», las cifras de impacto entregado y las comunidades beneficiadas. |
| CU-02 | Explorar catálogo de productos | Visitante · Supabase | — | RF03 | tienda.html | GET /tienda | TiendaController → CatalogoService | producto (lee) | Recorre casacas, accesorios y café agrupados por nombre, con precio, tallas y compromiso de impacto; los productos sin stock comercial se muestran como «Agotado». |
| CU-02.1 | Filtrar por categoría, colección o impacto | Visitante · Supabase | «extend» de CU-02 | RF03 | tienda.html (panel de filtros) | GET /tienda?categoria=&coleccion=&impacto= | TiendaController → CatalogoService | producto (lee) | Acota el listado por categoría (Casacas, Accesorios, Café), colección (Älpafill, Comunidad, Urbana) o tipo de impacto (abrigo / árbol). |
| CU-03 | Ver detalle de producto | Visitante · Supabase | — | RF03, RF04 | producto-detalle.html | GET /tienda/{codigo} | TiendaController → CatalogoService | producto (lee) | Ve fotos, descripción, precio y las tallas disponibles (cada talla es un código distinto); elige talla y cantidad y ve qué donación genera su compra. |
| CU-03.1 | Consultar guía de tallas | Visitante | «extend» de CU-03 | — | producto-detalle.html (modal Bootstrap) | Sin petición al servidor | — | Ninguna | Abre la tabla comparativa de medidas S · M · L · XL antes de elegir talla. |
| CU-04 | Gestionar carrito de compras | Visitante · Supabase | — | RF02 | carrito.html | GET /carrito · POST /carrito/agregar · POST /carrito/actualizar · POST /carrito/quitar | CarritoController → CarritoService (@SessionScope) | producto (lee) | Agrega, cambia la cantidad o quita prendas; ve subtotales, total y el impacto que generará. No necesita cuenta: el carrito vive en la sesión HTTP. |
| CU-05 | Conocer Höség Café | Visitante · Supabase | — | RF04, RF11* | cafe.html | GET /cafe | CafeController → CafeService | cafe_carta* (lee), cafe_local* (lee), contenido_web* (lee), producto (lee) | Conoce la propuesta «por cada taza, un árbol» junto a la ONG Pachamama Raymi; revisa la carta, los locales y horarios, y puede ir a comprar café en grano a la tienda (categoría Café, genera 1 árbol). |
| CU-06 | Conocer la empresa y su triple impacto | Visitante · Supabase | — | RF04, RF11* | nosotros.html · politica-rsu.html · terminos.html | GET /nosotros · GET /politica-rsu · GET /terminos | InstitucionalController → ContenidoService, ImpactoService | contenido_web* (lee), comunidad (lee), ong (lee) | Lee la historia de la marca, los modelos Buy One, Give One y Buy One, Plant One, las comunidades y la ONG aliada, y las políticas de responsabilidad social. |
| CU-07 | Registrarse como cliente | Visitante · Supabase, Correo | — | RF01 | registro.html | GET /registro · POST /registro | AuthController → ClienteService (DTO RegistroClienteForm: @NotBlank, @Email, @Size) | cliente (lee, inserta) | Completa nombre, apellido, documento, email, teléfono, dirección y contraseña; los errores se muestran junto a cada campo (th:errors). Si su email ya existe por una compra anterior, completa ese registro. |
| CU-08 | Enviar mensaje de contacto | Visitante · Supabase, Correo | — | RF12* | contacto.html | GET /contacto · POST /contacto | ContactoController → ContactoService (DTO MensajeContactoForm) | mensaje_contacto* (inserta) | Escribe su nombre, email, asunto y mensaje (tallas, pedidos corporativos, personal shopper) y recibe un acuse de recibo. |
| CU-09 | Suscribirse al boletín Changemaker | Visitante · Supabase, Correo | — | RF12* | fragments/footer.html (formulario) | POST /boletin | BoletinController → BoletinService | suscriptor_boletin* (lee, inserta) | Deja su email en el pie de página para recibir campañas, lanzamientos e informes de impacto. |
| CU-10 | Consultar impacto por boleta | Visitante · Supabase | — | RF04, RF06 | consulta-impacto.html | GET /consulta-impacto?boleta=WEB-000110 · GET /api/impacto?boleta= (AJAX) | ConsultaImpactoController → ImpactoService | venta (lee), detalle_venta (lee), donacion (lee), lote_donacion (lee), comunidad (lee), ong (lee) | Ingresa el código de su boleta y ve cada abrigo o árbol que generó, su estado (Pendiente de asignar → Lote asignado → Entregada) y la comunidad beneficiada. |
| CU-10.1 | Acceder desde el QR de la boleta | Visitante · Supabase | «extend» de CU-10 | RF06 | consulta-impacto.html | GET /consulta-impacto?donacion={id} | ConsultaImpactoController → ImpactoService | donacion (lee), lote_donacion (lee), comunidad (lee) | Escanea el QR impreso en su boleta y llega directo al estado de esa donación, sin escribir el código. |
| CU-11 | Iniciar y cerrar sesión | Cliente registrado · Supabase | — | RF01 | login.html | GET /login · POST /login · POST /logout | SecurityConfig (formLogin) → ClienteDetailsService | cliente (lee) | Ingresa email y contraseña; al validarse, el menú muestra su nombre y se habilitan la compra y su cuenta. El carrito armado como visitante se conserva. |
| CU-12 | Recuperar contraseña | Cliente registrado · Supabase, Correo | — | RF01 | recuperar.html · restablecer.html | GET/POST /recuperar · GET/POST /restablecer?token= | RecuperacionController → ClienteService | cliente (lee, actualiza), token_recuperacion* (inserta, actualiza) | Pide un enlace con su email, lo abre desde su correo y define una contraseña nueva. |
| CU-13 | Consultar mis pedidos e impacto | Cliente registrado · Supabase | — | RF04, RF06 | cuenta.html (pestaña Pedidos) · pedido-detalle.html | GET /cuenta · GET /cuenta/pedidos/{codigo} | CuentaController → VentaService, ImpactoService | venta (lee), detalle_venta (lee), donacion (lee), lote_donacion (lee), comunidad (lee) | Revisa su historial de compras con su estado (Pendiente, Pagado, Anulado), el detalle de cada una y el recorrido de cada donación hasta su comunidad. |
| CU-14 | Actualizar datos de cuenta | Cliente registrado · Supabase | — | RF01 | cuenta.html (pestaña Mis datos) | POST /cuenta/datos · POST /cuenta/contrasena | CuentaController → ClienteService (DTO DatosClienteForm) | cliente (actualiza) | Edita nombre, teléfono y dirección de envío; cambia su contraseña confirmando la actual. El email es su usuario y no se edita aquí. |
| CU-15 | Confirmar compra | Cliente registrado · Supabase, Correo | — | RF02, RF04 | carrito.html → compra-exito.html | POST /carrito/confirmar (exige sesión) | CarritoController → VentaService (@Transactional) | venta (inserta), detalle_venta (inserta), donacion (inserta), producto (lee), cliente (lee) | Confirma su carrito; en una sola transacción se registran la venta, sus líneas y las donaciones 1:1. Se vacía el carrito y se muestra el código de boleta. El pedido queda PENDIENTE hasta confirmarse el pago: la web no descuenta stock. |
| CU-15.1 | Validar carrito y stock | Cliente registrado · Supabase | «include» de CU-15 | RF03 | carrito.html (mensajes por línea) | Dentro de POST /carrito/confirmar | VentaService | producto (lee) | Rechaza el carrito vacío, los productos inactivos u ocultos y las cantidades mayores al stock comercial disponible, con un mensaje por línea. |
| CU-15.2 | Generar código de comprobante | Cliente registrado · Supabase | «include» de CU-15 | RF02 | compra-exito.html | Dentro de POST /carrito/confirmar | VentaService | seq_comprobante_web (nextval), venta (inserta) | Asigna el siguiente código WEB-nnnnnn con la secuencia de la base, sin calcular máximos, para que dos compras simultáneas nunca repitan número. |
| CU-15.3 | Registrar compromiso 1:1 | Cliente registrado · Supabase | «include» de CU-15 | RF04 | compra-exito.html (resumen de impacto) | Dentro de POST /carrito/confirmar | VentaService | producto (lee), donacion (inserta) | Por cada línea con compromiso crea una donación PENDIENTE: 1 abrigo por casaca o chaleco, 1 árbol por accesorio o café, tantas unidades como las compradas. |
| CU-16 | Iniciar sesión administrativa | Administrador web · Supabase | — | RF01 | admin/login.html | GET /admin/login · POST /admin/login · POST /admin/logout | SecurityConfig (cadena /admin/**) → UsuarioDetailsService | usuario (lee) | El personal de Höség con rol ADMINISTRADOR entra al panel del portal. Las cuentas de cliente no tienen acceso a /admin. |
| CU-17 | Gestionar destacados y visibilidad web | Administrador web · Supabase | — | RF11* | admin/catalogo.html | GET /admin/catalogo · POST /admin/catalogo/{codigo} | AdminCatalogoController → CatalogoService | producto (lee, actualiza) | Elige qué productos aparecen en el carrusel de la Home y oculta de la tienda los que no deben venderse en línea. |
| CU-18 | Gestionar Höség Café | Administrador web · Supabase | — | RF11* | admin/cafe.html | GET/POST /admin/cafe/carta · GET/POST /admin/cafe/locales | AdminCafeController → CafeService | cafe_carta* (inserta, actualiza), cafe_local* (inserta, actualiza), contenido_web* (actualiza) | Mantiene la carta (bebidas, precios, disponibilidad), los locales y horarios, y la cifra de árboles sembrados por el café que se publica en /cafe. |
| CU-19 | Gestionar banners y contenido institucional | Administrador web · Supabase | — | RF11* | admin/contenido.html | GET /admin/contenido · POST /admin/contenido/{clave} | AdminContenidoController → ContenidoService | contenido_web* (lee, inserta, actualiza) | Cambia imágenes y textos del banner de la Home, las campañas, la página Nosotros y las políticas sin modificar código. |
| CU-20 | Atender mensajes y suscriptores | Administrador web · Supabase | — | RF12* | admin/mensajes.html | GET /admin/mensajes · POST /admin/mensajes/{id}/atendido · GET /admin/suscriptores.csv | AdminMensajesController → ContactoService, BoletinService | mensaje_contacto* (lee, actualiza), suscriptor_boletin* (lee) | Revisa las consultas recibidas, las marca como atendidas y descarga la lista de suscriptores activos. |
| CU-21 | Consultar panel de pedidos web | Administrador web · Supabase | — | RF02, RF04 | admin/panel.html | GET /admin/panel?desde=&hasta= | AdminPanelController → VentaService, ImpactoService | venta (lee), detalle_venta (lee), cliente (lee), donacion (lee) | Ve los pedidos web del periodo por estado, el monto vendido y las donaciones generadas. Solo consulta: confirmar o anular pedidos no se hace desde el portal. |

\* Tabla o columna nueva que requiere un script `07` en el repositorio del escritorio.

## Tablas y columnas nuevas que suponen estos casos de uso

| Tabla / columna | Uso | Casos de uso |
|---|---|---|
| `cafe_carta` | Ítems de la carta del café (nombre, descripción, categoría, precio, disponible) | CU-05, CU-18 |
| `cafe_local` | Locales del café (nombre, dirección, horario, enlace al mapa, activo) | CU-05, CU-18 |
| `contenido_web` | Bloques editables por clave: banners, textos de Nosotros, políticas, cifra de árboles del café | CU-05, CU-06, CU-18, CU-19 |
| `mensaje_contacto` | Mensajes del formulario de contacto con estado NUEVO / ATENDIDO | CU-08, CU-20 |
| `suscriptor_boletin` | Emails suscritos al boletín, con indicador de activo | CU-09, CU-20 |
| `token_recuperacion` | Hash del token de recuperación, cliente, vencimiento y uso | CU-12 |
| `producto.destacado` | Marca los productos que van al carrusel de la Home | CU-17 (y CU-01) |

Todas las demás tablas (`producto`, `cliente`, `venta`, `detalle_venta`, `donacion`, `lote_donacion`, `comunidad`, `ong`, `usuario`) y la secuencia `seq_comprobante_web` ya existen en `01_schema.sql` y `06_web_clientes.sql`.
