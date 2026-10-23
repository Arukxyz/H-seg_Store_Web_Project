package pe.edu.utp.hoseg.web.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.hoseg.web.model.SuscriptorBoletin;
import pe.edu.utp.hoseg.web.repository.SuscriptorBoletinRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoletinServiceTest {

    @Mock
    private SuscriptorBoletinRepository suscriptorRepository;

    @InjectMocks
    private BoletinService boletinService;

    @Test
    void suscribirseDosVecesNoDuplicaYUnaBajaSeReactiva() {
        SuscriptorBoletin activo = suscriptor("ya@correo.pe");
        SuscriptorBoletin deBaja = suscriptor("baja@correo.pe");
        deBaja.darDeBaja();
        when(suscriptorRepository.findByEmailIgnoreCase("YA@correo.pe")).thenReturn(Optional.of(activo));
        when(suscriptorRepository.findByEmailIgnoreCase("baja@correo.pe")).thenReturn(Optional.of(deBaja));

        assertThat(boletinService.suscribir("YA@correo.pe")).isEqualTo(BoletinService.Resultado.YA_SUSCRITO);
        assertThat(boletinService.suscribir("baja@correo.pe")).isEqualTo(BoletinService.Resultado.SUSCRITO);
        assertThat(deBaja.isActivo()).isTrue();
        assertThat(deBaja.getBajaEn()).isNull();                              // suscriptor_baja_check
        verify(suscriptorRepository, never()).save(any());
    }

    @Test
    void elCsvNoDejaPasarFormulasDeExcel() {
        assertThat(BoletinService.campoCsv("ana@correo.pe")).isEqualTo("ana@correo.pe");
        assertThat(BoletinService.campoCsv("=HYPERLINK(\"x\")")).isEqualTo("\"'=HYPERLINK(\"\"x\"\")\"");
    }

    private static SuscriptorBoletin suscriptor(String email) {
        SuscriptorBoletin s = new SuscriptorBoletin();
        s.setEmail(email);
        return s;
    }
}
