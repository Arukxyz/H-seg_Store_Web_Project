package pe.edu.utp.hoseg.web.dto;

import java.time.OffsetDateTime;

/**
 * Una reseña publicada, sin datos personales: el autor firma "María G.".
 *
 * @param talla talla que compró el autor ("Única" en accesorios)
 */
public record ResenaVista(
        int calificacion,
        String titulo,
        String comentario,
        String autor,
        String talla,
        OffsetDateTime fecha,
        boolean editada) {
}
