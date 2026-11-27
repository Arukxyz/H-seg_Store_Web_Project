package pe.edu.utp.hoseg.web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.hoseg.web.config.AdministradorAutenticado;
import pe.edu.utp.hoseg.web.service.GestorImpactoService;
import pe.edu.utp.hoseg.web.service.NegocioException;

import java.util.List;

@Controller
@RequestMapping("/gestion-impacto")
public class GestorImpactoController {

    private final GestorImpactoService gestorImpactoService;

    public GestorImpactoController(GestorImpactoService gestorImpactoService) {
        this.gestorImpactoService = gestorImpactoService;
    }

    @GetMapping("/donaciones")
    public String donaciones(Model model) {
        model.addAttribute("pendientes", gestorImpactoService.donacionesPendientes());
        model.addAttribute("lotes", gestorImpactoService.lotes());
        return "impacto/donaciones";
    }

    @GetMapping("/lotes")
    public String lotes(Model model) {
        model.addAttribute("comunidades", gestorImpactoService.comunidades());
        model.addAttribute("ongs", gestorImpactoService.ongs());
        model.addAttribute("lotes", gestorImpactoService.lotes());
        return "impacto/lotes";
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        var lotes = gestorImpactoService.lotes();
        var pendientes = gestorImpactoService.donacionesPendientes();
        long lotesEntregados = lotes.stream().filter(l -> "ENTREGADO".equals(l.getEstado().name())).count();
        long lotesEnRuta = lotes.stream().filter(l -> "EN_RUTA".equals(l.getEstado().name())).count();
        int totalAbrigosPend = pendientes.stream()
                .filter(d -> "ABRIGO".equals(d.getTipo().name())).mapToInt(d -> d.getCantidad()).sum();
        int totalArbolesPend = pendientes.stream()
                .filter(d -> "ARBOL".equals(d.getTipo().name())).mapToInt(d -> d.getCantidad()).sum();

        model.addAttribute("totalLotes", lotes.size());
        model.addAttribute("lotesEntregados", lotesEntregados);
        model.addAttribute("lotesEnRuta", lotesEnRuta);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("totalAbrigosPend", totalAbrigosPend);
        model.addAttribute("totalArbolesPend", totalArbolesPend);
        return "impacto/reportes";
    }

    @PostMapping("/lotes")
    public String crearLote(@AuthenticationPrincipal AdministradorAutenticado usuario,
                            @RequestParam Integer idComunidad,
                            @RequestParam Integer idOng,
                            @RequestParam(required = false) String observaciones,
                            RedirectAttributes flash) {
        try {
            var lote = gestorImpactoService.crearLote(idComunidad, idOng, usuario.id(), observaciones);
            flash.addFlashAttribute("exito", "Lote " + lote.getCodigoLote() + " creado. Ya puedes asignarle donaciones pendientes.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion-impacto/lotes";
    }

    @PostMapping("/lotes/{id}/asignar")
    public String asignar(@PathVariable Integer id,
                          @RequestParam(name = "idsDonacion", required = false) List<Integer> idsDonacion,
                          RedirectAttributes flash) {
        try {
            int n = gestorImpactoService.asignarDonaciones(id, idsDonacion);
            flash.addFlashAttribute("exito", "Se asignaron " + n + " donación(es) al lote.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion-impacto/donaciones";
    }

    @PostMapping("/lotes/{id}/despachar")
    public String despachar(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            gestorImpactoService.despacharLote(id);
            flash.addFlashAttribute("exito", "Lote marcado como En Ruta hacia la comunidad.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion-impacto/lotes";
    }

    @PostMapping("/lotes/{id}/entregar")
    public String entregar(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            gestorImpactoService.entregarLote(id);
            flash.addFlashAttribute("exito", "Lote entregado. Las donaciones y el stock comprometido se actualizaron.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gestion-impacto/lotes";
    }

    @GetMapping(value = "/lotes.csv", produces = "text/csv;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> exportarCsv() {
        StringBuilder sb = new StringBuilder("\uFEFFCodigo Lote;Comunidad;ONG Aliada;Estado;Fecha Creacion;Observaciones\n");
        for (var l : gestorImpactoService.lotes()) {
            sb.append(l.getCodigoLote()).append(';')
              .append(l.getComunidad().getNombre()).append(';')
              .append(l.getOng().getNombre()).append(';')
              .append(l.getEstado().name()).append(';')
              .append(l.getFechaCreacion().toLocalDate()).append(';')
              .append(l.getObservaciones() != null ? l.getObservaciones().replace(';', ',') : "")
              .append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte-Impacto-Hoseg.csv\"")
                .body(sb.toString());
    }
}
