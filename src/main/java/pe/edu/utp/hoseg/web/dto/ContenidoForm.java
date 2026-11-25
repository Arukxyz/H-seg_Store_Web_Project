package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Editor de un bloque de contenido en el panel (4.5, CU-19). */
public class ContenidoForm {

    @Size(max = 200, message = "Máximo 200 caracteres.")
    private String titulo;

    @Size(max = 20000, message = "Máximo 20000 caracteres.")
    private String cuerpo;

    /** Ruta local (/images/…) o enlace https; nunca rutas de disco. */
    @Pattern(regexp = "^$|^/images/.+|^https://.+", message = "Usa una ruta /images/... o un enlace https://")
    private String imagenUrl;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getCuerpo() { return cuerpo; }
    public void setCuerpo(String cuerpo) { this.cuerpo = cuerpo; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}
