# Fotos de los locales y de la evidencia de impacto

Estas fotos **no se guardan en la base de datos**. Son archivos dentro de
`src/main/resources/static/images/`. Para agregarlas basta con copiar el archivo
con el nombre correcto y reiniciar la aplicación (con el perfil `dev`, devtools
recarga los estáticos solo).

## Locales (`static/images/locales/`)

Las usan el abanico de locales de **Nosotros** y la tarjeta del local en **Café**.

- Formato: `.jpg`, horizontal o cuadrada, de unos 1200 px de ancho como máximo.
- Nombre: la *clave* del local, es decir, su nombre en minúsculas, sin tildes y
  con guiones (`LocalCafe.clave()`), más `.jpg`.

| Local (tabla `cafe_local`) | Archivo |
|---|---|
| Höség Café Cusco | `hoseg-cafe-cusco.jpg` |
| Hoseg House | `hoseg-house.jpg` |
| Hoseg Larcomar | `hoseg-larcomar.jpg` |
| Hoseg Marriott Lima | `hoseg-marriott-lima.jpg` |

Mientras falte una foto se ve un fondo de la paleta con un icono (taza para la
cafetería, tienda para las demás). Si se cambia el nombre de un local desde el
panel, cambia su clave y hay que renombrar la foto.

Café muestra solo los locales cuyo nombre lleva «Café» (`CafeService.localesCafe`).
Nosotros muestra todos.

## Evidencia de impacto (`static/images/evidencia/`)

Galería de **Nosotros**. **Solo fotos reales de entregas o plantaciones**. Fotos
de banco presentadas como «evidencia» serían engañosas. Mientras la carpeta no
tenga fotos, la sección no aparece.

- Formato: `.jpg`, `.jpeg`, `.png` o `.webp`.
- Orden: por nombre de archivo, así que conviene empezar con un número.
- Pie de foto: sale del nombre del archivo. Se quitan el número inicial y la
  extensión, y los guiones pasan a espacios:
  `01-entrega-de-abrigos-en-omacha.jpg` → «Entrega de abrigos en omacha».
  Las tildes y mayúsculas del nombre se conservan, así que
  `02-plantación-de-cedros-en-Layo.jpg` → «Plantación de cedros en Layo».
- Antes de publicar, confirmar que se tiene permiso de las personas que aparecen
  en la foto (sobre todo si hay niños) y de la ONG aliada.
