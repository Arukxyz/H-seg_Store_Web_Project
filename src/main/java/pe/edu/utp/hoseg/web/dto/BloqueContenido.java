package pe.edu.utp.hoseg.web.dto;

import java.time.OffsetDateTime;

/**
 * Un bloque de contenido_web. Las plantillas lo usan con su texto por defecto:
 * {@code th:text="${contenido['home.banner']?.titulo ?: 'Abriga a alguien más…'}"}.
 */
public record BloqueContenido(String clave, String titulo, String cuerpo, String imagenUrl, OffsetDateTime actualizadoEn) {
}
