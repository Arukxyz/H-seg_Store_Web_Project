package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoVenta;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Resultado de la consulta pública de impacto (3.1, CU-10). Sin datos
 * personales del comprador: la página es pública.
 *
 * @param porQr true si llegó por el QR de la boleta (?donacion=id): muestra una sola donación
 */
public record ConsultaImpacto(
        String codigoComprobante,
        OffsetDateTime fechaCompra,
        EstadoVenta estadoVenta,
        List<DonacionVista> donaciones,
        boolean porQr) {

    public boolean tieneDonaciones() {
        return !donaciones.isEmpty();
    }
}
