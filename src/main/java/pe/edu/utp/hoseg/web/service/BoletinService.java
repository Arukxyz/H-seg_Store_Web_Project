package pe.edu.utp.hoseg.web.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.model.SuscriptorBoletin;
import pe.edu.utp.hoseg.web.repository.SuscriptorBoletinRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
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

    /**
     * "Exportar Excel": los mismos datos que el CSV, en un .xlsx real (Apache POI).
     * El email va en una celda de texto, que Excel nunca evalúa como fórmula, así que
     * aquí no hace falta el apóstrofo que sí usa {@link #campoCsv}. El ancho de las
     * columnas se fija a mano (no con autoSizeColumn) para no depender de las fuentes
     * del sistema, que pueden faltar en un servidor.
     */
    public byte[] exportarExcelActivos() {
        DateTimeFormatter fecha = DateTimeFormatter.ISO_LOCAL_DATE;
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Suscriptores");

            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFont(negrita);

            Row cabecera = hoja.createRow(0);
            String[] titulos = {"Email", "Suscrito el"};
            for (int i = 0; i < titulos.length; i++) {
                Cell celda = cabecera.createCell(i);
                celda.setCellValue(titulos[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            int numeroFila = 1;
            for (SuscriptorBoletin s : activos()) {
                Row fila = hoja.createRow(numeroFila++);
                fila.createCell(0).setCellValue(s.getEmail());
                fila.createCell(1).setCellValue(s.getSuscritoEn().format(fecha));
            }
            hoja.setColumnWidth(0, 40 * 256);
            hoja.setColumnWidth(1, 16 * 256);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el Excel de suscriptores.", e);
        }
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