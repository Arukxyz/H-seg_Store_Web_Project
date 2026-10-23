package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.TipoCompromiso;

import java.math.BigDecimal;
import java.util.List;

/**
 * Página de detalle (/tienda/{codigo}): el modelo con todas sus tallas.
 *
 * @param codigoElegido  talla que llegó en la URL; la vista la marca seleccionada
 * @param precioDesde    el menor precio entre las tallas
 * @param tipoCompromiso null si el producto no genera donación
 * @param sinTallas      true si la única talla es "Única" (accesorios, café): sin selector
 * @param relacionados   hasta 4 tarjetas de la misma categoría
 */
public record DetalleProducto(
        String codigoElegido,
        String nombre,
        String categoria,
        String coleccion,
        String descripcion,
        String urlImagen,
        BigDecimal precioDesde,
        TipoCompromiso tipoCompromiso,
        List<VarianteProducto> variantes,
        boolean disponible,
        boolean sinTallas,
        List<TarjetaProducto> relacionados) {
}
