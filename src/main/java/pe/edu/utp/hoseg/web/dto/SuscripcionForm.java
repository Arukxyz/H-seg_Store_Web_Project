package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Boletín Changemaker en el footer (CU-09). */
public class SuscripcionForm {

    @NotBlank(message = "Ingresa tu email.")
    @Email(message = "Revisa el formato del email.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
