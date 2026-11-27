package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.TipoEnvio;

import java.math.BigDecimal;

/**
 * Resultado de pagar y registrar la compra, para "Compra registrada" (1.6).
 */
public record CompraRegistrada(
        String codigoComprobante,
        BigDecimal totalProductos,
        BigDecimal costoEnvio,
        BigDecimal totalAPagar,
        TipoEnvio tipoEnvio,
        ImpactoCompra impacto,
        String metodoPago,
        String codigoTransaccion) {

    /** Constructor de compatibilidad para código/tests previos sin pasarela. */
    public CompraRegistrada(String codigoComprobante, BigDecimal totalProductos, BigDecimal costoEnvio,
                            BigDecimal totalAPagar, TipoEnvio tipoEnvio, ImpactoCompra impacto) {
        this(codigoComprobante, totalProductos, costoEnvio, totalAPagar, tipoEnvio, impacto, null, null);
    }
}
