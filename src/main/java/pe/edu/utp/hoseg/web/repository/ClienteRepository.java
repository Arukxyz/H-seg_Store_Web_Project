package pe.edu.utp.hoseg.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.hoseg.web.model.Cliente;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /** El email es la credencial; el índice único de 06 es sobre lower(email). */
    Optional<Cliente> findByEmailIgnoreCase(String email);
}
