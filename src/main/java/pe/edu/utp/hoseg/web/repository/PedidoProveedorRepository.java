package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.edu.utp.hoseg.web.model.PedidoProveedor;

import java.util.List;

public interface PedidoProveedorRepository extends JpaRepository<PedidoProveedor, Integer> {

    @Query("""
            select pp from PedidoProveedor pp
              join fetch pp.proveedor
              left join fetch pp.producto
             order by pp.fecha desc
            """)
    List<PedidoProveedor> todosConDetalle();
}
