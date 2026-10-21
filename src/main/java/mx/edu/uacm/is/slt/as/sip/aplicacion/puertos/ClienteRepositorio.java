package mx.edu.uacm.is.slt.as.sip.aplicacion.puertos;

import org.springframework.data.jpa.repository.JpaRepository;
import mx.edu.uacm.is.slt.as.sip.dominio.Cliente;

/**
 * Acceso al almacenamiento local de clientes.
 */
public interface ClienteRepositorio extends JpaRepository<Cliente, String> {
}