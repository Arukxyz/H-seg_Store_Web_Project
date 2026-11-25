package pe.edu.utp.hoseg.web.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sha256ConSaltPasswordEncoderTest {

    /** SHA-256("abc"), vector de prueba de FIPS 180-2. */
    private static final String SHA256_ABC = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    private final Sha256ConSaltPasswordEncoder codificador = new Sha256ConSaltPasswordEncoder();

    @Test
    void verificaComoElEscritorioHasheandoSaltMasContrasena() {
        // HashUtil del escritorio: SHA-256(salt + contraseña) → salt "a" + contraseña "bc" = "abc".
        String guardada = Sha256ConSaltPasswordEncoder.guardada("a", SHA256_ABC);

        assertThat(codificador.matches("bc", guardada)).isTrue();
        assertThat(codificador.matches("bd", guardada)).isFalse();
        assertThat(codificador.matches("abc", guardada)).isFalse();   // el salt no es opcional
    }

    @Test
    void aceptaElHashEnMayusculasYRechazaValoresMalFormados() {
        assertThat(codificador.matches("bc", "a:" + SHA256_ABC.toUpperCase())).isTrue();
        assertThat(codificador.matches("bc", SHA256_ABC)).isFalse();  // sin separador
        assertThat(codificador.matches("bc", null)).isFalse();
    }

    /** Spring Security llama a encode() en el primer login (protección contra ataques de tiempo). */
    @Test
    void encodeGeneraUnHashVerificableConSaltAleatorio() {
        String a = codificador.encode("Admin#2026");
        String b = codificador.encode("Admin#2026");

        assertThat(a).matches("^[0-9a-f]{32}:[0-9a-f]{64}$");   // mismo formato que salt + password_hash del escritorio
        assertThat(a).isNotEqualTo(b);
        assertThat(codificador.matches("Admin#2026", a)).isTrue();
        assertThat(codificador.matches("otra", a)).isFalse();
    }
}
