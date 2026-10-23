package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.TipoEnvio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Seguimiento del envío en "Detalle de pedido" (2.6).
 *
 * @param courier          null hasta que el pedido sale
 * @param codigoSeguimiento null hasta que el pedido sale
 */
public record EnvioVista(
        TipoEnvio tipo,
        EstadoEnvio estado,
        String nombreReceptor,
        String telefonoReceptor,
        String direccion,
        String referencia,
        String distrito,
        String provincia,
        String departamento,
        BigDecimal costo,
        String courier,
        String codigoSeguimiento,
        OffsetDateTime fechaDespacho,
        OffsetDateTime fechaEntrega) {
}
