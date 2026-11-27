package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Component;
import pe.edu.utp.hoseg.web.dto.DocumentoConsultado;

import java.util.Map;
import java.util.Optional;

/** Respaldo local con los datos del equipo y RUCs de prueba para la demostración. */
@Component
public class ConsultaDocumentoSimulador {

    private static final Map<String, DocumentoConsultado> PADRON_DEMO = Map.ofEntries(
            // --- Integrantes del equipo (DNI) ---
            Map.entry("79997259", new DocumentoConsultado("DNI", "79997259",
                    "AARON ASDRUBAL", "ANCA VASQUEZ", "AARON ASDRUBAL ANCA VASQUEZ",
                    "AV. EL SOL 310, CUSCO", "SIMULADOR")),
            Map.entry("70597604", new DocumentoConsultado("DNI", "70597604",
                    "MILAGROS PILAR", "LOZANO VILLENA", "MILAGROS PILAR LOZANO VILLENA",
                    "JR. MANTAS 145, CUSCO", "SIMULADOR")),
            Map.entry("75346883", new DocumentoConsultado("DNI", "75346883",
                    "NAJHELY PAOLA", "BANDA SANCHEZ", "NAJHELY PAOLA BANDA SANCHEZ",
                    "AV. LA CULTURA 820, CUSCO", "SIMULADOR")),
            Map.entry("77528562", new DocumentoConsultado("DNI", "77528562",
                    "JEAN CARLOS", "LOPEZ SUAZO", "JEAN CARLOS LOPEZ SUAZO",
                    "CALLE PLATEROS 230, CUSCO", "SIMULADOR")),
            Map.entry("45805802", new DocumentoConsultado("DNI", "45805802",
                    "JUAN CARLOS", "RUIZ QUISPE", "JUAN CARLOS RUIZ QUISPE",
                    "AV. PARDO 540, CUSCO", "SIMULADOR")),
            Map.entry("40675866", new DocumentoConsultado("DNI", "40675866",
                    "FRANZ BRUCE", "MENDOZA ARCE", "FRANZ BRUCE MENDOZA ARCE",
                    "CALLE TRIUNFO 118, CUSCO", "SIMULADOR")),

            // --- Empresas de prueba (RUC) — aquí mismo puedes cambiar o agregar más ---
            Map.entry("20601234567", new DocumentoConsultado("RUC", "20601234567",
                    "HÖSÉG STORE PERÚ S.A.C.", "", "HÖSÉG STORE PERÚ S.A.C.",
                    "CALLE PALACIO 110, CUSCO", "SIMULADOR")),
            Map.entry("20100047218", new DocumentoConsultado("RUC", "20100047218",
                    "BANCO DE CRÉDITO DEL PERÚ", "", "BANCO DE CRÉDITO DEL PERÚ",
                    "CALLE CENTENARIO 156, LA MOLINA", "SIMULADOR")),
            Map.entry("20503644968", new DocumentoConsultado("RUC", "20503644968",
                    "TEXTILES ANDINOS DEL SUR S.A.C.", "", "TEXTILES ANDINOS DEL SUR S.A.C.",
                    "PARQUE INDUSTRIAL F-14, WANCHAQ - CUSCO", "SIMULADOR"))
    );

    public Optional<DocumentoConsultado> consultar(String tipo, String numero) {
        if (PADRON_DEMO.containsKey(numero)) {
            return Optional.of(PADRON_DEMO.get(numero));
        }
        // Respaldo genérico para cualquier otro DNI (8 dígitos) o RUC (11 dígitos)
        if ("DNI".equals(tipo) && numero.matches("^[0-9]{8}$")) {
            return Optional.of(new DocumentoConsultado("DNI", numero, "VALERIA SOFÍA", "CCAHUANA VARGAS",
                    "VALERIA SOFÍA CCAHUANA VARGAS", "", "SIMULADOR"));
        }
        if ("RUC".equals(tipo) && numero.matches("^(10|20)[0-9]{9}$")) {
            return Optional.of(new DocumentoConsultado("RUC", numero, "INVERSIONES ANDINAS S.A.C.", "",
                    "INVERSIONES ANDINAS S.A.C.", "AV. LA CULTURA 1200, CUSCO", "SIMULADOR"));
        }
        return Optional.empty();
    }
}
