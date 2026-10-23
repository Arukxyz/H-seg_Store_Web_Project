package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    /** Catálogo web: solo lo activo y marcado visible, en el orden de la semilla. */
    List<Producto> findByActivoTrueAndVisibleWebTrueOrderByCodigoAsc();

    /** Una talla concreta, si se vende en la web. */
    Optional<Producto> findByCodigoAndActivoTrueAndVisibleWebTrue(String codigo);

    /** Todas las tallas visibles de un modelo (mismo nombre). */
    List<Producto> findByNombreAndActivoTrueAndVisibleWebTrueOrderByCodigoAsc(String nombre);
}
