package mx.edu.uacm.is.slt.as.sip.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import mx.edu.uacm.is.slt.as.sip.dominio.Cliente;

/**
 * Implementa las operaciones de persistencia mediante Spring Data JPA.
 */
public interface ClienteJpaRepositorio extends JpaRepository<Cliente, String> {
}