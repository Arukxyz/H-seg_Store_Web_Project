package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.ResenaForm;
import pe.edu.utp.hoseg.web.dto.ResenaModeracion;
import pe.edu.utp.hoseg.web.dto.ResenaVista;
import pe.edu.utp.hoseg.web.dto.ResumenResenas;
import pe.edu.utp.hoseg.web.model.Cliente;
import pe.edu.utp.hoseg.web.model.DetalleVenta;
import pe.edu.utp.hoseg.web.model.EstadoResena;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.Resena;
import pe.edu.utp.hoseg.web.model.Usuario;
import pe.edu.utp.hoseg.web.repository.DetalleVentaRepository;
import pe.edu.utp.hoseg.web.repository.ProductoRepository;
import pe.edu.utp.hoseg.web.repository.ResenaRepository;
import pe.edu.utp.hoseg.web.repository.UsuarioRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Reseñas de productos (CU-03, CU-22, CU-23). Reglas que la base no puede
 * forzar sin triggers y que por eso viven aquí:
 * <ul>
 *   <li>solo se reseña lo comprado en una venta PAGADO;</li>
 *   <li>una reseña por cliente y modelo (producto.nombre), editable;</li>
 *   <li>en público solo se ven las PUBLICADA de ventas que siguen PAGADO.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public ResenaService(ResenaRepository resenaRepository,
                         DetalleVentaRepository detalleVentaRepository,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository) {
        this.resenaRepository = resenaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** "★ 4.5 (12 reseñas)" junto al precio del modelo. */
    public ResumenResenas resumen(String nombreModelo) {
        return resenaRepository.resumenDelModelo(nombreModelo, EstadoResena.PUBLICADA, EstadoVenta.PAGADO);
    }

    /** Reseñas visibles del modelo, de la más reciente a la más antigua. */
    public List<ResenaVista> publicadas(String nombreModelo) {
        return resenaRepository.delModelo(nombreModelo, EstadoResena.PUBLICADA, EstadoVenta.PAGADO).stream()
                .map(r -> new ResenaVista(
                        r.getCalificacion(),
                        r.getTitulo(),
                        r.getComentario(),
                        r.getDetalleVenta().getVenta().getCliente().getNombrePublico(),
                        r.getDetalleVenta().getProducto().getTallaMostrada(),
                        r.getCreadoEn(),
                        r.getActualizadoEn() != null))
                .toList();
    }

    /**
     * Formulario "Escribe tu reseña" para este cliente y modelo:
     * vacío si no puede reseñar (no lo compró o el pedido no está pagado),
     * precargado si ya lo reseñó (edición), en blanco si puede escribir una.
     */
    public Optional<ResenaForm> formularioPara(Integer idCliente, String nombreModelo) {
        if (idCliente == null) {
            return Optional.empty();
        }
        Optional<Resena> existente = resenaRepository.delClienteParaModelo(idCliente, nombreModelo);
        if (existente.isPresent()) {
            Resena r = existente.get();
            ResenaForm form = new ResenaForm();
            form.setCalificacion(r.getCalificacion());
            form.setTitulo(r.getTitulo());
            form.setComentario(r.getComentario());
            return Optional.of(form);
        }
        boolean comproPagado = !detalleVentaRepository.comprasDelModelo(idCliente, nombreModelo, EstadoVenta.PAGADO).isEmpty();
        return comproPagado ? Optional.of(new ResenaForm()) : Optional.empty();
    }

    /** true si el cliente ya reseñó el modelo (el botón dice "Editar mi reseña"). */
    public boolean yaReseno(Integer idCliente, String nombreModelo) {
        return idCliente != null && resenaRepository.delClienteParaModelo(idCliente, nombreModelo).isPresent();
    }

    /**
     * Publica o edita la reseña del cliente sobre el modelo del código dado (CU-22).
     *
     * @throws NegocioException si el cliente no tiene una compra pagada de ese modelo
     */
    @Transactional
    public void guardar(Integer idCliente, String codigoProducto, ResenaForm form) {
        Producto producto = productoRepository.findById(codigoProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto inexistente: " + codigoProducto));
        String modelo = producto.getNombre();

        Resena resena = resenaRepository.delClienteParaModelo(idCliente, modelo).orElse(null);
        if (resena == null) {
            DetalleVenta linea = detalleVentaRepository.comprasDelModelo(idCliente, modelo, EstadoVenta.PAGADO).stream()
                    .findFirst()
                    .orElseThrow(() -> new NegocioException(
                            "Puedes reseñar este producto cuando tu pedido esté pagado."));
            resena = new Resena();
            resena.setDetalleVenta(linea);
            resena.setEstado(EstadoResena.PUBLICADA);
        } else {
            resena.setActualizadoEn(OffsetDateTime.now());
        }
        resena.setCalificacion(form.getCalificacion());
        resena.setTitulo(limpio(form.getTitulo()));
        resena.setComentario(form.getComentario().trim());
        resenaRepository.save(resena);
    }

    /** Panel 4.7: todas las reseñas, o solo las de un estado (null = todas). */
    public List<ResenaModeracion> paraModerar(EstadoResena estado) {
        return resenaRepository.paraModerar(estado).stream().map(ResenaService::aModeracion).toList();
    }

    /** Oculta una reseña ofensiva o ajena al producto; queda registrado quién y cuándo. */
    @Transactional
    public void ocultar(Integer idResena, Integer idUsuario) {
        buscar(idResena).ocultar(moderador(idUsuario));
    }

    @Transactional
    public void republicar(Integer idResena, Integer idUsuario) {
        buscar(idResena).publicar(moderador(idUsuario));
    }

    private Resena buscar(Integer idResena) {
        return resenaRepository.findById(idResena)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reseña inexistente: " + idResena));
    }

    private Usuario moderador(Integer idUsuario) {
        return usuarioRepository.getReferenceById(idUsuario);
    }

    private static ResenaModeracion aModeracion(Resena r) {
        DetalleVenta linea = r.getDetalleVenta();
        Cliente cliente = linea.getVenta().getCliente();
        return new ResenaModeracion(
                r.getId(),
                linea.getProducto().getNombre(),
                linea.getProducto().getTallaMostrada(),
                linea.getVenta().getCodigoComprobante(),
                r.getCalificacion(),
                r.getTitulo(),
                r.getComentario(),
                cliente.getNombreCompleto(),
                cliente.getEmail(),
                r.getCreadoEn(),
                r.getEstado(),
                r.getUsuarioModera() == null ? null : r.getUsuarioModera().getNombre());
    }

    private static String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
