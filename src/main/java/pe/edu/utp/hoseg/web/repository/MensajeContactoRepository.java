package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.EstadoMensaje;
import pe.edu.utp.hoseg.web.model.MensajeContacto;

import java.util.List;

public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Integer> {

    List<MensajeContacto> findAllByOrderByCreadoEnDesc();

    List<MensajeContacto> findByEstadoOrderByCreadoEnDesc(EstadoMensaje estado);

    long countByEstado(EstadoMensaje estado);
}
