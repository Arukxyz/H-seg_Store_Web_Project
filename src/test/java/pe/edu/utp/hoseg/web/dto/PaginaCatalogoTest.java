package pe.edu.utp.hoseg.web.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PaginaCatalogoTest {

    @Test
    void cortaLaPaginaPedida() {
        PaginaCatalogo pagina = PaginaCatalogo.de(tarjetas(30), 2, 12);

        assertThat(pagina.productos()).extracting(TarjetaProducto::codigo).first().isEqualTo("P13");
        assertThat(pagina.productos()).hasSize(12);
        assertThat(pagina.totalPaginas()).isEqualTo(3);
        assertThat(pagina.desde()).isEqualTo(13);
        assertThat(pagina.hasta()).isEqualTo(24);
        assertThat(pagina.hayAnterior()).isTrue();
        assertThat(pagina.haySiguiente()).isTrue();
    }

    @Test
    void laUltimaPaginaPuedeVenirIncompleta() {
        PaginaCatalogo pagina = PaginaCatalogo.de(tarjetas(30), 3, 12);

        assertThat(pagina.productos()).hasSize(6);
        assertThat(pagina.hasta()).isEqualTo(30);
        assertThat(pagina.haySiguiente()).isFalse();
    }

    @Test
    void valoresFueraDeRangoSeCorrigen() {
        assertThat(PaginaCatalogo.de(tarjetas(30), 99, 12).pagina()).isEqualTo(3);
        assertThat(PaginaCatalogo.de(tarjetas(30), -4, 12).pagina()).isEqualTo(1);
        assertThat(PaginaCatalogo.de(tarjetas(30), null, null).porPagina()).isEqualTo(PaginaCatalogo.POR_PAGINA_DEFECTO);
        assertThat(PaginaCatalogo.de(tarjetas(30), 1, 1000).porPagina()).isEqualTo(PaginaCatalogo.POR_PAGINA_DEFECTO);
    }

    @Test
    void sinResultadosHayUnaPaginaVacia() {
        PaginaCatalogo pagina = PaginaCatalogo.de(List.of(), 1, 6);

        assertThat(pagina.vacia()).isTrue();
        assertThat(pagina.totalPaginas()).isEqualTo(1);
        assertThat(pagina.desde()).isZero();
        assertThat(pagina.numeros()).containsExactly(1);
    }

    @Test
    void elPaginadorMuestraExtremosVecinasYSaltos() {
        assertThat(PaginaCatalogo.de(tarjetas(60), 6, 6).numeros()).containsExactly(1, 0, 5, 6, 7, 0, 10);
        assertThat(PaginaCatalogo.de(tarjetas(60), 1, 6).numeros()).containsExactly(1, 2, 0, 10);
        assertThat(PaginaCatalogo.de(tarjetas(18), 2, 6).numeros()).containsExactly(1, 2, 3);
    }

    @Test
    void unSaltoNuncaOcultaUnSoloNumero() {
        // Página 4 de 10: entre 1 y 3 solo falta el 2, así que se muestra
        assertThat(PaginaCatalogo.de(tarjetas(60), 4, 6).numeros()).containsExactly(1, 2, 3, 4, 5, 0, 10);
        assertThat(PaginaCatalogo.de(tarjetas(60), 7, 6).numeros()).containsExactly(1, 0, 6, 7, 8, 9, 10);
    }

    private static List<TarjetaProducto> tarjetas(int cantidad) {
        return IntStream.rangeClosed(1, cantidad)
                .mapToObj(i -> new TarjetaProducto("P" + i, "Producto " + i, null, BigDecimal.TEN, null, List.of(), true, null))
                .toList();
    }
}
