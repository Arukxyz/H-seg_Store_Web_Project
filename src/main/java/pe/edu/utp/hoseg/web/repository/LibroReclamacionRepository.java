package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.EstadoReclamo;
import pe.edu.utp.hoseg.web.model.LibroReclamacion;

import java.util.List;

public interface LibroReclamacionRepository extends JpaRepository<LibroReclamacion, Integer> {

    List<LibroReclamacion> findAllByOrderByCreadoEnDesc();

    long countByEstado(EstadoReclamo estado);
}
