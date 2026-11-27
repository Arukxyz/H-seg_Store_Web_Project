package pe.edu.utp.hoseg.web.dto;

import java.util.Locale;

/**
 * Una foto real de entrega para la galería "Evidencia de impacto" de Nosotros.
 * Las fotos son archivos en static/images/evidencia/ (no están en la base). El pie sale
 * del nombre del archivo: "02-entrega-de-abrigos-en-omacha.jpg" → "Entrega de abrigos en omacha"
 * (el número inicial solo ordena).
 *
 * @param url ruta pública de la imagen
 * @param pie texto bajo la foto, también usado como texto alternativo
 */
public record FotoEvidencia(String url, String pie) {

    /** Pie a partir del nombre de archivo: sin extensión ni número de orden, guiones por espacios. */
    public static String pieDesdeArchivo(String archivo) {
        String base = archivo.replaceFirst("\\.[^.]+$", "")
                .replaceFirst("^\\d+([-_ ]+|$)", "")
                .replaceAll("[-_]+", " ")
                .trim();
        if (base.isEmpty()) {
            return "Entrega en Cusco";
        }
        return base.substring(0, 1).toUpperCase(Locale.ROOT) + base.substring(1);
    }
}
