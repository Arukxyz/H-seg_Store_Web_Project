package pe.edu.utp.hoseg.web.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.config.ClienteAutenticado;
import pe.edu.utp.hoseg.web.model.Cliente;
import pe.edu.utp.hoseg.web.repository.ClienteRepository;

/**
 * Login de clientes (/login, el email es el usuario). Los compradores
 * históricos sin contraseña no pueden entrar hasta registrarse; para Spring
 * Security es lo mismo que un email inexistente ("credenciales incorrectas").
 */
@Service
@Transactional(readOnly = true)
public class ClienteDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    public ClienteDetailsService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Cliente cliente = clienteRepository.findByEmailIgnoreCase(email.trim())
                .filter(Cliente::puedeIniciarSesion)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente no registrado"));
        boolean bloqueado = clienteRepository.minutosDeBloqueo(cliente.getEmail()).isPresent();
        return new ClienteAutenticado(cliente.getId(), cliente.getNombre(), cliente.getEmail(),
                cliente.getPasswordHash(), bloqueado);
    }
}
