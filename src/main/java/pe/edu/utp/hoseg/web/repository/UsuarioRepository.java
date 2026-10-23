package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.model.Usuario;

import java.util.Optional;

/**
 * Personal interno, solo para el login del panel. El bloqueo usa las mismas
 * columnas y reglas que AuthService del escritorio: un usuario bloqueado en
 * un sistema también lo está en el otro.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsername(String username);

    @Modifying
    @Query(value = """
            update usuario
               set intentos_fallidos = intentos_fallidos + 1,
                   bloqueado_hasta   = case when intentos_fallidos + 1 >= :maximo
                                            then now() + make_interval(mins => :minutos)
                                            else null end
             where username = :username
            """, nativeQuery = true)
    int registrarIntentoFallido(@Param("username") String username, @Param("maximo") int maximo, @Param("minutos") int minutos);

    @Modifying
    @Query(value = "update usuario set intentos_fallidos = 0, bloqueado_hasta = null where username = :username",
            nativeQuery = true)
    int reiniciarIntentos(@Param("username") String username);

    @Query(value = """
            select cast(ceil(extract(epoch from (bloqueado_hasta - now())) / 60) as integer)
              from usuario
             where username = :username and bloqueado_hasta > now()
            """, nativeQuery = true)
    Optional<Integer> minutosDeBloqueo(@Param("username") String username);
}
