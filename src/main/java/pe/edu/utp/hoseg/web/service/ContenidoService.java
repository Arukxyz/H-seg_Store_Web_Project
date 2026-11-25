package pe.edu.utp.hoseg.web.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;
import pe.edu.utp.hoseg.web.dto.BloqueContenido;
import pe.edu.utp.hoseg.web.dto.ContenidoForm;
import pe.edu.utp.hoseg.web.dto.FotoEvidencia;
import pe.edu.utp.hoseg.web.model.ContenidoWeb;
import pe.edu.utp.hoseg.web.repository.ContenidoWebRepository;
import pe.edu.utp.hoseg.web.repository.UsuarioRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Contenido editable sin tocar código (CU-06, CU-19): banners, textos de
 * Nosotros y políticas. Si un bloque no existe o está vacío, la plantilla
 * muestra su texto por defecto.
 */
@Service
@Transactional(readOnly = true)
public class ContenidoService {

    private final ContenidoWebRepository contenidoRepository;
    private final UsuarioRepository usuarioRepository;

    public ContenidoService(ContenidoWebRepository contenidoRepository, UsuarioRepository usuarioRepository) {
        this.contenidoRepository = contenidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<BloqueContenido> bloque(String clave) {
        return contenidoRepository.findById(clave).map(ContenidoService::aBloque);
    }

    /**
     * Varios bloques para una página, por clave. En Thymeleaf:
     * {@code ${bloques['home.banner']?.titulo ?: 'texto por defecto'}} (no llamar "contenido" al atributo: es un parámetro del layout).
     */
    public Map<String, BloqueContenido> bloques(String... claves) {
        Map<String, BloqueContenido> mapa = new HashMap<>();
        contenidoRepository.findAllById(List.of(claves)).forEach(c -> mapa.put(c.getClave(), aBloque(c)));
        return mapa;
    }

    /**
     * Fotos reales de entregas para la galería de Nosotros: los archivos .jpg, .jpeg, .png o
     * .webp de static/images/evidencia/, ordenados por nombre. Si no hay ninguno la lista
     * queda vacía y la página no muestra la galería: sin fotos propias no hay "evidencia".
     */
    public List<FotoEvidencia> fotosEvidencia() {
        try {
            Resource[] archivos = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:/static/images/evidencia/*");
            return Arrays.stream(archivos)
                    .map(Resource::getFilename)
                    .filter(n -> n != null && n.toLowerCase(Locale.ROOT).matches(".+\\.(jpe?g|png|webp)"))
                    .sorted()
                    .map(n -> new FotoEvidencia("/images/evidencia/" + UriUtils.encodePathSegment(n, StandardCharsets.UTF_8),
                            FotoEvidencia.pieDesdeArchivo(n)))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    // ---------- Panel (4.5) ----------

    /** Lista de bloques editables (la cifra del café se edita en su pestaña). */
    public List<BloqueContenido> todos() {
        return contenidoRepository.findAllByOrderByClaveAsc().stream()
                .filter(c -> !ContenidoWeb.CAFE_ARBOLES.equals(c.getClave()))
                .map(ContenidoService::aBloque)
                .toList();
    }

    public ContenidoForm paraEditar(String clave) {
        ContenidoWeb c = contenidoRepository.findById(clave)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bloque inexistente: " + clave));
        ContenidoForm form = new ContenidoForm();
        form.setTitulo(c.getTitulo());
        form.setCuerpo(c.getCuerpo());
        form.setImagenUrl(c.getImagenUrl());
        return form;
    }

    /** Guarda el bloque y quién lo cambió. Solo bloques existentes: las claves las fija el código. */
    @Transactional
    public void actualizar(String clave, ContenidoForm form, Integer idUsuario) {
        if (ContenidoWeb.CAFE_ARBOLES.equals(clave)) {
            throw new NegocioException("La cifra de árboles del café se edita en la pestaña del café.");
        }
        ContenidoWeb c = contenidoRepository.findById(clave)
                .orElseThrow(() -> new RecursoNoEncontradoException("Bloque inexistente: " + clave));
        c.setTitulo(limpio(form.getTitulo()));
        c.setCuerpo(limpio(form.getCuerpo()));
        c.setImagenUrl(limpio(form.getImagenUrl()));
        c.setUsuario(usuarioRepository.getReferenceById(idUsuario));
    }

    private static BloqueContenido aBloque(ContenidoWeb c) {
        return new BloqueContenido(c.getClave(), c.getTitulo(), c.getCuerpo(), c.getImagenUrl(), c.getActualizadoEn());
    }

    private static String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
