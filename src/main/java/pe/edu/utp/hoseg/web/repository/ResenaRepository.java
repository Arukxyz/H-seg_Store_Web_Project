package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.dto.ResumenResenas;
import pe.edu.utp.hoseg.web.model.EstadoResena;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.Resena;

import java.util.List;
import java.util.Optional;

/**
 * Las reseñas se agrupan por modelo (producto.nombre): el detalle de la
 * casaca muestra las de todas sus tallas. Las públicas son las PUBLICADA de
 * ventas que siguen PAGADO: si el escritorio anula el pedido, desaparecen.
 */
public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    @Query("""
            select r from Resena r
              join fetch r.detalleVenta dv
              join fetch dv.producto p
              join fetch dv.venta v
              join fetch v.cliente
             where p.nombre = :nombre and r.estado = :estado and v.estado = :estadoVenta
             order by r.creadoEn desc
            """)
    List<Resena> delModelo(@Param("nombre") String nombre,
                           @Param("estado") EstadoResena estado,
                           @Param("estadoVenta") EstadoVenta estadoVenta);

    @Query("""
            select new pe.edu.utp.hoseg.web.dto.ResumenResenas(avg(r.calificacion), count(r))
              from Resena r
             where r.detalleVenta.producto.nombre = :nombre
               and r.estado = :estado
               and r.detalleVenta.venta.estado = :estadoVenta
            """)
    ResumenResenas resumenDelModelo(@Param("nombre") String nombre,
                                    @Param("estado") EstadoResena estado,
                                    @Param("estadoVenta") EstadoVenta estadoVenta);

    /** La reseña que el cliente ya escribió para el modelo (una por modelo), para editarla. */
    @Query("""
            select r from Resena r
             where r.detalleVenta.venta.cliente.id = :idCliente
               and r.detalleVenta.producto.nombre = :nombre
            """)
    Optional<Resena> delClienteParaModelo(@Param("idCliente") Integer idCliente, @Param("nombre") String nombre);

    /** Panel de moderación: todas, o solo las de un estado, con producto y autor. */
    @Query("""
            select r from Resena r
              join fetch r.detalleVenta dv
              join fetch dv.producto
              join fetch dv.venta v
              join fetch v.cliente
             where :estado is null or r.estado = :estado
             order by r.creadoEn desc
            """)
    List<Resena> paraModerar(@Param("estado") EstadoResena estado);
}
