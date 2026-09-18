# HÖSÉG Web — Portal e-commerce

Tienda en línea de Höség Store (14-DIEZ S.A.C.) bajo el modelo **"Buy One, Give One"**: por cada prenda comprada se registra una donación de abrigo (o árbol) para comunidades altoandinas de Cusco.

Java 21 + Spring Boot 3 (Web, Thymeleaf, Data JPA, Validation, Security) + Bootstrap 5, sobre PostgreSQL en Supabase. **Comparte la base de datos con el escritorio [SEGITD-HÖSÉG](../Hoseg_Store)**, que es quien define el esquema.

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
2. Copiar `src/main/resources/application-local.properties.example` a `application-local.properties` (ignorado por git) y activar el perfil `local`.

`spring.jpa.hibernate.ddl-auto` está fijado en `validate`: Hibernate solo comprueba que las entidades coincidan con el esquema; cualquier cambio de tablas se hace con un script SQL versionado en el repositorio del escritorio.

## Ejecutar

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev,local   # desarrollo: SQL visible, plantillas sin caché
mvn package && java -jar target/hoseg-web.jar               # producción (credenciales por variables de entorno)
```

Aplicación en <http://localhost:8080>.

## Estructura

```
src/main/java/pe/edu/utp/hoseg/web/
├── HosegWebApplication.java
├── config/       SecurityConfig (BCrypt, rutas públicas), WebConfig
├── model/        entidades mapeadas 1:1 al esquema compartido + enums de estado
├── repository/   Spring Data JPA
├── dto/          formularios validados (@NotBlank, @Email, @Size) e ítems de carrito
├── service/      CatalogoService, ClienteService, CarritoService (sesión), VentaService (checkout), ConsultaImpactoService
└── controller/   Home, Producto, Carrito, ConsultaImpacto, Auth

src/main/resources/
├── application.properties            # configuración base (ddl-auto=validate)
├── application-dev.properties        # perfil de desarrollo
├── static/{css,js,images}/
└── templates/fragments/{header,navbar,footer}.html + vistas
```

Regla de capas: los controladores no contienen reglas de negocio ni acceden a repositorios; la lógica transaccional vive en `service`.
