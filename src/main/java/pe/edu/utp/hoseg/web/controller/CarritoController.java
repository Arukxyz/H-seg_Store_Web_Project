package pe.edu.utp.hoseg.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.hoseg.web.config.ClienteAutenticado;
import pe.edu.utp.hoseg.web.dto.CompraRegistrada;
import pe.edu.utp.hoseg.web.dto.DatosEnvioForm;
import pe.edu.utp.hoseg.web.dto.ProblemaCarrito;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.service.CafeService;
import pe.edu.utp.hoseg.web.service.CarritoInvalidoException;
import pe.edu.utp.hoseg.web.service.CarritoService;
import pe.edu.utp.hoseg.web.service.CatalogoService;
import pe.edu.utp.hoseg.web.service.NegocioException;
import pe.edu.utp.hoseg.web.service.NotificacionService;
import pe.edu.utp.hoseg.web.service.VentaService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Carrito y compra (bloque 1: CU-04, CU-15). El carrito vive en la sesión;
 * confirmar exige cliente con sesión (SecurityConfig) y crea el pedido
 * PENDIENTE. La web no cobra: el pago lo confirma el encargado en el escritorio.
 */
@Controller
public class CarritoController {

    private final CarritoService carritoService;
    private final CatalogoService catalogoService;
    private final VentaService ventaService;
    private final CafeService cafeService;
    private final NotificacionService notificacionService;

    public CarritoController(CarritoService carritoService, CatalogoService catalogoService, VentaService ventaService,
                             CafeService cafeService, NotificacionService notificacionService) {
        this.carritoService = carritoService;
        this.catalogoService = catalogoService;
        this.ventaService = ventaService;
        this.cafeService = cafeService;
        this.notificacionService = notificacionService;
    }

    @GetMapping("/carrito")
    public String carrito(Model model) {
        prepararResumen(model);
        return "carrito/carrito";
    }

    @PostMapping("/carrito/agregar")
    public String agregar(@RequestParam String codigo,
                          @RequestParam(defaultValue = "1") int cantidad,
                          RedirectAttributes flash) {
        try {
            carritoService.agregar(catalogoService.paraCarrito(codigo), cantidad);
            flash.addFlashAttribute("exito", "Agregado al carrito.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/tienda/" + codigo;
        }
        return "redirect:/carrito";
    }

    /** Botones − / +; cantidad 0 quita la línea. */
    @PostMapping("/carrito/actualizar")
    public String actualizar(@RequestParam String codigo, @RequestParam int cantidad) {
        carritoService.actualizar(codigo, cantidad);
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/quitar")
    public String quitar(@RequestParam String codigo) {
        carritoService.quitar(codigo);
        return "redirect:/carrito";
    }

    /** "Confirmar compra" (1.5): resumen y entrega precargada con los datos de la cuenta. */
    @GetMapping("/carrito/confirmar")
    public String confirmar(@AuthenticationPrincipal ClienteAutenticado cliente, Model model) {
        if (carritoService.estaVacio()) {
            return "redirect:/carrito";
        }
        model.addAttribute("envioForm", ventaService.datosEnvioIniciales(cliente.id()));
        prepararConfirmacion(model);
        return "carrito/confirmar";
    }

    @PostMapping("/carrito/confirmar")
    public String registrar(@AuthenticationPrincipal ClienteAutenticado cliente,
                            @Valid @ModelAttribute("envioForm") DatosEnvioForm envioForm,
                            BindingResult errores,
                            Model model,
                            RedirectAttributes flash) {
        if (errores.hasErrors()) {
            prepararConfirmacion(model);
            return "carrito/confirmar";
        }
        try {
            CompraRegistrada compra = ventaService.confirmarCompra(cliente.id(), carritoService.items(), envioForm);
            carritoService.vaciar();
            notificacionService.pedidoRegistrado(cliente.email(), cliente.nombre(), compra);
            flash.addFlashAttribute("compra", compra);
            return "redirect:/compra-exito";
        } catch (CarritoInvalidoException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/carrito";
        }
    }

    /** "Compra registrada" (1.6). Solo se llega aquí justo después de confirmar. */
    @GetMapping("/compra-exito")
    public String exito(Model model) {
        if (!model.containsAttribute("compra")) {
            return "redirect:/cuenta";
        }
        return "carrito/compra-exito";
    }

    private void prepararResumen(Model model) {
        List<ProblemaCarrito> problemas = ventaService.validarCarrito(carritoService.items());
        model.addAttribute("items", carritoService.items());
        model.addAttribute("total", carritoService.total());
        model.addAttribute("impacto", carritoService.impacto());
        model.addAttribute("vacio", carritoService.estaVacio());
        model.addAttribute("problemasPorCodigo", problemas.stream()
                .filter(p -> p.codigoProducto() != null)
                .collect(Collectors.toMap(ProblemaCarrito::codigoProducto, ProblemaCarrito::mensaje, (a, b) -> a)));
        model.addAttribute("sePuedeConfirmar", problemas.isEmpty());
    }

    private void prepararConfirmacion(Model model) {
        prepararResumen(model);
        model.addAttribute("tiposEnvio", TipoEnvio.values());
        model.addAttribute("costoDelivery", ventaService.costoEnvio(TipoEnvio.DELIVERY));
        model.addAttribute("localesRecojo", cafeService.locales());
    }
}
