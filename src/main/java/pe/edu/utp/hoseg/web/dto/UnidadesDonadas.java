package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoDonacion;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;

/**
 * Suma de unidades donadas agrupada por venta, tipo y estado. La suma la hace
 * Postgres (DonacionRepository); el servicio solo la reparte.
 */
public record UnidadesDonadas(Integer idVenta, TipoCompromiso tipo, EstadoDonacion estado, Long unidades) {

    /** Suma de varias ventas juntas (panel): sin venta concreta. */
    public UnidadesDonadas(TipoCompromiso tipo, EstadoDonacion estado, Long unidades) {
        this(null, tipo, estado, unidades);
    }
}
