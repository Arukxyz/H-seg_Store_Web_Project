package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.TipoCompromiso;

import java.math.BigDecimal;

/** Una línea de un pedido ya registrado, con el precio que se cobró. */
public record LineaPedido(
        String codigoProducto,
        String nombre,
        String talla,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        TipoCompromiso tipoCompromiso) {
}
