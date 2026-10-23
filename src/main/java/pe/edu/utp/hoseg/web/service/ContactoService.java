package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.MensajeAdmin;
import pe.edu.utp.hoseg.web.dto.MensajeContactoForm;
import pe.edu.utp.hoseg.web.model.EstadoMensaje;
import pe.edu.utp.hoseg.web.model.MensajeContacto;
import pe.edu.utp.hoseg.web.repository.MensajeContactoRepository;
import pe.edu.utp.hoseg.web.repository.UsuarioRepository;

import java.util.List;

/** Formulario de contacto (CU-08) y su bandeja en el panel (CU-20). */
@Service
@Transactional(readOnly = true)
public class ContactoService {

    private final MensajeContactoRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    public ContactoService(MensajeContactoRepository mensajeRepository,
                           UsuarioRepository usuarioRepository,
                           NotificacionService notificacionService) {
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
    }

    /** Guarda el mensaje y envía el acuse de recibo. */
    @Transactional
    public void registrar(MensajeContactoForm form) {
        MensajeContacto m = new MensajeContacto();
        m.setNombre(form.getNombre().trim());
        m.setEmail(form.getEmail().trim());
        m.setAsunto(form.getAsunto().trim());
        m.setMensaje(form.getMensaje().trim());
        mensajeRepository.save(m);
        notificacionService.acuseContacto(m.getEmail(), m.getNombre(), m.getAsunto());
    }

    // ---------- Panel (4.6) ----------

    /** Bandeja: todos, o solo los de un estado (null = todos), del más reciente al más antiguo. */
    public List<MensajeAdmin> bandeja(EstadoMensaje estado) {
        List<MensajeContacto> mensajes = estado == null
                ? mensajeRepository.findAllByOrderByCreadoEnDesc()
                : mensajeRepository.findByEstadoOrderByCreadoEnDesc(estado);
        return mensajes.stream().map(ContactoService::aAdmin).toList();
    }

    public long nuevos() {
        return mensajeRepository.countByEstado(EstadoMensaje.NUEVO);
    }

    @Transactional
    public void marcarAtendido(Integer id, Integer idUsuario) {
        MensajeContacto m = mensajeRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mensaje inexistente: " + id));
        if (m.getEstado() == EstadoMensaje.NUEVO) {
            m.marcarAtendido(usuarioRepository.getReferenceById(idUsuario));
        }
    }

    private static MensajeAdmin aAdmin(MensajeContacto m) {
        return new MensajeAdmin(m.getId(), m.getNombre(), m.getEmail(), m.getAsunto(), m.getMensaje(), m.getEstado(),
                m.getCreadoEn(), m.getAtendidoEn(), m.getUsuarioAtiende() == null ? null : m.getUsuarioAtiende().getNombre());
    }
}
