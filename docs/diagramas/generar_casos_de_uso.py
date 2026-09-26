# -*- coding: utf-8 -*-
"""Genera los diagramas de casos de uso del portal web HÖSÉG para draw.io
(un archivo, 6 páginas) y la matriz de trazabilidad en Markdown.

Uso: python docs/diagramas/generar_casos_de_uso.py docs/diagramas"""
import os
import sys
from xml.sax.saxutils import escape

SALIDA = sys.argv[1]

PROPUESTAS = {"cafe_carta", "cafe_local", "contenido_web", "mensaje_contacto",
              "suscriptor_boletin", "token_recuperacion"}

RF_NOMBRES = {
    "RF01": "Gestión de autenticación",
    "RF02": "Registro de ventas web",
    "RF03": "Control de stock (lectura de disponibilidad)",
    "RF04": "Registro de triple impacto",
    "RF06": "Seguimiento de donaciones",
    "RF11*": "Gestión de contenido web y Höség Café (propuesto)",
    "RF12*": "Comunicación con el cliente: contacto, boletín y correos (propuesto)",
}

# ----------------------------------------------------------------------------
# Casos de uso
# grupo: vis | cli | adm ; tipo: base | include | extend
# ----------------------------------------------------------------------------
CU = [
    dict(cod="CU-01", nombre="Explorar página principal", grupo="vis", tipo="base",
         rf="RF04", vista="index.html (fragmentos navbar, footer, product-card)",
         ruta="GET /", ctrl="HomeController → CatalogoService, ImpactoService",
         tablas=["producto (lee)", "donacion (lee)", "lote_donacion (lee)", "comunidad (lee)", "venta (lee)"],
         logica="Ve el banner de identidad, el carrusel de productos destacados con su badge «1 abrigo» / «1 árbol», las cifras de impacto entregado y las comunidades beneficiadas.",
         db="Solo lectura. Destacados: activo AND visible_web. Cifras: donaciones ENTREGADA por tipo, comunidades con lote ENTREGADO y ventas PAGADO con donación."),
    dict(cod="CU-02", nombre="Explorar catálogo de productos", grupo="vis", tipo="base",
         rf="RF03", vista="tienda.html", ruta="GET /tienda",
         ctrl="TiendaController → CatalogoService",
         tablas=["producto (lee)"],
         logica="Recorre casacas, accesorios y café agrupados por nombre, con precio, tallas y compromiso de impacto; los productos sin stock comercial se muestran como «Agotado».",
         db="Solo lectura. Filtra activo AND visible_web; «Agotado» si stock_comercial = 0."),
    dict(cod="CU-02.1", nombre="Filtrar por categoría, colección o impacto", grupo="vis", tipo="extend", base="CU-02",
         rf="RF03", vista="tienda.html (panel de filtros)", ruta="GET /tienda?categoria=&coleccion=&impacto=",
         ctrl="TiendaController → CatalogoService",
         tablas=["producto (lee)"],
         logica="Acota el listado por categoría (Casacas, Accesorios, Café), colección (Älpafill, Comunidad, Urbana) o tipo de impacto (abrigo / árbol)."),
    dict(cod="CU-03", nombre="Ver detalle de producto", grupo="vis", tipo="base",
         rf="RF03, RF04", vista="producto-detalle.html", ruta="GET /tienda/{codigo}",
         ctrl="TiendaController → CatalogoService",
         tablas=["producto (lee)"],
         logica="Ve fotos, descripción, precio y las tallas disponibles (cada talla es un código distinto); elige talla y cantidad y ve qué donación genera su compra.",
         db="Lee todas las filas con el mismo nombre para armar el selector de tallas."),
    dict(cod="CU-03.1", nombre="Consultar guía de tallas", grupo="vis", tipo="extend", base="CU-03",
         rf="—", vista="producto-detalle.html (modal Bootstrap)", ruta="Sin petición al servidor",
         ctrl="—", tablas=[],
         logica="Abre la tabla comparativa de medidas S · M · L · XL antes de elegir talla."),
    dict(cod="CU-04", nombre="Gestionar carrito de compras", grupo="vis", tipo="base",
         rf="RF02", vista="carrito.html",
         ruta="GET /carrito · POST /carrito/agregar · POST /carrito/actualizar · POST /carrito/quitar",
         ctrl="CarritoController → CarritoService (@SessionScope)",
         tablas=["producto (lee)"],
         logica="Agrega, cambia la cantidad o quita prendas; ve subtotales, total y el impacto que generará. No necesita cuenta: el carrito vive en la sesión HTTP.",
         db="Lee precio, activo y stock_comercial al agregar. El carrito no se guarda en la base."),
    dict(cod="CU-05", nombre="Conocer Höség Café", grupo="vis", tipo="base",
         rf="RF04, RF11*", vista="cafe.html", ruta="GET /cafe",
         ctrl="CafeController → CafeService",
         tablas=["cafe_carta (lee)", "cafe_local (lee)", "contenido_web (lee)", "producto (lee)"],
         logica="Conoce la propuesta «por cada taza, un árbol» junto a la ONG Pachamama Raymi; revisa la carta, los locales y horarios, y puede ir a comprar café en grano a la tienda (categoría Café, genera 1 árbol).",
         db="Solo lectura. El café en grano es un producto más (categoria = 'Café', tipo_compromiso = ARBOL): no necesita tabla nueva."),
    dict(cod="CU-06", nombre="Conocer la empresa y su triple impacto", grupo="vis", tipo="base",
         rf="RF04, RF11*", vista="nosotros.html · politica-rsu.html · terminos.html",
         ruta="GET /nosotros · GET /politica-rsu · GET /terminos",
         ctrl="InstitucionalController → ContenidoService, ImpactoService",
         tablas=["contenido_web (lee)", "comunidad (lee)", "ong (lee)"],
         logica="Lee la historia de la marca, los modelos Buy One, Give One y Buy One, Plant One, las comunidades y la ONG aliada, y las políticas de responsabilidad social.",
         db="Solo lectura."),
    dict(cod="CU-07", nombre="Registrarse como cliente", grupo="vis", tipo="base",
         rf="RF01", vista="registro.html", ruta="GET /registro · POST /registro",
         ctrl="AuthController → ClienteService (DTO RegistroClienteForm: @NotBlank, @Email, @Size)",
         tablas=["cliente (lee, inserta)"],
         logica="Completa nombre, apellido, documento, email, teléfono, dirección y contraseña; los errores se muestran junto a cada campo (th:errors). Si su email ya existe por una compra anterior, completa ese registro.",
         db="Índice único sobre lower(email). Contraseña con BCrypt en password_hash. Cliente histórico sin contraseña: UPDATE en lugar de INSERT.",
         correo=dict(tipo="Bienvenida", para="El nuevo cliente",
                     contenido="Confirma la cuenta y enlaza a la tienda y a la consulta de impacto.",
                     plantilla="templates/correo/bienvenida.html")),
    dict(cod="CU-08", nombre="Enviar mensaje de contacto", grupo="vis", tipo="base",
         rf="RF12*", vista="contacto.html", ruta="GET /contacto · POST /contacto",
         ctrl="ContactoController → ContactoService (DTO MensajeContactoForm)",
         tablas=["mensaje_contacto (inserta)"],
         logica="Escribe su nombre, email, asunto y mensaje (tallas, pedidos corporativos, personal shopper) y recibe un acuse de recibo.",
         db="INSERT con estado NUEVO.",
         correo=dict(tipo="Acuse de recibo + aviso interno", para="El visitante y el buzón del equipo",
                     contenido="Al visitante: su mensaje fue recibido. Al equipo: datos del mensaje para atenderlo.",
                     plantilla="templates/correo/contacto-acuse.html · contacto-aviso.html")),
    dict(cod="CU-09", nombre="Suscribirse al boletín Changemaker", grupo="vis", tipo="base",
         rf="RF12*", vista="fragments/footer.html (formulario)", ruta="POST /boletin",
         ctrl="BoletinController → BoletinService",
         tablas=["suscriptor_boletin (lee, inserta)"],
         logica="Deja su email en el pie de página para recibir campañas, lanzamientos e informes de impacto.",
         db="Email único; si ya existía dado de baja, se reactiva.",
         correo=dict(tipo="Confirmación de suscripción", para="El suscriptor",
                     contenido="Bienvenida al boletín y enlace para darse de baja.",
                     plantilla="templates/correo/boletin-bienvenida.html")),
    dict(cod="CU-10", nombre="Consultar impacto por boleta", grupo="vis", tipo="base",
         rf="RF04, RF06", vista="consulta-impacto.html",
         ruta="GET /consulta-impacto?boleta=WEB-000110 · GET /api/impacto?boleta= (AJAX)",
         ctrl="ConsultaImpactoController → ImpactoService",
         tablas=["venta (lee)", "detalle_venta (lee)", "donacion (lee)", "lote_donacion (lee)", "comunidad (lee)", "ong (lee)"],
         logica="Ingresa el código de su boleta y ve cada abrigo o árbol que generó, su estado (Pendiente de asignar → Lote asignado → Entregada) y la comunidad beneficiada.",
         db="venta.codigo_comprobante → detalle_venta → donacion (índice idx_donacion_detalle) → lote_donacion → comunidad, ong. Solo lectura."),
    dict(cod="CU-10.1", nombre="Acceder desde el QR de la boleta", grupo="vis", tipo="extend", base="CU-10",
         rf="RF06", vista="consulta-impacto.html", ruta="GET /consulta-impacto?donacion={id}",
         ctrl="ConsultaImpactoController → ImpactoService",
         tablas=["donacion (lee)", "lote_donacion (lee)", "comunidad (lee)"],
         logica="Escanea el QR impreso en su boleta y llega directo al estado de esa donación, sin escribir el código."),
    # --- Cliente registrado ---------------------------------------------------
    dict(cod="CU-11", nombre="Iniciar y cerrar sesión", grupo="cli", tipo="base",
         rf="RF01", vista="login.html", ruta="GET /login · POST /login · POST /logout",
         ctrl="SecurityConfig (formLogin) → ClienteDetailsService",
         tablas=["cliente (lee)"],
         logica="Ingresa email y contraseña; al validarse, el menú muestra su nombre y se habilitan la compra y su cuenta. El carrito armado como visitante se conserva.",
         db="Busca por email sin distinguir mayúsculas y compara BCrypt. Un cliente sin password_hash no puede ingresar."),
    dict(cod="CU-12", nombre="Recuperar contraseña", grupo="cli", tipo="base",
         rf="RF01", vista="recuperar.html · restablecer.html",
         ruta="GET/POST /recuperar · GET/POST /restablecer?token=",
         ctrl="RecuperacionController → ClienteService",
         tablas=["cliente (lee, actualiza)", "token_recuperacion (inserta, actualiza)"],
         logica="Pide un enlace con su email, lo abre desde su correo y define una contraseña nueva.",
         db="Se guarda el hash del token, no el token. Vence a los 30 minutos y es de un solo uso.",
         correo=dict(tipo="Enlace de restablecimiento", para="El cliente que lo solicitó",
                     contenido="Enlace de un solo uso, válido 30 minutos. Si el email no existe, no se envía nada y la pantalla responde igual.",
                     plantilla="templates/correo/recuperar.html")),
    dict(cod="CU-13", nombre="Consultar mis pedidos e impacto", grupo="cli", tipo="base",
         rf="RF04, RF06", vista="cuenta.html (pestaña Pedidos) · pedido-detalle.html",
         ruta="GET /cuenta · GET /cuenta/pedidos/{codigo}",
         ctrl="CuentaController → VentaService, ImpactoService",
         tablas=["venta (lee)", "detalle_venta (lee)", "donacion (lee)", "lote_donacion (lee)", "comunidad (lee)"],
         logica="Revisa su historial de compras con su estado (Pendiente, Pagado, Anulado), el detalle de cada una y el recorrido de cada donación hasta su comunidad.",
         db="Solo pedidos con id_cliente = cliente autenticado."),
    dict(cod="CU-14", nombre="Actualizar datos de cuenta", grupo="cli", tipo="base",
         rf="RF01", vista="cuenta.html (pestaña Mis datos)",
         ruta="POST /cuenta/datos · POST /cuenta/contrasena",
         ctrl="CuentaController → ClienteService (DTO DatosClienteForm)",
         tablas=["cliente (actualiza)"],
         logica="Edita nombre, teléfono y dirección de envío; cambia su contraseña confirmando la actual. El email es su usuario y no se edita aquí.",
         db="UPDATE de su propia fila; la contraseña nueva se guarda con BCrypt."),
    dict(cod="CU-15", nombre="Confirmar compra", grupo="cli", tipo="base",
         rf="RF02, RF04", vista="carrito.html → compra-exito.html",
         ruta="POST /carrito/confirmar (exige sesión)",
         ctrl="CarritoController → VentaService (@Transactional)",
         tablas=["venta (inserta)", "detalle_venta (inserta)", "donacion (inserta)", "producto (lee)", "cliente (lee)"],
         logica="Confirma su carrito; en una sola transacción se registran la venta, sus líneas y las donaciones 1:1. Se vacía el carrito y se muestra el código de boleta. El pedido queda PENDIENTE hasta confirmarse el pago: la web no descuenta stock.",
         db="Una transacción: venta (origen WEB, estado PENDIENTE) + detalle_venta + donacion (PENDIENTE). Si algo falla se revierte todo.",
         correo=dict(tipo="Pedido recibido", para="El cliente que compró",
                     contenido="Código WEB-nnnnnn, detalle, impacto generado y enlace a la consulta de impacto. Se envía después del commit: si el correo falla, la compra no se revierte.",
                     plantilla="templates/correo/pedido-recibido.html")),
    dict(cod="CU-15.1", nombre="Validar carrito y stock", grupo="cli", tipo="include", base="CU-15",
         rf="RF03", vista="carrito.html (mensajes por línea)", ruta="Dentro de POST /carrito/confirmar",
         ctrl="VentaService", tablas=["producto (lee)"],
         logica="Rechaza el carrito vacío, los productos inactivos u ocultos y las cantidades mayores al stock comercial disponible, con un mensaje por línea."),
    dict(cod="CU-15.2", nombre="Generar código de comprobante", grupo="cli", tipo="include", base="CU-15",
         rf="RF02", vista="compra-exito.html", ruta="Dentro de POST /carrito/confirmar",
         ctrl="VentaService", tablas=["seq_comprobante_web (nextval)", "venta (inserta)"],
         logica="Asigna el siguiente código WEB-nnnnnn con la secuencia de la base, sin calcular máximos, para que dos compras simultáneas nunca repitan número."),
    dict(cod="CU-15.3", nombre="Registrar compromiso 1:1", grupo="cli", tipo="include", base="CU-15",
         rf="RF04", vista="compra-exito.html (resumen de impacto)", ruta="Dentro de POST /carrito/confirmar",
         ctrl="VentaService", tablas=["producto (lee)", "donacion (inserta)"],
         logica="Por cada línea con compromiso crea una donación PENDIENTE: 1 abrigo por casaca o chaleco, 1 árbol por accesorio o café, tantas unidades como las compradas."),
    # --- Administrador web ----------------------------------------------------
    dict(cod="CU-16", nombre="Iniciar sesión administrativa", grupo="adm", tipo="base",
         rf="RF01", vista="admin/login.html", ruta="GET /admin/login · POST /admin/login · POST /admin/logout",
         ctrl="SecurityConfig (cadena /admin/**) → UsuarioDetailsService",
         tablas=["usuario (lee)"],
         logica="El personal de Höség con rol ADMINISTRADOR entra al panel del portal. Las cuentas de cliente no tienen acceso a /admin.",
         db="Lee username, password_hash (SHA-256 + salt), rol, activo y bloqueado_hasta. La web solo lee esta tabla."),
    dict(cod="CU-17", nombre="Gestionar destacados y visibilidad web", grupo="adm", tipo="base",
         rf="RF11*", vista="admin/catalogo.html", ruta="GET /admin/catalogo · POST /admin/catalogo/{codigo}",
         ctrl="AdminCatalogoController → CatalogoService",
         tablas=["producto (lee, actualiza)"],
         logica="Elige qué productos aparecen en el carrusel de la Home y oculta de la tienda los que no deben venderse en línea.",
         db="Actualiza solo visible_web y destacado* (columna nueva). Precio y stock no se editan desde la web."),
    dict(cod="CU-18", nombre="Gestionar Höség Café", grupo="adm", tipo="base",
         rf="RF11*", vista="admin/cafe.html", ruta="GET/POST /admin/cafe/carta · GET/POST /admin/cafe/locales",
         ctrl="AdminCafeController → CafeService",
         tablas=["cafe_carta (inserta, actualiza)", "cafe_local (inserta, actualiza)", "contenido_web (actualiza)"],
         logica="Mantiene la carta (bebidas, precios, disponibilidad), los locales y horarios, y la cifra de árboles sembrados por el café que se publica en /cafe.",
         db="Los ítems se desactivan en lugar de borrarse."),
    dict(cod="CU-19", nombre="Gestionar banners y contenido institucional", grupo="adm", tipo="base",
         rf="RF11*", vista="admin/contenido.html", ruta="GET /admin/contenido · POST /admin/contenido/{clave}",
         ctrl="AdminContenidoController → ContenidoService",
         tablas=["contenido_web (lee, inserta, actualiza)"],
         logica="Cambia imágenes y textos del banner de la Home, las campañas, la página Nosotros y las políticas sin modificar código.",
         db="Una fila por bloque de contenido (clave única)."),
    dict(cod="CU-20", nombre="Atender mensajes y suscriptores", grupo="adm", tipo="base",
         rf="RF12*", vista="admin/mensajes.html",
         ruta="GET /admin/mensajes · POST /admin/mensajes/{id}/atendido · GET /admin/suscriptores.csv",
         ctrl="AdminMensajesController → ContactoService, BoletinService",
         tablas=["mensaje_contacto (lee, actualiza)", "suscriptor_boletin (lee)"],
         logica="Revisa las consultas recibidas, las marca como atendidas y descarga la lista de suscriptores activos.",
         db="Cambia mensaje_contacto.estado de NUEVO a ATENDIDO."),
    dict(cod="CU-21", nombre="Consultar panel de pedidos web", grupo="adm", tipo="base",
         rf="RF02, RF04", vista="admin/panel.html", ruta="GET /admin/panel?desde=&hasta=",
         ctrl="AdminPanelController → VentaService, ImpactoService",
         tablas=["venta (lee)", "detalle_venta (lee)", "cliente (lee)", "donacion (lee)"],
         logica="Ve los pedidos web del periodo por estado, el monto vendido y las donaciones generadas. Solo consulta: confirmar o anular pedidos no se hace desde el portal.",
         db="Solo lectura, filtrada por origen = 'WEB' y rango de fechas."),
]
POR_COD = {c["cod"]: c for c in CU}

