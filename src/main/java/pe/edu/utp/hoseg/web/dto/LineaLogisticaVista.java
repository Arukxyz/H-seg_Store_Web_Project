package pe.edu.utp.hoseg.web.dto;

/** Línea de producto a empacar (solo lo necesario para almacén). */
public record LineaLogisticaVista(
        String codigoProducto,
        String nombreProducto,
        String talla,
        int cantidad) {
}
