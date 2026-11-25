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
import org.springframework.security.core.GrantedAuthority;
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
import java.util.Set;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final BloqueoAccesoService bloqueoAccesoService;

    public SecurityConfig(BloqueoAccesoService bloqueoAccesoService) {
        this.bloqueoAccesoService = bloqueoAccesoService;
    }

    @Bean
    @Order(1)
    SecurityFilterChain cadenaPersonal(HttpSecurity http, UsuarioDetailsService usuarioDetailsService) throws Exception {
        http
                .securityMatcher("/admin/**", "/logistica/**", "/gestion-impacto/**")
                .authenticationManager(gestor(usuarioDetailsService, new Sha256ConSaltPasswordEncoder()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll()
                        .requestMatchers("/admin/**").hasRole(RolUsuario.ADMINISTRADOR.name())
                        .requestMatchers("/logistica/**").hasRole(RolUsuario.LOGISTICA.name())
                        .requestMatchers("/gestion-impacto/**").hasRole(RolUsuario.GESTOR_IMPACTO.name())
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .successHandler(this::ingresoPersonal)
                        .failureHandler(falloPersonal())
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?salir")
                        .permitAll());
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
                        .requestMatchers(
                                "/carrito/entrega",
                                "/carrito/pago",
                                "/carrito/procesar-pago",
                                "/carrito/confirmar",
                                "/compra-exito",
                                "/cuenta/**"
                        ).hasRole(ClienteAutenticado.ROL)
                        .requestMatchers(HttpMethod.POST, "/tienda/*/resenas").hasRole(ClienteAutenticado.ROL)
                        .anyRequest().permitAll())
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

    private AuthenticationFailureHandler falloCliente() {
        return (request, response, error) -> {
            String email = valor(request, "email");
            Optional<Integer> bloqueo = error instanceof LockedException
                    ? bloqueoAccesoService.minutosDeBloqueoDeCliente(email)
                    : bloqueoAccesoService.falloDeCliente(email);
            // Capa adicional (Guava, solo auditoría): fuerza bruta distribuida por IP.
            bloqueoAccesoService.registrarIntentoFallidoDesdeIp(request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/login?" + motivo(error, bloqueo));
        };
    }

    private AuthenticationFailureHandler falloPersonal() {
        return (request, response, error) -> {
            String username = valor(request, "username");
            Optional<Integer> bloqueo = error instanceof LockedException
                    ? bloqueoAccesoService.minutosDeBloqueoDeUsuario(username)
                    : error instanceof DisabledException ? Optional.empty()
                    : bloqueoAccesoService.falloDeUsuario(username);
            bloqueoAccesoService.registrarIntentoFallidoDesdeIp(request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/admin/login?" + motivo(error, bloqueo));
        };
    }

    /** Redirige a cada miembro del personal según su rol; ENCARGADO solo usa el escritorio. */
    private void ingresoPersonal(HttpServletRequest request, HttpServletResponse response,
                                 Authentication autenticacion) throws IOException {
        bloqueoAccesoService.ingresoDeUsuario(autenticacion.getName());
        Set<String> roles = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String ctx = request.getContextPath();
        if (roles.contains("ROLE_" + RolUsuario.ADMINISTRADOR.name())) {
            response.sendRedirect(ctx + "/admin/panel");
        } else if (roles.contains("ROLE_" + RolUsuario.LOGISTICA.name())) {
            response.sendRedirect(ctx + "/logistica/pedidos");
        } else if (roles.contains("ROLE_" + RolUsuario.GESTOR_IMPACTO.name())) {
            // Como los demás roles, entra por la primera opción de su menú
            response.sendRedirect(ctx + "/gestion-impacto/donaciones");
        } else {
            new SecurityContextLogoutHandler().logout(request, response, autenticacion);
            response.sendRedirect(ctx + "/admin/login?sinPermiso");
        }
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