ACTORES = {
    "vis": ("Visitante", "Cualquier persona que entra al portal sin iniciar sesión."),
    "cli": ("Cliente registrado", "Visitante con cuenta; hereda todo lo que puede hacer el Visitante (CU-01 a CU-10, página 2) y además puede confirmar la compra."),
    "adm": ("Administrador web", "Personal de Höség con rol ADMINISTRADOR que mantiene el contenido del portal."),
    "db": ("Supabase Cloud DB", "PostgreSQL compartido: persiste los datos, ejecuta las transacciones y valida unicidad."),
    "mail": ("Servicio de correo", "Servidor SMTP usado por JavaMailSender para las notificaciones."),
}

# Relaciones de los actores de sistema en el diagrama general (las principales).
DB_GENERAL = ["CU-07", "CU-10", "CU-11", "CU-15", "CU-16"]
MAIL_CUS = [c["cod"] for c in CU if "correo" in c]

# ----------------------------------------------------------------------------
# Estilos
# ----------------------------------------------------------------------------
TXT = "#2C1810"
COLORES = {  # fill, stroke
    "vis": ("#FDFAF5", "#C4622D"),
    "cli": ("#FEF0E8", "#A8501E"),
    "adm": ("#E8F0E8", "#5A7A5C"),
}
S_ACTOR = f"shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;outlineConnect=0;fillColor=#FDFAF5;strokeColor={TXT};fontColor={TXT};fontStyle=1;fontSize=12;"
S_SIS = f"rounded=1;arcSize=10;whiteSpace=wrap;html=1;fillColor=#EDE6D6;strokeColor=#4A2E20;fontColor={TXT};fontSize=12;"
S_LIMITE = f"rounded=0;whiteSpace=wrap;html=1;verticalAlign=top;align=center;fontStyle=1;fontSize=14;fillColor=#FFFFFF;strokeColor={TXT};fontColor={TXT};container=1;collapsible=0;spacingTop=8;"
S_SECCION = "text;html=1;align=left;verticalAlign=middle;fontStyle=3;fontSize=12;fontColor=#7A6152;"
S_TITULO = f"text;html=1;align=left;verticalAlign=middle;fontStyle=1;fontSize=20;fontColor={TXT};"
S_SUB = "text;html=1;align=left;verticalAlign=middle;fontSize=12;fontColor=#7A6152;"
S_NOTA = f"shape=note;whiteSpace=wrap;html=1;size=12;align=left;verticalAlign=top;spacingLeft=8;spacingRight=8;spacingTop=4;fontSize=10;fillColor=#FEF3DC;strokeColor=#D4820A;fontColor={TXT};"
S_LEYENDA = f"rounded=1;arcSize=4;whiteSpace=wrap;html=1;align=left;verticalAlign=top;spacingLeft=12;spacingRight=10;spacingTop=8;fontSize=11;fillColor=#F5F0E8;strokeColor=#D9CEB8;fontColor={TXT};"
S_ASOC = "endArrow=none;html=1;strokeColor=#4A2E20;"
# Actor humano → borde izquierdo del caso de uso: la línea no atraviesa elipses vecinas
S_ASOC_IZQ = S_ASOC + "entryX=0;entryY=0.5;entryDx=0;entryDy=0;"
S_ASOC_DER = S_ASOC + "entryX=1;entryY=0.5;entryDx=0;entryDy=0;"
S_ASOC_SIS = "endArrow=none;html=1;strokeColor=#7A6152;edgeStyle=none;jumpStyle=arc;jumpSize=8;"
S_INCLUDE = "edgeStyle=orthogonalEdgeStyle;rounded=1;endArrow=open;endFill=0;endSize=10;dashed=1;html=1;strokeColor=#A8501E;fontColor=#A8501E;fontSize=10;labelBackgroundColor=#FFFFFF;"
S_EXTEND = "edgeStyle=orthogonalEdgeStyle;rounded=1;endArrow=open;endFill=0;endSize=10;dashed=1;html=1;strokeColor=#5A7A5C;fontColor=#5A7A5C;fontSize=10;labelBackgroundColor=#FFFFFF;"
S_GENERAL = f"endArrow=block;endFill=0;endSize=14;html=1;strokeColor={TXT};edgeStyle=none;"
S_NOTA_LINK = "endArrow=none;dashed=1;html=1;strokeColor=#D4820A;"


