# HÖSÉG Web — contexto del proyecto

Portal e-commerce de Höség Store (14-DIEZ S.A.C.), proyecto académico UTP. Java 21 + Spring Boot 3.4 (Web, Thymeleaf, Data JPA, Validation, Security) + Bootstrap 5, sobre PostgreSQL en Supabase.

**Este repositorio es la mitad de un sistema.** La otra mitad es la aplicación de escritorio Swing `SEGITD-HÖSÉG` (repo `Hoseg_Store`, carpeta hermana `../Hoseg_Store`, GitHub `Arukxyz/Hoseg_Store`). Ambas comparten **la misma base de datos**. Casi todos los errores graves que se pueden cometer aquí vienen de ignorar ese hecho.

## Reglas que no se negocian

1. **El esquema de la base de datos NO se define en este repositorio.** Vive versionado en `../Hoseg_Store/src/main/resources/sql/01_schema.sql` .. `06_web_clientes.sql` y es la fuente única de verdad, compartida con el equipo de escritorio.
   - `spring.jpa.hibernate.ddl-auto` se queda en **`validate`**. Nunca `update` ni `create`: crearía tablas paralelas (`pedido`, `detalle_pedido`) y rompería la integración.
   - ¿Hace falta una columna nueva? Se agrega con un script SQL nuevo (`07_...sql`) **en el repo del escritorio**, idempotente y siguiendo el estilo de `04`/`05`/`06`, y se ejecuta a mano en el SQL Editor de Supabase. No se toca el esquema desde el dashboard.

2. **La web crea ventas; el escritorio las confirma.**

   | Hace la web (este repo) | Hace el escritorio |
   |---|---|
   | Catálogo, registro/login de clientes, carrito en sesión | Confirmar pedidos (`PENDIENTE` → `PAGADO`) |
   | Al confirmar compra: `INSERT venta` (`origen='WEB'`, `estado='PENDIENTE'`) + `detalle_venta` + `donacion` (`estado='PENDIENTE'`) | **Descontar `stock_comercial` e incrementar `stock_comprometido`** |
   | Consulta pública de impacto por boleta | Lotes de donación, despacho, reportes, boleta PDF con QR |

   **La web NO modifica stock.** Solo valida que `stock_comercial >= cantidad` antes de registrar la venta. Si además lo descontara, se descontaría dos veces cuando el encargado confirme el pedido. Tampoco registra movimientos en `movimiento_inventario`.

3. **Nada de leer-modificar-escribir sobre stock o correlativos.** La aritmética la hace Postgres. El código de comprobante sale de `nextval('seq_comprobante_web')` → `WEB-000110`, `WEB-000111`…, nunca de un `MAX(...) + 1`.

4. **Credenciales fuera del control de versiones.** `DB_URL`, `DB_USER`, `DB_PASSWORD` por variables de entorno (las mismas del escritorio, Session pooler de Supabase puerto 5432), o `application-local.properties`, que está en `.gitignore`.

## Mapeo al esquema real (no inventar nombres)

El markdown original del profesor (`pedido`, `numeroBoleta`, `idProducto`) **no corresponde** al esquema real. Lo correcto:

| Concepto | Tabla / columna real |
|---|---|
| Pedido | `venta` (`codigo_comprobante`, `origen`, `estado`, `total`, `id_cliente`) |
| Línea de pedido | `detalle_venta` (`id_venta`, `codigo_producto`, `cantidad`, `precio_unitario`, `subtotal`) |
| Producto | `producto`, **PK = `codigo VARCHAR(30)`** (`HSG-CAS-001`), no un id numérico |
| Donación | `donacion` (`id_detalle_venta`, `codigo_producto`, `cantidad`, `tipo`, `estado`, `id_lote`) |
| Comunidad beneficiada | `donacion.id_lote` → `lote_donacion.id_comunidad` → `comunidad.nombre` |

Detalles que se olvidan fácil:

