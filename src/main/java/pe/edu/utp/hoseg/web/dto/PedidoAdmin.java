package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.TipoEnvio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Fila de la tabla de pedidos del panel (4.2).
 *
 * @param tipoEnvio   null en pedidos anteriores al script 08
 * @param estadoEnvio null en pedidos anteriores al script 08
 */
public record PedidoAdmin(
        String codigoComprobante,
        String cliente,
        OffsetDateTime fecha,
        BigDecimal total,
        BigDecimal costoEnvio,
        EstadoVenta estado,
        TipoEnvio tipoEnvio,
        EstadoEnvio estadoEnvio) {
}
