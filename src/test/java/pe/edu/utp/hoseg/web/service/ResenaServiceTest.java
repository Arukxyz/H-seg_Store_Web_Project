package pe.edu.utp.hoseg.web.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.hoseg.web.dto.ResenaForm;
import pe.edu.utp.hoseg.web.model.DetalleVenta;
import pe.edu.utp.hoseg.web.model.EstadoResena;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.Resena;
import pe.edu.utp.hoseg.web.repository.DetalleVentaRepository;
import pe.edu.utp.hoseg.web.repository.ProductoRepository;
import pe.edu.utp.hoseg.web.repository.ResenaRepository;
import pe.edu.utp.hoseg.web.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    private static final String MODELO = "Casaca Älpafill Cusco";

    @Mock private ResenaRepository resenaRepository;
    @Mock private DetalleVentaRepository detalleVentaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ResenaService resenaService;

    @Test
    void sinUnaCompraPagadaDelModeloNoSePuedeResenar() {
        when(productoRepository.findById("HSG-CAS-002")).thenReturn(Optional.of(casaca()));
        when(resenaRepository.delClienteParaModelo(7, MODELO)).thenReturn(Optional.empty());
        when(detalleVentaRepository.comprasDelModelo(7, MODELO, EstadoVenta.PAGADO)).thenReturn(List.of());

        assertThatThrownBy(() -> resenaService.guardar(7, "HSG-CAS-002", form(5, "Muy abrigadora")))
                .isInstanceOf(NegocioException.class);
        verify(resenaRepository, never()).save(any());
    }

    @Test
    void conUnaCompraPagadaDeOtraTallaSePublicaAlInstante() {
        DetalleVenta lineaTallaM = new DetalleVenta();
        when(productoRepository.findById("HSG-CAS-002")).thenReturn(Optional.of(casaca()));
        when(resenaRepository.delClienteParaModelo(7, MODELO)).thenReturn(Optional.empty());
        when(detalleVentaRepository.comprasDelModelo(7, MODELO, EstadoVenta.PAGADO)).thenReturn(List.of(lineaTallaM));

        resenaService.guardar(7, "HSG-CAS-002", form(4, "  Muy abrigadora  "));

        ArgumentCaptor<Resena> guardada = ArgumentCaptor.forClass(Resena.class);
        verify(resenaRepository).save(guardada.capture());
        assertThat(guardada.getValue().getDetalleVenta()).isSameAs(lineaTallaM);
        assertThat(guardada.getValue().getEstado()).isEqualTo(EstadoResena.PUBLICADA);
        assertThat(guardada.getValue().getCalificacion()).isEqualTo(4);
        assertThat(guardada.getValue().getComentario()).isEqualTo("Muy abrigadora");
        assertThat(guardada.getValue().getActualizadoEn()).isNull();
    }

    @Test
    void siYaResenoElModeloSeEditaLaMismaResena() {
        Resena existente = new Resena();
        existente.setCalificacion(2);
        existente.setComentario("Me quedó grande");
        when(productoRepository.findById("HSG-CAS-002")).thenReturn(Optional.of(casaca()));
        when(resenaRepository.delClienteParaModelo(7, MODELO)).thenReturn(Optional.of(existente));

        resenaService.guardar(7, "HSG-CAS-002", form(5, "La cambié por talla M y quedó perfecta"));

        verify(resenaRepository).save(existente);
        assertThat(existente.getCalificacion()).isEqualTo(5);
        assertThat(existente.getActualizadoEn()).isNotNull();
    }

    @Test
    void elFormularioSoloSeOfreceAQuienComproYPrecargaLaResenaExistente() {
        when(resenaRepository.delClienteParaModelo(1, MODELO)).thenReturn(Optional.empty());
        when(detalleVentaRepository.comprasDelModelo(1, MODELO, EstadoVenta.PAGADO)).thenReturn(List.of());
        Resena existente = new Resena();
        existente.setCalificacion(3);
        existente.setComentario("Bien");
        when(resenaRepository.delClienteParaModelo(2, MODELO)).thenReturn(Optional.of(existente));

        assertThat(resenaService.formularioPara(null, MODELO)).isEmpty();       // sin sesión
        assertThat(resenaService.formularioPara(1, MODELO)).isEmpty();          // no lo compró
        assertThat(resenaService.formularioPara(2, MODELO)).get()
                .extracting(ResenaForm::getCalificacion).isEqualTo(3);
    }

    private static Producto casaca() {
        Producto p = new Producto();
        p.setCodigo("HSG-CAS-002");
        p.setNombre(MODELO);
        return p;
    }

    private static ResenaForm form(int calificacion, String comentario) {
        ResenaForm f = new ResenaForm();
        f.setCalificacion(calificacion);
        f.setComentario(comentario);
        return f;
    }
}
