package pe.edu.utp.hoseg.web.dto;

/**
 * Por qué una línea del carrito no se puede comprar (CU-15.1).
 *
 * @param codigoProducto línea afectada; null si el problema es del carrito entero (vacío)
 */
public record ProblemaCarrito(String codigoProducto, String mensaje) {
}