def a(s):
    """Escapa para atributo XML."""
    return escape(s, {'"': "&quot;", "\n": "&#10;"})


def h(s):
    """Escapa texto que irá dentro de HTML."""
    return escape(s)


def tabla_txt(t):
    nombre = t.split(" ")[0]
    return t.replace(nombre, nombre + "*", 1) if nombre in PROPUESTAS else t


def tablas_txt(c, sep=" · "):
    return sep.join(tabla_txt(t) for t in c["tablas"]) or "Ninguna"


def estilo_uc(c):
    fill, stroke = COLORES[c["grupo"]]
    extra = "fontSize=10;" if c["tipo"] != "base" else "fontSize=11;"
    return f"ellipse;whiteSpace=wrap;html=1;fillColor={fill};strokeColor={stroke};fontColor={TXT};strokeWidth=1.5;" + extra


def etiqueta_uc(c):
    return f"<b>{h(c['cod'])}</b><br>{h(c['nombre'])}"


def tooltip(c):
    return (f"{c['cod']} · {c['nombre']}\nRF: {c['rf']}\nRuta: {c['ruta']}\nVista: {c['vista']}\n"
            f"Controlador: {c['ctrl']}\nTablas: {tablas_txt(c)}\nLógica: {c['logica']}")


class Pagina:
    def __init__(self, nombre, pid):
        self.nombre, self.pid, self.celdas, self.n = nombre, pid, [], 0

    def _id(self, pref):
        self.n += 1
        return f"{self.pid}-{pref}{self.n}"

    def nodo(self, valor, estilo, x, y, w, hgt, padre="1", uc=None, cid=None):
        cid = cid or self._id("n")
        geo = f'<mxGeometry x="{x:.0f}" y="{y:.0f}" width="{w:.0f}" height="{hgt:.0f}" as="geometry"/>'
        if uc:
            props = (f' tooltip="{a(tooltip(uc))}" rf="{a(uc["rf"])}" ruta="{a(uc["ruta"])}" vista="{a(uc["vista"])}"'
                     f' controlador="{a(uc["ctrl"])}" tablas="{a(tablas_txt(uc))}"')
            self.celdas.append(f'<UserObject label="{a(valor)}"{props} id="{cid}">'
                               f'<mxCell style="{a(estilo)}" vertex="1" parent="{padre}">{geo}</mxCell></UserObject>')
        else:
            self.celdas.append(f'<mxCell id="{cid}" value="{a(valor)}" style="{a(estilo)}" vertex="1" parent="{padre}">{geo}</mxCell>')
        return cid

    def arista(self, src, dst, estilo, valor="", puntos=None):
        cid = self._id("e")
        pts = ""
        if puntos:
            pts = '<Array as="points">' + "".join(f'<mxPoint x="{x:.0f}" y="{y:.0f}"/>' for x, y in puntos) + "</Array>"
        self.celdas.append(f'<mxCell id="{cid}" value="{a(valor)}" style="{a(estilo)}" edge="1" parent="1" source="{src}" target="{dst}">'
                           f'<mxGeometry relative="1" as="geometry">{pts}</mxGeometry></mxCell>')
        return cid

    def xml(self):
        cuerpo = "".join(self.celdas)
        return (f'<diagram name="{a(self.nombre)}" id="{self.pid}"><mxGraphModel dx="1400" dy="900" grid="1" gridSize="10" '
                f'guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="0" pageScale="1" math="0" shadow="0">'
                f'<root><mxCell id="0"/><mxCell id="1" parent="0"/>{cuerpo}</root></mxGraphModel></diagram>')


