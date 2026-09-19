package pe.edu.utp.hoseg.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.utp.hoseg.web.config.SecurityConfig;
import pe.edu.utp.hoseg.web.dto.ResumenImpacto;
import pe.edu.utp.hoseg.web.dto.TarjetaProducto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.service.CarritoService;
import pe.edu.utp.hoseg.web.service.CatalogoService;
import pe.edu.utp.hoseg.web.service.ImpactoService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Renderiza la Home con datos simulados: verifica que las plantillas compilan y muestran lo que deben. */
@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CatalogoService catalogoService;

    @MockitoBean
    private ImpactoService impactoService;

    @MockitoBean
    private CarritoService carritoService;

    @Test
    void laHomeEsPublicaYMuestraDestacadosImpactoYComunidades() throws Exception {
        when(catalogoService.destacados(anyInt())).thenReturn(List.of(
                new TarjetaProducto("HSG-CAS-001", "Casaca Älpafill Cusco", "Älpafill", new BigDecimal("259.90"),
                        TipoCompromiso.ABRIGO, List.of("M", "L", "XL"), true, null),
                new TarjetaProducto("HSG-ACC-002", "Bufanda Paucartambo", "Comunidad", new BigDecimal("39.90"),
                        TipoCompromiso.ARBOL, List.of("Única"), false, null),
                new TarjetaProducto("HSG-ACC-007", "Mochila artesanal Höség", "Urbana", new BigDecimal("1079.90"),
                        null, List.of("Única"), true, null)));
        when(impactoService.resumen()).thenReturn(new ResumenImpacto(1248, 874, 5, 2104));
        when(impactoService.nombresComunidades()).thenReturn(List.of("Ccatca", "Layo", "Omacha"));
        when(carritoService.totalUnidades()).thenReturn(2);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Abriga a alguien más con cada compra.")))
                // ProductCard: nombre, precio con formato S/, tallas y badge
                .andExpect(content().string(containsString("Casaca Älpafill Cusco")))
                .andExpect(content().string(containsString("S/ 259.90")))
                .andExpect(content().string(containsString("S/ 1,079.90")))
                .andExpect(content().string(containsString("1 abrigo")))
                .andExpect(content().string(containsString("1 árbol")))
                .andExpect(content().string(containsString("hs-card--agotado")))
                .andExpect(content().string(containsString("/tienda/HSG-CAS-001")))
                // ImpactCounters y comunidades
                .andExpect(content().string(containsString("data-valor=\"1248\"")))
                .andExpect(content().string(containsString("1,248")))
                .andExpect(content().string(containsString("Omacha")))
                // Navbar: badge del carrito y estado no autenticado
                .andExpect(content().string(containsString("hs-carrito-icono__badge\">2<")))
                .andExpect(content().string(containsString("Ingresar")))
                // ImpactCTA apunta al portal de consulta configurado
                .andExpect(content().string(containsString("action=\"/consulta-impacto\"")));
    }

    @Test
    void sinItemsEnElCarritoNoSeRenderizaElBadge() throws Exception {
        when(catalogoService.destacados(anyInt())).thenReturn(List.of());
        when(impactoService.resumen()).thenReturn(new ResumenImpacto(0, 0, 0, 0));
        when(impactoService.nombresComunidades()).thenReturn(List.of());
        when(carritoService.totalUnidades()).thenReturn(0);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("hs-carrito-icono__badge"))))
                .andExpect(content().string(containsString("Estamos renovando el catálogo")));
    }
}
