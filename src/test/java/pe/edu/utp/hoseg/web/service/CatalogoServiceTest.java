package pe.edu.utp.hoseg.web.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.utp.hoseg.web.dto.TarjetaProducto;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.repository.ProductoRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CatalogoService catalogoService;

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
