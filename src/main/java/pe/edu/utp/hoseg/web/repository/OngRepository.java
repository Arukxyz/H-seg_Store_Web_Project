package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.Ong;

import java.util.List;

public interface OngRepository extends JpaRepository<Ong, Integer> {

    List<Ong> findAllByOrderByNombreAsc();
}
