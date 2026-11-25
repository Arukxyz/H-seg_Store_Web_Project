package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** "Escribe tu reseña" en el detalle de producto (CU-22). Sirve para crear y para editar. */
public class ResenaForm {

    @NotNull(message = "Elige de 1 a 5 estrellas.")
    @Min(value = 1, message = "Elige de 1 a 5 estrellas.")
    @Max(value = 5, message = "Elige de 1 a 5 estrellas.")
    private Integer calificacion;

    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String titulo;

    @NotBlank(message = "Cuéntanos qué te pareció.")
    @Size(min = 10, max = 1000, message = "Escribe entre 10 y 1000 caracteres.")
    private String comentario;

    public Integer getCalificacion() { return calificacion; }
    public void setCalificacion(Integer calificacion) { this.calificacion = calificacion; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
