package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.TipoCompromiso;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Una línea del carrito en sesión: producto (con su talla) y cantidad. El
 * precio es el del momento de agregar; al confirmar se cobra el de la base.
 *
 * @param tipoCompromiso null si el producto no genera donación
 */
public record ItemCarrito(
        String codigoProducto,
        String nombre,
        String talla,
        BigDecimal precioUnitario,
        int cantidad,
        TipoCompromiso tipoCompromiso,
        String urlImagen) implements Serializable {

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public ItemCarrito conCantidad(int nuevaCantidad) {
        return new ItemCarrito(codigoProducto, nombre, talla, precioUnitario, nuevaCantidad, tipoCompromiso, urlImagen);
    }
}
