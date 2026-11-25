package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.CafeCarta;

import java.util.List;

public interface CafeCartaRepository extends JpaRepository<CafeCarta, Integer> {

    /** Carta pública: lo activo, por categoría y en el orden que fija el panel. */
    List<CafeCarta> findByActivoTrueOrderByCategoriaAscOrdenAscNombreAsc();

    /** Panel: también lo dado de baja, para poder reactivarlo. */
    List<CafeCarta> findAllByOrderByActivoDescCategoriaAscOrdenAscNombreAsc();
}
