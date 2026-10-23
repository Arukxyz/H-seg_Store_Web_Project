package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/** "Nueva contraseña" (2.4) desde el enlace del correo; el token viaja en un campo oculto. */
public class RestablecerContrasenaForm {

    @NotBlank
    private String token;

    @ContrasenaSegura
    private String nueva;

    @NotBlank(message = "Repite la contraseña nueva.")
    private String confirmacion;

    @AssertTrue(message = "Las contraseñas no coinciden.")
    public boolean isContrasenasCoinciden() {
        return Objects.equals(nueva, confirmacion);
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNueva() { return nueva; }
    public void setNueva(String nueva) { this.nueva = nueva; }
    public String getConfirmacion() { return confirmacion; }
    public void setConfirmacion(String confirmacion) { this.confirmacion = confirmacion; }
}
