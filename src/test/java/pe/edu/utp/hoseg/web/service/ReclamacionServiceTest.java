package pe.edu.utp.hoseg.web.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.hoseg.web.dto.ReclamoForm;
import pe.edu.utp.hoseg.web.model.TipoBien;
import pe.edu.utp.hoseg.web.model.TipoDocumento;
import pe.edu.utp.hoseg.web.model.TipoReclamo;
import pe.edu.utp.hoseg.web.repository.LibroReclamacionRepository;
import pe.edu.utp.hoseg.web.repository.UsuarioRepository;
import pe.edu.utp.hoseg.web.repository.VentaRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReclamacionServiceTest {

    @Mock private LibroReclamacionRepository reclamoRepository;
    @Mock private VentaRepository ventaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private ReclamacionService reclamacionService;

    @Test
    void unCodigoDePedidoInexistenteSeMarcaEnSuCampoYNoSeGuarda() {
        when(ventaRepository.findByCodigoComprobante("WEB-999999")).thenReturn(Optional.empty());
        ReclamoForm form = form();
        form.setCodigoComprobante(" web-999999 ");

        assertThatThrownBy(() -> reclamacionService.registrar(form))
                .isInstanceOfSatisfying(CampoInvalidoException.class,
                        e -> assertThat(e.getCampo()).isEqualTo("codigoComprobante"));
        verify(reclamoRepository, never()).saveAndFlush(any());
    }

    private static ReclamoForm form() {
        ReclamoForm f = new ReclamoForm();
        f.setTipo(TipoReclamo.RECLAMO);
        f.setNombre("Ana Torres");
        f.setTipoDoc(TipoDocumento.DNI);
        f.setNumDoc("45678912");
        f.setDomicilio("Av. El Sol 100, Cusco");
        f.setEmail("ana@correo.pe");
        f.setTipoBien(TipoBien.PRODUCTO);
        f.setDescripcionBien("Casaca talla M");
        f.setDetalle("Llegó con una costura abierta.");
        f.setPedidoConsumidor("Cambio por otra igual.");
        return f;
    }
}
