package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.model.Donacion;
import pe.edu.utp.hoseg.web.model.EstadoDonacion;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;

public interface DonacionRepository extends JpaRepository<Donacion, Integer> {

    /** Unidades (abrigos o árboles) en un estado dado; la suma la hace Postgres. */
    @Query("""
            select coalesce(sum(d.cantidad), 0)
              from Donacion d
             where d.tipo = :tipo and d.estado = :estado
            """)
    long sumarUnidades(@Param("tipo") TipoCompromiso tipo, @Param("estado") EstadoDonacion estado);
}
