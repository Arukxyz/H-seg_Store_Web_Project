package pe.edu.utp.hoseg.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.edu.utp.hoseg.web.dto.EnlaceNavegacion;
import pe.edu.utp.hoseg.web.service.CarritoService;

import java.util.List;

/** Datos que necesitan todas las vistas (navbar y footer). */
@ControllerAdvice
public class AtributosGlobales {

    /**
     * Destinos del menú principal, en orden. Nosotros no lleva icono: el óvalo
     * móvil tiene sitio para cinco y el quinto es Cuenta (lo añade navbar.html).
     */
    private static final List<EnlaceNavegacion> MENU_PRINCIPAL = List.of(
            new EnlaceNavegacion("inicio", "Inicio", "/", "bi-house"),
            new EnlaceNavegacion("tienda", "Tienda", "/tienda", "bi-bag"),
            new EnlaceNavegacion("cafe", "Café", "/cafe", "bi-cup-hot"),
            new EnlaceNavegacion("impacto", "Impacto", "/#impacto", "bi-activity"),
            new EnlaceNavegacion("nosotros", "Nosotros", "/#nosotros", null));

    private final CarritoService carritoService;
    private final String rutaConsulta;

    public AtributosGlobales(CarritoService carritoService,
                             @Value("${hoseg.consulta.ruta}") String rutaConsulta) {
        this.carritoService = carritoService;
        this.rutaConsulta = rutaConsulta;
    }

    /** Contador del badge del carrito. */
    @ModelAttribute("carritoUnidades")
    public int carritoUnidades() {
        return carritoService.totalUnidades();
    }

    /** Ruta del portal de consulta; debe coincidir con la que imprime el QR del escritorio. */
    @ModelAttribute("rutaConsulta")
    public String rutaConsulta() {
        return rutaConsulta;
    }

    @ModelAttribute("menuPrincipal")
    public List<EnlaceNavegacion> menuPrincipal() {
        return MENU_PRINCIPAL;
    }
}
