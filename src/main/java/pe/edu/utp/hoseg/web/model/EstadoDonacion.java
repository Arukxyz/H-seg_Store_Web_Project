package pe.edu.utp.hoseg.web.model;

/**
 * donacion.estado. La web la crea EN_ESPERA (la venta aún no está pagada);
 * el escritorio la pasa a PENDIENTE al confirmar el pago, y luego la asigna
 * a un lote y la entrega (07_normalizacion.sql).
 */
public enum EstadoDonacion {
    EN_ESPERA("En espera de pago"),
    PENDIENTE("Pendiente de asignar"),
    ASIGNADA("Lote asignado"),
    ENTREGADA("Entregada");

    private final String etiqueta;

    EstadoDonacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto de cada paso de la línea de tiempo de la donación. */
    public String getEtiqueta() {
        return etiqueta;
    }

    /** Posición en la línea de tiempo (0 a 3): los pasos anteriores se marcan como cumplidos. */
    public int getPaso() {
        return ordinal();
    }
}
