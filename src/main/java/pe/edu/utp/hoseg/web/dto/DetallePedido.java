package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoVenta;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * "Detalle de pedido" (2.6, CU-13).
 *
 * @param envio         null en pedidos anteriores al script 08
 * @param puedeResenar  true si está PAGADO: cada línea muestra "Reseñar" (lleva a /tienda/{codigo}#resenas)
 */
public record DetallePedido(
        String codigoComprobante,
        OffsetDateTime fecha,
        EstadoVenta estado,
        List<LineaPedido> lineas,
        BigDecimal totalProductos,
        EnvioVista envio,
        BigDecimal totalAPagar,
        List<DonacionVista> donaciones,
        ImpactoCompra impacto,
        boolean puedeResenar) {
}
