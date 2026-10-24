package pe.edu.utp.hoseg.web.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Limpieza de texto que escribe el visitante (Apache Commons Lang3).
 *
 * <p>Los campos de una sola línea (nombre, asunto, título de reseña) no deben
 * llevar saltos de línea ni caracteres de control. Si uno de ellos termina en
 * el "Subject" de un correo, un salto de línea permitiría inyectar cabeceras
 * (por ejemplo, un "Bcc:" falso). Aquí se convierten en un solo renglón con
 * espacios simples.</p>
 *
 * <p>No escapa HTML: eso ya lo hace Thymeleaf al mostrar el texto con
 * {@code th:text}.</p>
 */
public final class SaneamientoTexto {

    private SaneamientoTexto() {
    }

    /**
     * Texto de una sola línea, obligatorio (nombre del cliente).
     * La validación de formulario ({@code @NotBlank}) corre antes de llegar aquí;
     * un {@code null} sería un error de programación, no un dato del usuario.
     */
    public static String normalizarLineaObligatoria(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El texto es obligatorio.");
        }
        return unaLinea(valor);
    }

    /** Texto de una sola línea, opcional: devuelve {@code null} si queda vacío (título de reseña). */
    public static String normalizarLinea(String valor) {
        String linea = unaLinea(valor);
        return StringUtils.isEmpty(linea) ? null : linea;
    }

    /** Asunto de correo: una sola línea, recortada a {@code maximo} caracteres. */
    public static String paraAsuntoDeCorreo(String valor, int maximo) {
        return StringUtils.truncate(normalizarLineaObligatoria(valor), maximo);
    }

    /** Cambia los caracteres de control (CR, LF, tabulación...) por espacio y deja espacios simples. */
    private static String unaLinea(String valor) {
        if (valor == null) {
            return "";
        }
        return StringUtils.normalizeSpace(valor.replaceAll("\\p{Cntrl}", " "));
    }
}