package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoDonacion;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;

import java.time.OffsetDateTime;

/**
 * Una donación con su línea de tiempo (En espera de pago → Pendiente de
 * asignar → Lote asignado → Entregada). La misma vista sirve para "Detalle de
 * pedido" (bloque 2) y para la consulta pública (bloque 3): no lleva datos
 * personales del comprador.
 *
 * @param comunidad null mientras no tenga lote
 * @param fechaEntrega fecha de entrega del lote a la comunidad; null si aún no llega
 */
public record DonacionVista(
        Integer id,
        TipoCompromiso tipo,
        int cantidad,
        String producto,
        EstadoDonacion estado,
        String comunidad,
        String ong,
        OffsetDateTime fechaDespacho,
        OffsetDateTime fechaEntrega) {

    /** "2 abrigos", "1 árbol". */
    public String getUnidades() {
        if (tipo == TipoCompromiso.ABRIGO) {
            return cantidad == 1 ? "1 abrigo" : cantidad + " abrigos";
        }
        return cantidad == 1 ? "1 árbol" : cantidad + " árboles";
    }
}
