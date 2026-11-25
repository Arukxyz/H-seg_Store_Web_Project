package pe.edu.utp.hoseg.web.service;

import pe.edu.utp.hoseg.web.dto.DocumentoConsultado;

import java.util.Optional;

public interface ConsultaDocumentoService {

    /** Consulta DNI (8 dígitos) o RUC (11 dígitos); devuelve vacío si no es válido o no existe. */
    Optional<DocumentoConsultado> consultar(String tipo, String numero);
}
