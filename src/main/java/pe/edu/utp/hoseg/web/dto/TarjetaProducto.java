package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.TipoCompromiso;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lo que muestra una ProductCard. Agrupa las filas de producto que comparten
 * nombre (una por talla) en una sola tarjeta.
 *
 * @param codigo         código de la primera variante, para enlazar al detalle
 * @param tipoCompromiso null si el producto no tiene compromiso social (sin badge)
 * @param tallas         tallas disponibles, en orden S → XL, "Única" al final
 * @param disponible     false cuando ninguna talla tiene stock comercial
 */
public record TarjetaProducto(
        String codigo,
        String nombre,
        String coleccion,
        BigDecimal precio,
        TipoCompromiso tipoCompromiso,
        List<String> tallas,
        boolean disponible,
        String urlImagen) {
}
