package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.TarjetaProducto;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.repository.ProductoRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Catálogo público: agrupa las filas por talla en tarjetas. */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    /** Orden de los chips de talla; lo que no esté aquí va después, y "Única" al final. */
    private static final List<String> ORDEN_TALLAS = List.of("XS", "S", "M", "L", "XL", "XXL");

    private final ProductoRepository productoRepository;

    public CatalogoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /** Todas las tarjetas del catálogo visible, una por nombre de producto. */
    public List<TarjetaProducto> tarjetas() {
        Map<String, List<Producto>> porNombre = new LinkedHashMap<>();
        for (Producto p : productoRepository.findByActivoTrueAndVisibleWebTrueOrderByCodigoAsc()) {
            porNombre.computeIfAbsent(p.getNombre(), k -> new ArrayList<>()).add(p);
        }
        return porNombre.values().stream().map(this::aTarjeta).toList();
    }

    /**
     * Tarjetas del carrusel de la Home. No hay columna "destacado" en el
     * esquema, así que se intercalan prendas (ABRIGO) y accesorios (ARBOL)
     * para que el carrusel muestre ambos compromisos desde la primera vista.
     */
    public List<TarjetaProducto> destacados(int maximo) {
        Deque<TarjetaProducto> abrigos = new ArrayDeque<>();
        Deque<TarjetaProducto> resto = new ArrayDeque<>();
        for (TarjetaProducto t : tarjetas()) {
            (t.tipoCompromiso() == TipoCompromiso.ABRIGO ? abrigos : resto).add(t);
        }
        List<TarjetaProducto> destacados = new ArrayList<>();
        while (destacados.size() < maximo && !(abrigos.isEmpty() && resto.isEmpty())) {
            if (!abrigos.isEmpty()) {
                destacados.add(abrigos.poll());
            }
            if (destacados.size() < maximo && !resto.isEmpty()) {
                destacados.add(resto.poll());
            }
        }
        return destacados;
    }

    private TarjetaProducto aTarjeta(List<Producto> variantes) {
        Producto primera = variantes.get(0);
        List<String> tallas = variantes.stream()
                .map(Producto::getTallaMostrada)
                .distinct()
                .sorted(Comparator.comparingInt(this::posicionTalla))
                .toList();
        return new TarjetaProducto(
                primera.getCodigo(),
                primera.getNombre(),
                primera.getColeccion(),
                variantes.stream().map(Producto::getPrecio).min(Comparator.naturalOrder()).orElseThrow(),
                variantes.stream().filter(Producto::tieneCompromiso).map(Producto::getTipoCompromiso).findFirst().orElse(null),
                tallas,
                variantes.stream().anyMatch(Producto::estaDisponible),
                variantes.stream().map(Producto::getUrlImagen).filter(Objects::nonNull).findFirst().orElse(null));
    }

    private int posicionTalla(String talla) {
        int i = ORDEN_TALLAS.indexOf(talla.toUpperCase());
        if (i >= 0) {
            return i;
        }
        return "Única".equalsIgnoreCase(talla) ? ORDEN_TALLAS.size() + 1 : ORDEN_TALLAS.size();
    }
}
