"""Genera mapas-navegacion.drawio: figuras 15 (portal web) y 16 (escritorio) del informe.

Uso: python generar_mapas_navegacion.py  (escribe el .drawio junto a este script)
"""
from pathlib import Path
from xml.sax.saxutils import quoteattr

# Paleta de tokens.css
BARK, MUTED, TERRA, SAGE, SAND = "#2C1810", "#7A6152", "#C4622D", "#5A7A5C", "#D9CEB8"
CARD, TERRA_L, SAGE_L, AMBER, AMBER_L = "#FDFAF5", "#FEF0E8", "#E8F0E8", "#D4820A", "#FEF3DC"
GRIS, GRIS_L = "#8A8A8A", "#F2F2F2"

BASE = "rounded=1;whiteSpace=wrap;html=1;arcSize=12;fontSize=12;fontColor=%s;spacing=6;" % BARK
HECHO = BASE + f"fillColor={TERRA_L};strokeColor={TERRA};strokeWidth=2;"
PENDIENTE = BASE + f"fillColor=#FFFFFF;strokeColor={MUTED};dashed=1;"
AMBOS = BASE + f"fillColor={SAGE_L};strokeColor={SAGE};strokeWidth=1.5;"
ADMIN = BASE + f"fillColor={AMBER_L};strokeColor={AMBER};strokeWidth=1.5;"
PANTALLA = BASE + f"fillColor={CARD};strokeColor={BARK};strokeWidth=1.5;"
EXTERNO = BASE + f"fillColor={GRIS_L};strokeColor={GRIS};dashed=1;fontColor=#444444;"
SALIDA = ("shape=document;whiteSpace=wrap;html=1;boundedLbl=1;fontSize=11;size=0.18;"
          f"fillColor=#FFFFFF;strokeColor={MUTED};fontColor={BARK};")
FILA_HOME = ("rounded=1;whiteSpace=wrap;html=1;arcSize=20;fontSize=11;align=left;spacingLeft=10;"
             f"fillColor=#FFFFFF;strokeColor={SAND};fontColor={BARK};")
TITULO = f"text;html=1;align=left;verticalAlign=middle;fontStyle=1;fontSize=20;fontColor={BARK};"
SUB = f"text;html=1;align=left;verticalAlign=top;fontSize=12;fontColor={MUTED};whiteSpace=wrap;"
ARISTA = (f"edgeStyle=orthogonalEdgeStyle;rounded=1;html=1;endArrow=block;endFill=1;strokeColor={MUTED};"
          f"fontSize=10;fontColor={MUTED};labelBackgroundColor=#FFFFFF;")
ARISTA_DEBIL = ARISTA + "dashed=1;"
ARISTA_EXT = ARISTA.replace(MUTED, GRIS) + "dashed=1;dashPattern=8 4;"


class Pagina:
    def __init__(self, nombre, pid):
        self.nombre, self.pid, self.celdas, self.n = nombre, pid, [], 0

    def _id(self):
        self.n += 1
        return f"{self.pid}-{self.n}"

    def nodo(self, texto, estilo, x, y, w, h, padre="1"):
        i = self._id()
        self.celdas.append(
            f'<mxCell id="{i}" value={quoteattr(texto)} style={quoteattr(estilo)} vertex="1" parent="{padre}">'
            f'<mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/></mxCell>')
        return i

    def arista(self, a, b, texto="", estilo=ARISTA, extra="", puntos=()):
        i = self._id()
        pts = "".join(f'<mxPoint x="{x}" y="{y}"/>' for x, y in puntos)
        geo = (f'<mxGeometry relative="1" as="geometry"><Array as="points">{pts}</Array></mxGeometry>'
               if puntos else '<mxGeometry relative="1" as="geometry"/>')
        self.celdas.append(
            f'<mxCell id="{i}" value={quoteattr(texto)} style={quoteattr(estilo + extra)} edge="1" '
            f'parent="1" source="{a}" target="{b}">{geo}</mxCell>')

    def xml(self):
        cuerpo = "".join(self.celdas)
        return (f'<diagram name={quoteattr(self.nombre)} id="{self.pid}"><mxGraphModel dx="1400" dy="900" '
                'grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="0" '
                'pageScale="1" math="0" shadow="0"><root><mxCell id="0"/><mxCell id="1" parent="0"/>'
                f'{cuerpo}</root></mxGraphModel></diagram>')