def actor_sistema(p, clave, x, y):
    nombre, desc = ACTORES[clave]
    sub = "PostgreSQL · Supabase" if clave == "db" else "SMTP · JavaMailSender"
    return p.nodo(f"«sistema externo»<br><b>{h(nombre)}</b><br><font style=\"font-size:10px\">{sub}</font>",
                  S_SIS, x, y, 180, 74)


def enlace_sub(p, c, ids, misma_fila):
    """include: base → sub ; extend: sub → base."""
    base, sub = ids[c["base"]], ids[c["cod"]]
    if c["tipo"] == "include":
        p.arista(base, sub, S_INCLUDE + "exitX=0.5;exitY=1;exitDx=0;exitDy=0;entryX=0;entryY=0.5;entryDx=0;entryDy=0;", "«include»")
    elif misma_fila:
        p.arista(sub, base, S_EXTEND + "exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=1;entryY=0.5;entryDx=0;entryDy=0;", "«extend»")
    else:
        p.arista(sub, base, S_EXTEND + "exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=0.5;entryY=1;entryDx=0;entryDy=0;", "«extend»")


# ----------------------------------------------------------------------------
# Página 1 · Diagrama general
# ----------------------------------------------------------------------------
def pagina_general():
    p = Pagina("1 · General", "gen")
    BX, BY, BW = 230, 90, 640
    AX, BXC, WA, WB, HU = 50, 370, 240, 220, 62
    pos = {
        "CU-01": ("A", 80), "CU-02": ("A", 158), "CU-02.1": ("B", 158), "CU-03": ("A", 236), "CU-03.1": ("B", 236),
        "CU-04": ("A", 314), "CU-05": ("A", 392), "CU-06": ("A", 470), "CU-07": ("A", 548), "CU-08": ("A", 626),
        "CU-09": ("A", 704), "CU-10": ("A", 782), "CU-10.1": ("B", 852),
        "CU-11": ("A", 980), "CU-12": ("A", 1058), "CU-13": ("A", 1136), "CU-14": ("A", 1214), "CU-15": ("A", 1292),
        "CU-15.1": ("B", 1362), "CU-15.2": ("B", 1432), "CU-15.3": ("B", 1502),
        "CU-16": ("A", 1630), "CU-17": ("A", 1708), "CU-18": ("A", 1786), "CU-19": ("A", 1864),
        "CU-20": ("A", 1942), "CU-21": ("A", 2020),
    }
    alto = 2020 + HU + 40
    p.nodo("Diagrama general de casos de uso · Portal web HÖSÉG", S_TITULO, 30, 20, 900, 40)
    lim = p.nodo("Portal web HÖSÉG · Spring Boot 3 + Thymeleaf + Supabase", S_LIMITE, BX, BY, BW, alto, cid="gen-lim")
    for txt, y in [("① Navegación y consulta pública", 48), ("② Cuenta y compra del cliente", 940), ("③ Administración del portal", 1590)]:
        p.nodo(txt, S_SECCION, AX, y, 540, 24, padre=lim)

    ids, absy = {}, {}
    for c in CU:
        col, ry = pos[c["cod"]]
        rx, w = (AX, WA) if col == "A" else (BXC, WB)
        ids[c["cod"]] = p.nodo(etiqueta_uc(c), estilo_uc(c), rx, ry, w, HU, padre=lim, uc=c)
        absy[c["cod"]] = BY + ry
    for c in CU:
        if c["tipo"] != "base":
            enlace_sub(p, c, ids, pos[c["cod"]][1] == pos[c["base"]][1])

    # Actores humanos
    def centro(cods):
        ys = [absy[x] + HU / 2 for x in cods]
        return (min(ys) + max(ys)) / 2
    grupos = {g: [c["cod"] for c in CU if c["grupo"] == g and c["tipo"] == "base"] for g in COLORES}
    act = {}
    for g in grupos:
        cy = centro(grupos[g])
        act[g] = p.nodo(ACTORES[g][0], S_ACTOR, 90, cy - 40, 40, 80)
        for cod in grupos[g]:
            p.arista(act[g], ids[cod], S_ASOC_IZQ)
    # Generalización Cliente → Visitante, por fuera a la izquierda
    ycli, yvis = centro(grupos["cli"]), centro(grupos["vis"])
    p.arista(act["cli"], act["vis"], S_GENERAL + "exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=0;entryY=0.5;entryDx=0;entryDy=0;",
             "", [(50, ycli), (50, yvis)])

    # Actores de sistema con canales verticales a la derecha del límite
    CANAL = {"mail": 910, "db": 950}
    YACT = {"mail": BY + 420, "db": BY + 1150}
    FRAC = {"mail": 0.35, "db": 0.65}
    for clave, cods in (("mail", MAIL_CUS), ("db", DB_GENERAL)):
        sid = actor_sistema(p, clave, 1010, YACT[clave])
        ya = YACT[clave] + 37
        for cod in cods:
            yt = absy[cod] + HU * FRAC[clave]
            p.arista(sid, ids[cod], S_ASOC_SIS + f"entryX=1;entryY={FRAC[clave]};entryDx=0;entryDy=0;",
                     "", [(CANAL[clave], ya), (CANAL[clave], yt)])

    rf = "<br>".join(f"<b>{h(k)}</b> {h(v)}" for k, v in RF_NOMBRES.items())
    leyenda = (
        "<b style=\"font-size:13px\">Cómo leer este diagrama</b><br><br>"
        "<b>───</b> Asociación: el actor inicia o participa en el caso de uso<br>"
        "<b>- - -▷ «include»</b>: el caso base siempre lo ejecuta<br>"
        "<b>- - -▷ «extend»</b>: comportamiento opcional del caso base<br>"
        "<b>───▷</b> Generalización: el Cliente hereda todo lo del Visitante<br><br>"
        "<b>Colores</b>: terracota = Visitante · terracota oscuro = Cliente · verde = Administrador<br><br>"
        "<b style=\"font-size:13px\">Requerimientos funcionales</b><br>" + rf +
        "<br>RF05 y RF07–RF10 se cubren fuera del portal web.<br><br>"
        "<b style=\"font-size:13px\">Tablas</b><br>"
        "<b>*</b> tabla o columna nueva, requiere un script 07: cafe_carta, cafe_local, contenido_web, "
        "mensaje_contacto, suscriptor_boletin, token_recuperacion y la columna producto.destacado.<br><br>"
        "<b>Supabase</b> participa en todo caso que lee o escribe datos; aquí solo se enlaza a los que "
        "validan credenciales o hacen transacciones. Su página individual muestra todos.<br><br>"
        "<i>Pasa el cursor sobre un caso de uso para ver su ruta, vista, controlador y tablas. "
        "Las notas completas están en las páginas 2 a 6.</i>")
    p.nodo(leyenda, S_LEYENDA, 1240, BY, 460, 400)
    return p


