package pe.edu.utp.hoseg.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.hoseg.web.dto.ImpactoCliente;
import pe.edu.utp.hoseg.web.dto.UnidadesDonadas;
import pe.edu.utp.hoseg.web.repository.ComunidadRepository;
import pe.edu.utp.hoseg.web.repository.DonacionRepository;
import pe.edu.utp.hoseg.web.repository.LoteDonacionRepository;
import pe.edu.utp.hoseg.web.repository.OngRepository;
import pe.edu.utp.hoseg.web.repository.VentaRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pe.edu.utp.hoseg.web.model.EstadoDonacion.ASIGNADA;
import static pe.edu.utp.hoseg.web.model.EstadoDonacion.ENTREGADA;
import static pe.edu.utp.hoseg.web.model.EstadoDonacion.EN_ESPERA;
import static pe.edu.utp.hoseg.web.model.TipoCompromiso.ABRIGO;
import static pe.edu.utp.hoseg.web.model.TipoCompromiso.ARBOL;

@ExtendWith(MockitoExtension.class)
class ImpactoServiceTest {

    @Mock private DonacionRepository donacionRepository;
    @Mock private LoteDonacionRepository loteDonacionRepository;
    @Mock private VentaRepository ventaRepository;
    @Mock private ComunidadRepository comunidadRepository;
    @Mock private OngRepository ongRepository;

    private ImpactoService impactoService;

    @BeforeEach
    void crear() {
        impactoService = new ImpactoService(donacionRepository, loteDonacionRepository, ventaRepository,
                comunidadRepository, ongRepository, "WEB-");
    }

    @Test
    void elImpactoDelClienteSeparaLoPagadoLoEntregadoYLoQueEsperaPago() {
        when(donacionRepository.delCliente(7)).thenReturn(List.of(
                new UnidadesDonadas(1, ABRIGO, ENTREGADA, 2L),
                new UnidadesDonadas(2, ABRIGO, ASIGNADA, 1L),
                new UnidadesDonadas(2, ARBOL, ENTREGADA, 3L),
                new UnidadesDonadas(3, ARBOL, EN_ESPERA, 1L)));

        ImpactoCliente impacto = impactoService.impactoDelCliente(7);

        assertThat(impacto.generado().getTexto()).isEqualTo("3 abrigos + 3 árboles");
        assertThat(impacto.entregado().getTexto()).isEqualTo("2 abrigos + 3 árboles");
        assertThat(impacto.enEsperaDePago().getTexto()).isEqualTo("1 árbol");
    }

    @Test
    void aceptaElCodigoDeBoletaComoLoEscribaLaPersona() {
        assertThat(impactoService.normalizarCodigo("WEB-000110")).contains("WEB-000110");
        assertThat(impactoService.normalizarCodigo(" web-110 ")).contains("WEB-000110");
        assertThat(impactoService.normalizarCodigo("110")).contains("WEB-000110");
        assertThat(impactoService.normalizarCodigo("ABC")).isEmpty();
        assertThat(impactoService.normalizarCodigo("1234567")).isEmpty();
        assertThat(impactoService.normalizarCodigo(null)).isEmpty();
    }

    @Test
    void unCodigoMalFormadoNoConsultaLaBase() {
        assertThat(impactoService.consultarBoleta("hola")).isEmpty();
        verify(ventaRepository, never()).findByCodigoComprobante(any());
    }

    @Test
    void unCodigoInexistenteDevuelveVacio() {
        when(ventaRepository.findByCodigoComprobante("WEB-999999")).thenReturn(Optional.empty());

        assertThat(impactoService.consultarBoleta("999999")).isEmpty();
    }
}
