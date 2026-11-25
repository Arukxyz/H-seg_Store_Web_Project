package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.TipoEnvio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /** "Distrito, provincia, departamento" sin los que falten: el recojo y muchos delivery no los traen todos. */
    public String getUbicacion() {
        return Stream.of(distrito, provincia, departamento)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
    }
}
