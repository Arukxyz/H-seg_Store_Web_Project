package pe.edu.utp.hoseg.web.model;

/**
 * donacion.estado. La web la crea EN_ESPERA (la venta aún no está pagada);
 * el escritorio la pasa a PENDIENTE al confirmar el pago, y luego la asigna
 * a un lote y la entrega (07_normalizacion.sql).
 */
public enum EstadoDonacion {
    EN_ESPERA, PENDIENTE, ASIGNADA, ENTREGADA
}