# ----------------------------------------------------------------------------
# Notas
# ----------------------------------------------------------------------------
CPL = 100  # caracteres por línea estimados en una nota de 520 px a 10 px


def lineas(texto):
    return max(1, math.ceil(len(texto) / CPL))


def nota_completa(c):
    campos = [("RF", c["rf"]), ("Vista", c["vista"]), ("Ruta", c["ruta"]), ("Controlador", c["ctrl"]),
              ("Tablas", tablas_txt(c)), ("Lógica", c["logica"])]
    return campos


def nota_db(c):
    campos = [("Tablas", tablas_txt(c))]
    if c.get("db"):
        campos.append(("En la base", c["db"]))
    subs = [s for s in CU if s.get("base") == c["cod"] and s["tablas"]]
    for s in subs:
        campos.append((s["cod"], tablas_txt(s)))
    return campos


def nota_correo(c):
    m = c["correo"]
    return [("Correo", m["tipo"]), ("Para", m["para"]), ("Contenido", m["contenido"]),
            ("Plantilla", m["plantilla"]), ("Envío", "CorreoService → JavaMailSender (spring-boot-starter-mail)")]


def render_nota(c, campos):
    cab = f"<b>{h(c['cod'])} · {h(c['nombre'])}</b>"
    cuerpo = "<br>".join(f"<b>{h(k)}:</b> {h(v)}" for k, v in campos)
    n = 1 + sum(lineas(f"{k}: {v}") for k, v in campos)
    return cab + "<br>" + cuerpo, n * 13 + 18


