package pe.edu.utp.hoseg.web.dto;

/**
 * Calificación del modelo junto al precio: "★ 4.5 (12 reseñas)".
 *
 * @param promedio null si todavía no tiene reseñas
 */
public record ResumenResenas(Double promedio, long total) {

    /** Promedio redondeado a medio punto, para pintar estrellas llenas y medias. */
    public double getEstrellas() {
        return promedio == null ? 0 : Math.round(promedio * 2) / 2.0;
    }

    public boolean tieneResenas() {
        return total > 0;
    }
}
