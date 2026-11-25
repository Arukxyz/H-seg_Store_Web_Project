package pe.edu.utp.hoseg.web.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalCafeTest {

    private static LocalCafe local(String nombre) {
        return new LocalCafe(nombre, "Dirección", null, null, null);
    }

    @Test
    void laClaveQuitaTildesYUsaGuiones() {
        assertThat(local("Höség Café Cusco").clave()).isEqualTo("hoseg-cafe-cusco");
        assertThat(local("Hoseg Marriott Lima").clave()).isEqualTo("hoseg-marriott-lima");
        assertThat(local("  Hoseg  House! ").clave()).isEqualTo("hoseg-house");
    }

    @Test
    void soloEsCafeteriaSiElNombreLlevaCafe() {
        assertThat(local("Höség Café Cusco").esCafe()).isTrue();
        assertThat(local("Hoseg Larcomar").esCafe()).isFalse();
        assertThat(local("Cafetería Norte").esCafe()).isFalse();
    }
}
