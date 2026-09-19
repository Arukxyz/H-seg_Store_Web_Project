package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.model.EstadoLote;
import pe.edu.utp.hoseg.web.model.LoteDonacion;

public interface LoteDonacionRepository extends JpaRepository<LoteDonacion, Integer> {

    /** Comunidades distintas con al menos un lote en el estado indicado. */
    @Query("select count(distinct l.comunidad.id) from LoteDonacion l where l.estado = :estado")
    long contarComunidades(@Param("estado") EstadoLote estado);
}
