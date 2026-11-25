package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.model.DetalleVenta;
import pe.edu.utp.hoseg.web.model.EstadoVenta;

import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    /** Líneas de un modelo (cualquier talla) que el cliente compró en ventas con el estado dado. */
    @Query("""
            select dv from DetalleVenta dv
             where dv.venta.cliente.id = :idCliente
               and dv.venta.estado = :estado
               and dv.producto.nombre = :nombre
             order by dv.id
            """)
    List<DetalleVenta> comprasDelModelo(@Param("idCliente") Integer idCliente,
                                        @Param("nombre") String nombre,
                                        @Param("estado") EstadoVenta estado);
}