# ----------------------------------------------------------------------------
# Páginas 2–4 · Actores humanos
# ----------------------------------------------------------------------------
def pagina_actor(nombre_pag, pid, grupo, titulo, con_visitante=False):
    p = Pagina(nombre_pag, pid)
    BX, BY, BW = 200, 90, 610
    AX, BXC, WA, WB, HU = 30, 330, 250, 250, 62
    NX, NW = 860, 540
    filas = [c for c in CU if c["grupo"] == grupo]
    p.nodo(titulo, S_TITULO, 30, 20, 1300, 40)
    p.nodo(f"<b>Actor {h(ACTORES[grupo][0])}:</b> {h(ACTORES[grupo][1])}", S_SUB, 30, 56, 1300, 24)

    y = 60
    geom = []
    for c in filas:
        html, nh = render_nota(c, nota_completa(c))
        fila = max(nh, 72)
        geom.append((c, y, fila, html, nh))
        y += fila + 22
    alto = y + 10
    lim = p.nodo("Portal web HÖSÉG", S_LIMITE, BX, BY, BW, alto)
    ids, cy = {}, {}
    for c, fy, fila, html, nh in geom:
        uy = fy + (fila - HU) / 2
        rx, w = (AX, WA) if c["tipo"] == "base" else (BXC, WB)
        ids[c["cod"]] = p.nodo(etiqueta_uc(c), estilo_uc(c), rx, uy, w, HU, padre=lim, uc=c)
        cy[c["cod"]] = BY + uy + HU / 2
        nid = p.nodo(html, S_NOTA, NX, BY + fy + (fila - nh) / 2, NW, nh)
        p.arista(nid, ids[c["cod"]], S_NOTA_LINK)
    for c in filas:
        if c["tipo"] != "base":
            enlace_sub(p, c, ids, False)

    bases = [c["cod"] for c in filas if c["tipo"] == "base"]
    ys = [cy[b] for b in bases]
    ya = (min(ys) + max(ys)) / 2
    aid = p.nodo(ACTORES[grupo][0], S_ACTOR, 70, ya - 40, 40, 80)
    for b in bases:
        p.arista(aid, ids[b], S_ASOC_IZQ)

    if con_visitante:
        vy = BY + 10
        vid = p.nodo(ACTORES["vis"][0], S_ACTOR, 70, vy, 40, 80)
        # Por la izquierda, para no cruzar la etiqueta del Visitante
        p.arista(aid, vid, S_GENERAL + "exitX=0;exitY=0.5;exitDx=0;exitDy=0;entryX=0;entryY=0.5;entryDx=0;entryDy=0;",
                 "", [(35, ya), (35, vy + 40)])
    return p


