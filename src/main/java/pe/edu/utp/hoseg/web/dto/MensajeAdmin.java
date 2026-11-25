package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoMensaje;

import java.time.OffsetDateTime;

/** Mensaje de contacto en la bandeja del panel (4.6). */
public record MensajeAdmin(
        Integer id,
        String nombre,
        String email,
        String asunto,
        String mensaje,
        EstadoMensaje estado,
        OffsetDateTime creadoEn,
        OffsetDateTime atendidoEn,
        String atendidoPor) {
}
