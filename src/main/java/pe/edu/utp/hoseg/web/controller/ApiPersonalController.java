package pe.edu.utp.hoseg.web.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.hoseg.web.dto.IndicadoresPanel;
import pe.edu.utp.hoseg.web.dto.PedidoLogisticaVista;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.service.GestorImpactoService;
import pe.edu.utp.hoseg.web.service.LogisticaService;
import pe.edu.utp.hoseg.web.service.PanelAdminService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Endpoints protegidos con JWT (ver ApiSecurityConfig). Solo consultan:
 * reutilizan
 * los mismos servicios de los paneles Thymeleaf, sin duplicar lógica.
 *
 * /api/personal/yo cualquier rol con token válido
 * /api/admin/resumen solo ADMINISTRADOR
 * /api/logistica/pedidos solo LOGISTICA
 * /api/impacto/lotes solo GESTOR_IMPACTO
 */
@RestController
@RequestMapping("/api")
public class ApiPersonalController {

    private final PanelAdminService panelAdminService;
    private final LogisticaService logisticaService;
    private final GestorImpactoService gestorImpactoService;

    public ApiPersonalController(PanelAdminService panelAdminService,
            LogisticaService logisticaService,
            GestorImpactoService gestorImpactoService) {
        this.panelAdminService = panelAdminService;
        this.logisticaService = logisticaService;
        this.gestorImpactoService = gestorImpactoService;
    }

    /**
     * Muestra lo que viaja dentro del token (sub, roles, emisión y vencimiento).
     */
    @GetMapping("/personal/yo")
    public Map<String, Object> yo(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "usuario", jwt.getSubject(),
                "nombre", String.valueOf(jwt.getClaimAsString("nombre")),
                "roles", jwt.getClaimAsStringList("roles"),
                "emitidoEn", jwt.getIssuedAt(),
                "venceEn", jwt.getExpiresAt());
    }

    /** Tarjetas del panel. Sin fechas: últimos 30 días. */
    @GetMapping("/admin/resumen")
    public IndicadoresPanel resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now(PanelAdminService.LIMA);
        LocalDate inicio = desde != null ? desde : fin.minusDays(30);
        return panelAdminService.indicadores(inicio, fin);
    }

    /**
     * Pedidos pagados por atender (sin email, DNI ni montos). tipo: DELIVERY o
     * RECOJO_TIENDA.
     */
    @GetMapping("/logistica/pedidos")
    public List<PedidoLogisticaVista> pedidos(@RequestParam(defaultValue = "DELIVERY") TipoEnvio tipo) {
        return logisticaService.pedidosPagadosPorTipo(tipo);
    }

    public record LoteResumen(Integer id, String codigo, String estado, String comunidad, String ong,
            OffsetDateTime creadoEn) {
    }

    /** Lotes de donación con su comunidad y ONG destino. */
    @GetMapping("/impacto/lotes")
    public List<LoteResumen> lotes() {
        return gestorImpactoService.lotes().stream()
                .map(l -> new LoteResumen(l.getId(), l.getCodigoLote(), l.getEstado().name(),
                        l.getComunidad().getNombre(), l.getOng().getNombre(), l.getFechaCreacion()))
                .toList();
    }
}
