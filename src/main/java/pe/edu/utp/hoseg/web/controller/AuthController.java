package pe.edu.utp.hoseg.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import pe.edu.utp.hoseg.web.dto.RegistroClienteForm;
import pe.edu.utp.hoseg.web.dto.RestablecerContrasenaForm;
import pe.edu.utp.hoseg.web.model.TipoDocumento;
import pe.edu.utp.hoseg.web.service.CampoInvalidoException;
import pe.edu.utp.hoseg.web.service.ClienteService;
import pe.edu.utp.hoseg.web.service.NegocioException;
import pe.edu.utp.hoseg.web.service.RecuperacionService;

/**
 * Acceso del cliente (bloque 2: CU-07, CU-11, CU-12). El POST de /login lo
 * procesa Spring Security (SecurityConfig); aquí solo se muestra la página.
 */
@Controller
public class AuthController {

    private final ClienteService clienteService;
    private final RecuperacionService recuperacionService;

    public AuthController(ClienteService clienteService, RecuperacionService recuperacionService) {
        this.clienteService = clienteService;
        this.recuperacionService = recuperacionService;
    }

    /**
     * Variantes por parámetro: ?error, ?bloqueado=5 (minutos), ?registrado, ?restablecida.
     * desdeCompra: llegó desde "Confirmar compra" (aviso "tu carrito se conserva").
     */
    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, Model model) {
        SavedRequest pendiente = new HttpSessionRequestCache().getRequest(request, response);
        model.addAttribute("desdeCompra", pendiente != null && pendiente.getRedirectUrl().contains("/carrito/confirmar"));
        return "cuenta/login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("registroForm", new RegistroClienteForm());
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        return "cuenta/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroClienteForm form,
                            BindingResult errores,
                            Model model) {
        if (!errores.hasErrors()) {
            try {
                clienteService.registrar(form);
                return "redirect:/login?registrado";
            } catch (CampoInvalidoException e) {
                errores.rejectValue(e.getCampo(), "negocio", e.getMessage());
            }
        }
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        return "cuenta/registro";
    }

    /** "Recuperar contraseña" (2.3). ?enviado muestra el mensaje, exista o no el correo. */
    @GetMapping("/recuperar")
    public String recuperar() {
        return "cuenta/recuperar";
    }

    @PostMapping("/recuperar")
    public String solicitar(@RequestParam String email) {
        if (!email.isBlank()) {
            recuperacionService.solicitar(email);
        }
        return "redirect:/recuperar?enviado";
    }

    /** "Nueva contraseña" (2.4) desde el enlace del correo; vencido=true si ya no sirve. */
    @GetMapping("/restablecer")
    public String restablecer(@RequestParam(required = false) String token, Model model) {
        boolean vigente = recuperacionService.tokenVigente(token);
        model.addAttribute("vencido", !vigente);
        if (vigente) {
            RestablecerContrasenaForm form = new RestablecerContrasenaForm();
            form.setToken(token);
            model.addAttribute("restablecerForm", form);
        }
        return "cuenta/restablecer";
    }

    @PostMapping("/restablecer")
    public String cambiar(@Valid @ModelAttribute("restablecerForm") RestablecerContrasenaForm form,
                          BindingResult errores,
                          Model model) {
        model.addAttribute("vencido", false);
        if (errores.hasErrors()) {
            return "cuenta/restablecer";
        }
        try {
            recuperacionService.restablecer(form);
            return "redirect:/login?restablecida";
        } catch (NegocioException e) {
            model.addAttribute("vencido", true);
            return "cuenta/restablecer";
        }
    }
}
