package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.model.Cliente;
import pe.edu.utp.hoseg.web.model.TipoDocumento;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /** El email es la credencial; el índice único de 06 es sobre lower(email). */
    Optional<Cliente> findByEmailIgnoreCase(String email);

    /** cliente_documento_key: un documento no se repite entre clientes. */
    boolean existsByTipoDocAndNumDocAndIdNot(TipoDocumento tipoDoc, String numDoc, Integer id);

    boolean existsByTipoDocAndNumDoc(TipoDocumento tipoDoc, String numDoc);

    /**
     * Suma un intento fallido y, al llegar al máximo, bloquea por unos minutos.
     * La suma y la hora las calcula Postgres, como UsuarioDAO del escritorio.
     */
    @Modifying
    @Query(value = """
            update cliente
               set intentos_fallidos = intentos_fallidos + 1,
                   bloqueado_hasta   = case when intentos_fallidos + 1 >= :maximo
                                            then now() + make_interval(mins => :minutos)
                                            else null end
             where lower(email) = lower(:email)
            """, nativeQuery = true)
    int registrarIntentoFallido(@Param("email") String email, @Param("maximo") int maximo, @Param("minutos") int minutos);

    @Modifying
    @Query(value = "update cliente set intentos_fallidos = 0, bloqueado_hasta = null where lower(email) = lower(:email)",
            nativeQuery = true)
    int reiniciarIntentos(@Param("email") String email);

    /** Minutos que faltan para que expire el bloqueo, con el reloj de Postgres; vacío si no está bloqueado. */
    @Query(value = """
            select cast(ceil(extract(epoch from (bloqueado_hasta - now())) / 60) as integer)
              from cliente
             where lower(email) = lower(:email) and bloqueado_hasta > now()
            """, nativeQuery = true)
    Optional<Integer> minutosDeBloqueo(@Param("email") String email);
}