# ----------------------------------------------------------------------------
# Páginas 5–6 · Actores de sistema (notas a la izquierda, actor a la derecha)
# ----------------------------------------------------------------------------
def pagina_sistema(nombre_pag, pid, clave, titulo, secciones, fnota):
    p = Pagina(nombre_pag, pid)
    NX, NW = 20, 540
    BX, BY, BW = 600, 90, 330
    AX, WA, HU = 40, 250, 62
    p.nodo(titulo, S_TITULO, 20, 20, 1300, 40)
    p.nodo(f"<b>Actor {h(ACTORES[clave][0])}:</b> {h(ACTORES[clave][1])}", S_SUB, 20, 56, 1300, 24)

    y = 20
    items = []
    for sec_titulo, cods in secciones:
        items.append(("sec", sec_titulo, y))
        y += 40
        for cod in cods:
            c = POR_COD[cod]
            html, nh = render_nota(c, fnota(c))
            fila = max(nh, 72)
            items.append(("cu", (c, html, nh, fila), y))
            y += fila + 20
        y += 20
    alto = y
    lim = p.nodo("Portal web HÖSÉG", S_LIMITE, BX, BY, BW, alto)
    ids, cy = {}, []
    for tipo, dato, fy in items:
        if tipo == "sec":
            p.nodo(dato, S_SECCION, 20, fy + 8, 300, 24, padre=lim)
            p.nodo(f"<b>{h(dato)}</b>", S_SECCION, NX, BY + fy + 8, NW, 24)
            continue
        c, html, nh, fila = dato
        uy = fy + (fila - HU) / 2
        ids[c["cod"]] = p.nodo(etiqueta_uc(c), estilo_uc(c), AX, uy, WA, HU, padre=lim, uc=c)
        cy.append(BY + uy + HU / 2)
        nid = p.nodo(html, S_NOTA, NX, BY + fy + (fila - nh) / 2, NW, nh)
        p.arista(nid, ids[c["cod"]], S_NOTA_LINK + "exitX=1;exitY=0.5;exitDx=0;exitDy=0;entryX=0;entryY=0.5;entryDx=0;entryDy=0;")
    ya = (min(cy) + max(cy)) / 2
    sid = actor_sistema(p, clave, 1010, ya - 37)
    for cod in ids:
        p.arista(sid, ids[cod], S_ASOC_DER)
    return p


