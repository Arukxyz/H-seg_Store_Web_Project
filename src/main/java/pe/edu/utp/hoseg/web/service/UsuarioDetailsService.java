package pe.edu.utp.hoseg.web.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.config.AdministradorAutenticado;
import pe.edu.utp.hoseg.web.config.Sha256ConSaltPasswordEncoder;
import pe.edu.utp.hoseg.web.model.Usuario;
import pe.edu.utp.hoseg.web.repository.UsuarioRepository;

/**
 * Login del panel /admin con la tabla usuario del escritorio. Un ENCARGADO
 * existe pero no tiene el rol del panel: entra y recibe "acceso denegado".
 */
@Service
@Transactional(readOnly = true)
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no registrado"));
        boolean bloqueado = usuarioRepository.minutosDeBloqueo(usuario.getUsername()).isPresent();
        return new AdministradorAutenticado(usuario.getId(), usuario.getNombre(), usuario.getUsername(),
                Sha256ConSaltPasswordEncoder.guardada(usuario.getSalt(), usuario.getPasswordHash()),
                usuario.getRol(), usuario.isActivo(), bloqueado);
    }
}
