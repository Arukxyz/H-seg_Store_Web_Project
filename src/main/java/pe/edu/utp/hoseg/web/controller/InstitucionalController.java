package pe.edu.utp.hoseg.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.edu.utp.hoseg.web.service.CafeService;
import pe.edu.utp.hoseg.web.service.ContenidoService;
import pe.edu.utp.hoseg.web.service.ImpactoService;

/** Höség Café, Nosotros y páginas legales (bloque 3: CU-05, CU-06). Solo lectura. */
@Controller
public class InstitucionalController {

    private final CafeService cafeService;
    private final ContenidoService contenidoService;
    private final ImpactoService impactoService;

    public InstitucionalController(CafeService cafeService, ContenidoService contenidoService,
                                   ImpactoService impactoService) {
        this.cafeService = cafeService;
        this.contenidoService = contenidoService;
        this.impactoService = impactoService;
    }

    @GetMapping("/cafe")
    public String cafe(Model model) {
        model.addAttribute("arboles", cafeService.arbolesSembrados());
        model.addAttribute("carta", cafeService.carta());
        model.addAttribute("locales", cafeService.locales());
        model.addAttribute("cafesEnGrano", cafeService.cafesEnGrano());
        return "institucional/cafe";
    }

    @GetMapping("/nosotros")
    public String nosotros(Model model) {
        model.addAttribute("bloques", contenidoService.bloques("nosotros.historia"));
        model.addAttribute("impacto", impactoService.resumen());
        model.addAttribute("comunidades", impactoService.nombresComunidades());
        model.addAttribute("ongs", impactoService.nombresOng());
        return "institucional/nosotros";
    }

    @GetMapping("/terminos")
    public String terminos(Model model) {
        return legal("legal.terminos", "Términos y condiciones", model);
    }

    @GetMapping("/politica-rsu")
    public String politicaRsu(Model model) {
        return legal("legal.politica-rsu", "Política de responsabilidad social", model);
    }

    /** Una sola plantilla para ambas páginas legales (3.4). */
    private String legal(String clave, String tituloPorDefecto, Model model) {
        model.addAttribute("bloque", contenidoService.bloque(clave).orElse(null));
        model.addAttribute("tituloPorDefecto", tituloPorDefecto);
        return "institucional/legal";
    }
}
