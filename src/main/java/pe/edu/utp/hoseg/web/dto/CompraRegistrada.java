package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.TipoEnvio;

import java.math.BigDecimal;

/**
 * Resultado de confirmar la compra, para "Compra registrada" (1.6).
 *
 * @param totalProductos lo que se guarda en venta.total (solo mercadería)
 * @param totalAPagar    productos + envío: lo que el cliente debe pagar
 */
public record CompraRegistrada(
        String codigoComprobante,
        BigDecimal totalProductos,
        BigDecimal costoEnvio,
        BigDecimal totalAPagar,
        TipoEnvio tipoEnvio,
        ImpactoCompra impacto) {
}
