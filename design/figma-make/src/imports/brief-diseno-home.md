# Brief de diseño — Página principal HÖSÉG Web

**Para:** diseño UI en Figma
**Entrega:** frames Desktop (1440) y Móvil (390), tokens y componentes nombrados según la sección 8
**Alcance:** solo la página principal (Home). Las demás páginas se diseñan después, reutilizando los componentes que salgan de aquí.

---

## 1. Quiénes somos y qué debe transmitir la página

HÖSÉG es una marca peruana de ropa de abrigo y accesorios, **empresa de triple impacto** (personas, planeta, negocio). Cada compra tiene una consecuencia real y verificable:

| Compras | Se genera | Modelo |
|---|---|---|
| Casaca, chaleco, poncho, chompa, cortavientos | **1 abrigo** para un niño de una comunidad altoandina de Cusco | *Buy One, Give One* |
| Gorro, chalina, guantes, medias, accesorios | **1 árbol** (cedro andino) plantado en Cusco | *Buy One, Plant One* |

Las comunidades beneficiadas son reales y aparecen en la web: **Omacha (Paruro), Ccatca y Marcapata (Quispicanchi), Paucartambo, Layo (Canas)**. La entrega la coordina una ONG aliada y el cliente puede rastrear su donación con el número de su boleta.

**Sensación buscada:** cálida, cercana, honesta. El cliente debe sentir que entra a una tienda que hace bien las cosas, no a una campaña de caridad ni a un catálogo frío. Referencias: outdoor andino, lana, tierra, madera, luz de altura. Evitar: verdes "eco" saturados, iconografía de reciclaje genérica, estética de ONG con niños en primer plano pidiendo ayuda. El impacto se muestra con orgullo y con datos, no con lástima.

Tono de textos: segunda persona, directo, en español peruano neutro. Ejemplo de titular: *"Abriga a alguien más con cada compra."*

---

## 2. Requisitos obligatorios

1. **Ambiente de triple impacto** presente en toda la página, no solo en una sección: badges en productos, cifras de impacto, comunidades con nombre.
2. **Responsiva real**: el contenido se ve completo y legible en cualquier móvil (desde 360 px de ancho) sin scroll horizontal. Diseñar primero móvil, luego desktop.
3. **La Home solo navega**: presenta la marca y los destacados; **no** tiene carrito desplegado, filtros, formularios de login ni checkout. Cada sección enlaza a la página que corresponde.
4. **Banner de identidad** con imagen representativa, nombre HÖSÉG y un texto breve de "quiénes somos".
5. **Productos destacados en cards con rotación animada** (carrusel/slider automático).
6. **Navegación en móvil abajo, en forma de óvalo (pill) con iconos**, sustituyendo a la barra superior.

---

## 3. Estructura de la página (orden de arriba hacia abajo)

### 3.1 Navbar — `Navbar` / `MobileNavPill`
- **Desktop:** barra superior fija. Izquierda: logo HÖSÉG. Centro/derecha: enlaces **Inicio · Tienda · Impacto · Nosotros**. Extremo derecho: icono de cuenta (Ingresar / nombre del cliente si está logueado) e icono de carrito con **contador numérico** (badge) de ítems.
- **Móvil:** la barra superior se reduce a solo el logo (y opcionalmente el badge del carrito). La navegación pasa a un **óvalo flotante fijo en la parte inferior**, centrado, con margen respecto al borde y sombra suave, con **5 iconos**: Inicio, Tienda, Impacto, Carrito (con badge), Cuenta. Icono activo resaltado (relleno o punto). Sin etiquetas de texto o con etiqueta muy pequeña bajo el icono; ver qué funciona mejor a 390 px.
- Diseñar el estado **activo, hover (desktop) y con contador en 0 / con contador > 0**.

