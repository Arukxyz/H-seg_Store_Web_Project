package pe.edu.utp.hoseg.web.dto;

import java.math.BigDecimal;

/**
 * Una talla del selector de chips: cada talla es un código de producto.
 *
 * @param maximo      unidades que se pueden pedir (stock comercial); 0 si está agotada
 * @param quedanPocas para el aviso "¡Quedan pocas unidades!"
 */
public record VarianteProducto(
        String codigo,
        String talla,
        BigDecimal precio,
        boolean disponible,
        int maximo,
        boolean quedanPocas) {
}
