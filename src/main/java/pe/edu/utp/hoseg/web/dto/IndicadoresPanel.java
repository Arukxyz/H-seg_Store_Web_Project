package pe.edu.utp.hoseg.web.dto;

import java.math.BigDecimal;

/**
 * Tarjetas del panel (4.2, CU-21). Solo consulta: confirmar o anular pedidos
 * se hace en el escritorio.
 *
 * @param pedidosHoy         pedidos web registrados hoy (hora de Lima)
 * @param pendientesDePago   pedidos web que esperan confirmación de pago (cualquier fecha)
 * @param montoVendido       suma de venta.total de los pedidos PAGADO del periodo (sin envío)
 * @param impactoGenerado    abrigos y árboles de los pedidos del periodo
 * @param mensajesNuevos     mensajes de contacto sin atender
 * @param reclamosSinResponder hojas del Libro de Reclamaciones sin respuesta
 */
public record IndicadoresPanel(
        long pedidosHoy,
        long pendientesDePago,
        BigDecimal montoVendido,
        ImpactoCompra impactoGenerado,
        long mensajesNuevos,
        long reclamosSinResponder) {
}