def main():
    escriben = ["CU-07", "CU-08", "CU-09", "CU-12", "CU-14", "CU-15", "CU-17", "CU-18", "CU-19", "CU-20"]
    leen = [c["cod"] for c in CU if c["tipo"] == "base" and c["cod"] not in escriben]
    paginas = [
        pagina_general(),
        pagina_actor("2 · Visitante", "vis", "vis", "Casos de uso del Visitante · navegación y consulta pública"),
        pagina_actor("3 · Cliente registrado", "cli", "cli", "Casos de uso del Cliente registrado · cuenta y compra", con_visitante=True),
        pagina_actor("4 · Administrador web", "adm", "adm", "Casos de uso del Administrador web · contenido del portal"),
        pagina_sistema("5 · Supabase Cloud DB", "db", "db", "Supabase Cloud DB · tablas que impacta cada caso de uso",
                       [("Escriben en la base (INSERT / UPDATE)", escriben), ("Solo leen", leen)], nota_db),
        pagina_sistema("6 · Servicio de correo", "mail", "mail", "Servicio de correo · notificaciones que envía el portal",
                       [("Correos transaccionales", MAIL_CUS)], nota_correo),
    ]
    xml = ('<?xml version="1.0" encoding="UTF-8"?>\n<mxfile host="app.diagrams.net" agent="hoseg-web" version="24.7.17">'
           + "".join(pg.xml() for pg in paginas) + "</mxfile>\n")
    os.makedirs(SALIDA, exist_ok=True)
    with open(os.path.join(SALIDA, "casos-de-uso-web.drawio"), "w", encoding="utf-8") as f:
        f.write(xml)
    escribir_matriz(os.path.join(SALIDA, "casos-de-uso-web.md"))
    print("OK", len(CU), "casos de uso,", len(paginas), "páginas")


def escribir_matriz(ruta):
    L = ["# Casos de uso · Portal web HÖSÉG", "",
         "Matriz de trazabilidad del portal web (Spring Boot + Thymeleaf + Supabase). Es la misma información de las notas de "
         "`casos-de-uso-web.drawio`, generada desde la misma fuente.", "",
         "## Actores", "", "| Actor | Tipo | Descripción |", "|---|---|---|"]
    for k, (n, d) in ACTORES.items():
        tipo = "Sistema externo (secundario)" if k in ("db", "mail") else "Humano (principal)"
        L.append(f"| **{n}** | {tipo} | {d} |")
    L += ["", "## Requerimientos funcionales cubiertos", "", "| Código | Requerimiento | Casos de uso |", "|---|---|---|"]
    for rf, nom in RF_NOMBRES.items():
        cus = ", ".join(c["cod"] for c in CU if rf in [x.strip() for x in c["rf"].split(",")])
        L.append(f"| {rf} | {nom} | {cus} |")
    L += ["", "RF05 (lotes), RF07 (alertas de stock), RF08 (abastecimiento), RF09 (bitácora) y RF10 (reportes) no se "
          "atienden desde el portal web. RF02 dice «descontar stock comercial»: en el portal la compra queda PENDIENTE y "
          "el stock se descuenta al confirmarse el pago, no al confirmar el carrito.", "",
          "## Matriz de casos de uso", "",
          "| Código | Caso de uso | Actor | Relación | RF | Vista | Ruta | Controlador | Tablas | Lógica |",
          "|---|---|---|---|---|---|---|---|---|---|"]
    for c in CU:
        actor = ACTORES[c["grupo"]][0]
        extra = []
        if c["cod"] in DB_GENERAL or c["tablas"]:
            extra.append("Supabase")
        if "correo" in c:
            extra.append("Correo")
        if extra:
            actor += " · " + ", ".join(extra)
        rel = f"«{c['tipo']}» de {c['base']}" if c["tipo"] != "base" else "—"
        fila = [c["cod"], c["nombre"], actor, rel, c["rf"], c["vista"], c["ruta"], c["ctrl"], tablas_txt(c, ", "), c["logica"]]
        L.append("| " + " | ".join(x.replace("|", "\\|") for x in fila) + " |")
    L += ["", "\\* Tabla o columna nueva que requiere un script `07` en el repositorio del escritorio.", "",
          "## Tablas y columnas nuevas que suponen estos casos de uso", "",
          "| Tabla / columna | Uso | Casos de uso |", "|---|---|---|",
          "| `cafe_carta` | Ítems de la carta del café (nombre, descripción, categoría, precio, disponible) | CU-05, CU-18 |",
          "| `cafe_local` | Locales del café (nombre, dirección, horario, enlace al mapa, activo) | CU-05, CU-18 |",
          "| `contenido_web` | Bloques editables por clave: banners, textos de Nosotros, políticas, cifra de árboles del café | CU-05, CU-06, CU-18, CU-19 |",
          "| `mensaje_contacto` | Mensajes del formulario de contacto con estado NUEVO / ATENDIDO | CU-08, CU-20 |",
          "| `suscriptor_boletin` | Emails suscritos al boletín, con indicador de activo | CU-09, CU-20 |",
          "| `token_recuperacion` | Hash del token de recuperación, cliente, vencimiento y uso | CU-12 |",
          "| `producto.destacado` | Marca los productos que van al carrusel de la Home | CU-17 (y CU-01) |",
          "", "Todas las demás tablas (`producto`, `cliente`, `venta`, `detalle_venta`, `donacion`, `lote_donacion`, "
          "`comunidad`, `ong`, `usuario`) y la secuencia `seq_comprobante_web` ya existen en `01_schema.sql` y "
          "`06_web_clientes.sql`.", ""]
    with open(ruta, "w", encoding="utf-8") as f:
        f.write("\n".join(L))


if __name__ == "__main__":
    main()
