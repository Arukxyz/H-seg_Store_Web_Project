package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.Comunidad;

import java.util.List;

public interface ComunidadRepository extends JpaRepository<Comunidad, Integer> {

    List<Comunidad> findAllByOrderByNombreAsc();
}
