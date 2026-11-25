package pe.edu.utp.hoseg.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pe.edu.utp.hoseg.web.config.JwtTokenProvider;
import pe.edu.utp.hoseg.web.dto.ConsultaImpacto;
import pe.edu.utp.hoseg.web.service.ImpactoService;

import java.util.Optional;

@Controller
public class ImpactoController {

    private final ImpactoService impactoService;
    private final JwtTokenProvider jwtTokenProvider;

    public ImpactoController(ImpactoService impactoService, JwtTokenProvider jwtTokenProvider) {
        this.impactoService = impactoService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Soporta ?token=<JWT> (QR de la boleta PDF web), ?boleta=WEB-000110 y ?donacion=12 (QR escritorio).
     */
    @GetMapping("${hoseg.consulta.ruta}")
    public String consulta(@RequestParam(required = false) String token,
                           @RequestParam(required = false) String boleta,
                           @RequestParam(required = false) Integer donacion,
                           Model model) {
        Optional<ConsultaImpacto> resultado = Optional.empty();
        boolean busco = false;

        if (token != null && !token.isBlank()) {
            busco = true;
            Optional<String> codigoJwt = jwtTokenProvider.validarYExtraerComprobante(token);
            if (codigoJwt.isPresent()) {
                boleta = codigoJwt.get();
                resultado = impactoService.consultarBoleta(boleta);
            } else {
                model.addAttribute("tokenInvalido", true);
            }
        } else if (donacion != null) {
            resultado = impactoService.consultarDonacion(donacion);
            busco = true;
        } else if (boleta != null && !boleta.isBlank()) {
            resultado = impactoService.consultarBoleta(boleta);
            busco = true;
        }

        model.addAttribute("boleta", boleta);
        model.addAttribute("consulta", resultado.orElse(null));
        model.addAttribute("noEncontrado", busco && resultado.isEmpty());
        return "impacto/consulta";
    }

    @GetMapping("/api/impacto")
    @ResponseBody
    public ResponseEntity<ConsultaImpacto> consultaJson(@RequestParam(required = false) String boleta,
                                                        @RequestParam(required = false) Integer donacion) {
        Optional<ConsultaImpacto> resultado = donacion != null
                ? impactoService.consultarDonacion(donacion)
                : impactoService.consultarBoleta(boleta);
        return ResponseEntity.of(resultado);
    }
}
