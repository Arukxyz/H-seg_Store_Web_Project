package pe.edu.utp.hoseg.web.model;

import java.util.regex.Pattern;

/**
 * Tipos de documento de cliente.tipo_doc y libro_reclamaciones.tipo_doc, con
 * el mismo formato que exige cliente_documento_check (07_normalizacion.sql).
 */
public enum TipoDocumento {
    DNI("DNI", "^[0-9]{8}$"),
    CE("Carné de extranjería", "^[A-Za-z0-9]{9,12}$"),
    PASAPORTE("Pasaporte", "^[A-Za-z0-9]{6,12}$"),
    RUC("RUC", "^[0-9]{11}$");

    private final String etiqueta;
    private final Pattern formato;

    TipoDocumento(String etiqueta, String formato) {
        this.etiqueta = etiqueta;
        this.formato = Pattern.compile(formato);
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** true si el número tiene el formato que acepta la base para este tipo. */
    public boolean admite(String numero) {
        return numero != null && formato.matcher(numero).matches();
    }
}
