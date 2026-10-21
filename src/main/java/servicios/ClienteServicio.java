package mx.edu.uacm.is.slt.as.sip.aplicacion.servicios;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.ClienteRepositorio;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.dominio.Cliente;
import mx.edu.uacm.is.slt.as.sip.dominio.ClienteNoRegistradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;

/**
 * Casos de uso: alta, modificacion y consulta de clientes.
 *
 * <p>El Sistema Dueno es la fuente de verdad: primero se valida, luego se escribe
 * en el Sistema Dueno y solo si responde bien se actualiza la copia local.
 */
@Service
public class ClienteServicio {

    private final ClienteRepositorio clientes;

    private final SistemaDueno sistemaDueno;

    /**
     * Crea el servicio con sus puertos.
     *
     * @param clientes almacenamiento local
     * @param sistemaDueno puerto hacia el Sistema Dueno
     */
    public ClienteServicio(ClienteRepositorio clientes, SistemaDueno sistemaDueno) {
        this.clientes = clientes;
        this.sistemaDueno = sistemaDueno;
    }

    /**
     * Alta de cliente.
     *
     * @param datos datos del nuevo cliente
     * @return datos del cliente registrado
     */
    @Transactional
    public DatosCliente registrar(DatosCliente datos) {
        Cliente cliente = new Cliente(datos.curp(), datos.nombre(), datos.direccion(),
                datos.fechaNacimiento());
        if (sistemaDueno.consultarCliente(cliente.getCurp()).isPresent()) {
            throw new ReglaNegocioException("El cliente ya esta registrado.");
        }
        sistemaDueno.registrarCliente(aDatos(cliente));
        return aDatos(clientes.save(cliente));
    }

    /**
     * Modificacion de cliente.
     *
     * @param curp CURP del cliente
     * @param datos datos nuevos
     * @return datos del cliente modificado
     */
    @Transactional
    public DatosCliente modificar(String curp, DatosCliente datos) {
        Cliente cliente = sincronizar(curp);
        cliente.actualizar(datos.nombre(), datos.direccion(), datos.fechaNacimiento());
        sistemaDueno.modificarCliente(aDatos(cliente));
        return aDatos(clientes.save(cliente));
    }

    /**
     * Consulta de cliente.
     *
     * @param curp CURP del cliente
     * @return datos del cliente
     */
    @Transactional
    public DatosCliente consultar(String curp) {
        return aDatos(sincronizar(curp));
    }

    /**
     * Obtiene el cliente del Sistema Dueno y refresca la copia local.
     *
     * @param curp CURP del cliente
     * @return la entidad local actualizada
     */
    Cliente sincronizar(String curp) {
        DatosCliente remoto = sistemaDueno.consultarCliente(curp)
                .orElseThrow(() -> new ClienteNoRegistradoException(curp));
        Cliente local = clientes.findById(curp).orElse(null);
        if (local == null) {
            local = new Cliente(remoto.curp(), remoto.nombre(), remoto.direccion(),
                    remoto.fechaNacimiento());
        } else {
            local.actualizar(remoto.nombre(), remoto.direccion(), remoto.fechaNacimiento());
        }
        return clientes.save(local);
    }

    private static DatosCliente aDatos(Cliente cliente) {
        return new DatosCliente(cliente.getCurp(), cliente.getNombre(),
                cliente.getDireccion(), cliente.getFechaNacimiento());
    }
}