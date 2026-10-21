package mx.edu.uacm.is.slt.as.sip.infraestructura.persistencia;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.ClienteRepositorio;
import mx.edu.uacm.is.slt.as.sip.dominio.Cliente;

/**
 * Adapta el almacenamiento JPA al contrato de la aplicacion.
 */
@Repository
public class ClientePersistencia implements ClienteRepositorio {

    private final ClienteJpaRepositorio repositorio;

    /**
     * Crea el adaptador de persistencia.
     *
     * @param repositorio acceso a PostgreSQL mediante JPA
     */
    public ClientePersistencia(ClienteJpaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Cliente> findById(String curp) {
        return repositorio.findById(curp);
    }

    @Override
    public Cliente save(Cliente cliente) {
        return repositorio.save(cliente);
    }
}