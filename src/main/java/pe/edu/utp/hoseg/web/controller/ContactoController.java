package pe.edu.utp.hoseg.web.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.hoseg.web.dto.MensajeContactoForm;
import pe.edu.utp.hoseg.web.dto.ReclamoForm;
import pe.edu.utp.hoseg.web.dto.SuscripcionForm;
import pe.edu.utp.hoseg.web.model.TipoBien;
import pe.edu.utp.hoseg.web.model.TipoDocumento;
import pe.edu.utp.hoseg.web.model.TipoReclamo;
import pe.edu.utp.hoseg.web.service.BoletinService;
import pe.edu.utp.hoseg.web.service.CampoInvalidoException;
import pe.edu.utp.hoseg.web.service.ContactoService;
import pe.edu.utp.hoseg.web.service.ReclamacionService;

import java.net.URI;

/** Formularios públicos: contacto, boletín y Libro de Reclamaciones (bloque 3: CU-08, CU-09, CU-24). */
@Controller
public class ContactoController {

    private final ContactoService contactoService;
    private final BoletinService boletinService;
    private final ReclamacionService reclamacionService;

    public ContactoController(ContactoService contactoService, BoletinService boletinService,
                              ReclamacionService reclamacionService) {
        this.contactoService = contactoService;
        this.boletinService = boletinService;
        this.reclamacionService = reclamacionService;
    }

    /** ?enviado muestra la variante "Mensaje enviado". */
    @GetMapping("/contacto")
    public String contacto(Model model) {
        model.addAttribute("mensajeForm", new MensajeContactoForm());
        return "institucional/contacto";
    }

    @PostMapping("/contacto")
    public String enviar(@Valid @ModelAttribute("mensajeForm") MensajeContactoForm form, BindingResult errores) {
        if (errores.hasErrors()) {
            return "institucional/contacto";
        }
        contactoService.registrar(form);
        return "redirect:/contacto?enviado";
    }

    /** Formulario del footer: vuelve a la página donde estaba, con el resultado en el flash "boletin". */
    @PostMapping("/boletin")
    public String suscribir(@Valid SuscripcionForm form,
                            BindingResult errores,
                            @RequestHeader(value = "Referer", required = false) String origen,
                            RedirectAttributes flash) {
        if (errores.hasErrors()) {
            flash.addFlashAttribute("boletinError", errores.getFieldError("email").getDefaultMessage());
        } else {
            flash.addFlashAttribute("boletin", boletinService.suscribir(form.getEmail()).getMensaje());
        }
        return "redirect:" + rutaLocal(origen) + "#boletin";
    }

    /** ?registrado muestra el número de hoja (flash "numeroHoja"). */
    @GetMapping("/libro-reclamaciones")
    public String libro(Model model) {
        model.addAttribute("reclamoForm", new ReclamoForm());
        prepararLibro(model);
        return "institucional/libro-reclamaciones";
    }

    @PostMapping("/libro-reclamaciones")
    public String reclamar(@Valid @ModelAttribute("reclamoForm") ReclamoForm form,
                           BindingResult errores,
                           Model model,
                           RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                flash.addFlashAttribute("numeroHoja", reclamacionService.registrar(form));
                return "redirect:/libro-reclamaciones?registrado";
            } catch (CampoInvalidoException e) {
                errores.rejectValue(e.getCampo(), "negocio", e.getMessage());
            }
        }
        prepararLibro(model);
        return "institucional/libro-reclamaciones";
    }

    private static void prepararLibro(Model model) {
        model.addAttribute("tiposReclamo", TipoReclamo.values());
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        model.addAttribute("tiposBien", TipoBien.values());
    }

    /**
     * Solo la ruta del Referer (sin host): el redirect nunca sale del portal,
     * aunque alguien falsifique la cabecera.
     */
    static String rutaLocal(String referer) {
        if (referer == null) {
            return "/";
        }
        try {
            URI uri = URI.create(referer);
            String ruta = uri.getRawPath();
            if (ruta == null || !ruta.startsWith("/") || ruta.startsWith("//")) {
                return "/";
            }
            return uri.getRawQuery() == null ? ruta : ruta + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException e) {
            return "/";
        }
    }
}
