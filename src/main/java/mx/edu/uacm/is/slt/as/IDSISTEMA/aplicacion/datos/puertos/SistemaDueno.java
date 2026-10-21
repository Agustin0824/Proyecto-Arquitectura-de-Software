package mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.puertos;

import java.util.List;
import java.util.Optional;

import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.datos.DatosPoliza;

/**
 * Puerto hacia el Sistema Dueno. Todo fallo de comunicacion se informa con
 * SistemaDuenoNoDisponibleException (EX-DND).
 */
public interface SistemaDueno {

    /**
     * Consulta un cliente.
     *
     * @param curp CURP del cliente
     * @return el cliente, o vacio si no existe
     */
    Optional<DatosCliente> consultarCliente(String curp);

    /**
     * Registra un cliente.
     *
     * @param cliente datos del cliente
     */
    void registrarCliente(DatosCliente cliente);

    /**
     * Modifica un cliente.
     *
     * @param cliente datos nuevos del cliente
     */
    void modificarCliente(DatosCliente cliente);

    /**
     * Consulta una poliza por clave.
     *
     * @param clave clave de la poliza
     * @return la poliza, o vacio si no existe
     */
    Optional<DatosPoliza> consultarPoliza(String clave);

    /**
     * Lista todas las polizas.
     *
     * @return polizas existentes
     */
    List<DatosPoliza> listarPolizas();

    /**
     * Lista las polizas de un cliente.
     *
     * @param curp CURP del cliente
     * @return polizas del cliente
     */
    List<DatosPoliza> listarPolizasPorCurp(String curp);

    /**
     * Lista las polizas de un tipo.
     *
     * @param tipo tipo de poliza
     * @return polizas del tipo
     */
    List<DatosPoliza> listarPolizasPorTipo(String tipo);

    /**
     * Registra una poliza.
     *
     * @param poliza datos de la poliza
     */
    void registrarPoliza(DatosPoliza poliza);

    /**
     * Modifica una poliza.
     *
     * @param poliza datos nuevos de la poliza
     */
    void modificarPoliza(DatosPoliza poliza);

    /**
     * Elimina una poliza.
     *
     * @param clave clave de la poliza
     */
    void eliminarPoliza(String clave);

    /**
     * Lista los beneficiarios de una poliza.
     *
     * @param clavePoliza clave de la poliza
     * @return beneficiarios registrados
     */
    List<DatosBeneficiario> listarBeneficiarios(String clavePoliza);

    /**
     * Registra un beneficiario.
     *
     * @param beneficiario datos del beneficiario
     */
    void registrarBeneficiario(DatosBeneficiario beneficiario);
}
