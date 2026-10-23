package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.TipoCompromiso;

/**
 * Fila del catálogo web en el panel (4.3, CU-17): una por talla, con el
 * stock en solo lectura. "Destacado" se aplica al modelo entero.
 */
public record ProductoAdmin(
        String codigo,
        String nombre,
        String talla,
        String categoria,
        TipoCompromiso tipoCompromiso,
        int stockComercial,
        boolean visibleWeb,
        boolean destacado) {
}
