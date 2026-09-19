package pe.edu.utp.hoseg.web.model;

/** producto.tipo_compromiso / donacion.tipo: qué genera cada compra. */
public enum TipoCompromiso {
    ABRIGO("1 abrigo"),
    ARBOL("1 árbol");

    private final String etiqueta;

    TipoCompromiso(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto del badge de impacto en el catálogo. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
