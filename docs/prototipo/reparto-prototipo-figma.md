# Reparto del prototipo en Figma · Portal web HÖSÉG

**Entrega al profesor:** imágenes del prototipo de toda la web (desktop y móvil).
**Ya hecho:** la página principal (Home). **Falta:** todo lo demás, repartido en 4 bloques.
**Fecha límite interna:** [poner fecha] · **Coordina y une todo:** [nombre]

Cada bloque es un flujo completo, así que cada uno puede diseñar sin esperar al resto. Antes de empezar, lean la sección 1 (reglas comunes), que es lo que hace que las 26 pantallas nuevas parezcan de la misma web que la Home.

---

## 1. Reglas comunes (para los 4)

### Partir de la Home, no de cero
- Trabajen **en el archivo de Figma del equipo**, cada uno en su propia página (`01 Tienda y compra`, `02 Cuenta del cliente`, `03 Impacto, Café e institucional`, `04 Administración`).
- **Copien los componentes de la Home, no los rediseñen:** barra de navegación, óvalo de navegación móvil, footer, tarjeta de producto, botones y badges «1 abrigo» / «1 árbol».
- Si usan **Figma Make**, adjunten al prompt el brief de la Home (`docs/brief-diseno-home.md`) y su sección de este documento, y pidan que respete exactamente la paleta y las tipografías de abajo.

### Estilo
| | Valor |
|---|---|
| Fondo | Crema `#F5F0E8` · tarjetas `#FDFAF5` · franjas alternas `#EDE6D6` |
| Texto | Marrón `#2C1810` · secundario `#7A6152` · bordes `#D9CEB8` |
| Botón principal y badge «1 abrigo» | Terracota `#C4622D` (hover `#A8501E`) |
| Badge «1 árbol» e impacto ambiental | Verde salvia `#5A7A5C` (fondo `#E8F0E8`) |
| Cifras y avisos | Ámbar `#D4820A` (fondo `#FEF3DC`) |
| Tipografías | Títulos **Fraunces** · texto **Outfit** (ambas en Google Fonts; soportan Ö, É, Ä) |
| Esquinas | 14 px en tarjetas y botones · óvalos totalmente redondeados |

### Frames y nombres
- Cada pantalla en **Desktop 1440** y **Móvil 390**. El panel de administración (bloque 4) solo en Desktop, salvo lo que se indique.
- Nombre del frame: `Pantalla / Variante / Tamaño`. Ejemplos: `Tienda / Default / Desktop`, `Carrito / Vacío / Móvil`.
- En móvil la navegación va en el **óvalo inferior**: nada importante debe quedar tapado por él (dejen unos 100 px libres abajo).

### Datos: siempre los reales del sistema
- Productos: *Casaca Älpafill Cusco* (S/ 259.90, tallas M · L · XL, 1 abrigo), *Chaleco Älpafill* (S/ 219.90, S, 1 abrigo), *Gorro de lana Layo* (S/ 49.90, única, 1 árbol), *Bufanda Paucartambo* (S/ 39.90, única, 1 árbol), *Chullo andino*, *Mochila artesanal Höség*.
- Cafés en grano (se agregarán al catálogo): *Café Bourbon · Yesica Llanqui* y *Café Typica · Eulogia Kehuarucho*, 250 g, S/ 40.00, 1 árbol.
- Comunidades beneficiadas: **Omacha, Ccatca, Marcapata, Paucartambo, Layo**. ONG aliada: **Pachamama Raymi**.
- Código de boleta: **`WEB-000110`**. Precios siempre con formato `S/ 259.90`.
- Estados de un pedido: **Pendiente · Pagado · Anulado**. Estados de una donación: **Pendiente de asignar → Lote asignado → Entregada**.
- No inventen campos que el sistema no tiene (colores de prenda, reseñas con estrellas, puntos de fidelidad, cuotas, cupones). Si creen que algo hace falta, avísenlo al coordinador antes de diseñarlo.

### Estados que siempre hay que diseñar
Además de la pantalla "normal": **vacío** (sin resultados, carrito vacío), **error de formulario** (mensaje debajo de cada campo) y **éxito** (confirmación). Cada bloque indica cuáles le tocan.

### Entrega
1. Frames terminados en su página del archivo compartido.
2. Exportación **PNG a 2x** con nombre `bloque-numero-pantalla-tamaño.png`. Ejemplo: `1-02-detalle-producto-movil.png`.
3. Un mensaje al coordinador con la lista de pantallas y cualquier decisión que hayan tomado fuera de este documento.

