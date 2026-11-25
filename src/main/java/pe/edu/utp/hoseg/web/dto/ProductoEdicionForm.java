package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;

import java.math.BigDecimal;

/**
 * Edición de un producto desde Logística → Inventario (modal "Editar").
 * No incluye el código (es la clave y lo referencian ventas, donaciones y movimientos)
 * ni el stock, que se mueve con "reponer" y con las órdenes a talleres.
 */
public class ProductoEdicionForm {

    @NotBlank(message = "El nombre del producto es obligatorio.")
    @Size(max = 150, message = "El nombre admite como máximo 150 caracteres.")
    private String nombre;

    @NotBlank(message = "Elige una categoría.")
    @Size(max = 60, message = "La categoría admite como máximo 60 caracteres.")
    private String categoria;

    @Size(max = 80, message = "La colección admite como máximo 80 caracteres.")
    private String coleccion;

    @Size(max = 10, message = "La talla admite como máximo 10 caracteres.")
    private String talla;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero.")
    @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 2 decimales.")
    private BigDecimal precio;

    @NotNull(message = "El stock mínimo es obligatorio.")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo.")
    @Max(value = 10000, message = "El stock mínimo es demasiado alto.")
    private Integer stockMinimo;

    /** Nulo = sin compromiso social. */
    private TipoCompromiso tipoCompromiso;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getColeccion() { return coleccion; }
    public void setColeccion(String coleccion) { this.coleccion = coleccion; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public TipoCompromiso getTipoCompromiso() { return tipoCompromiso; }
    public void setTipoCompromiso(TipoCompromiso tipoCompromiso) { this.tipoCompromiso = tipoCompromiso; }
}
