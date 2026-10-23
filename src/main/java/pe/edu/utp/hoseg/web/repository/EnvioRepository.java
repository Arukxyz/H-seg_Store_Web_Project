package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.Envio;

import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    /** Vacío en las ventas anteriores al script 08. */
    Optional<Envio> findByVentaId(Integer idVenta);
}
