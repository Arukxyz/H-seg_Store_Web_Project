package pe.edu.utp.hoseg.web.dto;

/**
 * "Tu impacto" del cliente (la fidelidad del portal, sin puntos): lo que
 * generaron sus compras pagadas, cuánto ya llegó y cuánto espera el pago.
 *
 * @param generado       donaciones de pedidos pagados (pendientes, asignadas y entregadas)
 * @param entregado      las que ya llegaron a una comunidad
 * @param enEsperaDePago las de pedidos todavía PENDIENTE
 */
public record ImpactoCliente(ImpactoCompra generado, ImpactoCompra entregado, ImpactoCompra enEsperaDePago) {
}
