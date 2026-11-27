package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PagoBilleteraForm {

    @NotBlank(message = "Elige Yape o Plin.")
    @Pattern(regexp = "^(YAPE|PLIN)$", message = "Billetera no válida.")
    private String billetera = "YAPE";

    @NotBlank(message = "Ingresa tu número de celular.")
    @Pattern(regexp = "^9[0-9]{8}$", message = "Ingresa un celular válido de 9 dígitos que empiece con 9.")
    private String celular;

    public String getBilletera() { return billetera; }
    public void setBilletera(String billetera) { this.billetera = billetera; }
    public String getCelular() { return celular; }
    public void setCelular(String celular) { this.celular = celular; }
}
