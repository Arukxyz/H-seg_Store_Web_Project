package pe.edu.utp.hoseg.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Portal e-commerce de Höség Store. Comparte la base PostgreSQL de Supabase
 * con el escritorio SEGITD-HÖSÉG: aquí nacen las ventas (origen WEB) y sus
 * donaciones PENDIENTE; el escritorio las confirma, mueve stock y arma lotes.
 */
@SpringBootApplication
public class HosegWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HosegWebApplication.class, args);
    }
}
