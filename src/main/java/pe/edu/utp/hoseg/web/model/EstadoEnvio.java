package pe.edu.utp.hoseg.web.model;

/**
 * envio.estado (08_portal_web_compra.sql). La web lo crea PENDIENTE; el
 * escritorio lo pasa a PREPARANDO al confirmar el pago o a CANCELADO al anular.
 */
public enum EstadoEnvio {
    PENDIENTE("Pendiente de pago"),
    PREPARANDO("Preparando"),
    EN_CAMINO("En camino"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoEnvio(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
