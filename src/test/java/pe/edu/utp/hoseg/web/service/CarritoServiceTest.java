package pe.edu.utp.hoseg.web.service;

import org.junit.jupiter.api.Test;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CarritoServiceTest {

    private final CarritoService carrito = new CarritoService();

    @Test
    void sumaLaMismaTallaEnUnaLineaYCalculaElImpacto() {
        carrito.agregar(producto("HSG-CAS-001", "259.90", TipoCompromiso.ABRIGO), 1);
        carrito.agregar(producto("HSG-CAS-001", "259.90", TipoCompromiso.ABRIGO), 1);
        carrito.agregar(producto("HSG-ACC-001", "49.90", TipoCompromiso.ARBOL), 1);
        carrito.agregar(producto("HSG-ACC-007", "79.90", null), 1);

        assertThat(carrito.items()).hasSize(3);
        assertThat(carrito.totalUnidades()).isEqualTo(4);
        assertThat(carrito.total()).isEqualByComparingTo("649.60");
        assertThat(carrito.impacto().getTexto()).isEqualTo("2 abrigos + 1 árbol");
    }

    @Test
    void actualizarACeroQuitaLaLinea() {
        carrito.agregar(producto("HSG-ACC-001", "49.90", TipoCompromiso.ARBOL), 2);

        carrito.actualizar("HSG-ACC-001", 3);
        assertThat(carrito.totalUnidades()).isEqualTo(3);

        carrito.actualizar("HSG-ACC-001", 0);
        assertThat(carrito.estaVacio()).isTrue();
    }

    private static Producto producto(String codigo, String precio, TipoCompromiso tipo) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(codigo);
        p.setPrecio(new BigDecimal(precio));
        p.setTipoCompromiso(tipo);
        p.setAplicaTripleImpacto(tipo != null);
        return p;
    }
}
