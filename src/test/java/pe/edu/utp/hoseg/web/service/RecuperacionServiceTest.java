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
import pe.edu.utp.hoseg.web.dto.RestablecerContrasenaForm;
import pe.edu.utp.hoseg.web.model.Cliente;
import pe.edu.utp.hoseg.web.model.TokenRecuperacion;
import pe.edu.utp.hoseg.web.repository.ClienteRepository;
import pe.edu.utp.hoseg.web.repository.TokenRecuperacionRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecuperacionServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private TokenRecuperacionRepository tokenRepository;
    @Mock private NotificacionService notificacionService;
    @Spy private PasswordEncoder codificador = new BCryptPasswordEncoder(4);

    @InjectMocks
    private RecuperacionService recuperacionService;

    @Test
    void unEmailDesconocidoOSinCuentaNoRecibeNadaPeroLaRespuestaEsLaMisma() {
        when(clienteRepository.findByEmailIgnoreCase("nadie@correo.pe")).thenReturn(Optional.empty());
        when(clienteRepository.findByEmailIgnoreCase("historico@correo.pe")).thenReturn(Optional.of(new Cliente()));

        recuperacionService.solicitar("nadie@correo.pe");
        recuperacionService.solicitar("historico@correo.pe");                   // sin contraseña: debe registrarse

        verifyNoInteractions(tokenRepository, notificacionService);
    }

    @Test
    void guardaSoloElHashDelTokenYEnviaElTokenPorCorreo() {
        Cliente cliente = cliente();
        when(clienteRepository.findByEmailIgnoreCase("ana@correo.pe")).thenReturn(Optional.of(cliente));

        recuperacionService.solicitar("ana@correo.pe");

        ArgumentCaptor<TokenRecuperacion> guardado = ArgumentCaptor.forClass(TokenRecuperacion.class);
        verify(tokenRepository).save(guardado.capture());
        ArgumentCaptor<String> enviado = ArgumentCaptor.forClass(String.class);
        verify(notificacionService).recuperarContrasena(eq("ana@correo.pe"), eq("Ana"), enviado.capture());

        String token = enviado.getValue();
        assertThat(guardado.getValue().getTokenHash())
                .matches("^[0-9a-f]{64}$")                                      // token_recuperacion_token_hash_check
                .isEqualTo(RecuperacionService.sha256(token))
                .isNotEqualTo(token);
        assertThat(guardado.getValue().getExpiraEn()).isAfter(OffsetDateTime.now().plusMinutes(59));
    }

    @Test
    void conUnEnlaceVencidoNoSeCambiaNada() {
        when(tokenRepository.buscarVigente(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recuperacionService.restablecer(form("vencido")))
                .isInstanceOf(NegocioException.class);
        verify(clienteRepository, never()).reiniciarIntentos(any());
    }

    @Test
    void conUnEnlaceVigenteCambiaLaContrasenaLoGastaYDesbloquea() {
        Cliente cliente = cliente();
        TokenRecuperacion registro = new TokenRecuperacion();
        registro.setCliente(cliente);
        when(tokenRepository.buscarVigente(RecuperacionService.sha256("vigente"))).thenReturn(Optional.of(registro));

        recuperacionService.restablecer(form("vigente"));

        assertThat(codificador.matches("Nueva2026", cliente.getPasswordHash())).isTrue();
        verify(tokenRepository).invalidarPendientes(cliente.getId());
        verify(clienteRepository).reiniciarIntentos("ana@correo.pe");
    }

    private static Cliente cliente() {
        Cliente c = new Cliente();
        c.setEmail("ana@correo.pe");
        c.setNombre("Ana");
        c.setPasswordHash("$2a$04$hashanterior");
        return c;
    }

    private static RestablecerContrasenaForm form(String token) {
        RestablecerContrasenaForm f = new RestablecerContrasenaForm();
        f.setToken(token);
        f.setNueva("Nueva2026");
        f.setConfirmacion("Nueva2026");
        return f;
    }
}
