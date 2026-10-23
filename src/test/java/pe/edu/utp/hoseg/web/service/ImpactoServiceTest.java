package pe.edu.utp.hoseg.web.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.hoseg.web.dto.ImpactoCliente;
import pe.edu.utp.hoseg.web.dto.UnidadesDonadas;
import pe.edu.utp.hoseg.web.repository.ComunidadRepository;
import pe.edu.utp.hoseg.web.repository.DonacionRepository;
import pe.edu.utp.hoseg.web.repository.LoteDonacionRepository;
import pe.edu.utp.hoseg.web.repository.VentaRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private ImpactoService impactoService;

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
}
