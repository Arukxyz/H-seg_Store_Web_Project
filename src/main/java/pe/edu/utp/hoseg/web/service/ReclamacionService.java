package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.ReclamoAdmin;
import pe.edu.utp.hoseg.web.dto.ReclamoForm;
import pe.edu.utp.hoseg.web.dto.RespuestaReclamoForm;
import pe.edu.utp.hoseg.web.model.EstadoReclamo;
import pe.edu.utp.hoseg.web.model.LibroReclamacion;
import pe.edu.utp.hoseg.web.model.TipoReclamo;
import pe.edu.utp.hoseg.web.repository.LibroReclamacionRepository;
import pe.edu.utp.hoseg.web.repository.UsuarioRepository;
import pe.edu.utp.hoseg.web.repository.VentaRepository;

import java.util.List;
import java.util.Locale;

/**
 * Libro de Reclamaciones virtual (CU-24), obligatorio para tiendas en línea
 * en Perú. El número de hoja lo genera la base (LR-000001); aquí solo se lee.
 */
@Service
@Transactional(readOnly = true)
public class ReclamacionService {

    private final LibroReclamacionRepository reclamoRepository;
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    public ReclamacionService(LibroReclamacionRepository reclamoRepository,
                              VentaRepository ventaRepository,
                              UsuarioRepository usuarioRepository,
                              NotificacionService notificacionService) {
        this.reclamoRepository = reclamoRepository;
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
    }

    /**
     * Registra la hoja y envía la constancia por correo.
     *
     * @return número de hoja (LR-000001) para la pantalla "Registrado"
     * @throws CampoInvalidoException "codigoComprobante" si el código de pedido no existe
     */
    @Transactional
    public String registrar(ReclamoForm form) {
        String codigo = limpio(form.getCodigoComprobante());
        if (codigo != null) {
            codigo = codigo.toUpperCase(Locale.ROOT);
            if (ventaRepository.findByCodigoComprobante(codigo).isEmpty()) {
                throw new CampoInvalidoException("codigoComprobante",
                        "No encontramos ese código de pedido. Revísalo o deja el campo vacío.");
            }
        }
        LibroReclamacion r = new LibroReclamacion();
        r.setTipo(form.getTipo());
        r.setNombre(form.getNombre().trim());
        r.setTipoDoc(form.getTipoDoc());
        r.setNumDoc(form.getNumDoc().trim());
        r.setDomicilio(form.getDomicilio().trim());
        r.setEmail(form.getEmail().trim());
        r.setTelefono(limpio(form.getTelefono()));
        r.setTipoBien(form.getTipoBien());
        r.setMontoReclamado(form.getMontoReclamado());
        r.setDescripcionBien(form.getDescripcionBien().trim());
        r.setDetalle(form.getDetalle().trim());
        r.setPedidoConsumidor(form.getPedidoConsumidor().trim());
        r.setCodigoComprobante(codigo);
        reclamoRepository.saveAndFlush(r);   // el insert trae numero_hoja generado por la base

        notificacionService.constanciaReclamo(r.getEmail(), r.getNombre(), r.getNumeroHoja(),
                r.getTipo() == TipoReclamo.RECLAMO ? "reclamo" : "queja");
        return r.getNumeroHoja();
    }

    // ---------- Panel (4.6, pestaña Reclamaciones) ----------

    public List<ReclamoAdmin> todos() {
        return reclamoRepository.findAllByOrderByCreadoEnDesc().stream().map(ReclamacionService::aAdmin).toList();
    }

    public long sinResponder() {
        return reclamoRepository.countByEstado(EstadoReclamo.REGISTRADO);
    }

    /** Responde la hoja (estado, respuesta y fecha van juntos) y envía la respuesta por correo. */
    @Transactional
    public void responder(Integer id, RespuestaReclamoForm form, Integer idUsuario) {
        LibroReclamacion r = reclamoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Hoja inexistente: " + id));
        if (r.getEstado() == EstadoReclamo.ATENDIDO) {
            throw new NegocioException("La hoja " + r.getNumeroHoja() + " ya fue respondida.");
        }
        r.responder(form.getRespuesta().trim(), usuarioRepository.getReferenceById(idUsuario));
        notificacionService.respuestaReclamo(r.getEmail(), r.getNombre(), r.getNumeroHoja(), r.getRespuesta());
    }

    private static ReclamoAdmin aAdmin(LibroReclamacion r) {
        return new ReclamoAdmin(r.getId(), r.getNumeroHoja(), r.getTipo(), r.getNombre(), r.getTipoDoc(), r.getNumDoc(),
                r.getDomicilio(), r.getEmail(), r.getTelefono(), r.getTipoBien(), r.getMontoReclamado(),
                r.getDescripcionBien(), r.getDetalle(), r.getPedidoConsumidor(), r.getCodigoComprobante(),
                r.getEstado(), r.getRespuesta(), r.getCreadoEn(), r.getAtendidoEn(),
                r.getUsuarioAtiende() == null ? null : r.getUsuarioAtiende().getNombre());
    }

    private static String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
