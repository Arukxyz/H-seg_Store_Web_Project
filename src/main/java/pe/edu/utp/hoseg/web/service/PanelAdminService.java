package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.IndicadoresPanel;
import pe.edu.utp.hoseg.web.dto.PedidoAdmin;
import pe.edu.utp.hoseg.web.model.Envio;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.OrigenVenta;
import pe.edu.utp.hoseg.web.model.Venta;
import pe.edu.utp.hoseg.web.repository.DonacionRepository;
import pe.edu.utp.hoseg.web.repository.EnvioRepository;
import pe.edu.utp.hoseg.web.repository.VentaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Panel del administrador web (4.2, CU-21). Solo consulta: confirmar o anular
 * pedidos y mover stock es del escritorio. Las fechas se interpretan en hora
 * de Lima; el periodo es [desde, hasta] por días completos.
 */
@Service
@Transactional(readOnly = true)
public class PanelAdminService {

    public static final ZoneId LIMA = ZoneId.of("America/Lima");

    private final VentaRepository ventaRepository;
    private final EnvioRepository envioRepository;
    private final DonacionRepository donacionRepository;
    private final ContactoService contactoService;
    private final ReclamacionService reclamacionService;

    public PanelAdminService(VentaRepository ventaRepository,
                             EnvioRepository envioRepository,
                             DonacionRepository donacionRepository,
                             ContactoService contactoService,
                             ReclamacionService reclamacionService) {
        this.ventaRepository = ventaRepository;
        this.envioRepository = envioRepository;
        this.donacionRepository = donacionRepository;
        this.contactoService = contactoService;
        this.reclamacionService = reclamacionService;
    }

    /** Tarjetas del panel para el periodo (desde y hasta incluidos). */
    public IndicadoresPanel indicadores(LocalDate desde, LocalDate hasta) {
        OffsetDateTime inicio = inicioDe(desde);
        OffsetDateTime fin = inicioDe(hasta.plusDays(1));
        BigDecimal vendido = ventaRepository.sumarTotal(OrigenVenta.WEB, EstadoVenta.PAGADO, inicio, fin);
        return new IndicadoresPanel(
                ventaRepository.countByOrigenAndFechaGreaterThanEqual(OrigenVenta.WEB, inicioDe(LocalDate.now(LIMA))),
                ventaRepository.countByOrigenAndEstado(OrigenVenta.WEB, EstadoVenta.PENDIENTE),
                vendido,
                ImpactoService.sumar(donacionRepository.delPeriodo(inicio, fin), u -> true),
                contactoService.nuevos(),
                reclamacionService.sinResponder());
    }

    /** Tabla de pedidos web del periodo, del más reciente al más antiguo. */
    public List<PedidoAdmin> pedidos(LocalDate desde, LocalDate hasta) {
        List<Venta> ventas = ventaRepository.delPeriodo(OrigenVenta.WEB, inicioDe(desde), inicioDe(hasta.plusDays(1)));
        if (ventas.isEmpty()) {
            return List.of();
        }
        Map<Integer, Envio> envios = envioRepository.findByVentaIdIn(ventas.stream().map(Venta::getId).toList()).stream()
                .collect(Collectors.toMap(e -> e.getVenta().getId(), Function.identity()));
        return ventas.stream().map(v -> {
            Envio e = envios.get(v.getId());
            return new PedidoAdmin(v.getCodigoComprobante(),
                    v.getCliente() == null ? "—" : v.getCliente().getNombreCompleto(),
                    v.getFecha(), v.getTotal(), e == null ? BigDecimal.ZERO : e.getCosto(), v.getEstado(),
                    e == null ? null : e.getTipo(), e == null ? null : e.getEstado());
        }).toList();
    }

    private static OffsetDateTime inicioDe(LocalDate dia) {
        return dia.atStartOfDay(LIMA).toOffsetDateTime();
    }
}
