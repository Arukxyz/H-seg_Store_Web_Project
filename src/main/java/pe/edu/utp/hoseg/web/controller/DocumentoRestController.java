package pe.edu.utp.hoseg.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.utp.hoseg.web.dto.DocumentoConsultado;
import pe.edu.utp.hoseg.web.service.ConsultaDocumentoService;

@RestController
@RequestMapping("/api/documento")
public class DocumentoRestController {

    private final ConsultaDocumentoService consultaDocumentoService;

    public DocumentoRestController(ConsultaDocumentoService consultaDocumentoService) {
        this.consultaDocumentoService = consultaDocumentoService;
    }

    @GetMapping("/{tipo}/{numero}")
    public ResponseEntity<DocumentoConsultado> consultar(@PathVariable String tipo,
                                                         @PathVariable String numero) {
        return ResponseEntity.of(consultaDocumentoService.consultar(tipo, numero));
    }
}
