package mx.edu.uacm.is.slt.as.sip.aplicacion.servicios;

import java.util.List;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.dominio.ClienteNoRegistradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.Poliza;
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
     * Consulta polizas con paginacion opcional.
     *
     * @param curp CURP del cliente, opcional
     * @param tipo tipo de poliza, opcional
     * @param pagina numero de pagina desde cero, opcional
     * @param tamano cantidad de registros por pagina, opcional
     * @return registros de la pagina solicitada
     */
    public List<DatosPoliza> listar(String curp, String tipo,
            Integer pagina, Integer tamano) {
        if (pagina == null && tamano == null) {
            return listar(curp, tipo);
        }

        int numeroPagina = pagina == null ? 0 : pagina;
        int cantidad = tamano == null ? 20 : tamano;

        if (numeroPagina < 0) {
            throw new ReglaNegocioException(
                    "La pagina debe ser mayor o igual que cero.");
        }

        if (cantidad < 1 || cantidad > 100) {
            throw new ReglaNegocioException(
                    "El tamano de pagina debe estar entre 1 y 100.");
        }

        List<DatosPoliza> resultados = listar(curp, tipo);
        long inicio = (long) numeroPagina * cantidad;

        if (inicio >= resultados.size()) {
            return List.of();
        }

        int desde = (int) inicio;
        int hasta = (int) Math.min(inicio + cantidad, resultados.size());

        return List.copyOf(resultados.subList(desde, hasta));
    }

    /**
     * Valida y registra una poliza en el Sistema Dueno.
     *
     * @param datos datos de la poliza
     * @return datos confirmados mediante consulta remota
     */
    public DatosPoliza registrar(DatosPoliza datos) {
        DatosPoliza solicitud = validarDatosPoliza(datos);

        var cliente = sistemaDueno.consultarCliente(solicitud.curpCliente())
                .orElseThrow(() -> new ClienteNoRegistradoException(
                        solicitud.curpCliente()));

        if (!solicitud.curpCliente().equals(cliente.curp())) {
            throw new ReglaNegocioException(
                    "La CURP recibida no coincide con el cliente solicitado.");
        }

        if (sistemaDueno.consultarPoliza(solicitud.clave()).isPresent()) {
            throw new ReglaNegocioException(
                    "Ya existe una poliza con la clave proporcionada.");
        }

        sistemaDueno.registrarPoliza(solicitud);

        DatosPoliza registrada = consultar(solicitud.clave());

        boolean coincide = solicitud.tipo().equals(registrada.tipo())
                && registrada.montoAsegurado() != null
                && solicitud.montoAsegurado()
                        .compareTo(registrada.montoAsegurado()) == 0
                && solicitud.descripcion().equals(registrada.descripcion())
                && solicitud.curpCliente().equals(registrada.curpCliente());

        if (!coincide) {
            throw new ReglaNegocioException(
                    "No se pudo confirmar que la poliza coincida con el alta."
                            + " Consulta su estado antes de repetir la solicitud.");
        }

        return registrada;
    }

    /**
     * Modifica una poliza y comprueba el resultado remoto.
     *
     * @param clave identificador de la poliza
     * @param datos datos nuevos
     * @return poliza confirmada mediante consulta remota
     */
    public DatosPoliza modificar(String clave, DatosPoliza datos) {
        if (clave == null || clave.isBlank()) {
            throw new ReglaNegocioException(
                    "Ingresa la clave de la poliza.");
        }

        DatosPoliza solicitud = validarDatosPoliza(datos);
        String identificador = clave.strip();

        if (!identificador.equals(solicitud.clave())) {
            throw new ReglaNegocioException(
                    "La clave de la poliza no puede modificarse.");
        }

        consultar(identificador);

        var cliente = sistemaDueno.consultarCliente(solicitud.curpCliente())
                .orElseThrow(() -> new ClienteNoRegistradoException(
                        solicitud.curpCliente()));

        if (!solicitud.curpCliente().equals(cliente.curp())) {
            throw new ReglaNegocioException(
                    "La CURP recibida no coincide con el cliente solicitado.");
        }

        sistemaDueno.modificarPoliza(solicitud);

        DatosPoliza modificada = consultar(identificador);

        boolean coincide = solicitud.tipo().equals(modificada.tipo())
                && modificada.montoAsegurado() != null
                && solicitud.montoAsegurado()
                        .compareTo(modificada.montoAsegurado()) == 0
                && solicitud.descripcion().equals(modificada.descripcion())
                && solicitud.curpCliente().equals(modificada.curpCliente());

        if (!coincide) {
            throw new ReglaNegocioException(
                    "No se pudo confirmar la modificacion de la poliza."
                            + " Consulta su estado antes de repetir la solicitud.");
        }

        return modificada;
    }

    /**
     * Elimina una poliza y comprueba su ausencia en el Sistema Dueno.
     *
     * @param clave identificador de la poliza
     */
    public void eliminar(String clave) {
        DatosPoliza existente = consultar(clave);
        String identificador = existente.clave();

        sistemaDueno.eliminarPoliza(identificador);

        if (sistemaDueno.consultarPoliza(identificador).isPresent()) {
            throw new ReglaNegocioException(
                    "No se pudo confirmar la baja de la poliza."
                            + " Consulta su estado antes de repetir la solicitud.");
        }
    }

    /**
     * Valida los datos reutilizando las reglas del modelo de dominio.
     *
     * @param datos datos recibidos
     * @return datos validados y normalizados
     */
    private static DatosPoliza validarDatosPoliza(DatosPoliza datos) {
        if (datos == null) {
            throw new ReglaNegocioException(
                    "Los datos de la poliza son obligatorios.");
        }

        if (datos.tipo() == null || datos.tipo().isBlank()) {
            throw new ReglaNegocioException(
                    "El tipo de poliza es obligatorio.");
        }

        String codigo = normalizarTipo(datos.tipo().strip());
        TipoPoliza tipo = TipoPoliza.desdeCodigo(Integer.parseInt(codigo));

        Poliza poliza = new Poliza(
                datos.clave(), tipo, datos.montoAsegurado(),
                datos.descripcion(), datos.curpCliente());

        return new DatosPoliza(
                poliza.getClave(), codigo, poliza.getMontoAsegurado(),
                poliza.getDescripcion(), poliza.getCurpCliente());
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