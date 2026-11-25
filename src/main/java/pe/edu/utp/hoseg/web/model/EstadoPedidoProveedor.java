package pe.edu.utp.hoseg.web.model;

public enum EstadoPedidoProveedor {
    SOLICITADO("Solicitado"),
    EN_PROCESO("En proceso"),
    RECIBIDO("Recibido"),
    ANULADO("Anulado");

    private final String etiqueta;

    EstadoPedidoProveedor(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
