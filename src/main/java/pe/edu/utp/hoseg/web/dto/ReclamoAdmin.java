package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoReclamo;
import pe.edu.utp.hoseg.web.model.TipoBien;
import pe.edu.utp.hoseg.web.model.TipoDocumento;
import pe.edu.utp.hoseg.web.model.TipoReclamo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Hoja del Libro de Reclamaciones en el panel (4.6, pestaña Reclamaciones). */
public record ReclamoAdmin(
        Integer id,
        String numeroHoja,
        TipoReclamo tipo,
        String nombre,
        TipoDocumento tipoDoc,
        String numDoc,
        String domicilio,
        String email,
        String telefono,
        TipoBien tipoBien,
        BigDecimal montoReclamado,
        String descripcionBien,
        String detalle,
        String pedidoConsumidor,
        String codigoComprobante,
        EstadoReclamo estado,
        String respuesta,
        OffsetDateTime creadoEn,
        OffsetDateTime atendidoEn,
        String atendidoPor) {
}
