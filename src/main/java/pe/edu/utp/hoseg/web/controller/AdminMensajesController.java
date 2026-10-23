package pe.edu.utp.hoseg.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import pe.edu.utp.hoseg.web.dto.RespuestaReclamoForm;
import pe.edu.utp.hoseg.web.model.EstadoMensaje;
import pe.edu.utp.hoseg.web.service.BoletinService;
import pe.edu.utp.hoseg.web.service.ContactoService;
import pe.edu.utp.hoseg.web.service.NegocioException;
import pe.edu.utp.hoseg.web.service.ReclamacionService;

import java.nio.charset.StandardCharsets;

/** Mensajes, reclamaciones y suscriptores en el panel (bloque 4: CU-20). */
@Controller
public class AdminMensajesController {

    private final ContactoService contactoService;
    private final ReclamacionService reclamacionService;
    private final BoletinService boletinService;

    public AdminMensajesController(ContactoService contactoService, ReclamacionService reclamacionService,
                                   BoletinService boletinService) {
        this.contactoService = contactoService;
        this.reclamacionService = reclamacionService;
        this.boletinService = boletinService;
    }

    /** Pestañas (?pestana=bandeja|reclamaciones|suscriptores); ?estado=NUEVO|ATENDIDO filtra la bandeja. */
    @GetMapping("/admin/mensajes")
    public String mensajes(@RequestParam(defaultValue = "bandeja") String pestana,
                           @RequestParam(required = false) EstadoMensaje estado,
                           Model model) {
        model.addAttribute("respuestaForm", new RespuestaReclamoForm());
        preparar(pestana, estado, model);
        return "admin/mensajes";
    }

    @PostMapping("/admin/mensajes/{id}/atendido")
    public String atendido(@PathVariable Integer id, @AuthenticationPrincipal AdministradorAutenticado admin) {
        contactoService.marcarAtendido(id, admin.id());
        return "redirect:/admin/mensajes";
    }

    @PostMapping("/admin/reclamaciones/{id}/respuesta")
    public String responder(@PathVariable Integer id,
                            @Valid @ModelAttribute("respuestaForm") RespuestaReclamoForm form,
                            BindingResult errores,
                            @AuthenticationPrincipal AdministradorAutenticado admin,
                            Model model,
                            RedirectAttributes flash) {
        if (errores.hasErrors()) {
            model.addAttribute("reclamoConError", id);
            preparar("reclamaciones", null, model);
            return "admin/mensajes";
        }
        try {
            reclamacionService.responder(id, form, admin.id());
            flash.addFlashAttribute("exito", "Respuesta enviada al consumidor.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/mensajes?pestana=reclamaciones";
    }

    /** "Exportar CSV" de suscriptores activos. */
    @GetMapping("/admin/suscriptores.csv")
    public ResponseEntity<byte[]> suscriptoresCsv() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("suscriptores-hoseg.csv").build().toString())
                .body(boletinService.csvActivos().getBytes(StandardCharsets.UTF_8));
    }

    private void preparar(String pestana, EstadoMensaje estado, Model model) {
        model.addAttribute("pestana", pestana);
        model.addAttribute("estado", estado);
        model.addAttribute("mensajes", contactoService.bandeja(estado));
        model.addAttribute("reclamos", reclamacionService.todos());
        model.addAttribute("suscriptores", boletinService.activos());
    }
}
