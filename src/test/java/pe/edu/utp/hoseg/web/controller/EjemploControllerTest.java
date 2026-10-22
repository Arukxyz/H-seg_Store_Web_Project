package pe.edu.utp.hoseg.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.hoseg.web.config.SecurityConfig;
import pe.edu.utp.hoseg.web.service.CarritoService;
import pe.edu.utp.hoseg.web.service.CatalogoService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La página de ejemplo usa el layout base: si compila aquí, una página copiada de ella también. */
@WebMvcTest(EjemploController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("dev")
class EjemploControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CatalogoService catalogoService;

    @MockitoBean
    private CarritoService carritoService;

    @Test
    void laPaginaDeEjemploRecibeNavbarFooterYScriptsDelLayout() throws Exception {
        when(catalogoService.destacados(anyInt())).thenReturn(List.of());

        mvc.perform(get("/ejemplo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Ejemplo · HÖSÉG</title>")))
                .andExpect(content().string(containsString("<a class=\"hs-pill__item is-active\" href=\"/tienda\" data-seccion=\"tienda\">")))
                .andExpect(content().string(containsString("hs-footer")))
                .andExpect(content().string(containsString("/js/navbar.js")))
                .andExpect(content().string(containsString("1 abrigo")))
                .andExpect(content().string(containsString("1 árbol")))
                // Sin estilos ni scripts propios: el layout no añade los de la Home
                .andExpect(content().string(not(containsString("home.css"))))
                .andExpect(content().string(not(containsString("home.js"))));
    }

    @Test
    void losRecursosQuePideElLayoutSeSirvenDesdeLosWebJars() throws Exception {
        mvc.perform(get("/webjars/bootstrap/css/bootstrap.min.css")).andExpect(status().isOk());
        mvc.perform(get("/webjars/bootstrap-icons/font/bootstrap-icons.min.css")).andExpect(status().isOk());
        // La hoja de iconos pide la fuente con ruta relativa: fonts/bootstrap-icons.woff2
        mvc.perform(get("/webjars/bootstrap-icons/font/fonts/bootstrap-icons.woff2")).andExpect(status().isOk());
    }
}
