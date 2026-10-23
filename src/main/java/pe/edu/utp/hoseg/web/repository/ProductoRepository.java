package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /** Panel: todo lo activo, visible o no (lo inactivo lo gestiona el escritorio). */
    List<Producto> findByActivoTrueOrderByNombreAscCodigoAsc();

    /** Modelos distintos marcados como destacados (máximo 8). */
    @Query("select count(distinct p.nombre) from Producto p where p.destacado = true")
    long contarModelosDestacados();

    /** "Destacado" vale para el modelo: se marca o desmarca en todas sus tallas. */
    @Modifying
    @Query("update Producto p set p.destacado = :destacado where p.nombre = :nombre")
    int marcarDestacado(@Param("nombre") String nombre, @Param("destacado") boolean destacado);
}
