package pe.edu.utp.hoseg.web.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

/**
 * Cliente con sesión iniciada. Los controladores lo reciben con
 * {@code @AuthenticationPrincipal ClienteAutenticado cliente} y pasan
 * {@link #id()} a los servicios; las vistas usan {@code ${#authentication.principal.nombre}}.
 */
public record ClienteAutenticado(Integer id, String nombre, String email, String passwordHash, boolean bloqueado)
        implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ROL = "CLIENTE";

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + ROL));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !bloqueado;
    }

    /** Para Thymeleaf: ${#authentication.principal.nombre}. */
    public String getNombre() {
        return nombre;
    }

    /** Inicial del avatar de la barra de navegación. */
    public String getInicial() {
        return nombre.substring(0, 1).toUpperCase();
    }
}
