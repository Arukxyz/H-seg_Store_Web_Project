package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.dto.UnidadesDonadas;
import pe.edu.utp.hoseg.web.model.Donacion;
import pe.edu.utp.hoseg.web.model.EstadoDonacion;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface DonacionRepository extends JpaRepository<Donacion, Integer> {

    /** Unidades (abrigos o árboles) en un estado dado; la suma la hace Postgres. */
    @Query("""
            select coalesce(sum(d.cantidad), 0)
              from Donacion d
             where d.tipo = :tipo and d.estado = :estado
            """)
    long sumarUnidades(@Param("tipo") TipoCompromiso tipo, @Param("estado") EstadoDonacion estado);

    /** Donaciones de una venta con su lote, comunidad y ONG (línea de tiempo). */
    @Query("""
            select d from Donacion d
              join fetch d.producto
              left join fetch d.lote l
              left join fetch l.comunidad
              left join fetch l.ong
             where d.detalleVenta.venta.id = :idVenta
             order by d.id
            """)
    List<Donacion> deLaVenta(@Param("idVenta") Integer idVenta);

    /** Una donación para el QR de la boleta (?donacion=id). */
    @Query("""
            select d from Donacion d
              join fetch d.producto
              join fetch d.detalleVenta dv
              join fetch dv.venta
              left join fetch d.lote l
              left join fetch l.comunidad
              left join fetch l.ong
             where d.id = :id
            """)
    Optional<Donacion> conTrazabilidad(@Param("id") Integer id);

    /** Unidades del cliente por venta, tipo y estado ("Mis pedidos" y "Tu impacto"). */
    @Query("""
            select new pe.edu.utp.hoseg.web.dto.UnidadesDonadas(v.id, d.tipo, d.estado, sum(d.cantidad))
              from Donacion d join d.detalleVenta dv join dv.venta v
             where v.cliente.id = :idCliente
             group by v.id, d.tipo, d.estado
            """)
    List<UnidadesDonadas> delCliente(@Param("idCliente") Integer idCliente);

    /** Unidades generadas por ventas web del periodo, por tipo y estado (panel). */
    @Query("""
            select new pe.edu.utp.hoseg.web.dto.UnidadesDonadas(d.tipo, d.estado, sum(d.cantidad))
              from Donacion d join d.detalleVenta dv join dv.venta v
             where v.fecha >= :desde and v.fecha < :hasta
             group by d.tipo, d.estado
            """)
    List<UnidadesDonadas> delPeriodo(@Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);
}
