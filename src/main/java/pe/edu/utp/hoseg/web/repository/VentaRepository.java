package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.Venta;

import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    Optional<Venta> findByCodigoComprobante(String codigoComprobante);

    /** Ventas en el estado dado que generaron al menos una donación. */
    @Query("""
            select count(v)
              from Venta v
             where v.estado = :estado
               and exists (select 1 from Donacion d where d.detalleVenta.venta = v)
            """)
    long contarConDonacion(@Param("estado") EstadoVenta estado);
}
