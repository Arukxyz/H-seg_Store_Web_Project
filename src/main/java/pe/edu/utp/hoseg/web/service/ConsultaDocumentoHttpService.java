package pe.edu.utp.hoseg.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import pe.edu.utp.hoseg.web.dto.DocumentoConsultado;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Consulta DNI (RENIEC) y RUC (SUNAT) en Decolecta.
 *
 * Si no hay token o Decolecta no puede responder (sin red, token inválido,
 * sin créditos, caída del servicio), responde {@link ConsultaDocumentoSimulador}
 * para que el autocompletado funcione igual en la demostración. Si Decolecta
 * responde que el documento no existe, no se inventan datos.
 */
@Service
@Primary
public class ConsultaDocumentoHttpService implements ConsultaDocumentoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaDocumentoHttpService.class);

    /** Respuestas que indican un problema nuestro o del proveedor, no un documento inexistente. */
    private static final Set<HttpStatus> FALLAS_DEL_SERVICIO = Set.of(HttpStatus.UNAUTHORIZED,
            HttpStatus.PAYMENT_REQUIRED, HttpStatus.FORBIDDEN, HttpStatus.TOO_MANY_REQUESTS);

    private final RestClient restClient;
    private final String token;
    private final ConsultaDocumentoSimulador simulador;

    public ConsultaDocumentoHttpService(
            @Value("${hoseg.documento-api.url:https://api.decolecta.com/v1}") String baseUrl,
            @Value("${hoseg.documento-api.token:${DOCUMENTO_API_TOKEN:}}") String token,
            ConsultaDocumentoSimulador simulador) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
        this.token = token == null ? "" : token.trim();
        this.simulador = simulador;
        if (this.token.isBlank()) {
            log.info("Sin hoseg.documento-api.token: la consulta de DNI/RUC usará el simulador.");
        }
    }

    @Override
    public Optional<DocumentoConsultado> consultar(String tipo, String numero) {
        if (tipo == null || numero == null) {
            return Optional.empty();
        }
        String tipoNorm = tipo.trim().toUpperCase();
        String numNorm = numero.trim();

        if (!("DNI".equals(tipoNorm) && numNorm.matches("^[0-9]{8}$"))
                && !("RUC".equals(tipoNorm) && numNorm.matches("^[0-9]{11}$"))) {
            return Optional.empty();
        }

        if (token.isBlank()) {
            return simulador.consultar(tipoNorm, numNorm);
        }

        try {
            String endpoint = "DNI".equals(tipoNorm)
                    ? "/reniec/dni?numero={numero}"
                    : "/sunat/ruc?numero={numero}";

            Map<?, ?> body = restClient.get()
                    .uri(endpoint, numNorm)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(Map.class);

            if (body == null || body.isEmpty()) {
                return Optional.empty();
            }
            return "DNI".equals(tipoNorm) ? dni(numNorm, body) : ruc(numNorm, body);

        } catch (RestClientResponseException e) {
            HttpStatus estado = HttpStatus.resolve(e.getStatusCode().value());
            if (e.getStatusCode().is5xxServerError() || FALLAS_DEL_SERVICIO.contains(estado)) {
                log.warn("Decolecta respondió {} al consultar {}: se usa el simulador.", e.getStatusCode(), tipoNorm);
                return simulador.consultar(tipoNorm, numNorm);
            }
            // 404, 422…: Decolecta no encontró el documento
            log.debug("Decolecta no encontró el {} consultado ({}).", tipoNorm, e.getStatusCode());
            return Optional.empty();

        } catch (RestClientException e) {
            // Solo la causa: el mensaje completo trae la URL con el número consultado
            log.warn("No se pudo conectar con Decolecta ({}): se usa el simulador.",
                    e.getMostSpecificCause().getMessage());
            return simulador.consultar(tipoNorm, numNorm);
        }
    }

    private static Optional<DocumentoConsultado> dni(String numero, Map<?, ?> body) {
        String nombres = str(body, "first_name");
        String apellidos = (str(body, "first_last_name") + " " + str(body, "second_last_name")).trim();
        if (nombres.isBlank() || apellidos.isBlank()) {
            return Optional.empty();
        }
        String nombreCompleto = str(body, "full_name");
        if (nombreCompleto.isBlank()) {
            nombreCompleto = nombres + " " + apellidos;
        }
        return Optional.of(new DocumentoConsultado("DNI", numero, nombres, apellidos, nombreCompleto, "",
                "DECOLECTA - RENIEC"));
    }

    private static Optional<DocumentoConsultado> ruc(String numero, Map<?, ?> body) {
        String razonSocial = str(body, "razon_social");
        if (razonSocial.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new DocumentoConsultado("RUC", numero, razonSocial, "", razonSocial,
                str(body, "direccion"), "DECOLECTA - SUNAT"));
    }

    private static String str(Map<?, ?> map, String clave) {
        Object valor = map.get(clave);
        return valor == null ? "" : valor.toString().trim();
    }
}
