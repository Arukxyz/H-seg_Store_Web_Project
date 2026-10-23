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
import pe.edu.utp.hoseg.web.config.ClienteAutenticado;
import pe.edu.utp.hoseg.web.dto.CambioContrasenaForm;
import pe.edu.utp.hoseg.web.dto.DatosClienteForm;
import pe.edu.utp.hoseg.web.service.CampoInvalidoException;
import pe.edu.utp.hoseg.web.service.ClienteService;
import pe.edu.utp.hoseg.web.service.ImpactoService;
import pe.edu.utp.hoseg.web.service.VentaService;

/**
 * Mi cuenta (bloque 2: CU-13, CU-14). Una sola página con dos pestañas:
 * pedidos e impacto (?seccion=pedidos, por defecto) y mis datos (?seccion=datos).
 */
@Controller
public class CuentaController {

    private final ClienteService clienteService;
    private final VentaService ventaService;
    private final ImpactoService impactoService;

    public CuentaController(ClienteService clienteService, VentaService ventaService, ImpactoService impactoService) {
        this.clienteService = clienteService;
        this.ventaService = ventaService;
        this.impactoService = impactoService;
    }

    @GetMapping("/cuenta")
    public String cuenta(@AuthenticationPrincipal ClienteAutenticado cliente,
                         @RequestParam(defaultValue = "pedidos") String seccion,
                         Model model) {
        model.addAttribute("datosForm", clienteService.datos(cliente.id()));
        model.addAttribute("contrasenaForm", new CambioContrasenaForm());
        preparar(cliente, seccion, model);
        return "cuenta/cuenta";
    }

    @PostMapping("/cuenta/datos")
    public String guardarDatos(@AuthenticationPrincipal ClienteAutenticado cliente,
                               @Valid @ModelAttribute("datosForm") DatosClienteForm form,
                               BindingResult errores,
                               Model model,
                               RedirectAttributes flash) {
        if (errores.hasErrors()) {
            form.setEmail(cliente.email());
            model.addAttribute("contrasenaForm", new CambioContrasenaForm());
            preparar(cliente, "datos", model);
            return "cuenta/cuenta";
        }
        clienteService.actualizarDatos(cliente.id(), form);
        flash.addFlashAttribute("exito", "Guardamos tus datos.");
        return "redirect:/cuenta?seccion=datos";
    }

    @PostMapping("/cuenta/contrasena")
    public String cambiarContrasena(@AuthenticationPrincipal ClienteAutenticado cliente,
                                    @Valid @ModelAttribute("contrasenaForm") CambioContrasenaForm form,
                                    BindingResult errores,
                                    Model model,
                                    RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                clienteService.cambiarContrasena(cliente.id(), form);
                flash.addFlashAttribute("exito", "Cambiamos tu contraseña.");
                return "redirect:/cuenta?seccion=datos";
            } catch (CampoInvalidoException e) {
                errores.rejectValue(e.getCampo(), "negocio", e.getMessage());
            }
        }
        model.addAttribute("datosForm", clienteService.datos(cliente.id()));
        preparar(cliente, "datos", model);
        return "cuenta/cuenta";
    }

    /** "Detalle de pedido" (2.6). Un código ajeno responde 404. */
    @GetMapping("/cuenta/pedidos/{codigo}")
    public String pedido(@AuthenticationPrincipal ClienteAutenticado cliente,
                         @PathVariable String codigo,
                         Model model) {
        model.addAttribute("pedido", ventaService.detallePedido(cliente.id(), codigo));
        return "cuenta/pedido-detalle";
    }

    private void preparar(ClienteAutenticado cliente, String seccion, Model model) {
        model.addAttribute("seccion", "datos".equals(seccion) ? "datos" : "pedidos");
        model.addAttribute("nombre", cliente.nombre());
        model.addAttribute("impacto", impactoService.impactoDelCliente(cliente.id()));
        model.addAttribute("pedidos", ventaService.pedidosDe(cliente.id()));
    }
}
