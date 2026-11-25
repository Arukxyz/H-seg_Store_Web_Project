package pe.edu.utp.hoseg.web.controller;

import jakarta.servlet.http.HttpSession;
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
import pe.edu.utp.hoseg.web.dto.PagoBilleteraForm;
import pe.edu.utp.hoseg.web.dto.PagoTarjetaForm;
import pe.edu.utp.hoseg.web.dto.ProblemaCarrito;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.service.CafeService;
import pe.edu.utp.hoseg.web.service.CarritoInvalidoException;
import pe.edu.utp.hoseg.web.service.CarritoService;
import pe.edu.utp.hoseg.web.service.CatalogoService;
import pe.edu.utp.hoseg.web.service.NegocioException;
import pe.edu.utp.hoseg.web.service.NotificacionService;
import pe.edu.utp.hoseg.web.service.VentaService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CarritoController {

    private static final String SESION_ENVIO = "datosEnvioCheckout";

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

    @GetMapping("/carrito/confirmar")
    public String confirmarCompatibilidad() {
        return "redirect:/carrito/entrega";
    }

    @GetMapping("/carrito/entrega")
    public String entrega(@AuthenticationPrincipal ClienteAutenticado cliente,
                          HttpSession session,
                          Model model) {
        if (carritoService.estaVacio()) {
            return "redirect:/carrito";
        }
        DatosEnvioForm envioForm = (DatosEnvioForm) session.getAttribute(SESION_ENVIO);
        if (envioForm == null) {
            envioForm = ventaService.datosEnvioIniciales(cliente.id());
        }
        model.addAttribute("envioForm", envioForm);
        prepararEntrega(model);
        return "carrito/entrega";
    }

    @PostMapping("/carrito/entrega")
    public String guardarEntrega(@Valid @ModelAttribute("envioForm") DatosEnvioForm envioForm,
                                 BindingResult errores,
                                 HttpSession session,
                                 Model model) {
        if (carritoService.estaVacio()) {
            return "redirect:/carrito";
        }
        if (errores.hasErrors()) {
            prepararEntrega(model);
            return "carrito/entrega";
        }
        session.setAttribute(SESION_ENVIO, envioForm);
        return "redirect:/carrito/pago";
    }
    @GetMapping("/carrito/pago")
    public String pago(HttpSession session, Model model) {
        if (carritoService.estaVacio()) {
            return "redirect:/carrito";
        }
        DatosEnvioForm envioForm = (DatosEnvioForm) session.getAttribute(SESION_ENVIO);
        if (envioForm == null) {
            return "redirect:/carrito/entrega";
        }
        model.addAttribute("tarjetaForm", new PagoTarjetaForm());
        model.addAttribute("billeteraForm", new PagoBilleteraForm());
        model.addAttribute("medioActivo", "TARJETA");
        prepararPago(model, envioForm);
        return "carrito/pago";
    }

    @PostMapping("/carrito/procesar-pago")
    public String procesarPago(@AuthenticationPrincipal ClienteAutenticado cliente,
                               @RequestParam(defaultValue = "TARJETA") String medioPago,
                               @Valid @ModelAttribute("tarjetaForm") PagoTarjetaForm tarjetaForm,
                               BindingResult erroresTarjeta,
                               @Valid @ModelAttribute("billeteraForm") PagoBilleteraForm billeteraForm,
                               BindingResult erroresBilletera,
                               HttpSession session,
                               Model model,
                               RedirectAttributes flash) {
        if (carritoService.estaVacio()) {
            return "redirect:/carrito";
        }
        DatosEnvioForm envioForm = (DatosEnvioForm) session.getAttribute(SESION_ENVIO);
        if (envioForm == null) {
            return "redirect:/carrito/entrega";
        }

        String referenciaPago;
        if ("BILLETERA".equalsIgnoreCase(medioPago)) {
            if (erroresBilletera.hasErrors()) {
                model.addAttribute("medioActivo", "BILLETERA");
                prepararPago(model, envioForm);
                return "carrito/pago";
            }
            referenciaPago = billeteraForm.getBilletera().toUpperCase();
        } else {
            medioPago = "TARJETA";
            if (erroresTarjeta.hasErrors()) {
                model.addAttribute("medioActivo", "TARJETA");
                prepararPago(model, envioForm);
                return "carrito/pago";
            }
            if (!tarjetaForm.esLuhnValido()) {
                model.addAttribute("errorPago", "El número de tarjeta no es válido. Verifica los 16 dígitos.");
                model.addAttribute("medioActivo", "TARJETA");
                prepararPago(model, envioForm);
                return "carrito/pago";
            }
            if (tarjetaForm.simulaRechazo()) {
                model.addAttribute("errorPago", "Pago rechazado por el emisor de la tarjeta. Intenta con otra tarjeta o usa Yape/Plin.");
                model.addAttribute("medioActivo", "TARJETA");
                prepararPago(model, envioForm);
                return "carrito/pago";
            }
            referenciaPago = tarjetaForm.referenciaSegura();
        }

        try {
            CompraRegistrada compra = ventaService.pagarYRegistrar(
                    cliente.id(), carritoService.items(), envioForm, referenciaPago);
            carritoService.vaciar();
            session.removeAttribute(SESION_ENVIO);
            notificacionService.pedidoRegistrado(cliente.email(), cliente.nombre(), compra);
            flash.addFlashAttribute("compra", compra);
            return "redirect:/compra-exito";
        } catch (CarritoInvalidoException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/carrito";
        }
    }

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

    private void prepararEntrega(Model model) {
        prepararResumen(model);
        model.addAttribute("tiposEnvio", TipoEnvio.values());
        model.addAttribute("costoDelivery", ventaService.costoEnvio(TipoEnvio.DELIVERY));
        model.addAttribute("localesRecojo", cafeService.locales());
    }

    private void prepararPago(Model model, DatosEnvioForm envioForm) {
        prepararResumen(model);
        BigDecimal costoEnvio = ventaService.costoEnvio(envioForm.getTipo());
        model.addAttribute("envioForm", envioForm);
        model.addAttribute("costoEnvio", costoEnvio);
        model.addAttribute("totalConEnvio", carritoService.total().add(costoEnvio));
    }
}
