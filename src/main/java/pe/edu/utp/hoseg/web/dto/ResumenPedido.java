package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.EstadoVenta;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Fila de "Mis pedidos" (2.5).
 *
 * @param estadoEnvio null en pedidos anteriores al script 08 (sin envío)
 */
public record ResumenPedido(
        String codigoComprobante,
        OffsetDateTime fecha,
        EstadoVenta estado,
        BigDecimal totalProductos,
        BigDecimal costoEnvio,
        BigDecimal totalAPagar,
        ImpactoCompra impacto,
        EstadoEnvio estadoEnvio) {
}
