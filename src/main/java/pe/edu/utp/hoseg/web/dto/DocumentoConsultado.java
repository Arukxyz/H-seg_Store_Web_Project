package pe.edu.utp.hoseg.web.dto;

/** Resultado normalizado de consulta de DNI (RENIEC) o RUC (SUNAT). */
public record DocumentoConsultado(
        String tipo,
        String numero,
        String nombres,
        String apellidos,
        String nombreCompleto,
        String direccion,
        String fuente) {
}