---

## 2. Qué mejoramos frente a las webs actuales de Höség

Esto es lo que el prototipo tiene que **hacer visible**, porque es el valor de nuestro sistema. Revisamos [hosegstore.com](https://hosegstore.com) y [hosegcafe.com](https://www.hosegcafe.com):

| Hoy en las webs de Höség | En nuestro portal | Se ve en |
|---|---|---|
| Donan el 2 % de las ventas: el cliente no sabe qué generó su compra | Modelo 1:1: cada prenda genera **1 abrigo o 1 árbol** exacto, indicado en la tarjeta, el detalle, el carrito y la confirmación | Bloque 1 |
| No hay forma de verificar la donación | **Consulta por código de boleta o QR**: estado de cada abrigo o árbol y comunidad que lo recibió | Bloques 2 y 3 |
| Cifras de impacto fijas en el texto (35 434 árboles, 22 245 niños, 5 018 árboles del café) | Cifras **calculadas desde la base de datos**, siempre al día | Bloques 3 y 4 |
| Höség Café es otro sitio, con la carta en PDF y pedidos solo por WhatsApp | El Café vive **dentro del portal**: carta en la página, local y horario, y el café en grano se compra en la tienda y genera su árbol trazable | Bloques 3 y 4 |
| Botón "Comprar por WhatsApp": la venta no queda registrada | Toda compra queda registrada con su boleta y su donación | Bloque 1 |
| Aviso de stock genérico ("¡Sólo queda 1 unidad!") | Disponibilidad real por talla; tallas agotadas deshabilitadas | Bloque 1 |
| Contenido editable solo por quien administra Shopify | Panel propio para destacados, contenido y carta del café | Bloque 4 |

**Lo que sí conviene conservar de las webs actuales:** buscador en la tienda, acordeones de descripción y cuidados en el producto, productos relacionados, contacto por WhatsApp y personal shopper, sello B Corp y el **Libro de Reclamaciones**, obligatorio por ley para tiendas en línea en Perú.

---

## 3. Bloque 1 · Tienda y compra

**Objetivo:** que un visitante encuentre una prenda, entienda qué impacto genera y confirme su compra.
**Casos de uso:** CU-02, CU-02.1, CU-03, CU-03.1, CU-04, CU-15.

| # | Pantalla | Ruta | Qué debe mostrar | Variantes |
|---|---|---|---|---|
| 1.1 | Tienda | `/tienda` | Buscador; filtros por **categoría** (Casacas, Accesorios, Café), **colección** (Älpafill, Comunidad, Urbana) e **impacto** (abrigo / árbol), y "solo disponibles"; orden por precio; grilla de tarjetas de producto (las de la Home); contador "12 productos" | Default · Filtro aplicado · Sin resultados · Móvil con filtros en panel desplegable |
| 1.2 | Detalle de producto | `/tienda/{codigo}` | Galería, colección, nombre, precio; **selector de tallas en chips** (talla agotada tachada y deshabilitada); cantidad; "Agregar al carrito"; **bloque de impacto exacto**: "Esta compra abriga a 1 niño en Cusco" (se multiplica con la cantidad); aviso de pocas unidades; acordeones Descripción / Cuidados; 4 productos relacionados | Default · Producto agotado · Producto de café (badge árbol, sin tallas) |
| 1.3 | Guía de tallas | Modal sobre 1.2 | Tabla S · M · L · XL con pecho, largo y manga en cm, y cómo medirse | — |
| 1.4 | Carrito | `/carrito` | Líneas con imagen, nombre, talla, precio, cantidad (− / +), subtotal y quitar; resumen con total; **"Tu impacto con esta compra: 2 abrigos + 1 árbol"**; botón "Confirmar compra". En móvil, cada línea es una tarjeta y el resumen queda fijo encima del óvalo | Con productos · **Vacío** · Una línea supera el stock (mensaje en esa línea) |
| 1.5 | Confirmar compra | `/carrito/confirmar` | Resumen del pedido, datos de envío del cliente (ya cargados de su cuenta, editables), impacto total y botón final. Si no inició sesión: aviso "Inicia sesión para confirmar; tu carrito se conserva" con enlace al login del bloque 2 | Con sesión · Sin sesión |
| 1.6 | Compra registrada | `/compra-exito` | Código **WEB-000110** grande con botón copiar; mensaje "Tu pedido quedó registrado y está pendiente de confirmación de pago"; resumen del impacto generado; botones "Ver mi pedido" y "Sigue tu impacto" | — |

**Ojo:** el sistema **no cobra en línea**; el pedido queda *Pendiente* hasta que se confirma el pago. En 1.5 y 1.6 dejen un bloque "Instrucciones de pago" con texto de ejemplo; el equipo definirá el medio (Yape, transferencia).

---

## 4. Bloque 2 · Cuenta del cliente

**Objetivo:** registrarse, entrar, recuperar el acceso y revisar sus pedidos y su impacto.
**Casos de uso:** CU-07, CU-11, CU-12, CU-13, CU-14.

| # | Pantalla | Ruta | Qué debe mostrar | Variantes |
|---|---|---|---|---|
| 2.1 | Iniciar sesión | `/login` | Email, contraseña, "¿Olvidaste tu contraseña?", enlace a registro | Default · Credenciales incorrectas · Llegó desde "Confirmar compra" (aviso arriba) |
| 2.2 | Registro | `/registro` | Nombre, apellido, tipo de documento (DNI / CE / Pasaporte) y número, email, teléfono, dirección, contraseña y confirmación, aceptar términos | Default · **Errores por campo** · Registro exitoso |
| 2.3 | Recuperar contraseña | `/recuperar` | Campo email y botón; luego "Si el correo está registrado, te enviamos un enlace" (el mensaje es el mismo exista o no el correo) | Formulario · Enviado |
| 2.4 | Nueva contraseña | `/restablecer` | Nueva contraseña y confirmación | Default · Enlace vencido |
| 2.5 | Mi cuenta · Pedidos | `/cuenta` | Saludo; **"Tu impacto": abrigos y árboles generados por tus compras**; lista de pedidos con código, fecha, total y estado (chip Pendiente / Pagado / Anulado) | Con pedidos · Sin pedidos |
| 2.6 | Detalle de pedido | `/cuenta/pedidos/{codigo}` | Productos, total y estado del pedido; por cada donación: tipo, cantidad y **línea de tiempo** Pendiente de asignar → Lote asignado → Entregada, con la comunidad cuando ya tiene lote | Pedido reciente (todo pendiente) · Pedido entregado |
| 2.7 | Mi cuenta · Mis datos | `/cuenta` (pestaña) | Formulario con sus datos (el email se ve pero no se edita) y bloque "Cambiar contraseña" | Default · Guardado |

Diseñen también la **barra de navegación con sesión iniciada** (nombre o inicial del cliente en vez de "Ingresar", y menú con Mi cuenta / Cerrar sesión).
La **línea de tiempo de la donación** la diseña el bloque 3; pónganse de acuerdo para usar el mismo componente.

---

## 5. Bloque 3 · Impacto, Höség Café e institucional

**Objetivo:** que cualquiera verifique su impacto y conozca la marca y el Café.
**Casos de uso:** CU-05, CU-06, CU-08, CU-10, CU-10.1 (+ Libro de Reclamaciones, obligatorio por ley).

| # | Pantalla | Ruta | Qué debe mostrar | Variantes |
|---|---|---|---|---|
| 3.1 | Consulta de impacto | `/consulta-impacto` | Buscador por código de boleta y explicación de dónde encontrarlo (boleta impresa o QR). Resultado: fecha de compra y, por cada abrigo o árbol, estado con **línea de tiempo** (componente que este bloque define), comunidad, ONG y fecha de entrega. **Sin datos personales del comprador**: la página es pública | Inicial · Resultado con donaciones en distintos estados · Código no encontrado · Llegada por QR (muestra una sola donación) |
| 3.2 | Höség Café | `/cafe` | Portada "Por cada café, sembramos un árbol en Cusco"; **contador de árboles del café**; la carta **en la página, no en PDF** (bebidas por categoría con precio); cafés de especialidad con productora, altitud, notas de sabor y precio, con botón **"Comprar en la tienda"** (badge 1 árbol); el local: Calle Palacio 110 int. 2, Cusco, lunes a domingo de 8:30 a 19:30, mapa y WhatsApp; bloque sobre Omacha y Pachamama Raymi; enlace a la tienda de ropa | — |
| 3.3 | Nosotros | `/nosotros` | Historia de la marca; modelos *Buy One, Give One* y *Buy One, Plant One*; **cifras de impacto**; comunidades beneficiadas (lista o mapa de Cusco); ONG aliada; sello B Corp; código de conducta (las 5 R) | — |
| 3.4 | Página legal | `/terminos`, `/politica-rsu` | Plantilla de texto largo con índice lateral (en móvil, índice desplegable arriba). Una sola plantilla sirve para ambas | — |
| 3.5 | Contacto | `/contacto` | Formulario (nombre, email, asunto, mensaje); WhatsApp, correo y horario de atención; personal shopper | Default · Errores · Mensaje enviado |
| 3.6 | Libro de Reclamaciones | `/libro-reclamaciones` | Formulario oficial: datos del consumidor, producto o servicio, tipo (reclamo / queja), detalle, pedido del consumidor, código de pedido opcional | Default · Registrado (con número de hoja) |
| 3.7 | Error 404 | cualquier ruta inexistente | Mensaje amable con la marca y botones a Inicio y Tienda | — |

---

## 6. Bloque 4 · Panel de administración

**Objetivo:** que el personal de Höség mantenga el portal sin tocar código.
**Casos de uso:** CU-16 a CU-21. **Solo Desktop 1440**, salvo 4.2, que va también en móvil.

Estructura común: menú lateral (Panel, Catálogo web, Höség Café, Contenido, Mensajes), barra superior con el nombre del usuario y "Cerrar sesión". Misma paleta que la tienda, pero más sobria: tablas, formularios y botones.

| # | Pantalla | Ruta | Qué debe mostrar | Variantes |
|---|---|---|---|---|
| 4.1 | Login administrativo | `/admin/login` | Usuario y contraseña; aviso de cuenta bloqueada | Default · Error · Bloqueada |
| 4.2 | Panel | `/admin/panel` | Indicadores (pedidos web de hoy, pendientes de pago, monto vendido del periodo, abrigos y árboles generados); filtro por fechas; tabla de pedidos (código, cliente, fecha, total, estado). **Solo consulta**: sin botones de confirmar ni anular | Desktop · Móvil |
| 4.3 | Catálogo web | `/admin/catalogo` | Tabla de productos (código, nombre, talla, stock en solo lectura) con interruptores **"Visible en la web"** y **"Destacado en la Home"** (máximo 8); vista previa del carrusel | Default · Límite de destacados alcanzado |
| 4.4 | Höség Café | `/admin/cafe` | Pestañas: **Carta** (lista con agregar, editar y desactivar; formulario con nombre, categoría, precio, disponible), **Local** (dirección, horario, enlace al mapa) y **Árboles del café** (cifra publicada) | Lista · Formulario de edición |
| 4.5 | Contenido | `/admin/contenido` | Lista de bloques editables (banner de la Home, textos de Nosotros, políticas) y editor con título, texto, imagen y vista previa | Lista · Editando |
| 4.6 | Mensajes y suscriptores | `/admin/mensajes` | Bandeja de mensajes de contacto con estado Nuevo / Atendido y detalle; pestaña Suscriptores con "Exportar CSV" | Bandeja · Detalle · Suscriptores |

**Si les sobra tiempo:** los 3 correos que envía el portal, a 600 px de ancho: *Bienvenida*, *Pedido registrado* (con código y enlace a la consulta de impacto) y *Recuperar contraseña*.

---

## 7. Ajustes a la Home (coordinador)

Para que las páginas nuevas encajen, la Home necesita:
- **Barra de navegación:** agregar **Café** a los enlaces de escritorio.
- **Óvalo móvil:** queda **Inicio · Tienda · Café · Impacto · Cuenta** (ícono de taza). El **Carrito sale del óvalo** porque ya está en la barra superior móvil con su contador; así siguen siendo 5 destinos y entra en pantallas de 360 px. Todas las pantallas móviles deben usar este óvalo.
- **Footer:** formulario del **boletín Changemaker** (email + botón), enlaces a **Höség Café**, **Contacto** y **Libro de Reclamaciones** (con su ícono, visible), y el sello **B Corp**.
- **Barra de navegación con sesión iniciada**: coordinarla con el bloque 2 para que sea idéntica en todas las páginas.

---

## 8. Checklist antes de entregar

- [ ] Cada pantalla está en Desktop y en Móvil (el bloque 4 solo en Desktop, salvo 4.2).
- [ ] Barra de navegación, óvalo móvil y footer son los de la Home, sin cambios.
- [ ] Toda tarjeta, detalle y carrito muestra el impacto (**1 abrigo** / **1 árbol**).
- [ ] Se usaron los datos reales de la sección 1: nombres, precios, comunidades, `WEB-000110`.
- [ ] Están los estados pedidos: vacío, error y éxito.
- [ ] En móvil nada queda tapado por el óvalo inferior.
- [ ] PNG exportados a 2x con el nombre acordado.
