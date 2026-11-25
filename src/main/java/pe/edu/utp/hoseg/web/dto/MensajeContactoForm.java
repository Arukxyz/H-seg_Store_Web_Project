package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Formulario de contacto (3.5, CU-08). */
public class MensajeContactoForm {

    @NotBlank(message = "Ingresa tu nombre.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String nombre;

    @NotBlank(message = "Ingresa tu email.")
    @Email(message = "Revisa el formato del email.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String email;

    @NotBlank(message = "Indica el asunto.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String asunto;

    @NotBlank(message = "Escribe tu mensaje.")
    @Size(max = 4000, message = "Máximo 4000 caracteres.")
    private String mensaje;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
