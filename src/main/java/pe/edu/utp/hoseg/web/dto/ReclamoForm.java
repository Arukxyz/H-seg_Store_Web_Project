package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.edu.utp.hoseg.web.model.TipoBien;
import pe.edu.utp.hoseg.web.model.TipoDocumento;
import pe.edu.utp.hoseg.web.model.TipoReclamo;

import java.math.BigDecimal;

/**
 * Hoja de reclamación virtual (3.6, CU-24). Los campos son los que exige el
 * Libro de Reclamaciones; el código de pedido es opcional.
 */
public class ReclamoForm {

    @NotNull(message = "Elige si es un reclamo o una queja.")
    private TipoReclamo tipo;

    @NotBlank(message = "Ingresa tu nombre completo.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String nombre;

    @NotNull(message = "Elige el tipo de documento.")
    private TipoDocumento tipoDoc;

    @NotBlank(message = "Ingresa tu número de documento.")
    @Size(max = 20, message = "Máximo 20 caracteres.")
    private String numDoc;

    @NotBlank(message = "Ingresa tu domicilio.")
    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String domicilio;

    @NotBlank(message = "Ingresa tu email.")
    @Email(message = "Revisa el formato del email.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String email;

    @Pattern(regexp = "^$|^\\+?[0-9 ]{9,16}$", message = "Revisa el teléfono.")
    private String telefono;

    @NotNull(message = "Indica si es sobre un producto o un servicio.")
    private TipoBien tipoBien;

    @DecimalMin(value = "0.00", message = "El monto no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "Monto inválido.")
    private BigDecimal montoReclamado;

    @NotBlank(message = "Describe el producto o servicio.")
    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String descripcionBien;

    @NotBlank(message = "Cuéntanos qué pasó.")
    @Size(max = 4000, message = "Máximo 4000 caracteres.")
    private String detalle;

    @NotBlank(message = "Indica qué solicitas.")
    @Size(max = 2000, message = "Máximo 2000 caracteres.")
    private String pedidoConsumidor;

    @Size(max = 30, message = "Máximo 30 caracteres.")
    private String codigoComprobante;

    @AssertTrue(message = "El número no tiene el formato de ese tipo de documento.")
    public boolean isDocumentoValido() {
        return tipoDoc == null || numDoc == null || numDoc.isBlank() || tipoDoc.admite(numDoc.trim());
    }

    public TipoReclamo getTipo() { return tipo; }
    public void setTipo(TipoReclamo tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public TipoDocumento getTipoDoc() { return tipoDoc; }
    public void setTipoDoc(TipoDocumento tipoDoc) { this.tipoDoc = tipoDoc; }
    public String getNumDoc() { return numDoc; }
    public void setNumDoc(String numDoc) { this.numDoc = numDoc; }
    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public TipoBien getTipoBien() { return tipoBien; }
    public void setTipoBien(TipoBien tipoBien) { this.tipoBien = tipoBien; }
    public BigDecimal getMontoReclamado() { return montoReclamado; }
    public void setMontoReclamado(BigDecimal montoReclamado) { this.montoReclamado = montoReclamado; }
    public String getDescripcionBien() { return descripcionBien; }
    public void setDescripcionBien(String descripcionBien) { this.descripcionBien = descripcionBien; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getPedidoConsumidor() { return pedidoConsumidor; }
    public void setPedidoConsumidor(String pedidoConsumidor) { this.pedidoConsumidor = pedidoConsumidor; }
    public String getCodigoComprobante() { return codigoComprobante; }
    public void setCodigoComprobante(String codigoComprobante) { this.codigoComprobante = codigoComprobante; }
}
