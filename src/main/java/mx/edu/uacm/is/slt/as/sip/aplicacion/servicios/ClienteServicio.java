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
 * Coordina la consulta y sincronizacion de clientes.
 */
@Service
public class ClienteServicio {

    private final ClienteRepositorio clientes;

    private final SistemaDueno sistemaDueno;

    /**
     * Crea el servicio con sus dependencias.
     *
     * @param clientes almacenamiento local
     * @param sistemaDueno acceso al Sistema Dueno
     */
    public ClienteServicio(ClienteRepositorio clientes, SistemaDueno sistemaDueno) {
        this.clientes = clientes;
        this.sistemaDueno = sistemaDueno;
    }

    /**
     * Representa el alta pendiente de implementacion.
     *
     * @param datos datos del cliente
     * @return cliente registrado cuando se implemente la operacion
     */
    public DatosCliente registrar(DatosCliente datos) {
        throw new UnsupportedOperationException(
                "El alta de clientes aun no esta implementada.");
    }

    /**
     * Representa la modificacion pendiente de implementacion.
     *
     * @param curp identificador del cliente
     * @param datos datos nuevos
     * @return cliente modificado cuando se implemente la operacion
     */
    public DatosCliente modificar(String curp, DatosCliente datos) {
        throw new UnsupportedOperationException(
                "La modificacion de clientes aun no esta implementada.");
    }

    /**
     * Consulta al servidor y actualiza la copia local.
     *
     * @param curp identificador del cliente
     * @return datos actuales del cliente
     */
    @Transactional
    public DatosCliente consultar(String curp) {
        return convertirADatos(sincronizar(curp));
    }

    /**
     * Recupera el cliente remoto y actualiza su registro local.
     *
     * @param curp identificador del cliente
     * @return entidad local sincronizada
     */
    Cliente sincronizar(String curp) {
        if (curp == null || curp.isBlank()) {
            throw new ReglaNegocioException("Ingresa la CURP del cliente.");
        }

        String identificador = curp.strip();
        DatosCliente remoto = sistemaDueno.consultarCliente(identificador)
                .orElseThrow(() -> new ClienteNoRegistradoException(identificador));

        if (!identificador.equals(remoto.curp())) {
            throw new ReglaNegocioException(
                    "La CURP recibida no coincide con la solicitada.");
        }

        Cliente local = clientes.findById(identificador).orElse(null);
        if (local == null) {
            local = new Cliente(remoto.curp(), remoto.nombres(),
                    remoto.primerApellido(), remoto.segundoApellido(),
                    remoto.direccion(), remoto.fechaNacimiento());
        } else {
            local.actualizar(remoto.nombres(), remoto.primerApellido(),
                    remoto.segundoApellido(), remoto.direccion(),
                    remoto.fechaNacimiento());
        }

        return clientes.save(local);
    }

    private static DatosCliente convertirADatos(Cliente cliente) {
        return new DatosCliente(cliente.getCurp(), cliente.getNombres(),
                cliente.getPrimerApellido(), cliente.getSegundoApellido(),
                cliente.getDireccion(), cliente.getFechaNacimiento());
    }
}