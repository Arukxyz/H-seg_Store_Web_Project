package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.CafeLocal;

import java.util.List;

public interface CafeLocalRepository extends JpaRepository<CafeLocal, Integer> {

    List<CafeLocal> findByActivoTrueOrderByNombreAsc();

    List<CafeLocal> findAllByOrderByNombreAsc();
}
