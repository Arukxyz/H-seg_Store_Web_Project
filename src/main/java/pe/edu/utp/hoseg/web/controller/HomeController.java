package pe.edu.utp.hoseg.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.edu.utp.hoseg.web.service.CatalogoService;
import pe.edu.utp.hoseg.web.service.ContenidoService;
import pe.edu.utp.hoseg.web.service.ImpactoService;

/** Página principal: presenta la marca, los destacados y el impacto; no vende. */
@Controller
public class HomeController {

    /** Tarjetas en el carrusel: dos vueltas completas a 4 visibles en desktop. */
    private static final int DESTACADOS = CatalogoService.MAX_DESTACADOS;

    private final CatalogoService catalogoService;
    private final ImpactoService impactoService;
    private final ContenidoService contenidoService;

    public HomeController(CatalogoService catalogoService, ImpactoService impactoService,
                          ContenidoService contenidoService) {
        this.catalogoService = catalogoService;
        this.impactoService = impactoService;
        this.contenidoService = contenidoService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("destacados", catalogoService.destacados(DESTACADOS));
        model.addAttribute("impacto", impactoService.resumen());
        model.addAttribute("comunidades", impactoService.nombresComunidades());
        // Banner y "Quiénes somos" editables desde el panel (4.5); la plantilla tiene su texto por defecto.
        model.addAttribute("bloques", contenidoService.bloques("home.banner", "nosotros.historia"));
        return "index";
    }
}
