package pe.edu.utp.hoseg.web.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaneamientoTextoTest {

    @Test
    void unSaltoDeLineaNoPuedeInyectarUnaCabeceraDeCorreo() {
        String limpio = SaneamientoTexto.paraAsuntoDeCorreo("Consulta\r\nBcc: espia@correo.pe", 150);
        assertThat(limpio).isEqualTo("Consulta Bcc: espia@correo.pe").doesNotContain("\r", "\n");
    }

    @Test
    void elAsuntoSeRecortaAlMaximo() {
        assertThat(SaneamientoTexto.paraAsuntoDeCorreo("abcdefghij", 5)).isEqualTo("abcde");
    }

    @Test
    void losEspaciosYTabulacionesSeReducenAUnoSolo() {
        assertThat(SaneamientoTexto.normalizarLineaObligatoria("  Ana \t  Pérez  ")).isEqualTo("Ana Pérez");
    }

    @Test
    void unTextoOpcionalVacioDevuelveNull() {
        assertThat(SaneamientoTexto.normalizarLinea("  \n ")).isNull();
        assertThat(SaneamientoTexto.normalizarLinea(null)).isNull();
    }

    @Test
    void unTextoObligatorioNuloEsUnError() {
        assertThatThrownBy(() -> SaneamientoTexto.normalizarLineaObligatoria(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}