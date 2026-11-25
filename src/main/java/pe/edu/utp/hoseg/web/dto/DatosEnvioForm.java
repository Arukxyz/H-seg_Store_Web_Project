package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.edu.utp.hoseg.web.model.TipoEnvio;

/**
 * Entrega elegida en "Confirmar compra" (1.5). Llega precargado con los datos
 * de la cuenta y el cliente puede editarlo; no cambia su cuenta, solo el envío.
 */
public class DatosEnvioForm {

    @NotNull(message = "Elige cómo quieres recibir tu pedido.")
    private TipoEnvio tipo = TipoEnvio.RECOJO_TIENDA;

    @NotBlank(message = "Indica quién recibe el pedido.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String nombreReceptor;

    /** Móvil peruano: 9 dígitos que empiezan con 9, con +51 opcional. */
    @Pattern(regexp = "^$|^(\\+51 ?)?9\\d{8}$", message = "Ingresa un celular de 9 dígitos que empiece con 9.")
    private String telefonoReceptor;

    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String direccion;

    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String referencia;

    @Size(max = 80, message = "Máximo 80 caracteres.")
    private String distrito;

    @Size(max = 80, message = "Máximo 80 caracteres.")
    private String provincia;

    @Size(max = 80, message = "Máximo 80 caracteres.")
    private String departamento;

    /** envio_delivery_check: el delivery necesita dirección y distrito. */
    @AssertTrue(message = "Para delivery indica la dirección y el distrito.")
    public boolean isDireccionCompleta() {
        return tipo != TipoEnvio.DELIVERY || (tieneTexto(direccion) && tieneTexto(distrito));
    }

    public boolean esDelivery() {
        return tipo == TipoEnvio.DELIVERY;
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    public TipoEnvio getTipo() { return tipo; }
    public void setTipo(TipoEnvio tipo) { this.tipo = tipo; }
    public String getNombreReceptor() { return nombreReceptor; }
    public void setNombreReceptor(String nombreReceptor) { this.nombreReceptor = nombreReceptor; }
    public String getTelefonoReceptor() { return telefonoReceptor; }
    public void setTelefonoReceptor(String telefonoReceptor) { this.telefonoReceptor = telefonoReceptor; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
}