- **Cada talla es una fila distinta de `producto`** (`HSG-CAS-001` = M, `HSG-CAS-002` = L, mismo `nombre`). El "selector de tallas" agrupa por `nombre` y lista las filas.
- Precios: `NUMERIC(10,2)` → **`BigDecimal`**. Fechas: `TIMESTAMPTZ` → **`OffsetDateTime`**.
- El catálogo web filtra **`activo = true AND visible_web = true`**.
- Estados reales: `venta.estado` ∈ `PENDIENTE|PAGADO|ANULADO`; `donacion.estado` ∈ `PENDIENTE|ASIGNADA|ENTREGADA`; `donacion.tipo` ∈ `ABRIGO|ARBOL`.
- Solo se crea `donacion` si el producto tiene `aplica_triple_impacto = true` y `tipo_compromiso` no nulo. `tipo` de la donación = `producto.tipo_compromiso`.
- `cliente.password_hash` guarda **BCrypt**. Los clientes de la semilla lo tienen en `NULL`: existen como compradores históricos y no pueden iniciar sesión hasta registrarse con su mismo email. (`usuario` es otra tabla, del personal interno, con SHA-256+salt; la web no la toca.)
- Portal de consulta: debe aceptar `?boleta=WEB-000110` y `?donacion=<id>`, porque el QR de las boletas PDF que genera el escritorio apunta a la segunda forma.

## Checkout (`VentaService`), en una sola transacción

1. Rechazar carrito vacío y cliente no autenticado.
2. Por ítem: producto `activo`, `visible_web`, `stock_comercial >= cantidad` (solo validar).
3. `INSERT venta` con `codigo_comprobante` de la secuencia, `origen='WEB'`, `estado='PENDIENTE'`.
4. `INSERT detalle_venta` por línea.
5. `INSERT donacion` (`PENDIENTE`) por cada línea con compromiso.
6. Limpiar el carrito de sesión y redirigir mostrando el código de comprobante.

Resultado esperado: el pedido aparece en `PedidosWebJFrame` del escritorio (recarga cada 30 s), el encargado lo confirma y ahí recién se mueve el stock.

## Estructura y convenciones

```
src/main/java/pe/edu/utp/hoseg/web/
├── config/      SecurityConfig, WebConfig
├── model/       entidades mapeadas 1:1 al esquema + enums de estado
├── repository/  Spring Data JPA
├── dto/         formularios validados (@NotBlank, @Email, @Size) e ítems de carrito
├── service/     lógica de negocio y transacciones
└── controller/  solo orquestación
```

- Los controladores **no** contienen reglas de negocio ni usan repositorios directamente; la lógica vive en `service`. (El escritorio sigue la misma disciplina: `vista → controlador → servicio → dao`.)
- Las validaciones van en los **DTO** de formulario, no en las entidades.
- El carrito vive en `HttpSession` (bean `@SessionScope`), sin tabla en base de datos.
- Código, comentarios, nombres de variables y mensajes de commit **en español**.

## Diseño de la interfaz

- `docs/brief-diseno-home.md` — brief entregado a Figma para la página principal.
- `design/figma-make/` — export de Figma Make: app **React 19 + Vite + Tailwind v4** con toda la Home en `src/App.tsx`. Es **referencia visual, no se compila ni se sirve**; queda fuera del build de Maven. Para verla: `pnpm install && pnpm dev` dentro de esa carpeta.
- `docs/diseno/mapeo-figma-thymeleaf.md` — qué componente del export corresponde a cada fragmento Thymeleaf, qué trae y qué no trae el export, y las decisiones pendientes.
- `src/main/resources/static/css/tokens.css` — paleta, tipografías, radios y animaciones del diseño, ya traducidos a CSS estándar y mapeados a variables de Bootstrap. **Es la fuente de verdad del color en el código**: no escribir colores literales en las plantillas.

Los nombres de componentes de la sección 8 del brief (`Navbar`, `MobileNavPill`, `HeroBanner`, `FeaturedCarousel`, `ProductCard`, `ImpactSteps`, `ImpactCounters`, `ImpactCTA`, `AboutStrip`, `Footer`) se mapean 1:1 a fragmentos Thymeleaf en `templates/fragments/`. Puntos fijos del diseño: responsivo desde 360 px, navegación inferior en óvalo con iconos en móvil, carrusel de destacados con auto-avance, y presencia del triple impacto (badge "1 abrigo" / "1 árbol") en toda la página.

**Decidido: Bootstrap 5 + `tokens.css`.** El export está hecho en Tailwind v4, pero las plantillas se escriben con la grilla y los componentes de Bootstrap, tomando color y tipografía de `tokens.css`. **No se agrega Tailwind al proyecto** ni se copian sus clases utilitarias: del export se leen medidas, estructura y comportamiento, y se traducen.

## Comandos

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev,local   # desarrollo
mvn package && java -jar target/hoseg-web.jar              # empaquetado
```

## Git

- Flujo rama → PR → merge a `main`. Mensajes en español, formato `Área: qué cambió`.
- **No añadir `Co-Authored-By` ni ninguna atribución a Claude** en commits ni en descripciones de PR: es un repositorio académico público.
