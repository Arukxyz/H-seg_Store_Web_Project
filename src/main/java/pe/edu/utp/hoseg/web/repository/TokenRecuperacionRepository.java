package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.model.TokenRecuperacion;

import java.util.Optional;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Integer> {

    /** Token sin usar y sin vencer, con el reloj de la base (current_timestamp). */
    @Query("""
            select t from TokenRecuperacion t join fetch t.cliente
             where t.tokenHash = :hash and t.usadoEn is null and t.expiraEn > current_timestamp
            """)
    Optional<TokenRecuperacion> buscarVigente(@Param("hash") String tokenHash);

    /** Al pedir un enlace nuevo o usar uno, los demás dejan de servir. */
    @Modifying
    @Query("update TokenRecuperacion t set t.usadoEn = current_timestamp where t.cliente.id = :idCliente and t.usadoEn is null")
    int invalidarPendientes(@Param("idCliente") Integer idCliente);
}
