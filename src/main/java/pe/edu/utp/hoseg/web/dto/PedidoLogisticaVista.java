package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.TipoEnvio;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Vista de pedido para el rol LOGISTICA.
 * Aplica principio de mínimo privilegio: no expone email, DNI ni montos financieros.
 */
public record PedidoLogisticaVista(
        Integer idEnvio,
        Integer idVenta,
        String codigoComprobante,
        OffsetDateTime fechaVenta,
        TipoEnvio tipoEnvio,
        EstadoEnvio estadoEnvio,
        String nombreReceptor,
        String telefonoReceptor,
        String destinoResumen,
        String courier,
        String codigoSeguimiento,
        List<LineaLogisticaVista> lineas) {

    public boolean puedeDespachar() {
        return tipoEnvio == TipoEnvio.DELIVERY && estadoEnvio == EstadoEnvio.PREPARANDO;
    }

    public boolean puedeEntregar() {
        if (tipoEnvio == TipoEnvio.RECOJO_TIENDA) {
            return estadoEnvio == EstadoEnvio.PREPARANDO;
        }
        return estadoEnvio == EstadoEnvio.EN_CAMINO;
    }
}
