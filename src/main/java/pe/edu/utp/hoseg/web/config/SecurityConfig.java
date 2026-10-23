package pe.edu.utp.hoseg.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import pe.edu.utp.hoseg.web.model.RolUsuario;
import pe.edu.utp.hoseg.web.service.BloqueoAccesoService;
import pe.edu.utp.hoseg.web.service.ClienteDetailsService;
import pe.edu.utp.hoseg.web.service.UsuarioDetailsService;

import java.io.IOException;
import java.util.Optional;

/**
 * Dos inicios de sesión independientes:
 * <ul>
 *   <li>Panel {@code /admin/**}: personal de la tabla usuario (SHA-256 + salt,
 *       igual que el escritorio), solo rol ADMINISTRADOR.</li>
 *   <li>Resto del portal: clientes (BCrypt). El catálogo, la Home, la consulta
 *       de impacto, el Café, las páginas institucionales y los formularios
 *       públicos no exigen sesión; comprar, la cuenta y reseñar, sí.</li>
 * </ul>
 * En ambos, 3 intentos fallidos bloquean 5 minutos (BloqueoAccesoService).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final BloqueoAccesoService bloqueoAccesoService;

    public SecurityConfig(BloqueoAccesoService bloqueoAccesoService) {
        this.bloqueoAccesoService = bloqueoAccesoService;
    }

    @Bean
    @Order(1)
    SecurityFilterChain cadenaAdministracion(HttpSecurity http, UsuarioDetailsService usuarioDetailsService) throws Exception {
        http
                .securityMatcher("/admin/**")
                .authenticationManager(gestor(usuarioDetailsService, new Sha256ConSaltPasswordEncoder()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll()
                        .anyRequest().hasRole(RolUsuario.ADMINISTRADOR.name()))
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .successHandler(this::ingresoAdministrador)
                        .failureHandler(falloAdministrador())
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?salir"));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain cadenaClientes(HttpSecurity http, ClienteDetailsService clienteDetailsService,
                                       PasswordEncoder codificadorContrasena) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler volverADondeIba = new SavedRequestAwareAuthenticationSuccessHandler();
        volverADondeIba.setDefaultTargetUrl("/");
        http
                .authenticationManager(gestor(clienteDetailsService, codificadorContrasena))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/carrito/confirmar", "/compra-exito", "/cuenta/**").hasRole(ClienteAutenticado.ROL)
                        .requestMatchers(HttpMethod.POST, "/tienda/*/resenas").hasRole(ClienteAutenticado.ROL)
                        .requestMatchers("/", "/tienda/**", "/carrito/**", "/consulta-impacto", "/api/impacto",
                                "/login", "/registro", "/recuperar", "/restablecer", "/error", "/ejemplo",
                                // Enlazadas desde el navbar y el footer
                                "/cafe", "/nosotros", "/contacto", "/terminos", "/politica-rsu",
                                "/libro-reclamaciones", "/boletin",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .successHandler((request, response, autenticacion) -> {
                            bloqueoAccesoService.ingresoDeCliente(autenticacion.getName());
                            volverADondeIba.onAuthenticationSuccess(request, response, autenticacion);
                        })
                        .failureHandler(falloCliente())
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/"));
        return http.build();
    }

    /** cliente.password_hash guarda BCrypt (06_web_clientes.sql). */
    @Bean
    PasswordEncoder codificadorContrasena() {
        return new BCryptPasswordEncoder();
    }

    private static AuthenticationManager gestor(UserDetailsService origen,
                                                PasswordEncoder codificador) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(codificador);
        proveedor.setUserDetailsService(origen);
        return new ProviderManager(proveedor);
    }

    /**
     * /login?error (credenciales incorrectas) o /login?bloqueado=5 (minutos).
     * Una cuenta ya bloqueada no suma intentos: el chequeo de bloqueo va antes
     * que el de contraseña, como en el escritorio.
     */
    private AuthenticationFailureHandler falloCliente() {
        return (request, response, error) -> {
            String email = valor(request, "email");
            Optional<Integer> bloqueo = error instanceof LockedException
                    ? bloqueoAccesoService.minutosDeBloqueoDeCliente(email)
                    : bloqueoAccesoService.falloDeCliente(email);
            response.sendRedirect(request.getContextPath() + "/login?" + motivo(error, bloqueo));
        };
    }

    private AuthenticationFailureHandler falloAdministrador() {
        return (request, response, error) -> {
            String username = valor(request, "username");
            Optional<Integer> bloqueo = error instanceof LockedException
                    ? bloqueoAccesoService.minutosDeBloqueoDeUsuario(username)
                    : error instanceof DisabledException ? Optional.empty()
                    : bloqueoAccesoService.falloDeUsuario(username);
            response.sendRedirect(request.getContextPath() + "/admin/login?" + motivo(error, bloqueo));
        };
    }

    /** Un encargado tiene cuenta pero no acceso al panel: se cierra la sesión y se le avisa. */
    private void ingresoAdministrador(HttpServletRequest request, HttpServletResponse response,
                                      Authentication autenticacion) throws IOException {
        bloqueoAccesoService.ingresoDeUsuario(autenticacion.getName());
        boolean esAdministrador = autenticacion.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + RolUsuario.ADMINISTRADOR.name()));
        if (!esAdministrador) {
            new SecurityContextLogoutHandler().logout(request, response, autenticacion);
            response.sendRedirect(request.getContextPath() + "/admin/login?sinPermiso");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin/panel");
    }

    private static String motivo(AuthenticationException error, Optional<Integer> bloqueo) {
        if (bloqueo.isPresent()) {
            return "bloqueado=" + bloqueo.get();
        }
        return error instanceof DisabledException ? "inactivo" : "error";
    }

    private static String valor(HttpServletRequest request, String parametro) {
        String valor = request.getParameter(parametro);
        return valor == null ? "" : valor.trim();
    }
}
