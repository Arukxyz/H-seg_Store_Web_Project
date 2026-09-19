package pe.edu.utp.hoseg.web.dto;

/**
 * Cifras de la sección ImpactCounters. Misma semántica que el reporte del
 * escritorio: se cuenta lo ENTREGADO, no lo prometido.
 */
public record ResumenImpacto(
        long abrigosEntregados,
        long arbolesPlantados,
        long comunidadesAlcanzadas,
        long pedidosConImpacto) {
}
