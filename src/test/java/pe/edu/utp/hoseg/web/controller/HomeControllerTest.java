package pe.edu.utp.hoseg.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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
@ConSeguridadDelPortal
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
                .andExpect(content().string(containsString("action=\"/consulta-impacto\"")))
                // Navbar: enlace al Café, y en el óvalo móvil Café reemplaza a Carrito
                .andExpect(content().string(containsString("href=\"/cafe\"")))
                .andExpect(content().string(containsString("<span>Café</span>")))
                .andExpect(content().string(not(containsString("<span>Carrito</span>"))))
                // Footer: boletín con token CSRF, legales, contacto, sello B y Libro de Reclamaciones
                .andExpect(content().string(containsString("action=\"/boletin\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("href=\"/contacto\"")))
                .andExpect(content().string(containsString("href=\"/terminos\"")))
                .andExpect(content().string(containsString("href=\"/politica-rsu\"")))
                .andExpect(content().string(containsString("Empresa B · certificación en proceso")))
                .andExpect(content().string(containsString("href=\"/libro-reclamaciones\"")))
                // Layout: título de la página, CSS y JS propios de la Home además de los comunes
                .andExpect(content().string(containsString("<title>HÖSÉG · Abriga a alguien más con cada compra</title>")))
                .andExpect(content().string(containsString("/css/home.css")))
                .andExpect(content().string(containsString("/js/home.js")))
                .andExpect(content().string(containsString("/js/navbar.js")))
                // Iconos: Bootstrap Icons, sin SVG en línea
                .andExpect(content().string(containsString("bootstrap-icons.min.css")))
                .andExpect(content().string(containsString("bi bi-gift-fill")))
                .andExpect(content().string(containsString("bi bi-tree-fill")))
                .andExpect(content().string(containsString("hs-badge--arbol")))
                .andExpect(content().string(not(containsString("<svg"))))
                // Menú desde AtributosGlobales: Nosotros va en la barra pero no en el óvalo
                .andExpect(content().string(containsString("data-seccion=\"nosotros\"")))
                .andExpect(content().string(not(containsString("<span>Nosotros</span>"))))
                .andExpect(content().string(containsString("<a class=\"hs-pill__item is-active\" href=\"/\" data-seccion=\"inicio\">")))
                // Óvalo sin sesión: Cuenta lleva al login
                .andExpect(content().string(containsString("href=\"/login\" data-seccion=\"cuenta\"")));
    }

    @Test
    void lasCifrasDeImpactoSeRenderizanEnLosCuatroContadores() throws Exception {
        when(catalogoService.destacados(anyInt())).thenReturn(List.of());
        when(impactoService.resumen()).thenReturn(new ResumenImpacto(1248, 874, 5, 2104));
        when(impactoService.nombresComunidades()).thenReturn(List.of());

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-valor=\"874\"")))
                .andExpect(content().string(containsString("data-valor=\"5\"")))
                .andExpect(content().string(containsString("2,104")))
                .andExpect(content().string(containsString("Árboles plantados")))
                .andExpect(content().string(containsString("Registramos")));
    }

    @Test
    @WithMockUser(username = "maria.gutierrez@example.com")
    void conSesionElNavbarMuestraLaInicialYElMenuDeCuenta() throws Exception {
        when(catalogoService.destacados(anyInt())).thenReturn(List.of());
        when(impactoService.resumen()).thenReturn(new ResumenImpacto(0, 0, 0, 0));
        when(impactoService.nombresComunidades()).thenReturn(List.of());
        when(carritoService.totalUnidades()).thenReturn(0);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hs-avatar\" aria-hidden=\"true\">M<")))
                .andExpect(content().string(containsString("maria.gutierrez@example.com")))
                .andExpect(content().string(containsString("href=\"/cuenta?seccion=datos\"")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("Cerrar sesión")))
                .andExpect(content().string(containsString("href=\"/cuenta\" data-seccion=\"cuenta\"")))
                .andExpect(content().string(not(containsString("Ingresar"))));
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
