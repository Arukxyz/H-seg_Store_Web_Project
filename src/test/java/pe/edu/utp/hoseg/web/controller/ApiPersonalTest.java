package pe.edu.utp.hoseg.web.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pe.edu.utp.hoseg.web.config.AdministradorAutenticado;
import pe.edu.utp.hoseg.web.config.ApiSecurityConfig;
import pe.edu.utp.hoseg.web.config.JwtPersonalService;
import pe.edu.utp.hoseg.web.config.Sha256ConSaltPasswordEncoder;
import pe.edu.utp.hoseg.web.dto.ImpactoCompra;
import pe.edu.utp.hoseg.web.dto.IndicadoresPanel;
import pe.edu.utp.hoseg.web.model.RolUsuario;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.service.BloqueoAccesoService;
import pe.edu.utp.hoseg.web.service.CarritoService;
import pe.edu.utp.hoseg.web.service.GestorImpactoService;
import pe.edu.utp.hoseg.web.service.LogisticaService;
import pe.edu.utp.hoseg.web.service.PanelAdminService;
import pe.edu.utp.hoseg.web.service.UsuarioDetailsService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * API del personal con JWT (ApiSecurityConfig): POST /auth/login entrega el token
 * y cada ruta /api/... exige un token válido con el rol que le corresponde.
 */
@WebMvcTest(controllers = {AuthApiController.class, ApiPersonalController.class})
@ConSeguridadDelPortal
@Import({ApiSecurityConfig.class, JwtPersonalService.class})
@TestPropertySource(properties = "hoseg.jwt.secret=clave-solo-para-pruebas-de-la-api-del-personal-hoseg")
class ApiPersonalTest {

    private static final String CONTRASENA = "Clave#2026";

    @Autowired private MockMvc mvc;

    // Simulados por @ConSeguridadDelPortal
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private BloqueoAccesoService bloqueoAccesoService;

    // Lo pide AtributosGlobales, que se carga con cualquier controlador
    @MockitoBean private CarritoService carritoService;
    @MockitoBean private PanelAdminService panelAdminService;
    @MockitoBean private LogisticaService logisticaService;
    @MockitoBean private GestorImpactoService gestorImpactoService;

    @Test
    void loginCorrectoEntregaUnTokenQueAbreLasRutasDeSuRol() throws Exception {
        registrar("admin", RolUsuario.ADMINISTRADOR);
        when(panelAdminService.indicadores(any(), any())).thenReturn(new IndicadoresPanel(
                2, 3, new BigDecimal("1500.00"), new ImpactoCompra(4, 5), 1, 0));

        String token = login("admin")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.expiraEnSegundos").value(8 * 3600))
                .andReturn().getResponse().getContentAsString();
        String bearer = "Bearer " + JsonPath.read(token, "$.token");
        verify(bloqueoAccesoService).ingresoDeUsuario("admin");

        mvc.perform(get("/api/personal/yo").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ADMINISTRADOR"));
        mvc.perform(get("/api/admin/resumen").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidosHoy").value(2));
        // Token válido, pero el rol no alcanza
        mvc.perform(get("/api/logistica/pedidos").header("Authorization", bearer))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(containsString("rol")));
    }

    @Test
    void cadaRolSoloEntraASuRuta() throws Exception {
        registrar("almacen", RolUsuario.LOGISTICA);
        when(logisticaService.pedidosPagadosPorTipo(TipoEnvio.DELIVERY)).thenReturn(List.of());
        String bearer = bearerDe("almacen");

        mvc.perform(get("/api/logistica/pedidos").header("Authorization", bearer)).andExpect(status().isOk());
        mvc.perform(get("/api/admin/resumen").header("Authorization", bearer)).andExpect(status().isForbidden());
        mvc.perform(get("/api/impacto/lotes").header("Authorization", bearer)).andExpect(status().isForbidden());
    }

    @Test
    void contrasenaIncorrectaCuentaElIntentoFallido() throws Exception {
        registrar("admin", RolUsuario.ADMINISTRADOR);

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario o contraseña incorrectos."));
        verify(bloqueoAccesoService).falloDeUsuario("admin");
        verify(bloqueoAccesoService).registrarIntentoFallidoDesdeIp(anyString());
        verify(bloqueoAccesoService, never()).ingresoDeUsuario(anyString());

        // Al tercer intento el servicio bloquea la cuenta: 423
        when(bloqueoAccesoService.falloDeUsuario("admin")).thenReturn(Optional.of(5));
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"mala\"}"))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.error").value(containsString("5 minutos")));
    }

    @Test
    void usuarioInexistenteNoRecibeToken() throws Exception {
        when(usuarioDetailsService.loadUserByUsername("nadie")).thenThrow(new UsernameNotFoundException("x"));

        login("nadie").andExpect(status().isUnauthorized());
    }

    @Test
    void elEncargadoSoloUsaElEscritorio() throws Exception {
        registrar("caja", RolUsuario.ENCARGADO);

        login("caja")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.token").doesNotExist());
        verify(bloqueoAccesoService, never()).ingresoDeUsuario(anyString());
    }

    @Test
    void sinTokenOConTokenFalsoResponde401() throws Exception {
        mvc.perform(get("/api/personal/yo"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"))
                .andExpect(jsonPath("$.error").exists());
        mvc.perform(get("/api/personal/yo").header("Authorization", "Bearer no.es.un-token"))
                .andExpect(status().isUnauthorized());
        // Desde el navegador se usa la página de error del portal, no JSON
        mvc.perform(get("/api/admin/resumen").accept(MediaType.TEXT_HTML))
                .andExpect(status().isUnauthorized());
    }

    private void registrar(String username, RolUsuario rol) {
        String guardada = new Sha256ConSaltPasswordEncoder().encode(CONTRASENA);
        when(usuarioDetailsService.loadUserByUsername(username))
                .thenReturn(new AdministradorAutenticado(1, "Personal Höség", username, guardada, rol, true, false));
    }

    private ResultActions login(String username) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + CONTRASENA + "\"}"));
    }

    private String bearerDe(String username) throws Exception {
        String respuesta = login(username).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(respuesta, "$.token");
    }
}
