package pe.edu.utp.hoseg.web.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.service.CatalogoService;

/**
 * Muestra templates/ejemplo-pagina.html en /ejemplo: la plantilla que se
 * copia para crear páginas nuevas (ver docs/guia-plantillas.md). Solo existe
 * con el perfil dev; en el empaquetado no se registra.
 */
@Controller
@Profile("dev")
public class EjemploController {

    private final CatalogoService catalogoService;

    public EjemploController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/ejemplo")
    public String ejemplo(Model model) {
        model.addAttribute("paginaActiva", "tienda");
        model.addAttribute("tarjetas", catalogoService.destacados(4));
        model.addAttribute("abrigo", TipoCompromiso.ABRIGO);
        model.addAttribute("arbol", TipoCompromiso.ARBOL);
        return "ejemplo-pagina";
    }
}
