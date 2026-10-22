package pe.edu.utp.hoseg.web.model;

import java.util.Locale;

/** producto.tipo_compromiso / donacion.tipo: qué genera cada compra. */
public enum TipoCompromiso {
    ABRIGO("1 abrigo", "bi-gift"),
    ARBOL("1 árbol", "bi-tree");

    private final String etiqueta;
    private final String icono;

    TipoCompromiso(String etiqueta, String icono) {
        this.etiqueta = etiqueta;
        this.icono = icono;
    }

    /** Texto del badge de impacto en el catálogo. */
    public String getEtiqueta() {
        return etiqueta;
    }

    /** Clase de Bootstrap Icons (contorno); el badge usa la variante "-fill". */
    public String getIcono() {
        return icono;
    }

    /** Sufijo de las clases CSS del compromiso: hs-badge--abrigo, hs-badge--arbol. */
    public String getClave() {
        return name().toLowerCase(Locale.ROOT);
    }
}
