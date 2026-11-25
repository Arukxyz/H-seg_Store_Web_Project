package pe.edu.utp.hoseg.web.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.utp.hoseg.web.model.RolUsuario;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

/**
 * Personal con sesión en el panel /admin. Los controladores del panel lo
 * reciben con {@code @AuthenticationPrincipal AdministradorAutenticado admin}
 * y pasan {@link #id()} a los servicios para registrar quién hizo cada cambio.
 */
public record AdministradorAutenticado(Integer id, String nombre, String username, String contrasenaGuardada,
                                       RolUsuario rol, boolean activo, boolean bloqueado)
        implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return contrasenaGuardada;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !bloqueado;
    }

    public String getNombre() {
        return nombre;
    }
}
