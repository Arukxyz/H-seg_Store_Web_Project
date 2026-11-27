# Arquitectura del sistema HÖSÉG

Cómo se integran el **portal web** (este repositorio) y el **módulo de escritorio SEGITD-HÖSÉG** (repositorio [`Arukxyz/Hoseg_Store`](https://github.com/Arukxyz/Hoseg_Store)): qué hace cada uno, qué datos comparten y qué reglas evitan que se pisen.

> Los diagramas están en [Mermaid](https://mermaid.js.org/): GitHub los dibuja al abrir este archivo. En VS Code se ven con la extensión *Markdown Preview Mermaid Support*.

## 1. Visión general

Dos aplicaciones independientes, sin API entre ellas: **se comunican a través de la base de datos**. La web registra lo que hace el cliente; el escritorio lo procesa y lo lleva hasta la comunidad beneficiada.

```mermaid
flowchart LR
    C([Cliente]) -->|navegador o celular| WEB
    WEB["<b>Portal web</b><br/>Spring Boot · Thymeleaf<br/>catálogo · compra · consulta de impacto"]
    WEB <-->|JPA / Hibernate| DB[("<b>PostgreSQL</b><br/>Supabase")]
    DB <-->|JDBC / HikariCP| ESC
    ESC["<b>Módulo de escritorio</b><br/>Java Swing<br/>pedidos · inventario · lotes · reportes"]
    P([Personal de Höség<br/>administrador · encargado]) --> ESC
    ESC -.->|boleta PDF con QR| C
```

## 2. Componentes y tecnologías

| | Portal web | Módulo de escritorio |
|---|---|---|
| Usuarios | Clientes (público) | Personal interno con rol ADMINISTRADOR o ENCARGADO |
| Lenguaje | Java 21 | Java 25 |
| Framework | Spring Boot 3.4: Web, Data JPA, Validation, Security | Swing con tema FlatLaf |
| Vistas | Thymeleaf + Bootstrap 5 | JFrames |
| Acceso a datos | Spring Data JPA (Hibernate), `ddl-auto=validate` | JDBC con pool HikariCP |
| Archivos que genera | — | Excel (Apache POI), PDF y QR (PDFBox + ZXing), respaldos CSV |
| Contraseñas | Clientes: BCrypt | Personal: SHA-256 con salt |
| Base de datos | PostgreSQL en Supabase, **la misma para ambos**, por el *Session pooler* (puerto 5432) | ← |
| Dueño del esquema | No; solo valida | **Sí**: scripts `01`..`06` en `src/main/resources/sql/` |

## 3. Arquitectura en capas

Ambos sistemas siguen la misma disciplina: cada capa solo habla con la de abajo, las reglas de negocio viven en la capa de servicio y el SQL solo existe en la capa de datos.

```mermaid
flowchart TB
    subgraph web [Portal web]
        direction TB
        a1[controller<br/><small>orquesta, sin reglas de negocio</small>] --> a2[service<br/><small>reglas y transacciones</small>]
        a2 --> a3[repository<br/><small>Spring Data JPA</small>]
        a3 --> a4[model<br/><small>entidades 1:1 con las tablas</small>]
        a0[templates Thymeleaf] -.-> a1
        a5[dto<br/><small>formularios validados</small>] -.-> a1
    end
    subgraph esc [Escritorio]
        direction TB
        b0[vista<br/><small>JFrames</small>] --> b1[controlador]
        b1 --> b2[servicio<br/><small>reglas y transacciones</small>]
        b2 --> b3[dao<br/><small>único lugar con SQL</small>]
        b3 --> b4[db<br/><small>ConexionBD, pool</small>]
    end
    a4 --> DB[(PostgreSQL)]
    b4 --> DB
```

**Paquetes del portal web** (`pe.edu.utp.hoseg.web`):

| Paquete | Contenido actual |
|---|---|
| `config` | `SecurityConfig` (dos logins: clientes con BCrypt y panel `/admin` con la tabla `usuario`; bloqueo por intentos), `Sha256ConSaltPasswordEncoder`, `ClienteAutenticado`, `AdministradorAutenticado`, `WebConfig` (locale es-PE) |
| `model` | Una entidad por tabla que usa la web (incluidas `Envio`, `Resena`, `TokenRecuperacion`, `ContenidoWeb`, `CafeCarta`, `CafeLocal`, `MensajeContacto`, `SuscriptorBoletin`, `LibroReclamacion` y `Usuario` de solo lectura) y los enums de estado con su etiqueta |
| `repository` | Un repositorio por entidad. Sumas, conteos, secuencias y bloqueo se calculan en Postgres |
| `service` | Un servicio por área: `CatalogoService`, `CarritoService`, `VentaService`, `ResenaService`, `ClienteService`, `RecuperacionService`, `ImpactoService`, `CafeService`, `ContenidoService`, `ContactoService`, `BoletinService`, `ReclamacionService`, `PanelAdminService`, y `CorreoService`/`NotificacionService` para los correos |
| `dto` | Formularios validados (`*Form`) y vistas de solo lectura que reciben las plantillas (contrato en [contrato-backend.md](contrato-backend.md)) |
| `controller` | Uno por grupo de pantallas (tienda, carrito, cuenta, impacto, institucional, contacto y tres del panel), `AtributosGlobales` (datos comunes) y `EjemploController` (solo en desarrollo) |

## 4. Base de datos compartida

### 4.1 Modelo de datos

Tablas que usa el flujo de venta y donación. El esquema completo está en los scripts `01` a `09` del escritorio: además están `usuario`, `proveedor`, `pedido_proveedor`, `movimiento_inventario`, `categoria`, `coleccion`, y las del portal (`token_recuperacion`, `contenido_web`, `cafe_carta`, `cafe_local`, `mensaje_contacto`, `suscriptor_boletin`, `libro_reclamaciones`).

```mermaid
erDiagram
    CLIENTE ||--o{ VENTA : realiza
    VENTA ||--|{ DETALLE_VENTA : contiene
    VENTA ||--o| ENVIO : "se entrega con"
    PRODUCTO ||--o{ DETALLE_VENTA : "se vende en"
    DETALLE_VENTA ||--o| DONACION : genera
    DETALLE_VENTA ||--o| RESENA : "se reseña en"
    PRODUCTO ||--o{ DONACION : "de tipo"
    LOTE_DONACION ||--o{ DONACION : agrupa
    COMUNIDAD ||--o{ LOTE_DONACION : recibe
    ONG ||--o{ LOTE_DONACION : entrega

    PRODUCTO {
        varchar codigo PK "HSG-CAS-001, una fila por talla"
        varchar nombre
        varchar talla
        numeric precio
        int stock_comercial "para la venta"
        int stock_comprometido "reservado para donar"
        boolean aplica_triple_impacto
        varchar tipo_compromiso "ABRIGO o ARBOL"
        boolean visible_web
        boolean activo
        boolean destacado "carrusel de la Home"
    }
    CLIENTE {
        serial id PK
        varchar nombre
        varchar apellido
        varchar email UK
        varchar password_hash "BCrypt, NULL si no se registró"
        int intentos_fallidos
        timestamptz bloqueado_hasta
    }
    ENVIO {
        serial id PK
        int id_venta UK
        varchar tipo "RECOJO_TIENDA o DELIVERY"
        varchar estado "PENDIENTE, PREPARANDO, EN_CAMINO, ENTREGADO, CANCELADO"
        varchar direccion "foto tomada al comprar"
        numeric costo "aparte de venta.total"
    }
    RESENA {
        serial id PK
        int id_detalle_venta UK "solo compras pagadas"
        smallint calificacion "1 a 5"
        varchar comentario
        varchar estado "PUBLICADA u OCULTA"
    }
    VENTA {
        serial id PK
        varchar codigo_comprobante UK "WEB-000110"
        varchar origen "WEB o PRESENCIAL"
        varchar estado "PENDIENTE, PAGADO, ANULADO"
        numeric total
    }
    DETALLE_VENTA {
        serial id PK
        int cantidad
        numeric precio_unitario
        numeric subtotal
    }
    DONACION {
        serial id PK
        int cantidad
        varchar tipo "ABRIGO o ARBOL"
        varchar estado "EN_ESPERA, PENDIENTE, ASIGNADA, ENTREGADA"
    }
    LOTE_DONACION {
        serial id PK
        varchar codigo_lote UK "HSG-L001"
        varchar estado "PENDIENTE, EN_RUTA, ENTREGADO"
    }
    COMUNIDAD {
        serial id PK
        varchar nombre "Omacha, Ccatca, ..."
        varchar provincia
    }
```

Detalles del modelo que conviene tener presentes:

- **Cada talla es un producto distinto** (`HSG-CAS-001` = M, `HSG-CAS-002` = L, mismo `nombre`). La web los agrupa por nombre para mostrar una sola tarjeta con sus tallas.
- La **donación nace de una línea de venta** (`id_detalle_venta`) y termina en un lote. Es la tabla que responde a la pregunta del proyecto: *qué venta generó qué donación y a qué comunidad llegó*.
- Solo generan donación los productos con `tipo_compromiso` definido (`aplica_triple_impacto` es una columna generada a partir de él).
- El **envío** guarda una foto de la dirección al comprar, y su costo va aparte: `venta.total` es solo mercadería, para no inflar el monto vendido de los reportes. No hay tabla de pagos: el portal no cobra en línea.
- La **reseña** nace de una línea de venta: solo opina quien compró. El detalle de producto muestra las de todas las tallas del modelo, y solo las de ventas que siguen `PAGADO`.

### 4.2 Quién escribe cada tabla

| Tabla | Portal web | Escritorio |
|---|---|---|
| `producto` | Lee (catálogo). El panel web solo cambia `visible_web` y `destacado`. **Nunca modifica el stock** | Crea, edita, da de baja y mueve el stock |
| `cliente` | Crea y actualiza (registro, cuenta, bloqueo por intentos) | Lee |
| `venta`, `detalle_venta` | **Crea** pedidos `origen='WEB'`, `estado='PENDIENTE'` | Confirma (`PAGADO`) o anula |
| `donacion` | Crea en `EN_ESPERA` al comprar | La pasa a `PENDIENTE` al confirmar el pago, la asigna a un lote y la marca como entregada |
| `envio` | Crea en `PENDIENTE` al comprar | La pasa a `PREPARANDO` al confirmar y a `CANCELADO` al anular |
| `resena` | Crea, edita y modera | No usa |
| `lote_donacion`, `comunidad`, `ong` | Lee (consulta de impacto y cifras) | Crea y gestiona |
| `usuario` | Lee para el login del panel `/admin` y actualiza el bloqueo por intentos (mismas reglas que el escritorio) | Gestiona |
| `token_recuperacion`, `contenido_web`, `cafe_carta`, `cafe_local`, `mensaje_contacto`, `suscriptor_boletin`, `libro_reclamaciones` | Gestiona | No usa |
| `movimiento_inventario`, `proveedor`, `pedido_proveedor` | No usa | Gestiona |

## 5. Ciclo de vida de un pedido

### 5.1 Estados

```mermaid
stateDiagram-v2
    direction LR
    state "Venta" as V {
        [*] --> PENDIENTE: web registra la compra
        PENDIENTE --> PAGADO: escritorio confirma
        PENDIENTE --> ANULADO: escritorio anula
        PAGADO --> ANULADO: escritorio anula<br/>(si ninguna donación está en un lote)
    }
```

```mermaid
stateDiagram-v2
    direction LR
    state "Donación" as D {
        state "PENDIENTE" as DP
        [*] --> EN_ESPERA: nace con la venta
        EN_ESPERA --> DP: escritorio confirma el pago
        DP --> ASIGNADA: se agrega a un lote
        ASIGNADA --> ENTREGADA: el lote se entrega
    }
    state "Lote de donación" as L {
        state "PENDIENTE" as LP
        [*] --> LP: escritorio crea el lote
        LP --> EN_RUTA
        EN_RUTA --> ENTREGADO
    }
```

```mermaid
stateDiagram-v2
    direction LR
    state "Envío" as E {
        state "PENDIENTE" as EP
        state "ENTREGADO" as EE
        [*] --> EP: nace con la venta
        EP --> PREPARANDO: escritorio confirma el pago
        PREPARANDO --> EN_CAMINO: sale con el courier (delivery)
        EN_CAMINO --> EE
        PREPARANDO --> EE: recojo en tienda
        EP --> CANCELADO: escritorio anula
        PREPARANDO --> CANCELADO: escritorio anula
    }
```

Un pedido cuyo envío ya está `EN_CAMINO` o `ENTREGADO` no se puede anular.

### 5.2 De la compra a la comunidad

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant Web as Portal web
    participant DB as PostgreSQL
    participant Esc as Escritorio
    actor Encargado

    Cliente->>Web: Confirma su compra
    Web->>DB: Valida stock_comercial ≥ cantidad (solo lee)
    Web->>DB: INSERT venta (WEB-000110, PENDIENTE) + detalle_venta + donacion (EN_ESPERA) + envio (PENDIENTE)
    Web-->>Cliente: Muestra el código de comprobante y las instrucciones de pago

    loop cada 30 s
        Esc->>DB: Lista pedidos WEB
    end
    Encargado->>Esc: Confirma el pedido
    Esc->>DB: stock_comercial − n, stock_comprometido + n, donacion → PENDIENTE, envio → PREPARANDO, venta → PAGADO
    Encargado->>Esc: Emite la boleta
    Esc-->>Cliente: Boleta PDF con QR (…/consulta-impacto?donacion=id)

    Encargado->>Esc: Crea lote hacia una comunidad y lo despacha
    Esc->>DB: donacion → ASIGNADA, lote → EN_RUTA → ENTREGADO
    Esc->>DB: donacion → ENTREGADA, stock_comprometido − n

    Cliente->>Web: Escanea el QR o escribe su boleta
    Web->>DB: donacion → lote → comunidad
    Web-->>Cliente: "Tu abrigo llegó a Omacha"
```

## 6. Reglas de integración

Son las que evitan que un sistema corrompa los datos del otro.

1. **El esquema tiene un solo dueño: el escritorio.** La web usa `spring.jpa.hibernate.ddl-auto=validate`: Hibernate comprueba al arrancar que las entidades coinciden con las tablas y, si no, la aplicación no inicia. Nunca `update` ni `create`, que crearían tablas paralelas. Un cambio de esquema es un script SQL nuevo, numerado e idempotente, en el repositorio del escritorio.
2. **El stock lo mueve solo el escritorio.** La web valida que haya stock antes de registrar la venta, pero no lo descuenta. Si lo hiciera, el stock bajaría dos veces: al comprar y al confirmar.
3. **La aritmética la hace la base de datos.** Nada de leer un valor, calcular en Java y escribirlo, porque dos usuarios a la vez perderían una actualización. El stock se modifica en una sola sentencia con su condición (`UPDATE producto SET stock_comercial = stock_comercial - ? WHERE codigo = ? AND stock_comercial >= ?`) y el número de comprobante sale de una secuencia (`nextval('seq_comprobante_web')` → `WEB-000110`), nunca de `MAX(...) + 1`.
4. **Operaciones completas o nada.** Registrar una venta (venta + detalle + donaciones + envío), confirmarla o entregar un lote se hace en **una transacción**: si falla un paso, se deshace todo.
5. **El QR es un contrato.** La boleta del escritorio imprime `<portal.consulta.url>?donacion=<id>`, así que la web debe aceptar esa ruta con ese parámetro (además de `?boleta=WEB-…`). La ruta se configura en `hoseg.consulta.ruta` (web) y `portal.consulta.url` (escritorio).

## 7. Seguridad

| | Portal web | Escritorio |
|---|---|---|
| Autenticación | Spring Security con dos formularios: clientes (`/login`, **BCrypt** en `cliente.password_hash`) y panel (`/admin/login`, tabla `usuario` con **SHA-256 + salt**, igual que el escritorio) | Usuario y contraseña con **SHA-256 + salt** en `usuario` |
| Protección de fuerza bruta | Bloqueo de 5 minutos tras 3 intentos fallidos, para clientes y personal, con el reloj de Postgres | Bloqueo temporal tras 3 intentos fallidos, con el reloj del servidor |
| Autorización | Rutas públicas (catálogo, Home, consulta, Café), de cliente (`/carrito/confirmar`, `/cuenta/**`, reseñas) y de administrador (`/admin/**`, solo rol `ADMINISTRADOR`) | Módulos habilitados según el rol |
| Formularios | Token CSRF en todo `POST`; validación en DTOs (`@NotBlank`, `@Email`, `@Size`) | Validación centralizada en `util.Validador` |
| Clientes históricos | Los cargados por la semilla no tienen contraseña: deben registrarse con su mismo email | — |

**Credenciales fuera del repositorio.** Ninguna contraseña de base de datos se versiona. Web: variables de entorno `DB_URL`, `DB_USER`, `DB_PASSWORD` o `application-local.properties`. Escritorio: las mismas variables o `config.properties`. Ambos archivos están en `.gitignore`.

## 8. Configuración y ejecución

| | Portal web | Escritorio |
|---|---|---|
| Requisitos | JDK 21, Maven 3.9+ | JDK 25, Maven 3.9+ |
| Base de datos | Scripts `01`..`09` ejecutados en Supabase | ← los mismos |
| Credenciales | `application-local.properties` (copiar del `.example`) | `config.properties` (copiar del `.example`) |
| Ejecutar | `mvn spring-boot:run` → <http://localhost:8080> | `mvn compile exec:java` |
| Empaquetar | `mvn package` → `target/hoseg-web.jar` | `mvn package` → `target/segitd-hoseg-desktop.jar` |

Las instrucciones detalladas están en el `README.md` de cada repositorio.

## 9. Estado de implementación

| Sistema | Implementado | En desarrollo |
|---|---|---|
| Escritorio | RF-01 a RF-08: login con bloqueo, roles, catálogo e inventario dual, pedidos web, lotes y despacho, reportes Excel, proveedores, usuarios, boleta PDF con QR, respaldos | — |
| Portal web | Backend completo de los 5 bloques: catálogo con filtros, detalle con reseñas, carrito y checkout con envío, registro, login con bloqueo, recuperación de contraseña, mi cuenta y pedidos, consulta de impacto (boleta y QR), café, contenido editable, contacto, boletín, Libro de Reclamaciones y panel `/admin`. Página principal con datos reales y plantillas esqueleto funcionales de todas las pantallas | Diseño de las pantallas de cada bloque sobre los esqueletos ([contrato-backend.md](contrato-backend.md)) |
