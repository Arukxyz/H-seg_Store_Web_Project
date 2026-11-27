package pe.edu.utp.hoseg.web.dto;

import java.text.Normalizer;
import java.util.Locale;

/** Un local de Höség Café para /cafe: dirección, horario, teléfono (WhatsApp) y enlace al mapa. */
public record LocalCafe(String nombre, String direccion, String horario, String telefono, String urlMapa) {

    /**
     * Nombre sin tildes, en minúsculas y con guiones: "Höség Café Cusco" → "hoseg-cafe-cusco".
     * Es el nombre del archivo de su foto en static/images/locales/ (clave + ".jpg"); la foto
     * no se guarda en la base.
     */
    public String clave() {
        if (nombre == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    /** true si es un local de la cafetería (su nombre lleva "Café"), no solo una tienda. */
    public boolean esCafe() {
        return clave().matches("(.*-)?cafe(-.*)?");
    }
}
