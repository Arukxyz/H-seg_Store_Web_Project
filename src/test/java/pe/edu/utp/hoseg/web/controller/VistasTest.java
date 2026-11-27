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
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.RolUsuario;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.service.*;
import pe.edu.utp.hoseg.web.service.BoletaPdfService;
import pe.edu.utp.hoseg.web.service.LogisticaService;
import pe.edu.utp.hoseg.web.config.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    @MockitoBean private LogisticaService logisticaService;
    @MockitoBean private GestorImpactoService gestorImpactoService;
    @MockitoBean private BoletaPdfService boletaPdfService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private ConsultaDocumentoService consultaDocumentoService;


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
    void detalleParaUnVisitanteConLaTallaDeLaUrlElegidaYAvisoDeResena() throws Exception {
        when(catalogoService.detalle("HSG-CAS-001")).thenReturn(detalle());
        when(resenaService.resumen(anyString())).thenReturn(new ResumenResenas(null, 0));

        mvc.perform(get("/tienda/HSG-CAS-001"))
                .andExpect(status().isOk())
                // M disponible y elegida; L agotada y deshabilitada
                .andExpect(content().string(matchesRegex(
                        "(?s).*value=\"HSG-CAS-001\"\\s+data-precio=\"259.90\"\\s+data-maximo=\"3\"\\s+data-pocas=\"true\"\\s+checked.*")))
                .andExpect(content().string(containsString("data-maximo=\"0\" data-pocas=\"false\" disabled=\"disabled\"")))
                .andExpect(content().string(containsString("Esta compra abriga a 1 niño en Cusco.")))
                .andExpect(content().string(containsString("Guía de tallas")))
                .andExpect(content().string(containsString("Este producto aún no tiene reseñas.")))
                .andExpect(content().string(containsString("para dejar tu reseña")))
                .andExpect(content().string(not(containsString("Escribe tu reseña"))));
    }

    @Test
    void tiendaPaginaYConservaLosFiltrosEnElPaginador() throws Exception {
        when(catalogoService.tarjetas(any(FiltroCatalogo.class))).thenReturn(Collections.nCopies(14, tarjeta()));
        mvc.perform(get("/tienda").param("impacto", "ABRIGO").param("porPagina", "6").param("pagina", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mostrando 7–12 de 14 productos")))
                .andExpect(content().string(containsString("aria-current=\"page\"")))
                // El paginador va dentro del formulario de filtros: así conserva impacto y orden
                .andExpect(content().string(matchesRegex("(?s).*<form[^>]*data-tienda-filtros.*name=\"pagina\".*</form>.*")));
    }

    @Test
    void tiendaConCabeceraParcialDevuelveSoloLosResultados() throws Exception {
        when(catalogoService.tarjetas(any(FiltroCatalogo.class))).thenReturn(List.of(tarjeta()));
        mvc.perform(get("/tienda").header("X-Hoseg-Parcial", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-tienda-resultados")))
                .andExpect(content().string(containsString("Casaca Älpafill Cusco")))
                .andExpect(content().string(not(containsString("<html"))))
                .andExpect(content().string(not(containsString("Ropa que abriga dos veces."))));
    }

    @Test
    void tiendaSinResultados() throws Exception {
        mvc.perform(get("/tienda").param("q", "zzz"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No encontramos productos con esos filtros.")))
                .andExpect(content().string(containsString("Limpiar filtros")));
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

        mvc.perform(get("/carrito/entrega").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Recojo en tienda")));
    }


   @Test
    void confirmarRegistraLimpiaElCarritoYMuestraElCodigo() throws Exception {
        DatosEnvioForm envio = new DatosEnvioForm();
        envio.setTipo(TipoEnvio.RECOJO_TIENDA);
        envio.setNombreReceptor("Ana Torres");

        CompraRegistrada compra = new CompraRegistrada("WEB-000113", new BigDecimal("259.90"), BigDecimal.ZERO,
                new BigDecimal("259.90"), TipoEnvio.RECOJO_TIENDA, new ImpactoCompra(1, 0),
                "YAPE", "TRX-123456");
        when(ventaService.pagarYRegistrar(eq(7), any(), any(), eq("YAPE"))).thenReturn(compra);

        mvc.perform(post("/carrito/procesar-pago").with(user(ANA)).with(csrf())
                        .sessionAttr("datosEnvioCheckout", envio)
                        .param("medioPago", "BILLETERA")
                        .param("billetera", "YAPE")
                        .param("celular", "987654321"))
                .andExpect(redirectedUrl("/compra-exito"))
                .andExpect(flash().attribute("compra", compra));
        verify(carritoService).vaciar();
        verify(notificacionService).pedidoRegistrado("ana@correo.pe", "Ana", compra);

        mvc.perform(get("/compra-exito").with(user(ANA)).flashAttr("compra", compra))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("WEB-000113")))
                .andExpect(content().string(containsString("TRX-123456")));
    }

     @Test
    void deliverySinDireccionVuelveAlFormularioConElError() throws Exception {
        when(carritoService.total()).thenReturn(BigDecimal.ZERO);
        when(carritoService.impacto()).thenReturn(ImpactoCompra.NINGUNO);
        when(ventaService.costoEnvio(any())).thenReturn(BigDecimal.TEN);
        mvc.perform(post("/carrito/entrega").with(user(ANA)).with(csrf())
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
        // Celular y dirección son opcionales (DatosClienteForm): el navegador no debe exigirlos.
        mvc.perform(get("/cuenta").param("seccion", "datos").with(user(ANA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cambiar contraseña")))
                .andExpect(content().string(not(matchesRegex("(?s).*id=\"telefono\"[^>]*required.*"))))
                .andExpect(content().string(not(matchesRegex("(?s).*id=\"direccion\"[^>]*required.*"))));
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
                .andExpect(content().string(containsString("/tienda/HSG-ACC-005#resenas")))
                .andExpect(content().string(containsString("hs-linea-tiempo")))
                .andExpect(content().string(containsString("Total pagado")))
                // Provincia y departamento vienen null: la ubicación no debe imprimir "null".
                .andExpect(content().string(containsString("Cusco")))
                .andExpect(content().string(not(containsString(", null"))))
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
                .andExpect(content().string(containsString("data-valor=\"5018\">5,018</strong>")))
                .andExpect(content().string(containsString("árboles sembrados")))
                .andExpect(content().string(containsString("Agotado hoy")));

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
        // El panel no lista los pedidos: solo los deja como datos para los gráficos
        mvc.perform(get("/admin/panel").with(admin)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Admin Höség")))
                .andExpect(content().string(containsString("data-pedido")))
                .andExpect(content().string(not(containsString("WEB-000113"))));
        // La tabla vive en su propia pantalla, con el mismo rango de fechas (aunque llegue invertido)
        mvc.perform(get("/admin/pedidos").param("desde", "2026-10-23").param("hasta", "2026-10-01").with(admin))
                .andExpect(status().isOk())
                .andExpect(model().attribute("desde", LocalDate.of(2026, 10, 1)))
                .andExpect(model().attribute("hasta", LocalDate.of(2026, 10, 23)))
                .andExpect(content().string(containsString("WEB-000113")))
                .andExpect(content().string(containsString("Ana Torres")));
        verify(panelAdminService).pedidos(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 23));
        mvc.perform(get("/admin/pedidos").with(user(ANA))).andExpect(status().isForbidden());

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

    @Test
    void elExcelDeSuscriptoresSeDescarga() throws Exception {
        byte[] excel = {1, 2, 3};
        when(boletinService.exportarExcelActivos()).thenReturn(excel);
        mvc.perform(get("/admin/suscriptores.xlsx").with(user(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", containsString("suscriptores-hoseg.xlsx")))
                .andExpect(content().bytes(excel));
    }

    // ---------- Logística ----------

    private static final AdministradorAutenticado LOGISTICA =
            new AdministradorAutenticado(2, "Lucía Logística", "logistica", "s:h", RolUsuario.LOGISTICA, true, false);

    @Test
    void elAvisoDeEstadoDelPedidoSaleUnaSolaVez() throws Exception {
        mvc.perform(get("/logistica/pedidos").with(user(LOGISTICA))
                        .flashAttr("exito", "Estado del pedido actualizado a En ruta."))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Estado del pedido actualizado a En ruta.")))
                .andExpect(content().string(not(matchesRegex(
                        "(?s).*actualizado a En ruta\\..*actualizado a En ruta\\..*"))));
    }

    @Test
    void elInventarioOfreceEditarCadaProducto() throws Exception {
        Producto casaca = new Producto();
        casaca.setCodigo("HSG-CAS-001");
        casaca.setNombre("Casaca Älpafill Cusco");
        casaca.setCategoria("Casacas");
        casaca.setColeccion("Älpafill");
        casaca.setTalla("M");
        casaca.setPrecio(new BigDecimal("259.90"));
        casaca.setStockComercial(10);
        casaca.setStockMinimo(3);
        casaca.setTipoCompromiso(TipoCompromiso.ABRIGO);
        when(logisticaService.inventarioActivo()).thenReturn(List.of(casaca));
        when(catalogoService.categorias()).thenReturn(List.of("Casacas", "Chullos"));
        when(catalogoService.colecciones()).thenReturn(List.of("Älpafill"));

        mvc.perform(get("/logistica/inventario").with(user(LOGISTICA)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"modal-editar-producto\"")))
                .andExpect(content().string(containsString("data-codigo=\"HSG-CAS-001\"")))
                .andExpect(content().string(containsString("data-precio=\"259.90\"")))
                .andExpect(content().string(containsString("<option value=\"Chullos\">Chullos</option>")));
    }

    @Test
    void editarUnProductoLoGuardaYVuelveAlInventario() throws Exception {
        mvc.perform(post("/logistica/inventario/HSG-CAS-001/editar").with(user(LOGISTICA)).with(csrf())
                        .param("nombre", "Casaca Älpafill Cusco").param("categoria", "Casacas")
                        .param("coleccion", "").param("talla", "M").param("precio", "249.90")
                        .param("stockMinimo", "4").param("tipoCompromiso", ""))
                .andExpect(redirectedUrl("/logistica/inventario"))
                .andExpect(flash().attribute("exito", "Producto HSG-CAS-001 actualizado."));
        verify(logisticaService).editarProducto(eq("HSG-CAS-001"), any(ProductoEdicionForm.class));
    }

    @Test
    void editarConPrecioInvalidoNoLlegaAlServicio() throws Exception {
        mvc.perform(post("/logistica/inventario/HSG-CAS-001/editar").with(user(LOGISTICA)).with(csrf())
                        .param("nombre", "Casaca").param("categoria", "Casacas")
                        .param("precio", "0").param("stockMinimo", "3"))
                .andExpect(redirectedUrl("/logistica/inventario"))
                .andExpect(flash().attribute("error", "El precio debe ser mayor a cero."));
        verify(logisticaService, never()).editarProducto(anyString(), any());
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
