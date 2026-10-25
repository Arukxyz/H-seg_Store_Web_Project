# HÖSÉG Web — Portal e-commerce

Tienda en línea de Höség Store (14-DIEZ S.A.C.) bajo el modelo **"Buy One, Give One"**: por cada prenda comprada se registra una donación de abrigo (o árbol) para comunidades altoandinas de Cusco.

Java 21 + Spring Boot 3.4 (Web, Thymeleaf, Data JPA, Validation, Security, Mail) + Bootstrap 5, sobre PostgreSQL en Supabase. **Comparte la base de datos con el escritorio [SEGITD-HÖSÉG](https://github.com/Arukxyz/Hoseg_Store)**, que es quien define el esquema.

 **Documentación técnica:** [docs/README.md](docs/README.md) (índice) · [docs/arquitectura.md](docs/arquitectura.md) (cómo se integran web y escritorio) · [docs/contrato-backend.md](docs/contrato-backend.md) (qué datos recibe cada pantalla).

## División de responsabilidades

| Sistema | Hace | No hace |
|---|---|---|
| **Web (este repo)** | Catálogo, registro/login de clientes, carrito, confirmar compra (`venta` + `detalle_venta` + `donacion` EN_ESPERA + `envio`), reseñas, consulta pública de impacto, Höség Café, contacto, boletín, Libro de Reclamaciones y panel `/admin` | Descontar stock, confirmar/anular pedidos, armar lotes, cobrar en línea |
| **Escritorio** | Confirmar pedidos (mueve `stock_comercial` → `stock_comprometido`), lotes, despacho, reportes, boleta PDF con QR | Crear ventas |

## Requisitos

- JDK 21, Maven 3.9+
- La base de Supabase con los scripts `01`..`09` del repositorio del escritorio ejecutados (`06`, `08` y `09` son los que agregan lo que necesita la web).

## Configuración

Credenciales del **Session pooler** (puerto 5432) de Supabase, por una de estas vías:

1. Variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` (las mismas del escritorio), o
2. Copiar `src/main/resources/application-local.properties.example` a `application-local.properties` (ignorado por git) y completar la contraseña. Se carga siempre (`spring.config.import`), también con el botón Run del IDE; `mvn spring-boot:run` además activa el perfil `dev` (SQL visible, plantillas sin caché).

`spring.jpa.hibernate.ddl-auto` está fijado en `validate`: Hibernate solo comprueba que las entidades coincidan con el esquema; cualquier cambio de tablas se hace con un script SQL versionado en el repositorio del escritorio.

Opcionales (todas tienen un valor por defecto en [application.properties](src/main/resources/application.properties)):

| Variable o propiedad | Para qué | Por defecto |
|---|---|---|
| `PORTAL_URL` | Dirección pública del portal, para los enlaces de los correos | `http://localhost:8080` |
| `MAIL_FROM` | Remitente de los correos | `no-responder@hoseg.pe` |
| `spring.mail.*` | SMTP para enviar correos de verdad (en `application-local.properties`) | Sin SMTP: los correos se escriben en el log |
| `hoseg.envio.costo-delivery` | Costo del delivery en soles; el recojo en tienda es gratis | `10.00` (valor de ejemplo) |
| `LOG_DIR` | Carpeta de los archivos de log | `logs/` |
| `DB_POOL_SIZE` | Conexiones del pool HikariCP | `5` |

## Ejecutar

```bash
mvn spring-boot:run                             # desarrollo: perfiles dev,local (SQL visible, plantillas sin caché)
mvn test                                        # pruebas (no necesitan base de datos)
mvn package && java -jar target/hoseg-web.jar   # producción (credenciales por variables de entorno)
```

Aplicación en <http://localhost:8080>; panel de administración en <http://localhost:8080/admin> (usuarios ADMINISTRADOR del escritorio). En desarrollo, <http://localhost:8080/ejemplo> muestra la plantilla de partida para páginas nuevas ([docs/guia-plantillas.md](docs/guia-plantillas.md)).

## Estructura

```
src/main/java/pe/edu/utp/hoseg/web/
├── HosegWebApplication.java
├── config/       SecurityConfig (dos logins: clientes con BCrypt y panel /admin con SHA-256 + salt),
│                 Sha256ConSaltPasswordEncoder, ClienteAutenticado, AdministradorAutenticado, WebConfig
├── model/        entidades mapeadas 1:1 al esquema compartido + enums de estado
├── repository/   Spring Data JPA, un repositorio por entidad; sumas, conteos y secuencias en Postgres
├── dto/          formularios validados (*Form) y vistas de solo lectura para las plantillas
├── service/      lógica de negocio y transacciones, un servicio por área (ver abajo)
├── controller/   un controlador por grupo de pantallas, sin reglas de negocio
└── util/         SaneamientoTexto (limpieza del texto libre de los formularios públicos)

src/main/resources/
├── application.properties                 # configuración base (ddl-auto=validate)
├── application-dev.properties             # perfil de desarrollo
├── application-local.properties.example   # plantilla de credenciales (copiar sin .example)
├── logback-spring.xml                     # logs: consola, hoseg-web.log y auditoria.log
├── static/css/                            # tokens.css (paleta y tipografía), hoseg.css (componentes hs-*), home.css, tienda.css
├── static/js/                             # navbar.js, home.js, tienda.js
├── static/images/                         # fotos de la Home (créditos en docs/creditos-imagenes.md)
└── templates/
    ├── layout/          base.html (portal) y admin.html (panel)
    ├── fragments/       navbar, footer, product-card, impacto, avisos, estrellas, pago
    ├── index.html       página principal
    ├── tienda/          catálogo y detalle de producto
    ├── carrito/         carrito, confirmar compra y compra registrada
    ├── cuenta/          login, registro, recuperar/restablecer contraseña, mi cuenta y detalle de pedido
    ├── impacto/         consulta pública de impacto (boleta o QR)
    ├── institucional/   Café, Nosotros, legales, contacto y Libro de Reclamaciones
    ├── admin/           login, panel, catálogo web, Café, contenido, mensajes y reseñas
    ├── error.html, error/404.html
    └── ejemplo-pagina.html   # plantilla de partida (solo perfil dev)
```

| Área | Controlador | Servicios |
|---|---|---|
| Home | `HomeController` | `CatalogoService`, `ImpactoService`, `ContenidoService` |
| Tienda y reseñas | `TiendaController` | `CatalogoService`, `ResenaService` |
| Carrito y compra | `CarritoController` | `CarritoService` (en sesión), `CatalogoService`, `VentaService`, `CafeService`, `NotificacionService` |
| Acceso de clientes | `AuthController` | `ClienteService`, `RecuperacionService` |
| Mi cuenta | `CuentaController` | `ClienteService`, `VentaService`, `ImpactoService` |
| Consulta de impacto | `ImpactoController` | `ImpactoService` |
| Café, Nosotros y legales | `InstitucionalController` | `CafeService`, `ContenidoService`, `ImpactoService` |
| Contacto, boletín y reclamaciones | `ContactoController` | `ContactoService`, `BoletinService`, `ReclamacionService` |
| Panel `/admin` | `AdminController`, `AdminContenidoController`, `AdminMensajesController` | `PanelAdminService`, `CatalogoService`, `ResenaService`, `CafeService`, `ContenidoService`, `ContactoService`, `ReclamacionService`, `BoletinService` |
| Correos | — | `NotificacionService` (redacta) y `CorreoService` (envía) |

El POST de los dos formularios de login lo procesa Spring Security (`SecurityConfig`) con `ClienteDetailsService` y `UsuarioDetailsService`; `BloqueoAccesoService` lleva los intentos fallidos. `AtributosGlobales` pone en todas las vistas lo que necesitan el navbar y el footer. `EjemploController` solo existe con el perfil `dev`.

Regla de capas: los controladores no contienen reglas de negocio ni acceden a repositorios; la lógica transaccional vive en `service`.

## Librerías de apoyo

Además de los starters de Spring Boot:

| Librería | Versión | Dónde se usa | Para qué |
|---|---|---|---|
| Google Guava | 33.4.0-jre | `BloqueoAccesoService` | Cuenta en memoria los inicios de sesión fallidos por IP (15 min, como mucho 10 000 IP) y deja una alerta en `auditoria.log` al llegar a 10. No bloquea: el bloqueo real es por cuenta, en la base |
| Apache POI (`poi-ooxml`) | 5.3.0 | `BoletinService` | Exporta los suscriptores activos a Excel (`GET /admin/suscriptores.xlsx`) |
| Apache Commons Lang3 | 3.17.0 | `SaneamientoTexto`, usado por `ContactoService`, `ReclamacionService` y `ResenaService` | Deja en un solo renglón los campos de una línea (nombre, asunto, título); evita la inyección de cabeceras en el asunto de los correos |
| Logback | la de Spring Boot | `logback-spring.xml` | Logs en consola y en archivos rotativos (ver abajo) |
| Bootstrap / Bootstrap Icons | 5.3.3 / 1.11.3 | Plantillas | Servidos como WebJars, sin depender de un CDN |

## Logs

En la carpeta `logs/` (ignorada por git, porque guarda direcciones IP):

- `hoseg-web.log`: registro general; rota por día y a los 10 MB, se conservan 30 días (tope de 300 MB).
- `auditoria.log`: solo eventos de seguridad (bloqueos de cuenta, alertas por IP); se conserva 90 días.

Nunca se registran contraseñas, hashes ni tokens.

## Pruebas

`mvn test` corre 78 pruebas con JUnit 5 y Mockito, sin conexión a la base:

- `VistasTest` renderiza todas las plantillas con `@WebMvcTest` y servicios simulados, para detectar una plantilla rota antes de integrarla.
- Las pruebas de `service/` cubren las reglas de negocio: checkout (`VentaServiceTest`), reseñas, registro, recuperación de contraseña, impacto, catálogo, boletín (incluido el Excel) y reclamaciones.
- `Sha256ConSaltPasswordEncoderTest` comprueba que el login del panel calcula el hash igual que el escritorio; `SaneamientoTextoTest`, la limpieza de texto.

## Versiones

Los releases se publican en [GitHub Releases](https://github.com/Arukxyz/H-seg_Store_Web_Project/releases) con un tag `vX.Y.Z` sobre `main`.

| Versión | Fecha | Contenido |
|---|---|---|
| [v1.0.0](https://github.com/Arukxyz/H-seg_Store_Web_Project/releases/tag/v1.0.0) | 23/10/2026 | Backend de los 5 bloques (catálogo, carrito y checkout con envío, registro y login con bloqueo, recuperación de contraseña, mi cuenta, consulta de impacto por boleta y QR, Café, contenido editable, contacto, boletín, Libro de Reclamaciones y panel `/admin`), página principal con datos reales y plantillas esqueleto de las demás pantallas |

La siguiente versión se publicará al cerrar el diseño del panel de administración y de las pantallas de acceso de clientes.

## Estado

- **Backend:** completo para los 5 bloques ([docs/contrato-backend.md](docs/contrato-backend.md)).
- **Pantallas diseñadas:** página principal, tienda y detalle de producto, carrito, confirmar compra y compra registrada, consulta de impacto, Höség Café, Nosotros y páginas legales.
- **En diseño** (hoy son plantillas esqueleto funcionales): login, registro, recuperación de contraseña y mi cuenta de clientes; contacto y Libro de Reclamaciones; todo el panel `/admin`.
