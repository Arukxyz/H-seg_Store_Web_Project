package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.utp.hoseg.web.model.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    /** Catálogo web: solo lo activo y marcado visible, en el orden de la semilla. */
    List<Producto> findByActivoTrueAndVisibleWebTrueOrderByCodigoAsc();

    /** Una talla concreta, si se vende en la web. */
    Optional<Producto> findByCodigoAndActivoTrueAndVisibleWebTrue(String codigo);

    /** Todas las tallas visibles de un modelo (mismo nombre). */
    List<Producto> findByNombreAndActivoTrueAndVisibleWebTrueOrderByCodigoAsc(String nombre);

    /** Panel: todo lo activo, visible o no (lo inactivo lo gestiona el escritorio). */
    List<Producto> findByActivoTrueOrderByNombreAscCodigoAsc();

    /** Modelos distintos marcados como destacados (máximo 8). */
    @Query("select count(distinct p.nombre) from Producto p where p.destacado = true")
    long contarModelosDestacados();

    /** "Destacado" vale para el modelo: se marca o desmarca en todas sus tallas. */
    @Modifying
    @Query("update Producto p set p.destacado = :destacado where p.nombre = :nombre")
    int marcarDestacado(@Param("nombre") String nombre, @Param("destacado") boolean destacado);

    /** Descuenta stock comercial de forma atómica; devuelve 0 si no alcanzó el stock. */
    @Modifying
    @Query("update Producto p set p.stockComercial = p.stockComercial - :cantidad where p.codigo = :codigo and p.stockComercial >= :cantidad")
    int descontarStockComercial(@Param("codigo") String codigo, @Param("cantidad") int cantidad);

       /** Suma stock comercial cuando Logística registra ingreso o recibe orden de taller. */
    @Modifying
    @Query("update Producto p set p.stockComercial = p.stockComercial + :cantidad where p.codigo = :codigo")
    int sumarStockComercial(@Param("codigo") String codigo, @Param("cantidad") int cantidad);

    /** Suma stock comprometido cuando el producto tiene compromiso social (abrigo/árbol). */
    @Modifying
    @Query("update Producto p set p.stockComprometido = p.stockComprometido + :cantidad where p.codigo = :codigo")
    int sumarStockComprometido(@Param("codigo") String codigo, @Param("cantidad") int cantidad);

    /** Descuenta stock comprometido al entregar un lote de donación a la comunidad. */
    @Modifying
    @Query("update Producto p set p.stockComprometido = case when p.stockComprometido >= :cantidad then p.stockComprometido - :cantidad else 0 end where p.codigo = :codigo")
    int descontarStockComprometido(@Param("codigo") String codigo, @Param("cantidad") int cantidad);

     
}