def leyenda(p, x, y, items):
    p.nodo("<b>Leyenda</b>", f"text;html=1;align=left;fontSize=12;fontColor={BARK};", x, y, 200, 24)
    for k, (texto, estilo) in enumerate(items):
        p.nodo("", estilo, x, y + 32 + k * 34, 40, 24)
        p.nodo(texto, f"text;html=1;align=left;verticalAlign=middle;fontSize=11;fontColor={BARK};",
               x + 50, y + 32 + k * 34, 330, 24)


# ─── Figura 15 · Portal web ────────────────────────────────────────────────
w = Pagina("Figura 15 · Navegación web", "web")
w.nodo("Figura 15. Mapa de navegación del portal web HÖSÉG", TITULO, 20, 10, 900, 36)
w.nodo("La página principal está implementada; el resto de páginas está diseñado y en desarrollo. "
       "Las rutas son las que la Home ya enlaza.", SUB, 20, 44, 1000, 24)

home = w.nodo("", BASE + f"fillColor={TERRA_L};strokeColor={TERRA};strokeWidth=2;verticalAlign=top;"
              "container=1;collapsible=0;", 560, 90, 340, 470)
w.nodo("<b>Página principal (Home)</b><br><font color='%s'>/</font>" % MUTED,
       f"text;html=1;align=center;fontSize=13;fontColor={BARK};", 10, 6, 320, 36, home)
filas = [
    ("nav", "<b>Navbar</b> (escritorio) · <b>Óvalo</b> (móvil)<br><font color='%s'>Inicio · Tienda · Café · Impacto · Nosotros · Carrito · Cuenta</font>" % MUTED, 52),
    ("hero", "Banner de identidad · <i>#inicio</i>", 40),
    ("dest", "Productos destacados · <i>#destacados</i>", 40),
    ("pasos", "Así funciona · <i>#impacto</i>", 40),
    ("cifras", "Impacto en cifras · <i>#cifras</i>", 40),
    ("cta", "Consulta tu impacto · <i>#consulta</i>", 40),
    ("nos", "Quiénes somos · <i>#nosotros</i>", 40),
    ("pie", "Footer", 40),
]
f, y = {}, 50
for clave, texto, alto in filas:
    f[clave] = w.nodo(texto, FILA_HOME, 15, y, 310, alto, home)
    y += alto + 10

# Columna izquierda: flujo de compra y cuenta
tienda = w.nodo("<b>Tienda</b><br>/tienda", PENDIENTE, 280, 100, 210, 56)
detalle = w.nodo("<b>Detalle de producto</b><br>/tienda/{codigo}<br><font color='%s'>selector de tallas</font>" % MUTED,
                 PENDIENTE, 280, 205, 210, 66)
carrito = w.nodo("<b>Carrito</b><br>/carrito<br><font color='%s'>también desde el icono de la navbar</font>" % MUTED,
                 PENDIENTE, 280, 320, 210, 66)
confirmar = w.nodo("<b>Confirmar compra</b><br>/carrito/confirmar<br><font color='%s'>requiere sesión</font>" % TERRA,
                   PENDIENTE, 280, 435, 210, 66)
login = w.nodo("<b>Iniciar sesión</b><br>/login<br><font color='%s'>«Cuenta» en la navbar</font>" % MUTED,
               PENDIENTE, 280, 560, 210, 66)
registro = w.nodo("<b>Registro</b><br>/registro", PENDIENTE, 280, 680, 210, 56)
cuenta = w.nodo("<b>Mi cuenta</b><br>/cuenta<br><font color='%s'>pedidos e impacto · requiere sesión</font>" % TERRA,
                PENDIENTE, 0, 560, 200, 66)
datos = w.nodo("<b>Mis datos</b><br>/cuenta?seccion=datos", PENDIENTE, 0, 680, 200, 56)

# Columna derecha: café, consulta e institucional
cafe = w.nodo("<b>Höség Café</b><br>/cafe", PENDIENTE, 980, 100, 250, 56)
consulta = w.nodo("<b>Consulta de impacto</b><br>/consulta-impacto<br><font color='%s'>?boleta=WEB-000110 · ?donacion=&lt;id&gt;</font>" % MUTED,
                  PENDIENTE, 980, 350, 250, 70)
