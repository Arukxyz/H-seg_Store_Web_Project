package pe.edu.utp.hoseg.web.model;

/** envio.tipo: el recojo no tiene costo ni sale a ruta; el delivery exige dirección. */
public enum TipoEnvio {
    RECOJO_TIENDA("Recojo en tienda"),
    DELIVERY("Delivery");

    private final String etiqueta;

    TipoEnvio(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
