package pe.edu.utp.hoseg.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import pe.edu.utp.hoseg.web.service.CarritoService;

/** Datos que necesitan todas las vistas (navbar y footer). */
@ControllerAdvice
public class AtributosGlobales {

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
}
