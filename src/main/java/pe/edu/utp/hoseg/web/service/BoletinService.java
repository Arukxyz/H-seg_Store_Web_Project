package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.model.SuscriptorBoletin;
import pe.edu.utp.hoseg.web.repository.SuscriptorBoletinRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Boletín Changemaker (CU-09, CU-20). Suscribirse dos veces no duplica: el
 * email es único sin mayúsculas y una baja se reactiva en la misma fila.
 */
@Service
@Transactional(readOnly = true)
public class BoletinService {

    /** Para el mensaje que ve quien se suscribe. */
    public enum Resultado {
        SUSCRITO("¡Listo! Te avisaremos de campañas, lanzamientos e informes de impacto."),
        YA_SUSCRITO("Ese email ya está suscrito al boletín.");

        private final String mensaje;

        Resultado(String mensaje) {
            this.mensaje = mensaje;
        }

        public String getMensaje() {
            return mensaje;
        }
    }

    private final SuscriptorBoletinRepository suscriptorRepository;

    public BoletinService(SuscriptorBoletinRepository suscriptorRepository) {
        this.suscriptorRepository = suscriptorRepository;
    }

    @Transactional
    public Resultado suscribir(String email) {
        String limpio = email.trim();
        return suscriptorRepository.findByEmailIgnoreCase(limpio)
                .map(s -> {
                    if (s.isActivo()) {
                        return Resultado.YA_SUSCRITO;
                    }
                    s.reactivar();
                    return Resultado.SUSCRITO;
                })
                .orElseGet(() -> {
                    SuscriptorBoletin nuevo = new SuscriptorBoletin();
                    nuevo.setEmail(limpio);
                    suscriptorRepository.save(nuevo);
                    return Resultado.SUSCRITO;
                });
    }

    @Transactional
    public void darDeBaja(String email) {
        suscriptorRepository.findByEmailIgnoreCase(email.trim())
                .filter(SuscriptorBoletin::isActivo)
                .ifPresent(SuscriptorBoletin::darDeBaja);
    }

    // ---------- Panel (4.6) ----------

    public List<SuscriptorBoletin> activos() {
        return suscriptorRepository.findByActivoTrueOrderBySuscritoEnAsc();
    }

    /** "Exportar CSV": email y fecha de suscripción de los activos. */
    public String csvActivos() {
        DateTimeFormatter fecha = DateTimeFormatter.ISO_LOCAL_DATE;
        StringBuilder csv = new StringBuilder("email,suscrito_en\n");
        for (SuscriptorBoletin s : activos()) {
            csv.append(campoCsv(s.getEmail())).append(',').append(s.getSuscritoEn().format(fecha)).append('\n');
        }
        return csv.toString();
    }

    /** Comillas si hace falta, y sin fórmulas: Excel ejecuta celdas que empiezan con = + - @. */
    static String campoCsv(String valor) {
        String seguro = valor.matches("^[=+\\-@].*") ? "'" + valor : valor;
        if (seguro.contains(",") || seguro.contains("\"") || seguro.contains("\n")) {
            return "\"" + seguro.replace("\"", "\"\"") + "\"";
        }
        return seguro;
    }
}
