package pe.edu.utp.hoseg.web.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.hoseg.web.dto.DetalleProducto;
import pe.edu.utp.hoseg.web.dto.FiltroCatalogo;
import pe.edu.utp.hoseg.web.dto.OrdenCatalogo;
import pe.edu.utp.hoseg.web.dto.TarjetaProducto;
import pe.edu.utp.hoseg.web.dto.VarianteProducto;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.repository.CategoriaRepository;
import pe.edu.utp.hoseg.web.repository.ColeccionRepository;
import pe.edu.utp.hoseg.web.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ColeccionRepository coleccionRepository;

    @InjectMocks
    private CatalogoService catalogoService;

    @Test
    void filtraSinDistinguirTildesNiMayusculasPorImpactoYDisponibilidad() {
        Producto casaca = producto("HSG-CAS-001", "Casaca Älpafill Cusco", "M", "259.90", 5, TipoCompromiso.ABRIGO, true);
        Producto gorro = producto("HSG-ACC-001", "Gorro de lana Layo", null, "49.90", 0, TipoCompromiso.ARBOL, true);
        gorro.setColeccion("Comunidad");
        gorro.setCategoria("Accesorios");
        Producto cafe = producto("HSG-CAF-001", "Café Bourbon", null, "40.00", 50, TipoCompromiso.ARBOL, true);
        cafe.setColeccion(null);
        cafe.setCategoria("Café");
        when(productoRepository.findByActivoTrueAndVisibleWebTrueOrderByCodigoAsc()).thenReturn(List.of(casaca, gorro, cafe));

        assertThat(nombres(new FiltroCatalogo("ALPAFILL", null, null, null, false, null)))
                .containsExactly("Casaca Älpafill Cusco");
        assertThat(nombres(new FiltroCatalogo(null, null, null, TipoCompromiso.ARBOL, false, null)))
                .containsExactly("Gorro de lana Layo", "Café Bourbon");
        assertThat(nombres(new FiltroCatalogo(null, null, null, TipoCompromiso.ARBOL, true, null)))
                .containsExactly("Café Bourbon");                                  // el gorro está agotado
        assertThat(nombres(new FiltroCatalogo(null, "café", null, null, false, null)))
                .containsExactly("Café Bourbon");
        assertThat(nombres(new FiltroCatalogo(null, null, null, null, false, OrdenCatalogo.PRECIO_ASC)))
                .containsExactly("Café Bourbon", "Gorro de lana Layo", "Casaca Älpafill Cusco");
    }

    @Test
    void losDestacadosMarcadosEnElPanelTienenPrioridadSobreElIntercalado() {
        Producto casaca = producto("HSG-CAS-001", "Casaca", "M", "259.90", 1, TipoCompromiso.ABRIGO, true);
        Producto bufanda = producto("HSG-ACC-002", "Bufanda", null, "39.90", 1, TipoCompromiso.ARBOL, true);
        bufanda.setDestacado(true);
        when(productoRepository.findByActivoTrueAndVisibleWebTrueOrderByCodigoAsc()).thenReturn(List.of(casaca, bufanda));

        assertThat(catalogoService.destacados(8)).extracting(TarjetaProducto::nombre).containsExactly("Bufanda");
    }

    @Test
    void elDetalleReuneLasTallasDelModeloEnOrdenYSusRelacionados() {
        Producto l = producto("HSG-CAS-002", "Casaca Älpafill Cusco", "L", "259.90", 0, TipoCompromiso.ABRIGO, true);
        Producto m = producto("HSG-CAS-001", "Casaca Älpafill Cusco", "M", "259.90", 3, TipoCompromiso.ABRIGO, true);
        Producto chaleco = producto("HSG-CAS-003", "Chaleco Älpafill", "S", "219.90", 4, TipoCompromiso.ABRIGO, true);
        m.setStockMinimo(5);
        when(productoRepository.findByCodigoAndActivoTrueAndVisibleWebTrue("HSG-CAS-002")).thenReturn(Optional.of(l));
        when(productoRepository.findByNombreAndActivoTrueAndVisibleWebTrueOrderByCodigoAsc("Casaca Älpafill Cusco"))
                .thenReturn(List.of(m, l));
        when(productoRepository.findByActivoTrueAndVisibleWebTrueOrderByCodigoAsc()).thenReturn(List.of(m, l, chaleco));

        DetalleProducto detalle = catalogoService.detalle("HSG-CAS-002");

        assertThat(detalle.codigoElegido()).isEqualTo("HSG-CAS-002");
        assertThat(detalle.variantes()).extracting(VarianteProducto::talla).containsExactly("M", "L");
        assertThat(detalle.variantes().get(0).quedanPocas()).isTrue();             // 3 ≤ stock mínimo 5
        assertThat(detalle.variantes().get(1).disponible()).isFalse();             // L agotada
        assertThat(detalle.sinTallas()).isFalse();
        assertThat(detalle.relacionados()).extracting(TarjetaProducto::nombre).containsExactly("Chaleco Älpafill");
    }

    @Test
    void unProductoOcultoONoActivoNoTieneDetalle() {
        when(productoRepository.findByCodigoAndActivoTrueAndVisibleWebTrue("HSG-X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogoService.detalle("HSG-X")).isInstanceOf(RecursoNoEncontradoException.class);
    }

    private List<String> nombres(FiltroCatalogo filtro) {
        return catalogoService.tarjetas(filtro).stream().map(TarjetaProducto::nombre).toList();
    }

    @Test
    void agrupaLasTallasDeUnMismoNombreEnUnaTarjeta() {
        when(productoRepository.findByActivoTrueAndVisibleWebTrueOrderByCodigoAsc()).thenReturn(List.of(
                producto("HSG-CAS-001", "Casaca Älpafill Cusco", "L", "259.90", 0, TipoCompromiso.ABRIGO, true),
                producto("HSG-CAS-002", "Casaca Älpafill Cusco", "M", "259.90", 25, TipoCompromiso.ABRIGO, true),
                producto("HSG-CAS-004", "Casaca Älpafill Cusco", "XL", "249.90", 18, TipoCompromiso.ABRIGO, true),
                producto("HSG-ACC-001", "Gorro de lana Layo", null, "49.90", 0, TipoCompromiso.ARBOL, true),
                producto("HSG-ACC-007", "Mochila artesanal Höség", "Única", "79.90", 15, null, false)));

        List<TarjetaProducto> tarjetas = catalogoService.tarjetas();

        assertThat(tarjetas).hasSize(3);

        TarjetaProducto casaca = tarjetas.get(0);
        assertThat(casaca.codigo()).isEqualTo("HSG-CAS-001");
        assertThat(casaca.tallas()).containsExactly("M", "L", "XL");   // orden de talla, no alfabético
        assertThat(casaca.precio()).isEqualByComparingTo("249.90");    // el menor entre variantes
        assertThat(casaca.disponible()).isTrue();                      // basta una talla con stock

        TarjetaProducto gorro = tarjetas.get(1);
        assertThat(gorro.tallas()).containsExactly("Única");           // talla NULL se muestra como Única
        assertThat(gorro.disponible()).isFalse();
        assertThat(gorro.tipoCompromiso()).isEqualTo(TipoCompromiso.ARBOL);

        assertThat(tarjetas.get(2).tipoCompromiso()).isNull();         // sin compromiso: sin badge
    }

    @Test
    void losDestacadosIntercalanAbrigosYArbolesYRespetanElMaximo() {
        when(productoRepository.findByActivoTrueAndVisibleWebTrueOrderByCodigoAsc()).thenReturn(List.of(
                producto("HSG-ACC-001", "Gorro", null, "49.90", 1, TipoCompromiso.ARBOL, true),
                producto("HSG-ACC-002", "Bufanda", null, "39.90", 1, TipoCompromiso.ARBOL, true),
                producto("HSG-ACC-003", "Guantes", null, "34.90", 1, TipoCompromiso.ARBOL, true),
                producto("HSG-CAS-001", "Casaca", "M", "259.90", 1, TipoCompromiso.ABRIGO, true),
                producto("HSG-CAS-003", "Chaleco", "S", "219.90", 1, TipoCompromiso.ABRIGO, true)));

        List<TarjetaProducto> destacados = catalogoService.destacados(4);

        assertThat(destacados).extracting(TarjetaProducto::nombre)
                .containsExactly("Casaca", "Gorro", "Chaleco", "Bufanda");
    }

    private static Producto producto(String codigo, String nombre, String talla, String precio, int stock,
                                     TipoCompromiso tipo, boolean aplicaImpacto) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setColeccion("Älpafill");
        p.setCategoria(codigo.startsWith("HSG-CAS") ? "Casacas" : "Accesorios");
        p.setTalla(talla);
        p.setPrecio(new BigDecimal(precio));
        p.setStockComercial(stock);
        p.setTipoCompromiso(tipo);
        p.setAplicaTripleImpacto(aplicaImpacto);
        p.setActivo(true);
        p.setVisibleWeb(true);
        return p;
    }
}