### 3.2 Banner de identidad — `HeroBanner`
- Imagen a ancho completo (paisaje altoandino, prendas en uso, comunidad; nada de stock genérico de oficina).
- Sobre la imagen: nombre **HÖSÉG** (respetar diéresis y acento), un titular corto sobre el propósito y 2–3 líneas de "quiénes somos" (empresa peruana, ropa de abrigo, triple impacto, B Corp).
- Dos botones: primario **"Ver tienda"**, secundario **"Consulta tu impacto"**.
- En móvil la imagen recorta al centro y el texto baja o se apila; el titular no debe partirse en más de 3 líneas.

### 3.3 Productos destacados — `FeaturedCarousel` + `ProductCard`
- Título de sección + subtítulo ("Cada prenda abriga dos veces" o similar).
- Carrusel con **auto-avance cada ~5 s**, pausa al pasar el cursor, controles anterior/siguiente e indicadores. Desktop: 4 cards visibles; tablet: 2–3; móvil: 1 card y media (que se vea que hay más) con deslizamiento táctil.
- Cada `ProductCard` muestra **solo datos que existen en el sistema**:

  | Elemento | Origen | Notas |
  |---|---|---|
  | Imagen | `url_imagen` | Proporción fija 4:5, fondo neutro |
  | Nombre | `nombre` | Ej. "Casaca Älpafill Cusco", "Gorro de lana Layo" |
  | Colección | `coleccion` | Ej. Älpafill, Comunidad, Urbana — texto pequeño sobre el nombre |
  | Precio | `precio` | Formato **S/ 259.90** |
  | Badge de impacto | `tipo_compromiso` | **"1 abrigo"** (ABRIGO) o **"1 árbol"** (ARBOL); diseñar ambos con icono propio |
  | Tallas disponibles | `talla` | Chips pequeños (S · M · L · XL · Única). Un mismo nombre puede tener varias tallas |
  | Estado | `stock_comercial` | Variante **"Agotado"** (card atenuada, sin botón de compra) |
  | Acción | — | Botón "Ver detalle" (lleva a la página de producto; no se agrega al carrito desde la Home) |

- Diseñar estados: normal, hover (desktop), agotado.

### 3.4 Cómo funciona el impacto — `ImpactSteps`
Tres pasos horizontales (vertical en móvil), con icono e ilustración ligera:
1. **Compras** una prenda o accesorio.
2. **Registramos** tu abrigo o árbol con el número de tu boleta.
3. **Entregamos** en una comunidad de Cusco y puedes verlo en línea.

### 3.5 Impacto en cifras — `ImpactCounters`
- 3 o 4 indicadores grandes: **abrigos entregados**, **árboles plantados**, **comunidades alcanzadas**, opcional **pedidos con impacto**.
- Los números son **dinámicos** (salen de la base de datos); en el diseño usar valores placeholder tipo *1 248* y no cifras reales.
- Debajo, chips o lista con los nombres de las comunidades.

### 3.6 Consulta tu impacto — `ImpactCTA`
- Bloque destacado con un campo de texto para el **número de boleta** (formato `WEB-000110`) y botón "Consultar". Al enviar, lleva a la página de consulta; en la Home el campo solo redirige.
- Texto corto explicando que el QR de la boleta lleva al mismo lugar.

### 3.7 Nosotros / triple impacto — `AboutStrip`
- Franja con foto y 2–3 párrafos cortos: origen de la marca, alianza con la ONG, certificación B Corp. Enlace "Conoce más".

### 3.8 Footer — `Footer`
- Logo, frase de campaña ("Buy One, Give One · Buy One, Plant One"), enlaces: Tienda, Impacto, Nosotros, Política de responsabilidad social, Términos, Contacto (WhatsApp, correo).
- Redes sociales con iconos. Línea de copyright "© 2026 HÖSÉG Store · 14-DIEZ S.A.C.".
- En móvil, dejar **espacio inferior** suficiente para que el óvalo de navegación no tape el contenido del footer.

---

## 4. Sistema visual (propuesta; el diseñador define los valores finales)

