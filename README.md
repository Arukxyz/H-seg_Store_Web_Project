# HÖSÉG Web — Portal e-commerce

Tienda en línea de Höség Store (14-DIEZ S.A.C.) bajo el modelo **"Buy One, Give One"**: por cada prenda comprada se registra una donación de abrigo (o árbol) para comunidades altoandinas de Cusco.

Java 21 + Spring Boot 3 (Web, Thymeleaf, Data JPA, Validation, Security) + Bootstrap 5, sobre PostgreSQL en Supabase. **Comparte la base de datos con el escritorio [SEGITD-HÖSÉG](https://github.com/Arukxyz/Hoseg_Store)**, que es quien define el esquema.

📚 **Documentación técnica:** [docs/README.md](docs/README.md) (índice) · [docs/arquitectura.md](docs/arquitectura.md) (cómo se integran web y escritorio).

## División de responsabilidades

| Sistema | Hace | No hace |
|---|---|---|
| **Web (este repo)** | Catálogo, registro/login de clientes, carrito, confirmar compra (`venta` + `detalle_venta` + `donacion` PENDIENTE), consulta pública de impacto | Descontar stock, confirmar/anular pedidos, armar lotes |
| **Escritorio** | Confirmar pedidos (mueve `stock_comercial` → `stock_comprometido`), lotes, despacho, reportes, boleta PDF con QR | Crear ventas |

## Requisitos

- JDK 21, Maven 3.9+
- La base de Supabase con los scripts `01`..`06` del repositorio del escritorio ejecutados (`06_web_clientes.sql` es el que agrega lo que necesita la web).

## Configuración

Credenciales del **Session pooler** (puerto 5432) de Supabase, por una de estas vías:

1. Variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` (las mismas del escritorio), o
2. Copiar `src/main/resources/application-local.properties.example` a `application-local.properties` (ignorado por git). `mvn spring-boot:run` lo carga solo: los perfiles `dev,local` vienen activados por defecto en el `pom.xml`.

`spring.jpa.hibernate.ddl-auto` está fijado en `validate`: Hibernate solo comprueba que las entidades coincidan con el esquema; cualquier cambio de tablas se hace con un script SQL versionado en el repositorio del escritorio.

## Ejecutar

```bash
mvn spring-boot:run                             # desarrollo: perfiles dev,local (SQL visible, plantillas sin caché)
mvn test                                        # pruebas (no necesitan base de datos)
mvn package && java -jar target/hoseg-web.jar   # producción (credenciales por variables de entorno)
```

Aplicación en <http://localhost:8080>. En desarrollo, <http://localhost:8080/ejemplo> muestra la plantilla de partida para páginas nuevas ([docs/guia-plantillas.md](docs/guia-plantillas.md)).

## Estructura

```
src/main/java/pe/edu/utp/hoseg/web/
├── HosegWebApplication.java
├── config/       SecurityConfig (rutas públicas y protegidas, BCrypt), WebConfig (locale es-PE)
├── model/        entidades mapeadas 1:1 al esquema compartido + enums de estado
├── repository/   Spring Data JPA, un repositorio por entidad consultada
├── dto/          TarjetaProducto, ResumenImpacto, ItemCarrito (y los formularios validados)
├── service/      CatalogoService, ImpactoService, CarritoService (en sesión)
└── controller/   HomeController, AtributosGlobales (datos comunes a todas las vistas)

src/main/resources/
├── application.properties            # configuración base (ddl-auto=validate)
├── application-dev.properties        # perfil de desarrollo
├── static/css/                       # tokens.css (paleta y tipografía), hoseg.css (componentes), home.css
├── static/js/                        # navbar.js, home.js
└── templates/
    ├── index.html                    # página principal
    ├── ejemplo-pagina.html           # plantilla de partida (solo perfil dev)
    └── fragments/                    # head, navbar, footer, product-card, icons
```

Regla de capas: los controladores no contienen reglas de negocio ni acceden a repositorios; la lógica transaccional vive en `service`.

Estado: la **página principal** está implementada con datos reales. Tienda, carrito, registro e inicio de sesión, checkout y consulta de impacto están en desarrollo; sus rutas ya están definidas (ver [docs/guia-plantillas.md](docs/guia-plantillas.md#4-rutas-que-la-home-ya-enlaza)).
