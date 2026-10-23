package pe.edu.utp.hoseg.web.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.hoseg.web.config.AdministradorAutenticado;
import pe.edu.utp.hoseg.web.model.EstadoResena;
import pe.edu.utp.hoseg.web.service.CatalogoService;
import pe.edu.utp.hoseg.web.service.NegocioException;
import pe.edu.utp.hoseg.web.service.PanelAdminService;
import pe.edu.utp.hoseg.web.service.ResenaService;

import java.time.LocalDate;

/**
 * Panel de administración (bloque 4: CU-16, CU-17, CU-21, CU-23). El POST de
 * /admin/login lo procesa Spring Security. Solo consulta pedidos: confirmar
 * o anular se hace en el escritorio.
 */
@Controller
public class AdminController {

    private final PanelAdminService panelAdminService;
    private final CatalogoService catalogoService;
    private final ResenaService resenaService;

    public AdminController(PanelAdminService panelAdminService, CatalogoService catalogoService,
                           ResenaService resenaService) {
        this.panelAdminService = panelAdminService;
        this.catalogoService = catalogoService;
        this.resenaService = resenaService;
    }

    /** Variantes: ?error, ?bloqueado=5, ?inactivo, ?sinPermiso, ?salir. */
    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/admin")
    public String inicio() {
        return "redirect:/admin/panel";
    }

    /** ?desde=2026-10-01&hasta=2026-10-23; por defecto, del 1 del mes a hoy (hora de Lima). */
    @GetMapping("/admin/panel")
    public String panel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                        Model model) {
        LocalDate hoy = LocalDate.now(PanelAdminService.LIMA);
        LocalDate fin = hasta == null ? hoy : hasta;
        LocalDate inicio = desde == null ? fin.withDayOfMonth(1) : desde;
        if (inicio.isAfter(fin)) {
            LocalDate cambio = inicio;
            inicio = fin;
            fin = cambio;
        }
        model.addAttribute("desde", inicio);
        model.addAttribute("hasta", fin);
        model.addAttribute("indicadores", panelAdminService.indicadores(inicio, fin));
        model.addAttribute("pedidos", panelAdminService.pedidos(inicio, fin));
        return "admin/panel";
    }

    @GetMapping("/admin/catalogo")
    public String catalogo(Model model) {
        model.addAttribute("productos", catalogoService.paraAdministrar());
        model.addAttribute("destacados", catalogoService.modelosDestacados());
        model.addAttribute("maxDestacados", CatalogoService.MAX_DESTACADOS);
        model.addAttribute("vistaPrevia", catalogoService.destacados(CatalogoService.MAX_DESTACADOS));
        return "admin/catalogo";
    }

    @PostMapping("/admin/catalogo/{codigo}/visible")
    public String visible(@PathVariable String codigo, @RequestParam boolean visible) {
        catalogoService.cambiarVisibilidad(codigo, visible);
        return "redirect:/admin/catalogo";
    }

    @PostMapping("/admin/catalogo/{codigo}/destacado")
    public String destacado(@PathVariable String codigo, @RequestParam boolean destacado, RedirectAttributes flash) {
        try {
            catalogoService.cambiarDestacado(codigo, destacado);
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/catalogo";
    }

    /** ?estado=PUBLICADA|OCULTA; sin estado, todas. */
    @GetMapping("/admin/resenas")
    public String resenas(@RequestParam(required = false) EstadoResena estado, Model model) {
        model.addAttribute("estado", estado);
        model.addAttribute("estados", EstadoResena.values());
        model.addAttribute("resenas", resenaService.paraModerar(estado));
        return "admin/resenas";
    }

    @PostMapping("/admin/resenas/{id}/ocultar")
    public String ocultar(@PathVariable Integer id, @AuthenticationPrincipal AdministradorAutenticado admin) {
        resenaService.ocultar(id, admin.id());
        return "redirect:/admin/resenas";
    }

    @PostMapping("/admin/resenas/{id}/publicar")
    public String publicar(@PathVariable Integer id, @AuthenticationPrincipal AdministradorAutenticado admin) {
        resenaService.republicar(id, admin.id());
        return "redirect:/admin/resenas";
    }
}
