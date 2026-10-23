package pe.edu.utp.hoseg.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pe.edu.utp.hoseg.web.config.AdministradorAutenticado;
import pe.edu.utp.hoseg.web.config.ClienteAutenticado;
import pe.edu.utp.hoseg.web.config.Sha256ConSaltPasswordEncoder;
import pe.edu.utp.hoseg.web.dto.*;
import pe.edu.utp.hoseg.web.model.EstadoDonacion;
import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.EstadoResena;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.RolUsuario;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Renderiza cada vista del portal con datos simulados: comprueba que las
 * plantillas compilan contra el modelo que entrega cada controlador y que la
 * seguridad deja pasar (o no) a quien corresponde. Si un rediseño rompe una
 * plantilla, falla aquí y no en la demo.
 */
@WebMvcTest
@ConSeguridadDelPortal
class VistasTest {

    private static final ClienteAutenticado ANA = new ClienteAutenticado(7, "Ana", "ana@correo.pe", "x", false);
    private static final AdministradorAutenticado ADMIN =
            new AdministradorAutenticado(1, "Admin Höség", "admin", "s:h", RolUsuario.ADMINISTRADOR, true, false);
    private static final OffsetDateTime AHORA = OffsetDateTime.parse("2026-10-23T10:00:00-05:00");

    @Autowired private MockMvc mvc;

    // Simulados por @ConSeguridadDelPortal
    @Autowired private UsuarioDetailsService usuarioDetailsService;
    @Autowired private ClienteDetailsService clienteDetailsService;

    @MockitoBean private CatalogoService catalogoService;
    @MockitoBean private ResenaService resenaService;
    @MockitoBean private CarritoService carritoService;
    @MockitoBean private VentaService ventaService;
    @MockitoBean private CafeService cafeService;
    @MockitoBean private NotificacionService notificacionService;
    @MockitoBean private ClienteService clienteService;
    @MockitoBean private RecuperacionService recuperacionService;
    @MockitoBean private ImpactoService impactoService;
    @MockitoBean private ContenidoService contenidoService;
    @MockitoBean private ContactoService contactoService;
    @MockitoBean private BoletinService boletinService;
    @MockitoBean private ReclamacionService reclamacionService;
    @MockitoBean private PanelAdminService panelAdminService;

    // ---------- Bloque 1 ----------

