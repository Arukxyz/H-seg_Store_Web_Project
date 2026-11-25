package pe.edu.utp.hoseg.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.hoseg.web.config.AdministradorAutenticado;
import pe.edu.utp.hoseg.web.dto.ContenidoForm;
import pe.edu.utp.hoseg.web.dto.ItemCartaForm;
import pe.edu.utp.hoseg.web.dto.LocalCafeForm;
import pe.edu.utp.hoseg.web.service.CafeService;
import pe.edu.utp.hoseg.web.service.CampoInvalidoException;
import pe.edu.utp.hoseg.web.service.ContenidoService;

/** Höség Café y contenido editable en el panel (bloque 4: CU-18, CU-19). */
@Controller
public class AdminContenidoController {

    private final CafeService cafeService;
    private final ContenidoService contenidoService;

    public AdminContenidoController(CafeService cafeService, ContenidoService contenidoService) {
        this.cafeService = cafeService;
        this.contenidoService = contenidoService;
    }

    /**
     * Pestañas (?pestana=carta|local|arboles). ?item=5 o ?local=2 abren ese
     * registro en el formulario; sin ellos, el formulario es para uno nuevo.
     */
    @GetMapping("/admin/cafe")
    public String cafe(@RequestParam(defaultValue = "carta") String pestana,
                       @RequestParam(required = false) Integer item,
                       @RequestParam(required = false) Integer local,
                       Model model) {
        model.addAttribute("itemForm", item == null ? new ItemCartaForm() : cafeService.itemParaEditar(item));
        model.addAttribute("localForm", local == null ? new LocalCafeForm() : cafeService.localParaEditar(local));
        prepararCafe(pestana, model);
        return "admin/cafe";
    }

    @PostMapping("/admin/cafe/carta")
    public String guardarItem(@Valid @ModelAttribute("itemForm") ItemCartaForm form, BindingResult errores,
                              Model model, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("localForm", new LocalCafeForm());
            prepararCafe("carta", model);
            return "admin/cafe";
        }
        cafeService.guardarItem(form);
        flash.addFlashAttribute("exito", "Carta actualizada.");
        return "redirect:/admin/cafe?pestana=carta";
    }

    /** Desactivar es la baja lógica; activar lo vuelve a mostrar en /cafe. */
    @PostMapping("/admin/cafe/carta/{id}/activo")
    public String activarItem(@PathVariable Integer id, @RequestParam boolean activo) {
        cafeService.cambiarActivoItem(id, activo);
        return "redirect:/admin/cafe?pestana=carta";
    }

    @PostMapping("/admin/cafe/locales")
    public String guardarLocal(@Valid @ModelAttribute("localForm") LocalCafeForm form, BindingResult errores,
                               Model model, RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("itemForm", new ItemCartaForm());
            prepararCafe("local", model);
            return "admin/cafe";
        }
        cafeService.guardarLocal(form);
        flash.addFlashAttribute("exito", "Local guardado.");
        return "redirect:/admin/cafe?pestana=local";
    }

    @PostMapping("/admin/cafe/arboles")
    public String arboles(@RequestParam long arboles, @AuthenticationPrincipal AdministradorAutenticado admin,
                          RedirectAttributes flash) {
        try {
            cafeService.actualizarArboles(arboles, admin.id());
            flash.addFlashAttribute("exito", "Cifra publicada.");
        } catch (CampoInvalidoException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/cafe?pestana=arboles";
    }

    /** ?clave=home.banner abre ese bloque en el editor. */
    @GetMapping("/admin/contenido")
    public String contenido(@RequestParam(required = false) String clave, Model model) {
        model.addAttribute("bloques", contenidoService.todos());
        model.addAttribute("clave", clave);
        if (clave != null) {
            model.addAttribute("contenidoForm", contenidoService.paraEditar(clave));
        }
        return "admin/contenido";
    }

    @PostMapping("/admin/contenido/{clave}")
    public String guardarContenido(@PathVariable String clave,
                                   @Valid @ModelAttribute("contenidoForm") ContenidoForm form,
                                   BindingResult errores,
                                   @AuthenticationPrincipal AdministradorAutenticado admin,
                                   Model model,
                                   RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("bloques", contenidoService.todos());
            model.addAttribute("clave", clave);
            return "admin/contenido";
        }
        contenidoService.actualizar(clave, form, admin.id());
        flash.addFlashAttribute("exito", "Contenido publicado.");
        return "redirect:/admin/contenido?clave=" + clave;
    }

    private void prepararCafe(String pestana, Model model) {
        model.addAttribute("pestana", pestana);
        model.addAttribute("carta", cafeService.cartaParaEditar());
        model.addAttribute("locales", cafeService.localesParaEditar());
        model.addAttribute("arboles", cafeService.arbolesSembrados());
    }
}
