package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.OrigenVenta;
import pe.edu.utp.hoseg.web.model.Venta;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    Optional<Venta> findByCodigoComprobante(String codigoComprobante);

    /** "Mis pedidos": del más reciente al más antiguo. */
    List<Venta> findByClienteIdOrderByFechaDesc(Integer idCliente);

    /** Un pedido solo si es del cliente: el de otro se trata como inexistente. */
    Optional<Venta> findByCodigoComprobanteAndClienteId(String codigoComprobante, Integer idCliente);

    /** Ventas en el estado dado que generaron al menos una donación. */
    @Query("""
            select count(v)
              from Venta v
             where v.estado = :estado
               and exists (select 1 from Donacion d where d.detalleVenta.venta = v)
            """)
    long contarConDonacion(@Param("estado") EstadoVenta estado);

    long countByOrigenAndFechaGreaterThanEqual(OrigenVenta origen, OffsetDateTime desde);

    long countByOrigenAndEstado(OrigenVenta origen, EstadoVenta estado);

    /** Monto vendido del periodo: solo mercadería de pedidos pagados; la suma la hace Postgres. */
    @Query("""
            select coalesce(sum(v.total), 0) from Venta v
             where v.origen = :origen and v.estado = :estado and v.fecha >= :desde and v.fecha < :hasta
            """)
    BigDecimal sumarTotal(@Param("origen") OrigenVenta origen, @Param("estado") EstadoVenta estado,
                          @Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    /** Tabla de pedidos del panel, con el cliente en la misma consulta. */
    @Query("""
            select v from Venta v left join fetch v.cliente
             where v.origen = :origen and v.fecha >= :desde and v.fecha < :hasta
             order by v.fecha desc
            """)
    List<Venta> delPeriodo(@Param("origen") OrigenVenta origen,
                           @Param("desde") OffsetDateTime desde, @Param("hasta") OffsetDateTime hasta);

    /**
     * Siguiente número de comprobante web. Lo da la secuencia de la base
     * (06_web_clientes.sql), nunca un MAX + 1: dos compras simultáneas no
     * pueden repetir número.
     */
    @Query(value = "select nextval(cast(:secuencia as regclass))", nativeQuery = true)
    long siguienteComprobante(@Param("secuencia") String secuencia);
}
