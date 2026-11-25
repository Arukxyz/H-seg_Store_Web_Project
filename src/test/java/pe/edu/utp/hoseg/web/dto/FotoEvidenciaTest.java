package pe.edu.utp.hoseg.web.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FotoEvidenciaTest {

    @Test
    void elPieSaleDelNombreDelArchivo() {
        assertThat(FotoEvidencia.pieDesdeArchivo("02-entrega-de-abrigos-en-omacha.jpg"))
                .isEqualTo("Entrega de abrigos en omacha");
        assertThat(FotoEvidencia.pieDesdeArchivo("plantacion_de_cedros.webp"))
                .isEqualTo("Plantacion de cedros");
    }

    @Test
    void sinTextoUsaUnPiePorDefecto() {
        assertThat(FotoEvidencia.pieDesdeArchivo("01.jpg")).isEqualTo("Entrega en Cusco");
    }
}
