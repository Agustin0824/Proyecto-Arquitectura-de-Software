package mx.edu.uacm.is.slt.as.sip.aplicacion.servicios;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.ClienteRepositorio;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.dominio.Cliente;
import mx.edu.uacm.is.slt.as.sip.dominio.ClienteNoRegistradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;

/**
 * Coordina las operaciones y sincronizacion de clientes.
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
     * Registra un cliente en el Sistema Dueno y confirma el resultado.
     *
     * @param datos datos del cliente
     * @return datos del cliente registrado y confirmado
     */
    @Transactional
    public DatosCliente registrar(DatosCliente datos) {
        DatosCliente solicitud = validarDatosCliente(datos);

        var existente = sistemaDueno.consultarCliente(solicitud.curp());

        if (existente.isPresent()) {
            if (!solicitud.curp().equals(existente.get().curp())) {
                throw new ReglaNegocioException(
                        "La CURP recibida no coincide con el cliente solicitado.");
            }

            throw new ReglaNegocioException(
                    "Ya existe un cliente con la CURP proporcionada.");
        }

        sistemaDueno.registrarCliente(solicitud);

        DatosCliente registrado = consultar(solicitud.curp());

        if (!coinciden(solicitud, registrado)) {
            throw new ReglaNegocioException(
                    "No se pudo confirmar que el cliente coincida con el alta."
                    + " Consulta su estado antes de repetir la solicitud.");
        }

        return registrado;
    }

    /**
     * Modifica un cliente y comprueba el resultado remoto.
     *
     * @param curp identificador del cliente
     * @param datos datos nuevos
     * @return datos del cliente modificado y confirmado
     */
    @Transactional
    public DatosCliente modificar(String curp, DatosCliente datos) {
        if (curp == null || curp.isBlank()) {
            throw new ReglaNegocioException("Ingresa la CURP del cliente.");
        }

        String identificador = curp.strip();
        DatosCliente solicitud = validarDatosCliente(datos);

        if (!identificador.equals(solicitud.curp())) {
            throw new ReglaNegocioException(
                    "La CURP del cliente no puede modificarse.");
        }

        obtenerClienteRemoto(identificador);

        sistemaDueno.modificarCliente(solicitud);

        DatosCliente modificado = consultar(identificador);

        if (!coinciden(solicitud, modificado)) {
            throw new ReglaNegocioException(
                    "No se pudo confirmar la modificacion del cliente."
                    + " Consulta su estado antes de repetir la solicitud.");
        }

        return modificado;
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
        DatosCliente remoto = obtenerClienteRemoto(identificador);

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

    /**
     * Recupera un cliente del Sistema Dueno y comprueba su CURP.
     *
     * @param curp identificador solicitado
     * @return datos del cliente remoto
     */
    private DatosCliente obtenerClienteRemoto(String curp) {
        DatosCliente remoto = sistemaDueno.consultarCliente(curp)
                .orElseThrow(() -> new ClienteNoRegistradoException(curp));

        if (!curp.equals(remoto.curp())) {
            throw new ReglaNegocioException(
                    "La CURP recibida no coincide con la solicitada.");
        }

        return remoto;
    }

    /**
     * Valida los datos mediante las reglas de la entidad Cliente.
     *
     * @param datos datos recibidos
     * @return datos validados y normalizados
     */
    private static DatosCliente validarDatosCliente(DatosCliente datos) {
        if (datos == null) {
            throw new ReglaNegocioException(
                    "Los datos del cliente son obligatorios.");
        }

        Cliente cliente = new Cliente(datos.curp(), datos.nombres(),
                datos.primerApellido(), datos.segundoApellido(),
                datos.direccion(), datos.fechaNacimiento());

        return convertirADatos(cliente);
    }

    /**
     * Comprueba que los datos remotos coincidan con la solicitud.
     *
     * @param solicitud datos enviados
     * @param resultado datos recuperados del servidor
     * @return verdadero si todos los datos coinciden
     */
    private static boolean coinciden(
            DatosCliente solicitud, DatosCliente resultado) {

        return solicitud.curp().equals(resultado.curp())
                && solicitud.nombres().equals(resultado.nombres())
                && solicitud.primerApellido().equals(resultado.primerApellido())
                && Objects.equals(
                        solicitud.segundoApellido(), resultado.segundoApellido())
                && solicitud.direccion().equals(resultado.direccion())
                && solicitud.fechaNacimiento().equals(resultado.fechaNacimiento());
    }

    /**
     * Convierte una entidad Cliente a los datos de aplicacion.
     *
     * @param cliente entidad del dominio
     * @return datos del cliente
     */
    private static DatosCliente convertirADatos(Cliente cliente) {
        return new DatosCliente(cliente.getCurp(), cliente.getNombres(),
                cliente.getPrimerApellido(), cliente.getSegundoApellido(),
                cliente.getDireccion(), cliente.getFechaNacimiento());
    }
}