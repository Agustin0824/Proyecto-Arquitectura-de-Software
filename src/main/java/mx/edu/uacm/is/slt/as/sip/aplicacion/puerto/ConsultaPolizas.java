package mx.edu.uacm.is.slt.as.sip.aplicacion.puerto;

import java.util.List;
import java.util.Optional;
import mx.edu.uacm.is.slt.as.sip.dominio.Poliza;
import mx.edu.uacm.is.slt.as.sip.dominio.TipoPoliza;

/**
 * Define las consultas de polizas al Sistema Dueno.
 *
 * @author Luis Angel
 */
public interface ConsultaPolizas {

    /**
     * Busca una poliza mediante su clave.
     *
     * @param clave identificador de la poliza
     * @return poliza encontrada o un resultado vacio si no existe
     */
    Optional<Poliza> buscarPorClave(String clave);

    /**
     * Consulta las polizas disponibles.
     *
     * @return listado de polizas
     */
    List<Poliza> consultarTodas();

    /**
     * Consulta las polizas de un titular.
     *
     * @param curp CURP del titular
     * @return polizas del cliente
     */
    List<Poliza> buscarPorCurp(String curp);

    /**
     * Consulta las polizas de un tipo de seguro.
     *
     * @param tipo tipo de seguro
     * @return polizas del tipo solicitado
     */
    List<Poliza> buscarPorTipo(TipoPoliza tipo);
}