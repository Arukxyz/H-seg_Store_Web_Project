package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.CategoriaCarta;
import pe.edu.utp.hoseg.web.dto.FiltroCatalogo;
import pe.edu.utp.hoseg.web.dto.ItemCartaForm;
import pe.edu.utp.hoseg.web.dto.LocalCafe;
import pe.edu.utp.hoseg.web.dto.LocalCafeForm;
import pe.edu.utp.hoseg.web.dto.TarjetaProducto;
import pe.edu.utp.hoseg.web.model.CafeCarta;
import pe.edu.utp.hoseg.web.model.CafeLocal;
import pe.edu.utp.hoseg.web.model.ContenidoWeb;
import pe.edu.utp.hoseg.web.repository.CafeCartaRepository;
import pe.edu.utp.hoseg.web.repository.CafeLocalRepository;
import pe.edu.utp.hoseg.web.repository.ContenidoWebRepository;
import pe.edu.utp.hoseg.web.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Höség Café (CU-05 y CU-18). La carta y los locales viven en tablas propias;
 * el café en grano es un producto más (categoría Café, 1 árbol) y la cifra de
 * árboles sembrados es el bloque 'cafe.arboles' de contenido_web.
 */
@Service
@Transactional(readOnly = true)
public class CafeService {

    /** Categoría de producto del café en grano (tabla categoria, 07). */
    public static final String CATEGORIA_CAFE = "Café";

    private final CafeCartaRepository cartaRepository;
    private final CafeLocalRepository localRepository;
    private final ContenidoWebRepository contenidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoService catalogoService;

    public CafeService(CafeCartaRepository cartaRepository,
                       CafeLocalRepository localRepository,
                       ContenidoWebRepository contenidoRepository,
                       UsuarioRepository usuarioRepository,
                       CatalogoService catalogoService) {
        this.cartaRepository = cartaRepository;
        this.localRepository = localRepository;
        this.contenidoRepository = contenidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.catalogoService = catalogoService;
    }

    /** La carta en la página, agrupada por categoría en el orden del panel. */
    public List<CategoriaCarta> carta() {
        Map<String, List<CategoriaCarta.Item>> porCategoria = new LinkedHashMap<>();
        for (CafeCarta c : cartaRepository.findByActivoTrueOrderByCategoriaAscOrdenAscNombreAsc()) {
            porCategoria.computeIfAbsent(c.getCategoria(), k -> new ArrayList<>())
                    .add(new CategoriaCarta.Item(c.getNombre(), c.getDescripcion(), c.getPrecio(), c.isDisponible()));
        }
        return porCategoria.entrySet().stream().map(e -> new CategoriaCarta(e.getKey(), e.getValue())).toList();
    }

    public List<LocalCafe> locales() {
        return localRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(l -> new LocalCafe(l.getNombre(), l.getDireccion(), l.getHorario(), l.getTelefono(), l.getUrlMapa()))
                .toList();
    }

    /** Cafés de especialidad con botón "Comprar en la tienda" (tarjetas del catálogo). */
    public List<TarjetaProducto> cafesEnGrano() {
        return catalogoService.tarjetas(new FiltroCatalogo(null, CATEGORIA_CAFE, null, null, null, null));
    }

    /** Contador "árboles sembrados por Höség Café"; 0 si el bloque no existe. */
    public long arbolesSembrados() {
        return contenidoRepository.findById(ContenidoWeb.CAFE_ARBOLES)
                .map(ContenidoWeb::getCuerpo)
                .filter(c -> c != null && c.matches("\\d+"))
                .map(Long::parseLong)
                .orElse(0L);
    }

    // ---------- Panel (4.4) ----------

    /** Toda la carta, también lo dado de baja, para editar o reactivar. */
    public List<CafeCarta> cartaParaEditar() {
        return cartaRepository.findAllByOrderByActivoDescCategoriaAscOrdenAscNombreAsc();
    }

    public ItemCartaForm itemParaEditar(Integer id) {
        CafeCarta c = buscarItem(id);
        ItemCartaForm form = new ItemCartaForm();
        form.setId(c.getId());
        form.setNombre(c.getNombre());
        form.setDescripcion(c.getDescripcion());
        form.setCategoria(c.getCategoria());
        form.setPrecio(c.getPrecio());
        form.setDisponible(c.isDisponible());
        form.setOrden(c.getOrden());
        return form;
    }

    /** Crea (id null) o edita un ítem de la carta. */
    @Transactional
    public void guardarItem(ItemCartaForm form) {
        CafeCarta c = form.getId() == null ? new CafeCarta() : buscarItem(form.getId());
        c.setNombre(form.getNombre().trim());
        c.setDescripcion(limpio(form.getDescripcion()));
        c.setCategoria(form.getCategoria().trim());
        c.setPrecio(form.getPrecio());
        c.setDisponible(form.isDisponible());
        c.setOrden(form.getOrden());
        cartaRepository.save(c);
    }

    /** Baja lógica: nunca DELETE, para poder reactivarlo. */
    @Transactional
    public void cambiarActivoItem(Integer id, boolean activo) {
        buscarItem(id).setActivo(activo);
    }

    public List<CafeLocal> localesParaEditar() {
        return localRepository.findAllByOrderByNombreAsc();
    }

    public LocalCafeForm localParaEditar(Integer id) {
        CafeLocal l = buscarLocal(id);
        LocalCafeForm form = new LocalCafeForm();
        form.setId(l.getId());
        form.setNombre(l.getNombre());
        form.setDireccion(l.getDireccion());
        form.setHorario(l.getHorario());
        form.setTelefono(l.getTelefono());
        form.setUrlMapa(l.getUrlMapa());
        form.setActivo(l.isActivo());
        return form;
    }

    @Transactional
    public void guardarLocal(LocalCafeForm form) {
        CafeLocal l = form.getId() == null ? new CafeLocal() : buscarLocal(form.getId());
        l.setNombre(form.getNombre().trim());
        l.setDireccion(form.getDireccion().trim());
        l.setHorario(limpio(form.getHorario()));
        l.setTelefono(limpio(form.getTelefono()));
        l.setUrlMapa(limpio(form.getUrlMapa()));
        l.setActivo(form.isActivo());
        localRepository.save(l);
    }

    /** Pestaña "Árboles del café": la cifra publicada en /cafe. */
    @Transactional
    public void actualizarArboles(long arboles, Integer idUsuario) {
        if (arboles < 0) {
            throw new CampoInvalidoException("arboles", "La cifra no puede ser negativa.");
        }
        ContenidoWeb bloque = contenidoRepository.findById(ContenidoWeb.CAFE_ARBOLES).orElseGet(() -> {
            ContenidoWeb nuevo = new ContenidoWeb();
            nuevo.setClave(ContenidoWeb.CAFE_ARBOLES);
            nuevo.setTitulo("Árboles sembrados por Höség Café");
            return nuevo;
        });
        bloque.setCuerpo(Long.toString(arboles));
        bloque.setUsuario(usuarioRepository.getReferenceById(idUsuario));
        contenidoRepository.save(bloque);
    }

    private CafeCarta buscarItem(Integer id) {
        return cartaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ítem de carta inexistente: " + id));
    }

    private CafeLocal buscarLocal(Integer id) {
        return localRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Local inexistente: " + id));
    }

    private static String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
