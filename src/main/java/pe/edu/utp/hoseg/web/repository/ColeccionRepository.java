package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.Coleccion;

import java.util.List;

public interface ColeccionRepository extends JpaRepository<Coleccion, String> {

    List<Coleccion> findAllByOrderByNombreAsc();
}
