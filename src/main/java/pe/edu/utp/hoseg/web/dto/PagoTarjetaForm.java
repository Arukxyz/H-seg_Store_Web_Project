package pe.edu.utp.hoseg.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PagoTarjetaForm {

    @NotBlank(message = "Ingresa el número de tarjeta.")
    @Pattern(regexp = "^[0-9]{16}$", message = "La tarjeta debe tener 16 dígitos sin espacios.")
    private String numeroTarjeta;

    @NotBlank(message = "Ingresa la fecha de vencimiento.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Usa el formato MM/AA.")
    private String vencimiento;

    @NotBlank(message = "Ingresa el código CVV.")
    @Pattern(regexp = "^[0-9]{3}$", message = "El CVV debe tener 3 dígitos.")
    private String cvv;

    @NotBlank(message = "Ingresa el nombre del titular.")
    private String titular;

    @NotBlank(message = "Ingresa el DNI del titular.")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener 8 dígitos.")
    private String dniTitular;

    /** Valida el número de tarjeta con el algoritmo de Luhn (estándar bancario). */
    public boolean esLuhnValido() {
        if (numeroTarjeta == null || !numeroTarjeta.matches("^[0-9]{16}$")) {
            return false;
        }
        int suma = 0;
        boolean alternar = false;
        for (int i = numeroTarjeta.length() - 1; i >= 0; i--) {
            int n = numeroTarjeta.charAt(i) - '0';
            if (alternar) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            suma += n;
            alternar = !alternar;
        }
        return (suma % 10 == 0);
    }

    /** Simulación: si termina en 0000 rechaza el pago. */
    public boolean simulaRechazo() {
        return numeroTarjeta != null && numeroTarjeta.endsWith("0000");
    }

    /** Devuelve solo los últimos 4 dígitos para guardar referencia segura: "TARJETA ****4512". */
    public String referenciaSegura() {
        if (numeroTarjeta == null || numeroTarjeta.length() < 4) return "TARJETA";
        return "TARJETA ****" + numeroTarjeta.substring(numeroTarjeta.length() - 4);
    }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta == null ? null : numeroTarjeta.replaceAll("\\s+", "");
    }
    public String getVencimiento() { return vencimiento; }
    public void setVencimiento(String vencimiento) { this.vencimiento = vencimiento; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }
    public String getDniTitular() { return dniTitular; }
    public void setDniTitular(String dniTitular) { this.dniTitular = dniTitular; }
}
