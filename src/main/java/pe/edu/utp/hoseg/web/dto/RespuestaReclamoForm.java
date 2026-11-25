package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Respuesta del administrador a una hoja del Libro de Reclamaciones. */
public class RespuestaReclamoForm {

    @NotBlank(message = "Escribe la respuesta.")
    @Size(max = 4000, message = "Máximo 4000 caracteres.")
    private String respuesta;

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
}