pie = w.nodo("", BASE + f"fillColor={CARD};strokeColor={SAND};dashed=1;verticalAlign=top;container=1;collapsible=0;",
             980, 470, 300, 270)
w.nodo("<b>Enlaces del footer</b>", f"text;html=1;align=center;fontSize=12;fontColor={BARK};", 10, 4, 280, 26, pie)
for k, texto in enumerate(["Contacto · /contacto", "Términos y condiciones · /terminos",
                           "Política de responsabilidad social · /politica-rsu",
                           "Libro de Reclamaciones · /libro-reclamaciones", "Boletín · POST /boletin"]):
    w.nodo(texto, PENDIENTE + "fontSize=11;arcSize=20;", 12, 36 + k * 46, 276, 36, pie)

boleta = w.nodo("<b>Boleta PDF con QR</b><br><font color='#666666'>emitida por el sistema de escritorio</font>",
                EXTERNO, 1360, 355, 200, 60)

# Aristas
w.arista(f["nav"], tienda, "Tienda", extra="exitX=0;exitY=0.5;entryX=1;entryY=0.3;")
w.arista(f["hero"], tienda, "Ver tienda", extra="exitX=0;exitY=0.5;entryX=1;entryY=0.75;")
w.arista(f["dest"], detalle, "Ver detalle", extra="exitX=0;exitY=0.5;entryX=1;entryY=0.5;")
w.arista(f["nav"], cafe, "Café", extra="exitX=1;exitY=0.5;entryX=0;entryY=0.5;")
w.arista(f["cta"], consulta, "Consultar", extra="exitX=1;exitY=0.5;entryX=0;entryY=0.5;")
w.arista(f["pie"], pie, "", extra="exitX=1;exitY=0.5;entryX=0;entryY=0.1;")
w.arista(tienda, detalle)
w.arista(detalle, carrito, "Agregar al carrito")
w.arista(carrito, confirmar, "Comprar")
w.arista(confirmar, login, "sin sesión", ARISTA_DEBIL)
w.arista(login, registro, "¿No tienes cuenta?", ARISTA_DEBIL)
w.arista(login, cuenta, "con sesión", extra="exitX=0;exitY=0.5;entryX=1;entryY=0.5;")
w.arista(cuenta, datos)
w.arista(boleta, consulta, "escaneo del QR<br>?donacion=&lt;id&gt;", ARISTA_EXT,
         extra="exitX=0;exitY=0.5;entryX=1;entryY=0.5;")

leyenda(w, 560, 600, [
    ("Implementado en este avance", HECHO),
    ("Diseñado, en desarrollo", PENDIENTE),
    ("Sistema de escritorio (otro sistema)", EXTERNO),
    ("Navegación opcional o condicional", "line;strokeWidth=1.5;dashed=1;strokeColor=%s;html=1;" % MUTED),
    ("Enlace entre sistemas", "line;strokeWidth=1.5;dashed=1;dashPattern=8 4;strokeColor=%s;html=1;" % GRIS),
])

# ─── Figura 16 · Escritorio ────────────────────────────────────────────────
d = Pagina("Figura 16 · Navegación escritorio", "esc")
d.nodo("Figura 16. Mapa de navegación del módulo de escritorio SEGITD-HÖSÉG", TITULO, 20, 10, 1000, 36)
d.nodo("Navegación en estrella: todo parte del menú principal. Los módulos que el rol no permite "
       "se muestran deshabilitados, no ocultos.", SUB, 20, 44, 1100, 24)

login_d = d.nodo("<b>Inicio de sesión</b><br>LoginJFrame · RF-01<br><font color='%s'>bloqueo tras 3 intentos fallidos</font>" % MUTED,
                 PANTALLA, 510, 90, 260, 70)
menu = d.nodo("<b>Menú principal · Dashboard</b><br>MenuPrincipalJFrame<br>"
              "<font color='%s'>Indicadores en vivo: productos activos, stock crítico, pedidos web pendientes,<br>"
              "donaciones por asignar, lotes en ruta · estado de conexión</font>" % MUTED,
              PANTALLA + "strokeWidth=2;", 390, 220, 500, 90)
stock = d.nodo("<b>Reporte de stock</b><br>ReporteStockJFrame<br><font color='%s'>ambos roles</font>" % MUTED,
               AMBOS, 960, 230, 210, 70)
