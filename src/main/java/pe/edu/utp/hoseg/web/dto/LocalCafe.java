package pe.edu.utp.hoseg.web.dto;

/** Un local de Höség Café para /cafe: dirección, horario, teléfono (WhatsApp) y enlace al mapa. */
public record LocalCafe(String nombre, String direccion, String horario, String telefono, String urlMapa) {
}
