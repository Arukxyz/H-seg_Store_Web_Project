package pe.edu.utp.hoseg.web.dto;

import org.junit.jupiter.api.Test;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DetalleProductoTest {

    private static final VarianteProducto M = new VarianteProducto("HSG-CAS-001", "M", new BigDecimal("259.90"), true, 3, true);
    private static final VarianteProducto L = new VarianteProducto("HSG-CAS-002", "L", new BigDecimal("259.90"), false, 0, false);
    private static final VarianteProducto XL = new VarianteProducto("HSG-CAS-004", "XL", new BigDecimal("249.90"), true, 9, false);

    @Test
    void laTallaDeLaUrlQuedaElegidaSiEstaDisponible() {
        assertThat(detalle("HSG-CAS-004", M, L, XL).varianteInicial()).isEqualTo(XL);
    }

    @Test
    void siLaTallaDeLaUrlEstaAgotadaSeEligeLaPrimeraDisponible() {
        assertThat(detalle("HSG-CAS-002", M, L, XL).varianteInicial()).isEqualTo(M);
    }

    @Test
    void siTodoEstaAgotadoDevuelveLaPrimera() {
        assertThat(detalle("HSG-CAS-002", L).varianteInicial()).isEqualTo(L);
    }

    private static DetalleProducto detalle(String elegido, VarianteProducto... variantes) {
        return new DetalleProducto(elegido, "Casaca", "Casacas", "Älpafill", null, null, new BigDecimal("249.90"),
                TipoCompromiso.ABRIGO, List.of(variantes), true, false, List.of());
    }
}
