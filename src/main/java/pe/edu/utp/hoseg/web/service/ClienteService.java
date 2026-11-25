package pe.edu.utp.hoseg.web.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.CambioContrasenaForm;
import pe.edu.utp.hoseg.web.dto.DatosClienteForm;
import pe.edu.utp.hoseg.web.dto.RegistroClienteForm;
import pe.edu.utp.hoseg.web.model.Cliente;
import pe.edu.utp.hoseg.web.repository.ClienteRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Cuenta del cliente (CU-07, CU-14). Los compradores históricos de la
 * semilla existen sin contraseña: al registrarse con su mismo email se
 * completa esa fila en lugar de crear otra (uq_cliente_email lo impediría).
 */
@Service
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder codificadorContrasena;
    private final NotificacionService notificacionService;

    public ClienteService(ClienteRepository clienteRepository,
                          PasswordEncoder codificadorContrasena,
                          NotificacionService notificacionService) {
        this.clienteRepository = clienteRepository;
        this.codificadorContrasena = codificadorContrasena;
        this.notificacionService = notificacionService;
    }

    /**
     * Registra al cliente (o completa el registro de un comprador histórico).
     *
     * @return id del cliente
     * @throws CampoInvalidoException "email" si ya tiene cuenta, "numDoc" si el documento es de otro cliente
     */
    @Transactional
    public Integer registrar(RegistroClienteForm form) {
        String email = form.getEmail().trim();
        Optional<Cliente> existente = clienteRepository.findByEmailIgnoreCase(email);
        if (existente.filter(Cliente::puedeIniciarSesion).isPresent()) {
            throw new CampoInvalidoException("email",
                    "Ya hay una cuenta con este email. Inicia sesión o recupera tu contraseña.");
        }
        String numDoc = form.getNumDoc().trim();
        boolean documentoAjeno = existente
                .map(c -> clienteRepository.existsByTipoDocAndNumDocAndIdNot(form.getTipoDoc(), numDoc, c.getId()))
                .orElseGet(() -> clienteRepository.existsByTipoDocAndNumDoc(form.getTipoDoc(), numDoc));
        if (documentoAjeno) {
            throw new CampoInvalidoException("numDoc", "Ese documento ya está registrado en otra cuenta.");
        }

        Cliente cliente = existente.orElseGet(Cliente::new);
        if (existente.isEmpty()) {
            cliente.setEmail(email);   // el histórico conserva el email tal como lo cargó la semilla
        }
        cliente.setNombre(form.getNombre().trim());
        cliente.setApellido(form.getApellido().trim());
        cliente.setTipoDoc(form.getTipoDoc());
        cliente.setNumDoc(numDoc);
        cliente.setTelefono(limpio(form.getTelefono()));
        cliente.setDireccion(limpio(form.getDireccion()));
        cliente.setPasswordHash(codificadorContrasena.encode(form.getContrasena()));
        cliente.setTerminosAceptadosEn(OffsetDateTime.now());
        clienteRepository.save(cliente);

        notificacionService.bienvenida(cliente.getEmail(), cliente.getNombre());
        return cliente.getId();
    }

    /** "Mis datos" precargado (2.7). */
    public DatosClienteForm datos(Integer idCliente) {
        Cliente cliente = buscar(idCliente);
        DatosClienteForm form = new DatosClienteForm();
        form.setEmail(cliente.getEmail());
        form.setNombre(cliente.getNombre());
        form.setApellido(cliente.getApellido());
        form.setTelefono(cliente.getTelefono());
        form.setDireccion(cliente.getDireccion());
        return form;
    }

    /** Guarda nombre, apellido, teléfono y dirección. El email no se cambia aquí. */
    @Transactional
    public void actualizarDatos(Integer idCliente, DatosClienteForm form) {
        Cliente cliente = buscar(idCliente);
        cliente.setNombre(form.getNombre().trim());
        cliente.setApellido(form.getApellido().trim());
        cliente.setTelefono(limpio(form.getTelefono()));
        cliente.setDireccion(limpio(form.getDireccion()));
    }

    /** @throws CampoInvalidoException "actual" si la contraseña actual no coincide */
    @Transactional
    public void cambiarContrasena(Integer idCliente, CambioContrasenaForm form) {
        Cliente cliente = buscar(idCliente);
        if (!codificadorContrasena.matches(form.getActual(), cliente.getPasswordHash())) {
            throw new CampoInvalidoException("actual", "La contraseña actual no es correcta.");
        }
        cliente.setPasswordHash(codificadorContrasena.encode(form.getNueva()));
    }

    private Cliente buscar(Integer idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente inexistente: " + idCliente));
    }

    private static String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
