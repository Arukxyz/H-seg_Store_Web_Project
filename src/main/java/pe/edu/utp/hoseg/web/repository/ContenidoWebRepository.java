package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.ContenidoWeb;

import java.util.List;

public interface ContenidoWebRepository extends JpaRepository<ContenidoWeb, String> {

    List<ContenidoWeb> findAllByOrderByClaveAsc();
}
