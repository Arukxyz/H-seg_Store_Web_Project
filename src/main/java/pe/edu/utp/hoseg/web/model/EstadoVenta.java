package pe.edu.utp.hoseg.web.model;

/** venta.estado. La web solo crea PENDIENTE; el escritorio confirma (PAGADO) o anula. */
public enum EstadoVenta {
    PENDIENTE("Pendiente"),
    PAGADO("Pagado"),
    ANULADO("Anulado");

    private final String etiqueta;

    EstadoVenta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto del chip de estado en "Mis pedidos" y en el panel. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
