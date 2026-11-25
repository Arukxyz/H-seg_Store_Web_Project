package pe.edu.utp.hoseg.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.hoseg.web.dto.CompraRegistrada;
import pe.edu.utp.hoseg.web.dto.DatosEnvioForm;
import pe.edu.utp.hoseg.web.dto.ItemCarrito;
import pe.edu.utp.hoseg.web.dto.ProblemaCarrito;
import pe.edu.utp.hoseg.web.model.Cliente;
import pe.edu.utp.hoseg.web.model.Donacion;
import pe.edu.utp.hoseg.web.model.Envio;
import pe.edu.utp.hoseg.web.model.EstadoDonacion;
import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.OrigenVenta;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.model.Venta;
import pe.edu.utp.hoseg.web.repository.ClienteRepository;
import pe.edu.utp.hoseg.web.repository.DonacionRepository;
import pe.edu.utp.hoseg.web.repository.EnvioRepository;
import pe.edu.utp.hoseg.web.repository.ProductoRepository;
import pe.edu.utp.hoseg.web.repository.VentaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private DonacionRepository donacionRepository;
    @Mock private EnvioRepository envioRepository;
    @Mock private ImpactoService impactoService;

    private VentaService ventaService;

    @BeforeEach
    void crear() {
        ventaService = new VentaService(ventaRepository, productoRepository, clienteRepository, donacionRepository,
                envioRepository, impactoService, "WEB-", "seq_comprobante_web", new BigDecimal("10.00"));
    }

    @Test
    void unCarritoVacioNoSePuedeConfirmar() {
        when(productoRepository.findAllById(anyIterable())).thenReturn(List.of());

        assertThat(ventaService.validarCarrito(List.of()))
                .extracting(ProblemaCarrito::mensaje).containsExactly("Tu carrito está vacío.");
    }

    @Test
    void avisaPorLineaSiElProductoSeOcultoSeAgotoOAlcanzaParaMenos() {
        Producto oculto = producto("HSG-ACC-001", "Gorro", "49.90", 10, TipoCompromiso.ARBOL);
        oculto.setVisibleWeb(false);
        Producto agotado = producto("HSG-ACC-002", "Bufanda", "39.90", 0, TipoCompromiso.ARBOL);
        Producto escaso = producto("HSG-CAS-001", "Casaca", "259.90", 2, TipoCompromiso.ABRIGO);
        when(productoRepository.findAllById(anyIterable())).thenReturn(List.of(oculto, agotado, escaso));

        List<ProblemaCarrito> problemas = ventaService.validarCarrito(List.of(
                item(oculto, 1), item(agotado, 1), item(escaso, 3)));

        assertThat(problemas).extracting(ProblemaCarrito::codigoProducto)
                .containsExactly("HSG-ACC-001", "HSG-ACC-002", "HSG-CAS-001");
        assertThat(problemas.get(2).mensaje()).isEqualTo("Solo quedan 2 unidades de Casaca (Única).");
    }

    @Test
    void confirmarRegistraVentaPendienteDonacionesEnEsperaYEnvioSinTocarElStock() {
        Producto casaca = producto("HSG-CAS-001", "Casaca", "259.90", 10, TipoCompromiso.ABRIGO);
        Producto mochila = producto("HSG-ACC-007", "Mochila", "79.90", 10, null);
        when(productoRepository.findAllById(anyIterable())).thenReturn(List.of(casaca, mochila));
        when(clienteRepository.findById(7)).thenReturn(Optional.of(new Cliente()));
        when(ventaRepository.siguienteComprobante("seq_comprobante_web")).thenReturn(111L);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        // El carrito guardó un precio viejo (199.90): se cobra el de la base.
        ItemCarrito casacaEnSesion = new ItemCarrito("HSG-CAS-001", "Casaca", "M", new BigDecimal("199.90"), 2,
                TipoCompromiso.ABRIGO, null);
        DatosEnvioForm envio = new DatosEnvioForm();
        envio.setTipo(TipoEnvio.DELIVERY);
        envio.setNombreReceptor("Ana Torres");
        envio.setDireccion("Av. El Sol 100");
        envio.setDistrito("Cusco");

        CompraRegistrada compra = ventaService.confirmarCompra(7, List.of(casacaEnSesion, item(mochila, 1)), envio);

        assertThat(compra.codigoComprobante()).isEqualTo("WEB-000111");
        assertThat(compra.totalProductos()).isEqualByComparingTo("599.70");    // 2 × 259.90 + 79.90
        assertThat(compra.costoEnvio()).isEqualByComparingTo("10.00");
        assertThat(compra.totalAPagar()).isEqualByComparingTo("609.70");
        assertThat(compra.impacto().abrigos()).isEqualTo(2);
        assertThat(compra.impacto().arboles()).isZero();

        ArgumentCaptor<Venta> venta = ArgumentCaptor.forClass(Venta.class);
        verify(ventaRepository).save(venta.capture());
        assertThat(venta.getValue().getEstado()).isEqualTo(EstadoVenta.PENDIENTE);
        assertThat(venta.getValue().getOrigen()).isEqualTo(OrigenVenta.WEB);
        assertThat(venta.getValue().getTotal()).isEqualByComparingTo("599.70");  // sin el envío

        ArgumentCaptor<Donacion> donacion = ArgumentCaptor.forClass(Donacion.class);
        verify(donacionRepository, times(1)).save(donacion.capture());          // la mochila no tiene compromiso
        assertThat(donacion.getValue().getEstado()).isEqualTo(EstadoDonacion.EN_ESPERA);
        assertThat(donacion.getValue().getCantidad()).isEqualTo(2);

        ArgumentCaptor<Envio> guardado = ArgumentCaptor.forClass(Envio.class);
        verify(envioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getEstado()).isEqualTo(EstadoEnvio.PENDIENTE);
        assertThat(guardado.getValue().getDireccion()).isEqualTo("Av. El Sol 100");

        verify(productoRepository, never()).save(any());                        // la web no mueve stock
    }

    @Test
    void elRecojoEnTiendaNoCobraEnvioNiGuardaDireccion() {
        Producto gorro = producto("HSG-ACC-001", "Gorro", "49.90", 3, TipoCompromiso.ARBOL);
        when(productoRepository.findAllById(anyIterable())).thenReturn(List.of(gorro));
        when(clienteRepository.findById(7)).thenReturn(Optional.of(new Cliente()));
        when(ventaRepository.siguienteComprobante(any())).thenReturn(5L);
        DatosEnvioForm recojo = new DatosEnvioForm();
        recojo.setNombreReceptor("Ana Torres");
        recojo.setDireccion("Se ignora en recojo");

        CompraRegistrada compra = ventaService.confirmarCompra(7, List.of(item(gorro, 1)), recojo);

        assertThat(compra.costoEnvio()).isEqualByComparingTo("0");
        ArgumentCaptor<Envio> guardado = ArgumentCaptor.forClass(Envio.class);
        verify(envioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getDireccion()).isNull();
    }

    @Test
    void noConfirmaSiElStockYaNoAlcanzaYNoGuardaNada() {
        Producto casaca = producto("HSG-CAS-001", "Casaca", "259.90", 1, TipoCompromiso.ABRIGO);
        when(productoRepository.findAllById(anyIterable())).thenReturn(List.of(casaca));

        assertThatThrownBy(() -> ventaService.confirmarCompra(7, List.of(item(casaca, 2)), new DatosEnvioForm()))
                .isInstanceOf(CarritoInvalidoException.class)
                .hasMessage("Solo quedan 1 unidades de Casaca (Única).");
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void sinSesionNoSeConfirma() {
        assertThatThrownBy(() -> ventaService.confirmarCompra(null, List.of(), new DatosEnvioForm()))
                .isInstanceOf(NegocioException.class);
    }

    private static ItemCarrito item(Producto p, int cantidad) {
        return new ItemCarrito(p.getCodigo(), p.getNombre(), p.getTallaMostrada(), p.getPrecio(), cantidad,
                p.getTipoCompromiso(), null);
    }

    private static Producto producto(String codigo, String nombre, String precio, int stock, TipoCompromiso tipo) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setPrecio(new BigDecimal(precio));
        p.setStockComercial(stock);
        p.setTipoCompromiso(tipo);
        p.setAplicaTripleImpacto(tipo != null);
        p.setActivo(true);
        p.setVisibleWeb(true);
        return p;
    }
}
