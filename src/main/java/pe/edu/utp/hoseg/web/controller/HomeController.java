package pe.edu.utp.hoseg.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.edu.utp.hoseg.web.service.CatalogoService;
import pe.edu.utp.hoseg.web.service.ImpactoService;

/** Página principal: presenta la marca, los destacados y el impacto; no vende. */
@Controller
public class HomeController {

    /** Tarjetas en el carrusel: dos vueltas completas a 4 visibles en desktop. */
    private static final int DESTACADOS = 8;

    private final CatalogoService catalogoService;
    private final ImpactoService impactoService;

    public HomeController(CatalogoService catalogoService, ImpactoService impactoService) {
        this.catalogoService = catalogoService;
        this.impactoService = impactoService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("paginaActiva", "inicio");
        model.addAttribute("destacados", catalogoService.destacados(DESTACADOS));
        model.addAttribute("impacto", impactoService.resumen());
        model.addAttribute("comunidades", impactoService.nombresComunidades());
        return "index";
    }
}
