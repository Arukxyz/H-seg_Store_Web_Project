package pe.edu.utp.hoseg.web.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.utp.hoseg.web.dto.CambioContrasenaForm;
import pe.edu.utp.hoseg.web.dto.RegistroClienteForm;
import pe.edu.utp.hoseg.web.model.Cliente;
import pe.edu.utp.hoseg.web.model.TipoDocumento;
import pe.edu.utp.hoseg.web.repository.ClienteRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private NotificacionService notificacionService;
    @Spy private PasswordEncoder codificador = new BCryptPasswordEncoder(4);   // costo bajo: solo pruebas

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void registraUnClienteNuevoConContrasenaBcryptYTerminosAceptados() {
        when(clienteRepository.findByEmailIgnoreCase("ana@correo.pe")).thenReturn(Optional.empty());
        when(clienteRepository.existsByTipoDocAndNumDoc(TipoDocumento.DNI, "45678912")).thenReturn(false);

        clienteService.registrar(form("ana@correo.pe", "45678912"));

        ArgumentCaptor<Cliente> guardado = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(guardado.capture());
        Cliente c = guardado.getValue();
        assertThat(c.getEmail()).isEqualTo("ana@correo.pe");
        assertThat(c.getPasswordHash()).startsWith("$2").isNotEqualTo("Abrigo2026");
        assertThat(codificador.matches("Abrigo2026", c.getPasswordHash())).isTrue();
        assertThat(c.getTerminosAceptadosEn()).isNotNull();
        verify(notificacionService).bienvenida("ana@correo.pe", "Ana");
    }

    @Test
    void unCompradorHistoricoCompletaSuMismaFilaAlRegistrarse() {
        Cliente historico = new Cliente();
        historico.setEmail("Ana@Correo.pe");
        historico.setNombre("Ana Belén");
        when(clienteRepository.findByEmailIgnoreCase("ana@correo.pe")).thenReturn(Optional.of(historico));
        when(clienteRepository.existsByTipoDocAndNumDocAndIdNot(any(), any(), any())).thenReturn(false);

        clienteService.registrar(form("ana@correo.pe", "45678912"));

        verify(clienteRepository).save(historico);
        assertThat(historico.getEmail()).isEqualTo("Ana@Correo.pe");          // no se cambia el email existente
        assertThat(historico.puedeIniciarSesion()).isTrue();
    }

    @Test
    void unEmailConCuentaODocumentoAjenoSeRechazanEnSuCampo() {
        Cliente conCuenta = new Cliente();
        conCuenta.setPasswordHash("$2a$...");
        when(clienteRepository.findByEmailIgnoreCase("ana@correo.pe")).thenReturn(Optional.of(conCuenta));
        when(clienteRepository.findByEmailIgnoreCase("otra@correo.pe")).thenReturn(Optional.empty());
        when(clienteRepository.existsByTipoDocAndNumDoc(TipoDocumento.DNI, "45678912")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.registrar(form("ana@correo.pe", "45678912")))
                .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("email"));
        assertThatThrownBy(() -> clienteService.registrar(form("otra@correo.pe", "45678912")))
                .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("numDoc"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void cambiarContrasenaExigeLaActual() {
        Cliente cliente = new Cliente();
        cliente.setPasswordHash(codificador.encode("Actual2026"));
        when(clienteRepository.findById(7)).thenReturn(Optional.of(cliente));
        CambioContrasenaForm cambio = new CambioContrasenaForm();
        cambio.setActual("Otra2026");
        cambio.setNueva("Nueva2026");

        assertThatThrownBy(() -> clienteService.cambiarContrasena(7, cambio))
                .isInstanceOfSatisfying(CampoInvalidoException.class, e -> assertThat(e.getCampo()).isEqualTo("actual"));

        cambio.setActual("Actual2026");
        clienteService.cambiarContrasena(7, cambio);
        assertThat(codificador.matches("Nueva2026", cliente.getPasswordHash())).isTrue();
    }

    private static RegistroClienteForm form(String email, String dni) {
        RegistroClienteForm f = new RegistroClienteForm();
        f.setNombre("Ana");
        f.setApellido("Torres");
        f.setTipoDoc(TipoDocumento.DNI);
        f.setNumDoc(dni);
        f.setEmail(email);
        f.setContrasena("Abrigo2026");
        f.setConfirmacion("Abrigo2026");
        f.setAceptaTerminos(true);
        return f;
    }
}
