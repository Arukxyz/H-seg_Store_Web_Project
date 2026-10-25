package pe.edu.utp.hoseg.web.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pe.edu.utp.hoseg.web.model.SuscriptorBoletin;
import pe.edu.utp.hoseg.web.repository.SuscriptorBoletinRepository;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoletinServiceExcelTest {

    @Mock
    private SuscriptorBoletinRepository suscriptorRepository;

    @InjectMocks
    private BoletinService boletinService;

    @Test
    void elExcelTieneEncabezadoYUnaFilaPorSuscriptorActivo() throws Exception {
        SuscriptorBoletin ana = new SuscriptorBoletin();
        ana.setEmail("ana@correo.pe");
        ReflectionTestUtils.setField(ana, "suscritoEn", OffsetDateTime.parse("2026-10-01T10:00:00-05:00"));
        when(suscriptorRepository.findByActivoTrueOrderBySuscritoEnAsc()).thenReturn(List.of(ana));

        byte[] excel = boletinService.exportarExcelActivos();

        try (Workbook libro = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            Sheet hoja = libro.getSheet("Suscriptores");
            assertThat(hoja.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Email");
            assertThat(hoja.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Suscrito el");
            assertThat(hoja.getRow(1).getCell(0).getStringCellValue()).isEqualTo("ana@correo.pe");
            assertThat(hoja.getRow(1).getCell(1).getStringCellValue()).isEqualTo("2026-10-01");
            assertThat(hoja.getLastRowNum()).isEqualTo(1);
        }
    }
}