respaldo = d.nodo("<b>Respaldo CSV</b><br>backups/<br><font color='%s'>«Respaldar ahora» (solo admin)<br>y automático al cerrar</font>" % MUTED,
                  SALIDA, 110, 225, 200, 84)

modulos = [
    ("Gestión de productos", "GestionProductosJFrame", "RF-02 · RF-03", "Admin: total<br>Encargado: solo consulta", AMBOS),
    ("Pedidos web", "PedidosWebJFrame", "RF-04", "Confirmar · Anular<br>recarga cada 30 s", AMBOS),
    ("Despacho de lotes", "DespachoLotesJFrame", "RF-05", "Crear lote · En ruta<br>Entregado", AMBOS),
    ("Reportes de impacto", "ReportesImpactoJFrame", "RF-07", "Solo administrador", ADMIN),
    ("Pedidos a proveedores", "ProveedoresJFrame", "RF-06", "Solo administrador", ADMIN),
    ("Gestión de usuarios", "UsuariosJFrame", "RF-08", "Solo administrador", ADMIN),
]
ids = []
for k, (nombre, clase, rf, nota, estilo) in enumerate(modulos):
    ids.append(d.nodo(f"<b>{nombre}</b><br>{clase}<br><font color='{MUTED}'>{rf}</font><br>{nota}",
                      estilo, 20 + k * 210, 400, 190, 96))

# Salidas y subpantallas
boleta_d = d.nodo("<b>Boleta PDF con QR</b><br>boletas/", SALIDA, 230, 560, 190, 60)
excel_lote = d.nodo("<b>Excel de donaciones</b><br>en riesgo", SALIDA, 440, 560, 190, 60)
dash = d.nodo("<b>Dashboard de ventas y demanda</b><br>DashboardVentasJFrame", ADMIN, 578, 660, 220, 60)
excel = d.nodo("<b>Reporte de impacto .xlsx</b><br>Resumen · Trazabilidad ·<br>Historial · Inventario",
               SALIDA, 810, 660, 200, 74)
cert = d.nodo("<b>Certificado de<br>Impacto B</b> (PDF)", SALIDA, 1030, 660, 170, 60)
portal = d.nodo("<b>Portal web</b> · Consulta de impacto<br><font color='#666666'>/consulta-impacto?donacion=&lt;id&gt;</font>",
                EXTERNO, 170, 680, 310, 60)

d.arista(login_d, menu, "credenciales válidas")
d.arista(menu, login_d, "Cerrar sesión", ARISTA_DEBIL, extra="exitX=0;exitY=0.25;entryX=0;entryY=0.5;")
d.arista(menu, stock, "", extra="exitX=1;exitY=0.5;entryX=0;entryY=0.5;")
d.arista(menu, respaldo, "", ARISTA_DEBIL, extra="exitX=0;exitY=0.6;entryX=1;entryY=0.5;")
for i in ids:
    d.arista(menu, i, "", extra="exitX=0.5;exitY=1;entryX=0.5;entryY=0;")
d.arista(ids[1], boleta_d, "Emitir boleta")
d.arista(ids[2], excel_lote, "Exportar")
d.arista(ids[3], dash, "Ver dashboard", extra="exitX=0.2;exitY=1;entryX=0.5;entryY=0;")
d.arista(ids[3], excel, "Exportar", extra="exitX=0.6;exitY=1;entryX=0.5;entryY=0;", puntos=[(764, 610), (910, 610)])
d.arista(ids[3], cert, "Emitir", extra="exitX=0.9;exitY=1;entryX=0.5;entryY=0;", puntos=[(821, 560), (1115, 560)])
d.arista(boleta_d, portal, "el cliente escanea el QR", ARISTA_EXT)

leyenda(d, 20, 780, [
    ("Pantalla común", PANTALLA),
    ("Módulo disponible para ambos roles", AMBOS),
    ("Módulo solo para ADMINISTRADOR (deshabilitado para ENCARGADO)", ADMIN),
    ("Archivo generado (PDF, Excel, CSV)", SALIDA),
    ("Portal web (otro sistema)", EXTERNO),
])

xml = ('<?xml version="1.0" encoding="UTF-8"?>\n<mxfile host="app.diagrams.net" agent="hoseg-web" version="24.7.17">'
       + w.xml() + d.xml() + "</mxfile>\n")
Path(__file__).with_name("mapas-navegacion.drawio").write_text(xml, encoding="utf-8", newline="\n")
print("mapas-navegacion.drawio generado")
