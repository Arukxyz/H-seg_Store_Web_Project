package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Local del café en el panel (4.4, pestaña Local). id null = local nuevo. */
public class LocalCafeForm {

    private Integer id;

    @NotBlank(message = "Ingresa el nombre del local.")
    @Size(max = 120, message = "Máximo 120 caracteres.")
    private String nombre;

    @NotBlank(message = "Ingresa la dirección.")
    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String direccion;

    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String horario;

    @Pattern(regexp = "^$|^\\+?[0-9 ]{9,16}$", message = "Revisa el teléfono.")
    private String telefono;

    @Pattern(regexp = "^$|^https://.+", message = "El enlace del mapa debe empezar con https://")
    private String urlMapa;

    private boolean activo = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getUrlMapa() { return urlMapa; }
    public void setUrlMapa(String urlMapa) { this.urlMapa = urlMapa; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
