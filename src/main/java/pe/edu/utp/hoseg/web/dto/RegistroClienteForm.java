package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.edu.utp.hoseg.web.model.TipoDocumento;

import java.util.Objects;

/** Formulario de registro (2.2, CU-07). Los errores se muestran junto a cada campo. */
public class RegistroClienteForm {

    @NotBlank(message = "Ingresa tu nombre.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String nombre;

    @NotBlank(message = "Ingresa tu apellido.")
    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String apellido;

    @NotNull(message = "Elige el tipo de documento.")
    private TipoDocumento tipoDoc;

    @NotBlank(message = "Ingresa tu número de documento.")
    @Size(max = 20, message = "Máximo 20 caracteres.")
    private String numDoc;

    @NotBlank(message = "Ingresa tu email.")
    @Email(message = "Revisa el formato del email.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String email;

    @Pattern(regexp = "^$|^(\\+51 ?)?9\\d{8}$", message = "Ingresa un celular de 9 dígitos que empiece con 9.")
    private String telefono;

    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String direccion;

    @ContrasenaSegura
    private String contrasena;

    @NotBlank(message = "Repite la contraseña.")
    private String confirmacion;

    @AssertTrue(message = "Debes aceptar los términos y la política de privacidad.")
    private boolean aceptaTerminos;

    /** cliente_documento_check: DNI 8 dígitos, RUC 11, CE 9-12 y pasaporte 6-12 alfanuméricos. */
    @AssertTrue(message = "El número no tiene el formato de ese tipo de documento.")
    public boolean isDocumentoValido() {
        return tipoDoc == null || numDoc == null || numDoc.isBlank() || tipoDoc.admite(numDoc.trim());
    }

    @AssertTrue(message = "Las contraseñas no coinciden.")
    public boolean isContrasenasCoinciden() {
        return Objects.equals(contrasena, confirmacion);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public TipoDocumento getTipoDoc() { return tipoDoc; }
    public void setTipoDoc(TipoDocumento tipoDoc) { this.tipoDoc = tipoDoc; }
    public String getNumDoc() { return numDoc; }
    public void setNumDoc(String numDoc) { this.numDoc = numDoc; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getConfirmacion() { return confirmacion; }
    public void setConfirmacion(String confirmacion) { this.confirmacion = confirmacion; }
    public boolean isAceptaTerminos() { return aceptaTerminos; }
    public void setAceptaTerminos(boolean aceptaTerminos) { this.aceptaTerminos = aceptaTerminos; }
}
