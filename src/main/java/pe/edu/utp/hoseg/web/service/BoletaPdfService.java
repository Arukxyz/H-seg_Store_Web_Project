package pe.edu.utp.hoseg.web.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.config.JwtTokenProvider;
import pe.edu.utp.hoseg.web.model.DetalleVenta;
import pe.edu.utp.hoseg.web.model.Envio;
import pe.edu.utp.hoseg.web.model.Venta;
import pe.edu.utp.hoseg.web.repository.EnvioRepository;
import pe.edu.utp.hoseg.web.repository.VentaRepository;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@Transactional(readOnly = true)
public class BoletaPdfService {

private static final DateTimeFormatter FECHA_FMT =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(java.time.ZoneId.of("America/Lima"));

    private final VentaRepository ventaRepository;
    private final EnvioRepository envioRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final String urlPublica;
    private final String rutaConsulta;

    public BoletaPdfService(VentaRepository ventaRepository,
                            EnvioRepository envioRepository,
                            JwtTokenProvider jwtTokenProvider,
                            @Value("${hoseg.url-publica}") String urlPublica,
                            @Value("${hoseg.consulta.ruta}") String rutaConsulta) {
        this.ventaRepository = ventaRepository;
        this.envioRepository = envioRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.urlPublica = urlPublica;
        this.rutaConsulta = rutaConsulta;
    }

    public byte[] generarBoletaPdf(Integer idCliente, String codigoComprobante) {
        Venta venta = ventaRepository.findByCodigoComprobanteAndClienteId(codigoComprobante, idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Boleta no encontrada: " + codigoComprobante));

        Envio envio = envioRepository.findByVentaId(venta.getId()).orElse(null);
        BigDecimal costoEnvio = envio != null ? envio.getCosto() : BigDecimal.ZERO;
        BigDecimal totalPagado = venta.getTotal().add(costoEnvio);

        String token = jwtTokenProvider.generarTokenComprobante(venta.getCodigoComprobante());
        String urlQr = urlPublica + rutaConsulta + "?token=" + token;

        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            BufferedImage qrImage = generarQr(urlQr, 150, 150);
            PDImageXObject pdQr = LosslessFactory.createFromImage(doc, qrImage);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 780;

                cs.beginText();
                cs.setFont(fontBold, 18);
                cs.newLineAtOffset(50, y);
                cs.showText("HOSEG STORE PERU S.A.C.");
                cs.endText();

                y -= 20;
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(50, y);
                cs.showText("RUC: 20601234567 | Calle Palacio 110, Cusco - Peru");
                cs.endText();

                y -= 28;
                cs.beginText();
                cs.setFont(fontBold, 13);
                cs.newLineAtOffset(50, y);
                cs.showText("BOLETA DE VENTA ELECTRONICA - Serie B001 | " + venta.getCodigoComprobante());
                cs.endText();

                y -= 22;
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(50, y);
                cs.showText("Fecha: " + venta.getFecha().format(FECHA_FMT) + "   |   Estado: " + venta.getEstado().name());
                cs.endText();

                if (venta.getMetodoPago() != null) {
                    y -= 16;
                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.newLineAtOffset(50, y);
                    cs.showText("Medio de pago: " + venta.getMetodoPago()
                            + (venta.getCodigoTransaccion() != null ? "   |   Operacion: " + venta.getCodigoTransaccion() : ""));
                    cs.endText();
                }

                if (venta.getCliente() != null) {
                    y -= 16;
                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.newLineAtOffset(50, y);
                    cs.showText("Cliente: " + venta.getCliente().getNombre() + " " + venta.getCliente().getApellido());
                    cs.endText();
                }

                y -= 28;
                cs.beginText();
                cs.setFont(fontBold, 10);
                cs.newLineAtOffset(50, y);
                cs.showText("CANT.   PRODUCTO                                              P. UNIT       SUBTOTAL");
                cs.endText();

                y -= 10;
                cs.moveTo(50, y);
                cs.lineTo(545, y);
                cs.stroke();

                for (DetalleVenta d : venta.getDetalles()) {
                    y -= 18;
                    String nombre = d.getProducto().getNombre();
                    if (nombre.length() > 38) nombre = nombre.substring(0, 35) + "...";
                    String fila = String.format("%-7d %-45s S/ %8.2f   S/ %8.2f",
                            d.getCantidad(), nombre, d.getPrecioUnitario(), d.getSubtotal());
                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.newLineAtOffset(50, y);
                    cs.showText(fila);
                    cs.endText();
                }

                y -= 16;
                cs.moveTo(50, y);
                cs.lineTo(545, y);
                cs.stroke();

                y -= 20;
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(380, y);
                cs.showText(String.format("Subtotal:      S/ %8.2f", venta.getTotal()));
                cs.endText();

                y -= 16;
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(380, y);
                cs.showText(String.format("Envio:         S/ %8.2f", costoEnvio));
                cs.endText();

                y -= 18;
                cs.beginText();
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(380, y);
                cs.showText(String.format("TOTAL PAGADO:  S/ %8.2f", totalPagado));
                cs.endText();

                // QR firmado con JWT para trazabilidad de impacto
                float yQr = y - 165;
                cs.drawImage(pdQr, 50, yQr, 130, 130);

                cs.beginText();
                cs.setFont(fontBold, 10);
                cs.newLineAtOffset(195, yQr + 85);
                cs.showText("TRAZABILIDAD DE IMPACTO SOCIAL");
                cs.endText();

                cs.beginText();
                cs.setFont(fontRegular, 9);
                cs.newLineAtOffset(195, yQr + 68);
                cs.showText("Escanea este codigo QR para verificar en tiempo real");
                cs.endText();

                cs.beginText();
                cs.setFont(fontRegular, 9);
                cs.newLineAtOffset(195, yQr + 54);
                cs.showText("el destino de tu donacion (abrigo o arbol) en Cusco.");
                cs.endText();
            }

            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new NegocioException("No se pudo generar la boleta PDF: " + e.getMessage());
        }
    }

    private static BufferedImage generarQr(String texto, int ancho, int alto) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, ancho, alto);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
}
