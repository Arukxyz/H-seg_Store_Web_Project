package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * "Mis datos" (2.7, CU-14). El email es el usuario: se muestra pero no se
 * edita (el servicio ignora lo que llegue en ese campo).
 */
public class DatosClienteForm {

    private String email;

    @NotBlank(message = "Ingresa tu nombre.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String nombre;

    @NotBlank(message = "Ingresa tu apellido.")
    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String apellido;

    @Pattern(regexp = "^$|^(\\+51 ?)?9\\d{8}$", message = "Ingresa un celular de 9 dígitos que empiece con 9.")
    private String telefono;

    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String direccion;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
