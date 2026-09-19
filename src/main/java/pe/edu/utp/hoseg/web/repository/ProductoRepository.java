package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    /** Catálogo web: solo lo activo y marcado visible, en el orden de la semilla. */
    List<Producto> findByActivoTrueAndVisibleWebTrueOrderByCodigoAsc();
}
