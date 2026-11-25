package pe.edu.utp.hoseg.web.dto;

/** Orden de la grilla de la tienda (/tienda?orden=). */
public enum OrdenCatalogo {
    DESTACADOS("Destacados"),
    PRECIO_ASC("Precio: menor a mayor"),
    PRECIO_DESC("Precio: mayor a menor"),
    NOMBRE("Nombre");

    private final String etiqueta;

    OrdenCatalogo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
