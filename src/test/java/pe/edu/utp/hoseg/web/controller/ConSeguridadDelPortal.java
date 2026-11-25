package pe.edu.utp.hoseg.web.controller;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pe.edu.utp.hoseg.web.config.SecurityConfig;
import pe.edu.utp.hoseg.web.service.BloqueoAccesoService;
import pe.edu.utp.hoseg.web.service.ClienteDetailsService;
import pe.edu.utp.hoseg.web.service.UsuarioDetailsService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Para los @WebMvcTest: aplica las reglas reales de SecurityConfig (qué es
 * público y qué no) con los servicios de login simulados.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SecurityConfig.class)
@MockitoBean(types = {BloqueoAccesoService.class, ClienteDetailsService.class, UsuarioDetailsService.class})
public @interface ConSeguridadDelPortal {
}
