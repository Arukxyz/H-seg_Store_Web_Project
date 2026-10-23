package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/** "Cambiar contraseña" en Mi cuenta (2.7): exige la actual. */
public class CambioContrasenaForm {

    @NotBlank(message = "Ingresa tu contraseña actual.")
    private String actual;

    @ContrasenaSegura
    private String nueva;

    @NotBlank(message = "Repite la contraseña nueva.")
    private String confirmacion;

    @AssertTrue(message = "Las contraseñas no coinciden.")
    public boolean isContrasenasCoinciden() {
        return Objects.equals(nueva, confirmacion);
    }

    public String getActual() { return actual; }
    public void setActual(String actual) { this.actual = actual; }
    public String getNueva() { return nueva; }
    public void setNueva(String nueva) { this.nueva = nueva; }
    public String getConfirmacion() { return confirmacion; }
    public void setConfirmacion(String confirmacion) { this.confirmacion = confirmacion; }
}
