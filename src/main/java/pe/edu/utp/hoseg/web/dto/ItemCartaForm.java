package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Ítem de la carta en el panel (4.4, pestaña Carta). id null = ítem nuevo. */
public class ItemCartaForm {

    private Integer id;

    @NotBlank(message = "Ingresa el nombre.")
    @Size(max = 120, message = "Máximo 120 caracteres.")
    private String nombre;

    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String descripcion;

    @NotBlank(message = "Indica la categoría (p. ej. Bebidas calientes).")
    @Size(max = 40, message = "Máximo 40 caracteres.")
    private String categoria;

    @NotNull(message = "Ingresa el precio.")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "Precio inválido.")
    private BigDecimal precio;

    private boolean disponible = true;

    @Min(value = 0, message = "El orden no puede ser negativo.")
    private int orden;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
}
