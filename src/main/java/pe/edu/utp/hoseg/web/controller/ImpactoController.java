package pe.edu.utp.hoseg.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pe.edu.utp.hoseg.web.dto.ConsultaImpacto;
import pe.edu.utp.hoseg.web.service.ImpactoService;

import java.util.Optional;

/**
 * Consulta pública de impacto (bloque 3: CU-10, CU-10.1). La ruta coincide
 * con hoseg.consulta.ruta y con el QR que imprime el escritorio en la boleta
 * (portal.consulta.url?donacion=id): es un contrato entre los dos sistemas.
 */
@Controller
public class ImpactoController {

    private final ImpactoService impactoService;

    public ImpactoController(ImpactoService impactoService) {
        this.impactoService = impactoService;
    }

    /**
     * Sin parámetros: buscador vacío. ?boleta=WEB-000110: todas las donaciones
     * de la compra. ?donacion=12 (QR): solo esa donación.
     */
    @GetMapping("${hoseg.consulta.ruta}")
    public String consulta(@RequestParam(required = false) String boleta,
                           @RequestParam(required = false) Integer donacion,
                           Model model) {
        Optional<ConsultaImpacto> resultado = Optional.empty();
        boolean busco = false;
        if (donacion != null) {
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

    /** La misma consulta en JSON, para buscar sin recargar la página. 404 si no existe. */
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
