package pe.edu.utp.hoseg.web.dto;

import java.math.BigDecimal;

/** Una línea del carrito en sesión: producto (con su talla) y cantidad. */
public record ItemCarrito(
        String codigoProducto,
        String nombre,
        String talla,
        BigDecimal precioUnitario,
        int cantidad) {

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public ItemCarrito conCantidad(int nuevaCantidad) {
        return new ItemCarrito(codigoProducto, nombre, talla, precioUnitario, nuevaCantidad);
    }
}
