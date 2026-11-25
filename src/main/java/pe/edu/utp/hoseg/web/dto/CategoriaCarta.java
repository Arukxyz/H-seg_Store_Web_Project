package pe.edu.utp.hoseg.web.dto;

import java.math.BigDecimal;
import java.util.List;

/** Una sección de la carta del café ("Bebidas calientes", "Postres"…) con sus ítems, en el orden del panel. */
public record CategoriaCarta(String categoria, List<Item> items) {

    /** @param disponible false = "agotado hoy" */
    public record Item(String nombre, String descripcion, BigDecimal precio, boolean disponible) {
    }
}
