package pe.edu.utp.hoseg.web.dto;

import pe.edu.utp.hoseg.web.model.TipoCompromiso;

/**
 * Filtros de la tienda, tal como llegan en la URL:
 * {@code /tienda?q=casaca&categoria=Casacas&coleccion=Älpafill&impacto=ABRIGO&disponibles=true&orden=PRECIO_ASC}.
 * Todos son opcionales; Spring los arma desde los parámetros con el mismo nombre.
 */
public record FiltroCatalogo(
        String q,
        String categoria,
        String coleccion,
        TipoCompromiso impacto,
        Boolean disponibles,
        OrdenCatalogo orden) {

    public static FiltroCatalogo ninguno() {
        return new FiltroCatalogo(null, null, null, null, null, null);
    }

    /** "Solo disponibles": Boolean porque el parámetro suele no venir en la URL. */
    public boolean soloDisponibles() {
        return Boolean.TRUE.equals(disponibles);
    }

    /** true si hay algún filtro aplicado (para mostrar "Limpiar filtros"). */
    public boolean hayFiltros() {
        return tieneTexto(q) || tieneTexto(categoria) || tieneTexto(coleccion) || impacto != null || soloDisponibles();
    }

    public OrdenCatalogo ordenEfectivo() {
        return orden == null ? OrdenCatalogo.DESTACADOS : orden;
    }

    static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
