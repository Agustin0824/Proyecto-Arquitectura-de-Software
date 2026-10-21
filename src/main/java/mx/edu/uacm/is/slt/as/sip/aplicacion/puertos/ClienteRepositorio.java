package mx.edu.uacm.is.slt.as.sip.aplicacion.puertos;

import java.util.Optional;
import mx.edu.uacm.is.slt.as.sip.dominio.Cliente;

/**
 * Define las operaciones de almacenamiento local de clientes.
 */
public interface ClienteRepositorio {

    /**
     * Busca un cliente por su identificador.
     *
     * @param curp identificador del cliente
     * @return cliente encontrado o resultado vacio
     */
    Optional<Cliente> findById(String curp);

    /**
     * Guarda el estado de un cliente.
     *
     * @param cliente cliente que se guardara
     * @return cliente guardado
     */
    Cliente save(Cliente cliente);
}