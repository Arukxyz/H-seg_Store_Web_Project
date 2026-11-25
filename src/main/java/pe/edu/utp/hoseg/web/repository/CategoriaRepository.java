package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.Categoria;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, String> {

    List<Categoria> findAllByOrderByNombreAsc();
}
