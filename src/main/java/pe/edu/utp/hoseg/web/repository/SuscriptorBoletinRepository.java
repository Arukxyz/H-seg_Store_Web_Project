package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.SuscriptorBoletin;

import java.util.List;
import java.util.Optional;

public interface SuscriptorBoletinRepository extends JpaRepository<SuscriptorBoletin, Integer> {

    /** uq_suscriptor_email es sobre lower(email). */
    Optional<SuscriptorBoletin> findByEmailIgnoreCase(String email);

    List<SuscriptorBoletin> findByActivoTrueOrderBySuscritoEnAsc();
}
