package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.EstadoResena;

import java.time.OffsetDateTime;

/**
 * Fila del panel de moderación de reseñas (4.7, CU-23).
 *
 * @param moderadaPor nombre del administrador que la ocultó o republicó; null si nadie la tocó
 */
public record ResenaModeracion(
        Integer id,
        String producto,
        String talla,
        String codigoComprobante,
        int calificacion,
        String titulo,
        String comentario,
        String cliente,
        String emailCliente,
        OffsetDateTime fecha,
        EstadoResena estado,
        String moderadaPor) {
}
