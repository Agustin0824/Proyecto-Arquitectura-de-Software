package mx.edu.uacm.is.slt.as.sip.aplicacion;

import java.util.List;
import java.util.Optional;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puerto.ConsultaPolizas;
import mx.edu.uacm.is.slt.as.sip.dominio.Poliza;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.sip.dominio.TipoPoliza;

/**
 * Coordina las consultas de polizas.
 *
 * @author Luis Angel
 */
public class ServicioPolizas {

    private final ConsultaPolizas consultaPolizas;

    /**
     * Crea el servicio con su proveedor de consultas.
     *
     * @param consultaPolizas proveedor de consultas al Sistema Dueno
     */
    public ServicioPolizas(ConsultaPolizas consultaPolizas) {
        if (consultaPolizas == null) {
            throw new IllegalArgumentException("El proveedor de consultas es obligatorio.");
        }
        this.consultaPolizas = consultaPolizas;
    }

    /**
     * Busca una poliza por su clave.
     *
     * @param clave identificador de la poliza
     * @return poliza encontrada o resultado vacio
     */
    public Optional<Poliza> buscarPorClave(String clave) {
        if (clave == null || clave.isBlank()) {
            throw new ReglaNegocioException("Ingresa la clave de la poliza.");
        }
        return consultaPolizas.buscarPorClave(clave.strip());
    }

    /**
     * Consulta todas las polizas.
     *
     * @return listado de polizas
     */
    public List<Poliza> consultarTodas() {
        return consultaPolizas.consultarTodas();
    }

    /**
     * Consulta las polizas de un cliente.
     *
     * @param curp CURP del titular
     * @return polizas del cliente
     */
    public List<Poliza> buscarPorCurp(String curp) {
        if (curp == null || curp.isBlank()) {
            throw new ReglaNegocioException("Ingresa la CURP del titular.");
        }
        return consultaPolizas.buscarPorCurp(curp.strip());
    }

    /**
     * Consulta las polizas del tipo indicado.
     *
     * @param tipo tipo de seguro
     * @return polizas del tipo solicitado
     */
    public List<Poliza> buscarPorTipo(TipoPoliza tipo) {
        if (tipo == null) {
            throw new ReglaNegocioException("Selecciona el tipo de poliza.");
        }
        return consultaPolizas.buscarPorTipo(tipo);
    }
}