package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.DetalleProducto;
import pe.edu.utp.hoseg.web.dto.FiltroCatalogo;
import pe.edu.utp.hoseg.web.dto.TarjetaProducto;
import pe.edu.utp.hoseg.web.dto.VarianteProducto;
import pe.edu.utp.hoseg.web.model.Categoria;
import pe.edu.utp.hoseg.web.model.Coleccion;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.repository.CategoriaRepository;
import pe.edu.utp.hoseg.web.repository.ColeccionRepository;
import pe.edu.utp.hoseg.web.repository.ProductoRepository;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Catálogo público. Cada talla es una fila de producto; aquí se agrupan por
 * nombre en "modelos": una tarjeta por modelo y un detalle con sus tallas.
 */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    /** Orden de los chips de talla; lo que no esté aquí va después, y "Única" al final. */
    private static final List<String> ORDEN_TALLAS = List.of("XS", "S", "M", "L", "XL", "XXL");
    private static final int RELACIONADOS = 4;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ColeccionRepository coleccionRepository;

    public CatalogoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           ColeccionRepository coleccionRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.coleccionRepository = coleccionRepository;
    }

    /** Todas las tarjetas del catálogo visible, una por nombre de producto. */
    public List<TarjetaProducto> tarjetas() {
        return modelosVisibles().values().stream().map(this::aTarjeta).toList();
    }

    /** La grilla de /tienda con los filtros y el orden de la URL (CU-02, CU-02.1). */
    public List<TarjetaProducto> tarjetas(FiltroCatalogo filtro) {
        Predicate<List<Producto>> cumple = modelo -> true;
        if (tieneTexto(filtro.q())) {
            String buscado = normalizar(filtro.q());
            cumple = cumple.and(m -> normalizar(textoBuscable(m.get(0))).contains(buscado));
        }
        if (tieneTexto(filtro.categoria())) {
            cumple = cumple.and(m -> m.get(0).getCategoria().equalsIgnoreCase(filtro.categoria()));
        }
        if (tieneTexto(filtro.coleccion())) {
            cumple = cumple.and(m -> filtro.coleccion().equalsIgnoreCase(m.get(0).getColeccion()));
        }
        if (filtro.impacto() != null) {
            cumple = cumple.and(m -> filtro.impacto() == compromiso(m));
        }
        if (filtro.disponibles()) {
            cumple = cumple.and(m -> m.stream().anyMatch(Producto::estaDisponible));
        }

        List<List<Producto>> elegidos = new ArrayList<>(modelosVisibles().values().stream().filter(cumple).toList());
        Collator alfabetico = Collator.getInstance(Locale.forLanguageTag("es-PE"));
        switch (filtro.ordenEfectivo()) {
            case PRECIO_ASC -> elegidos.sort(Comparator.comparing(CatalogoService::precioDesde));
            case PRECIO_DESC -> elegidos.sort(Comparator.comparing(CatalogoService::precioDesde).reversed());
            case NOMBRE -> elegidos.sort(Comparator.comparing(m -> m.get(0).getNombre(), alfabetico));
            case DESTACADOS -> elegidos.sort(Comparator.comparing(m -> !esDestacado(m)));   // estable: luego, el orden de la semilla
        }
        return elegidos.stream().map(this::aTarjeta).toList();
    }

    /** Chips del filtro de categoría (tabla categoria, 07). */
    public List<String> categorias() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream().map(Categoria::getNombre).toList();
    }

    /** Chips del filtro de colección (tabla coleccion, 07). */
    public List<String> colecciones() {
        return coleccionRepository.findAllByOrderByNombreAsc().stream().map(Coleccion::getNombre).toList();
    }

    /**
     * Tarjetas del carrusel de la Home. Si el panel marcó destacados
     * (producto.destacado), salen esos; si no, se intercalan prendas (ABRIGO)
     * y accesorios (ARBOL) para que se vean ambos compromisos desde el inicio.
     */
    public List<TarjetaProducto> destacados(int maximo) {
        List<List<Producto>> marcados = modelosVisibles().values().stream().filter(CatalogoService::esDestacado).toList();
        if (!marcados.isEmpty()) {
            return marcados.stream().limit(maximo).map(this::aTarjeta).toList();
        }
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

    /**
     * Detalle de un modelo a partir del código de cualquiera de sus tallas (CU-03).
     *
     * @throws RecursoNoEncontradoException si el código no existe o no se vende en la web
     */
    public DetalleProducto detalle(String codigo) {
        Producto elegido = productoRepository.findByCodigoAndActivoTrueAndVisibleWebTrue(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no disponible: " + codigo));
        List<Producto> modelo = productoRepository.findByNombreAndActivoTrueAndVisibleWebTrueOrderByCodigoAsc(elegido.getNombre());

        List<VarianteProducto> variantes = modelo.stream()
                .sorted(Comparator.comparingInt(p -> posicionTalla(p.getTallaMostrada())))
                .map(p -> new VarianteProducto(p.getCodigo(), p.getTallaMostrada(), p.getPrecio(),
                        p.estaDisponible(), Math.max(p.getStockComercial(), 0), p.quedanPocas()))
                .toList();

        List<TarjetaProducto> relacionados = modelosVisibles().values().stream()
                .filter(m -> m.get(0).getCategoria().equals(elegido.getCategoria()))
                .filter(m -> !m.get(0).getNombre().equals(elegido.getNombre()))
                .limit(RELACIONADOS)
                .map(this::aTarjeta)
                .toList();

        return new DetalleProducto(
                elegido.getCodigo(),
                elegido.getNombre(),
                elegido.getCategoria(),
                elegido.getColeccion(),
                elegido.getDescripcion(),
                imagen(modelo),
                precioDesde(modelo),
                compromiso(modelo),
                variantes,
                modelo.stream().anyMatch(Producto::estaDisponible),
                variantes.stream().allMatch(v -> "Única".equals(v.talla())),
                relacionados);
    }

    /**
     * Producto que se puede agregar al carrito: activo, visible y con stock.
     * El stock vuelve a validarse al confirmar la compra.
     */
    public Producto paraCarrito(String codigo) {
        Producto producto = productoRepository.findByCodigoAndActivoTrueAndVisibleWebTrue(codigo)
                .orElseThrow(() -> new NegocioException("Ese producto ya no está a la venta."));
        if (!producto.estaDisponible()) {
            throw new NegocioException(producto.getNombre() + " (" + producto.getTallaMostrada() + ") está agotado.");
        }
        return producto;
    }

    /** Modelos visibles en el orden de la semilla: nombre → sus tallas. */
    private Map<String, List<Producto>> modelosVisibles() {
        Map<String, List<Producto>> porNombre = new LinkedHashMap<>();
        for (Producto p : productoRepository.findByActivoTrueAndVisibleWebTrueOrderByCodigoAsc()) {
            porNombre.computeIfAbsent(p.getNombre(), k -> new ArrayList<>()).add(p);
        }
        return porNombre;
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
                precioDesde(variantes),
                compromiso(variantes),
                tallas,
                variantes.stream().anyMatch(Producto::estaDisponible),
                imagen(variantes));
    }

    private static BigDecimal precioDesde(List<Producto> modelo) {
        return modelo.stream().map(Producto::getPrecio).min(Comparator.naturalOrder()).orElseThrow();
    }

    private static TipoCompromiso compromiso(List<Producto> modelo) {
        return modelo.stream().filter(Producto::tieneCompromiso).map(Producto::getTipoCompromiso).findFirst().orElse(null);
    }

    private static String imagen(List<Producto> modelo) {
        return modelo.stream().map(Producto::getUrlImagen).filter(Objects::nonNull).findFirst().orElse(null);
    }

    /** Un modelo es destacado si el panel marcó cualquiera de sus tallas. */
    private static boolean esDestacado(List<Producto> modelo) {
        return modelo.stream().anyMatch(Producto::isDestacado);
    }

    private static String textoBuscable(Producto p) {
        return String.join(" ", p.getNombre(), Objects.toString(p.getDescripcion(), ""),
                Objects.toString(p.getColeccion(), ""), p.getCategoria());
    }

    /** Minúsculas y sin tildes ni diéresis: "Älpafill" y "alpafill" coinciden. */
    static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private int posicionTalla(String talla) {
        int i = ORDEN_TALLAS.indexOf(talla.toUpperCase());
        if (i >= 0) {
            return i;
        }
        return "Única".equalsIgnoreCase(talla) ? ORDEN_TALLAS.size() + 1 : ORDEN_TALLAS.size();
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