    @Test
    void tienda() throws Exception {
        when(catalogoService.tarjetas(any(FiltroCatalogo.class))).thenReturn(List.of(tarjeta()));
        mvc.perform(get("/tienda").param("impacto", "ABRIGO"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Casaca Älpafill Cusco")))
                .andExpect(content().string(containsString("1 producto")));
    }

    @Test
    void detalleConFormularioDeResenaParaQuienCompro() throws Exception {
        when(catalogoService.detalle("HSG-CAS-001")).thenReturn(detalle());
        when(resenaService.resumen(anyString())).thenReturn(new ResumenResenas(4.5, 2));
        when(resenaService.publicadas(anyString())).thenReturn(List.of(
                new ResenaVista(5, "Abriga", "Muy abrigadora", "María G.", "M", AHORA, false)));
        when(resenaService.formularioPara(eq(7), anyString())).thenReturn(Optional.of(new ResenaForm()));

        mvc.perform(get("/tienda/HSG-CAS-001").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Escribe tu reseña")))
                .andExpect(content().string(containsString("María G.")));
    }

    @Test
    void reseñarExigeSesionDeCliente() throws Exception {
        mvc.perform(post("/tienda/HSG-CAS-001/resenas").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void carritoConAvisoPorLinea() throws Exception {
        when(carritoService.items()).thenReturn(List.of(item()));
        when(carritoService.total()).thenReturn(new BigDecimal("259.90"));
        when(carritoService.impacto()).thenReturn(new ImpactoCompra(1, 0));
        when(ventaService.validarCarrito(any())).thenReturn(List.of(new ProblemaCarrito("HSG-CAS-001", "Solo quedan 0 unidades.")));
        mvc.perform(get("/carrito"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Solo quedan 0 unidades.")))
                .andExpect(content().string(containsString("1 abrigo")));
    }

    @Test
    void confirmarCompraSinSesionLlevaAlLogin() throws Exception {
        mvc.perform(get("/carrito/confirmar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void confirmarCompraConSesion() throws Exception {
        when(carritoService.items()).thenReturn(List.of(item()));
        when(carritoService.total()).thenReturn(new BigDecimal("259.90"));
        when(carritoService.impacto()).thenReturn(new ImpactoCompra(1, 0));
        when(ventaService.datosEnvioIniciales(7)).thenReturn(new DatosEnvioForm());
        when(ventaService.costoEnvio(TipoEnvio.DELIVERY)).thenReturn(new BigDecimal("10.00"));
        when(cafeService.locales()).thenReturn(List.of(new LocalCafe("Höség Café Cusco", "Calle Palacio 110", "8:30 a 19:30", null, null)));

        mvc.perform(get("/carrito/confirmar").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Recojo en tienda")));
    }

    @Test
    void confirmarRegistraLimpiaElCarritoYMuestraElCodigo() throws Exception {
        CompraRegistrada compra = new CompraRegistrada("WEB-000113", new BigDecimal("259.90"), BigDecimal.ZERO,
                new BigDecimal("259.90"), TipoEnvio.RECOJO_TIENDA, new ImpactoCompra(1, 0));
        when(ventaService.confirmarCompra(eq(7), any(), any())).thenReturn(compra);

        mvc.perform(post("/carrito/confirmar").with(user(ANA)).with(csrf())
                        .param("tipo", "RECOJO_TIENDA").param("nombreReceptor", "Ana Torres"))
                .andExpect(redirectedUrl("/compra-exito"))
                .andExpect(flash().attribute("compra", compra));
        verify(carritoService).vaciar();
        verify(notificacionService).pedidoRegistrado("ana@correo.pe", "Ana", compra);

        mvc.perform(get("/compra-exito").with(user(ANA)).flashAttr("compra", compra))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("WEB-000113")));
    }

    @Test
    void deliverySinDireccionVuelveAlFormularioConElError() throws Exception {
        when(carritoService.total()).thenReturn(BigDecimal.ZERO);
        when(carritoService.impacto()).thenReturn(ImpactoCompra.NINGUNO);
        when(ventaService.costoEnvio(any())).thenReturn(BigDecimal.TEN);
        mvc.perform(post("/carrito/confirmar").with(user(ANA)).with(csrf())
                        .param("tipo", "DELIVERY").param("nombreReceptor", "Ana Torres"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Para delivery indica la dirección y el distrito.")));
    }

    // ---------- Bloque 2 ----------

    @Test
    void paginasDeAcceso() throws Exception {
        mvc.perform(get("/login").param("bloqueado", "5"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("5 minuto(s)")));
        mvc.perform(get("/registro")).andExpect(status().isOk());
        mvc.perform(get("/recuperar").param("enviado", "")).andExpect(status().isOk());
        mvc.perform(get("/restablecer").param("token", "vencido"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("venció")));
    }

    @Test
    void registroConEmailYaUsadoMuestraElErrorEnSuCampo() throws Exception {
        when(clienteService.registrar(any())).thenThrow(new CampoInvalidoException("email", "Ya hay una cuenta con este email."));
        mvc.perform(post("/registro").with(csrf())
                        .param("nombre", "Ana").param("apellido", "Torres").param("tipoDoc", "DNI").param("numDoc", "45678912")
                        .param("email", "ana@correo.pe").param("contrasena", "Abrigo2026").param("confirmacion", "Abrigo2026")
                        .param("aceptaTerminos", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registroForm", "email"))
                .andExpect(content().string(containsString("Ya hay una cuenta con este email.")));
    }

    @Test
    void miCuentaConPedidosEImpacto() throws Exception {
        when(clienteService.datos(7)).thenReturn(new DatosClienteForm());
        when(impactoService.impactoDelCliente(7)).thenReturn(new ImpactoCliente(
                new ImpactoCompra(2, 1), new ImpactoCompra(1, 0), ImpactoCompra.NINGUNO));
        when(ventaService.pedidosDe(7)).thenReturn(List.of(new ResumenPedido("WEB-000113", AHORA, EstadoVenta.PENDIENTE,
                new BigDecimal("259.90"), BigDecimal.TEN, new BigDecimal("269.90"), new ImpactoCompra(1, 0), EstadoEnvio.PENDIENTE)));

        mvc.perform(get("/cuenta").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2 abrigos + 1 árbol")))
                .andExpect(content().string(containsString("WEB-000113")));
        mvc.perform(get("/cuenta").param("seccion", "datos").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cambiar contraseña")));
    }

    @Test
    void detalleDePedidoConEnvioYLineaDeTiempo() throws Exception {
        when(ventaService.detallePedido(7, "WEB-000106")).thenReturn(new DetallePedido("WEB-000106", AHORA, EstadoVenta.PAGADO,
                List.of(new LineaPedido("HSG-ACC-005", "Chullo andino", "Única", 3, new BigDecimal("29.90"),
                        new BigDecimal("89.70"), TipoCompromiso.ARBOL)),
                new BigDecimal("89.70"),
                new EnvioVista(TipoEnvio.DELIVERY, EstadoEnvio.EN_CAMINO, "Ana", null, "Av. El Sol 100", null, "Cusco",
                        null, null, BigDecimal.TEN, "Olva", "OL-123", AHORA, null),
                new BigDecimal("99.70"), List.of(donacion()), new ImpactoCompra(0, 3), true));

        mvc.perform(get("/cuenta/pedidos/WEB-000106").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ccatca")))
                .andExpect(content().string(containsString("Reseñar")))
                .andExpect(content().string(containsString("En camino")));
    }

    // ---------- Bloque 3 ----------

    @Test
    void consultaDeImpactoConLineaDeTiempoYCodigoNoEncontrado() throws Exception {
        when(impactoService.consultarBoleta("106")).thenReturn(Optional.of(
                new ConsultaImpacto("WEB-000106", AHORA, EstadoVenta.PAGADO, List.of(donacion()), false)));
        mvc.perform(get("/consulta-impacto").param("boleta", "106"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Lote asignado")))
                .andExpect(content().string(containsString("Ccatca")));
        mvc.perform(get("/consulta-impacto").param("donacion", "999"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No encontramos ese código")));
        mvc.perform(get("/api/impacto").param("boleta", "999")).andExpect(status().isNotFound());
    }

    @Test
    void cafeNosotrosYLegales() throws Exception {
        when(cafeService.carta()).thenReturn(List.of(new CategoriaCarta("Bebidas calientes",
                List.of(new CategoriaCarta.Item("Latte", null, new BigDecimal("9.00"), false)))));
        when(cafeService.arbolesSembrados()).thenReturn(5018L);
        mvc.perform(get("/cafe")).andExpect(status().isOk())
                .andExpect(content().string(containsString("5,018 árboles sembrados")))
                .andExpect(content().string(containsString("agotado hoy")));

        when(impactoService.resumen()).thenReturn(new ResumenImpacto(10, 20, 3, 5));
        when(contenidoService.bloques(any())).thenReturn(Map.of("nosotros.historia",
                new BloqueContenido("nosotros.historia", "Nuestra historia", "Texto", null, AHORA)));
        mvc.perform(get("/nosotros")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Nuestra historia")));

        mvc.perform(get("/terminos")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Términos y condiciones")));
        mvc.perform(get("/politica-rsu")).andExpect(status().isOk());
    }

    @Test
    void contactoBoletinYLibroDeReclamaciones() throws Exception {
        mvc.perform(get("/contacto")).andExpect(status().isOk());
        mvc.perform(post("/contacto").with(csrf()).param("nombre", "Ana"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("mensajeForm", "email", "asunto", "mensaje"));

        when(boletinService.suscribir("ana@correo.pe")).thenReturn(BoletinService.Resultado.SUSCRITO);
        mvc.perform(post("/boletin").with(csrf()).param("email", "ana@correo.pe")
                        .header("Referer", "https://otro-sitio.com/tienda?q=x"))
                .andExpect(redirectedUrl("/tienda?q=x#boletin"));               // nunca sale del portal

        mvc.perform(get("/libro-reclamaciones")).andExpect(status().isOk());
        mvc.perform(get("/libro-reclamaciones").param("registrado", "").flashAttr("numeroHoja", "LR-000001"))
                .andExpect(content().string(containsString("LR-000001")));
    }

    @Test
    void unaRutaInexistenteMuestraLa404SinPedirLogin() throws Exception {
        mvc.perform(get("/no-existe")).andExpect(status().isNotFound());
    }

    // ---------- Bloque 4 ----------

    @Test
    void elPanelExigeAdministrador() throws Exception {
        mvc.perform(get("/admin/panel")).andExpect(redirectedUrlPattern("**/admin/login"));
        mvc.perform(get("/admin/panel").with(user(ANA))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/login").param("sinPermiso", "")).andExpect(status().isOk())
                .andExpect(content().string(containsString("no tiene acceso")));
    }

    @Test
    void vistasDelPanel() throws Exception {
        RequestPostProcessor admin = user(ADMIN);
        when(panelAdminService.indicadores(any(LocalDate.class), any(LocalDate.class))).thenReturn(new IndicadoresPanel(
                2, 3, new BigDecimal("1500.00"), new ImpactoCompra(4, 5), 1, 0));
        when(panelAdminService.pedidos(any(), any())).thenReturn(List.of(new PedidoAdmin("WEB-000113", "Ana Torres", AHORA,
                new BigDecimal("259.90"), BigDecimal.ZERO, EstadoVenta.PENDIENTE, TipoEnvio.RECOJO_TIENDA, EstadoEnvio.PENDIENTE)));
        mvc.perform(get("/admin/panel").with(admin)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin Höség")))
                .andExpect(content().string(containsString("WEB-000113")));

        when(catalogoService.paraAdministrar()).thenReturn(List.of(new ProductoAdmin("HSG-CAS-001", "Casaca", "M", "Casacas",
                TipoCompromiso.ABRIGO, 25, true, false)));
        mvc.perform(get("/admin/catalogo").with(admin)).andExpect(status().isOk());

        when(cafeService.cartaParaEditar()).thenReturn(List.of());
        mvc.perform(get("/admin/cafe").with(admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/cafe").param("pestana", "local").with(admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/cafe").param("pestana", "arboles").with(admin)).andExpect(status().isOk());

        when(contenidoService.todos()).thenReturn(List.of(new BloqueContenido("home.banner", "Banner", "Texto", null, AHORA)));
        when(contenidoService.paraEditar("home.banner")).thenReturn(new ContenidoForm());
        mvc.perform(get("/admin/contenido").param("clave", "home.banner").with(admin)).andExpect(status().isOk());

        when(contactoService.bandeja(any())).thenReturn(List.of(new MensajeAdmin(1, "Ana", "ana@correo.pe", "Tallas",
                "¿Tienen XL?", pe.edu.utp.hoseg.web.model.EstadoMensaje.NUEVO, AHORA, null, null)));
        mvc.perform(get("/admin/mensajes").with(admin)).andExpect(status().isOk())
                .andExpect(content().string(containsString("¿Tienen XL?")));
        mvc.perform(get("/admin/mensajes").param("pestana", "reclamaciones").with(admin)).andExpect(status().isOk());
        mvc.perform(get("/admin/mensajes").param("pestana", "suscriptores").with(admin)).andExpect(status().isOk());

        when(resenaService.paraModerar(any())).thenReturn(List.of(new ResenaModeracion(1, "Casaca", "M", "WEB-000106", 2,
                null, "No me gustó", "Ana Torres", "ana@correo.pe", AHORA, EstadoResena.PUBLICADA, null)));
        mvc.perform(get("/admin/resenas").with(admin)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Ocultar")));
    }

    /** El login real del formulario: Spring Security usa el codificador también para usuarios inexistentes. */
    @Test
    void loginRealDelPanelConElHashDelEscritorio() throws Exception {
        String guardada = new Sha256ConSaltPasswordEncoder().encode("Admin#2026");
        when(usuarioDetailsService.loadUserByUsername("admin")).thenReturn(new AdministradorAutenticado(
                1, "Admin Höség", "admin", guardada, RolUsuario.ADMINISTRADOR, true, false));
        when(usuarioDetailsService.loadUserByUsername("nadie")).thenThrow(new UsernameNotFoundException("x"));

        mvc.perform(formLogin("/admin/login").user("admin").password("Admin#2026"))
                .andExpect(redirectedUrl("/admin/panel"));
        mvc.perform(formLogin("/admin/login").user("admin").password("mala"))
                .andExpect(redirectedUrl("/admin/login?error"));
        mvc.perform(formLogin("/admin/login").user("nadie").password("x"))
                .andExpect(redirectedUrl("/admin/login?error"));
    }

    @Test
    void loginRealDeClienteConBcrypt() throws Exception {
        String hash = new BCryptPasswordEncoder(4).encode("Abrigo2026");
        when(clienteDetailsService.loadUserByUsername("ana@correo.pe"))
                .thenReturn(new ClienteAutenticado(7, "Ana", "ana@correo.pe", hash, false));

        mvc.perform(formLogin("/login").userParameter("email").user("ana@correo.pe").password("Abrigo2026"))
                .andExpect(redirectedUrl("/"));
        mvc.perform(formLogin("/login").userParameter("email").user("ana@correo.pe").password("mala"))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void ocultarUnaResenaRegistraAlAdministrador() throws Exception {
        mvc.perform(post("/admin/resenas/1/ocultar").with(user(ADMIN)).with(csrf()))
                .andExpect(redirectedUrl("/admin/resenas"));
        verify(resenaService).ocultar(1, 1);
    }

    @Test
    void elCsvDeSuscriptoresSeDescarga() throws Exception {
        when(boletinService.csvActivos()).thenReturn("email,suscrito_en\nana@correo.pe,2026-10-23\n");
        mvc.perform(get("/admin/suscriptores.csv").with(user(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(containsString("ana@correo.pe")));
    }

    // ---------- Datos de ejemplo ----------

    private static TarjetaProducto tarjeta() {
        return new TarjetaProducto("HSG-CAS-001", "Casaca Älpafill Cusco", "Älpafill", new BigDecimal("259.90"),
                TipoCompromiso.ABRIGO, List.of("M", "L"), true, null);
    }

    private static DetalleProducto detalle() {
        return new DetalleProducto("HSG-CAS-001", "Casaca Älpafill Cusco", "Casacas", "Älpafill", "Abriga.", null,
                new BigDecimal("259.90"), TipoCompromiso.ABRIGO,
                List.of(new VarianteProducto("HSG-CAS-001", "M", new BigDecimal("259.90"), true, 3, true),
                        new VarianteProducto("HSG-CAS-002", "L", new BigDecimal("259.90"), false, 0, false)),
                true, false, List.of(tarjeta()));
    }

    private static ItemCarrito item() {
        return new ItemCarrito("HSG-CAS-001", "Casaca Älpafill Cusco", "M", new BigDecimal("259.90"), 1,
                TipoCompromiso.ABRIGO, null);
    }

    private static DonacionVista donacion() {
        return new DonacionVista(12, TipoCompromiso.ARBOL, 3, "Chullo andino", EstadoDonacion.ASIGNADA,
                "Ccatca", "Pachamama Raymi", AHORA, null);
    }
}
