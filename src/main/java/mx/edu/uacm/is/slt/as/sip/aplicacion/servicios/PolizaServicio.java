package mx.edu.uacm.is.slt.as.sip.aplicacion.servicios;

import java.util.List;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.dominio.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.sip.dominio.TipoPoliza;

/**
 * Coordina las consultas de polizas al Sistema Dueno.
 */
@Service
public class PolizaServicio {

    private final SistemaDueno sistemaDueno;

    /**
     * Crea el servicio con su proveedor de consultas.
     *
     * @param sistemaDueno acceso al Sistema Dueno
     */
    public PolizaServicio(SistemaDueno sistemaDueno) {
        this.sistemaDueno = sistemaDueno;
    }

    /**
     * Consulta una poliza por su clave.
     *
     * @param clave identificador de la poliza
     * @return datos de la poliza
     */
    public DatosPoliza consultar(String clave) {
        if (clave == null || clave.isBlank()) {
            throw new ReglaNegocioException("Ingresa la clave de la poliza.");
        }

        String identificador = clave.strip();
        DatosPoliza resultado = sistemaDueno.consultarPoliza(identificador)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La poliza solicitada no existe."));

        if (!identificador.equals(resultado.clave())) {
            throw new ReglaNegocioException(
                    "La clave recibida no coincide con la solicitada.");
        }

        return resultado;
    }

    /**
     * Consulta polizas con filtros opcionales.
     *
     * @param curp CURP del cliente, opcional
     * @param tipo codigo de seguro: 0, 1 o 2; opcional
     * @return polizas que cumplen los filtros
     */
    public List<DatosPoliza> listar(String curp, String tipo) {
        String filtroCurp = curp == null ? "" : curp.strip();
        String filtroTipo = tipo == null ? "" : tipo.strip();

        if (!filtroTipo.isEmpty()) {
            filtroTipo = normalizarTipo(filtroTipo);
        }

        if (!filtroCurp.isEmpty()) {
            List<DatosPoliza> resultados
                    = sistemaDueno.listarPolizasPorCurp(filtroCurp);

            if (filtroTipo.isEmpty()) {
                return resultados;
            }

            String codigoTipo = filtroTipo;
            return resultados.stream()
                    .filter(poliza -> codigoTipo.equals(poliza.tipo()))
                    .toList();
        }

        if (!filtroTipo.isEmpty()) {
            return sistemaDueno.listarPolizasPorTipo(filtroTipo);
        }

        return sistemaDueno.listarPolizas();
    }

    /**
     * Representa el alta pendiente de implementacion.
     *
     * @param datos datos de la poliza
     * @return poliza cuando se implemente la operacion
     */
    public DatosPoliza registrar(DatosPoliza datos) {
        throw new UnsupportedOperationException(
                "El alta de polizas aun no esta implementada.");
    }

    /**
     * Representa la modificacion pendiente de implementacion.
     *
     * @param clave identificador de la poliza
     * @param datos datos nuevos
     * @return poliza cuando se implemente la operacion
     */
    public DatosPoliza modificar(String clave, DatosPoliza datos) {
        throw new UnsupportedOperationException(
                "La modificacion de polizas aun no esta implementada.");
    }

    /**
     * Representa la baja pendiente de implementacion.
     *
     * @param clave identificador de la poliza
     */
    public void eliminar(String clave) {
        throw new UnsupportedOperationException(
                "La baja de polizas aun no esta implementada.");
    }

    /**
     * Valida y normaliza el codigo del tipo de seguro.
     *
     * @param tipo codigo recibido
     * @return codigo validado
     */
    private static String normalizarTipo(String tipo) {
        try {
            int codigo = Integer.parseInt(tipo);
            TipoPoliza tipoValidado = TipoPoliza.desdeCodigo(codigo);
            return Integer.toString(tipoValidado.getCodigo());
        } catch (NumberFormatException excepcion) {
            throw new ReglaNegocioException(
                    "El tipo de poliza debe ser 0, 1 o 2.");
        }
    }
}