- **Paleta:** tonos tierra y lana como base (crema/arena para fondos, marrón oscuro o gris cálido para texto), **un acento cálido** (terracota/ocre) para botones primarios y **un acento frío discreto** (azul de altura o verde salvia) para el badge de árbol. Contraste mínimo AA (4.5:1) en texto.
- **Tipografía:** una familia display con carácter para titulares y una sans legible para cuerpo. Deben tener soporte completo de **Ö, É, Ä** (HÖSÉG, Älpafill). Entregar como Google Fonts si es posible.
- **Formas:** esquinas redondeadas medias (12–16 px) en cards y botones; el óvalo de navegación totalmente redondeado.
- **Iconos:** un solo set (p. ej. Bootstrap Icons o Phosphor) para poder implementarlos sin exportar cada uno.
- **Imágenes:** fotografía real de producto y paisaje; ilustraciones solo en `ImpactSteps`.
- **Modo oscuro:** no requerido en esta etapa.

---

## 5. Comportamiento e interacción

| Elemento | Comportamiento |
|---|---|
| Navbar desktop | Fija al hacer scroll; fondo se vuelve opaco al salir del banner |
| Óvalo móvil | Fijo abajo, siempre visible; el icono de la página actual se marca |
| Contador de carrito | Aparece solo si hay ítems; animación breve al cambiar |
| Carrusel | Auto-avance ~5 s, pausa en hover, swipe en móvil, loop infinito |
| Contadores de impacto | Animación de conteo al entrar en pantalla (opcional) |
| Estado logueado | El icono de cuenta muestra el nombre del cliente o su inicial |

---

## 6. Breakpoints (Bootstrap 5)

| Nombre | Ancho | Cards visibles | Navegación |
|---|---|---|---|
| Móvil | < 768 px | 1.5 | Óvalo inferior |
| Tablet | 768–1199 px | 2–3 | Barra superior |
| Desktop | ≥ 1200 px | 4 | Barra superior |

Diseñar como mínimo Desktop 1440 y Móvil 390; Tablet puede ser derivado.

---

## 7. Lo que NO va en la Home

- Listado completo de productos con filtros (eso es *Tienda*).
- Selector de tallas funcional o "Agregar al carrito" (eso es *Detalle de producto*).
- Formularios de registro/login.
- Resumen del carrito.

---

## 8. Entregables

1. Frames **Home / Desktop** y **Home / Móvil** con todas las secciones.
2. **Componentes** en Figma con estos nombres exactos (se mapean 1:1 a fragmentos de código): `Navbar`, `MobileNavPill`, `HeroBanner`, `FeaturedCarousel`, `ProductCard` (variantes: default, hover, agotado, badge abrigo / badge árbol), `ImpactSteps`, `ImpactCounters`, `ImpactCTA`, `AboutStrip`, `Footer`, `Button` (primario, secundario, deshabilitado), `Badge`.
3. **Tokens**: colores, tipografías (familia, tamaño, peso, interlineado) y espaciados, exportables como variables.
4. **Assets**: logo en SVG (claro y oscuro), iconos en SVG, imágenes del banner y de muestra en JPG/WebP a 2x.
5. Notas de animación del carrusel y del óvalo si difieren de lo indicado aquí.

---

## 9. Datos de prueba para maquetar

Productos reales del catálogo (usar estos nombres y precios en el diseño):

| Nombre | Colección | Precio | Impacto | Tallas |
|---|---|---|---|---|
| Casaca Älpafill Cusco | Älpafill | S/ 259.90 | 1 abrigo | M · L · XL |
| Chaleco Älpafill | Älpafill | S/ 219.90 | 1 abrigo | S |
| Poncho Älpafill | Älpafill | (ver catálogo) | 1 abrigo | Única |
| Gorro de lana Layo | Comunidad | S/ 49.90 | 1 árbol | Única |
| Bufanda Paucartambo | Comunidad | S/ 39.90 | 1 árbol | Única |
| Mochila artesanal Höség | Urbana | (ver catálogo) | 1 árbol | Única |

Comunidades: Omacha, Ccatca, Marcapata, Paucartambo, Layo.
Formato de boleta: `WEB-000110`.
