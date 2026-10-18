package mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.servicios;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.puertos.BeneficiarioRepositorio;
import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.puertos.PolizaRepositorio;
import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.IDSISTEMA.dominio.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.dominio.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.dominio.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.dominio.ReglaNegocioException;

/**
 * Casos de uso: alta, consulta, modificacion y baja de polizas.
 *
 * <p>Las escrituras van primero al Sistema Dueno; la copia local solo cambia si
 * este responde bien.
 */
@Service
public class PolizaServicio {

    private final PolizaRepositorio polizas;

    private final BeneficiarioRepositorio beneficiarios;

    private final ClienteServicio clientes;

    private final SistemaDueno sistemaDueno;

    /**
     * Crea el servicio con sus dependencias.
     *
     * @param polizas almacenamiento local de polizas
     * @param beneficiarios almacenamiento local de beneficiarios
     * @param clientes casos de uso de clientes
     * @param sistemaDueno puerto hacia el Sistema Dueno
     */
    public PolizaServicio(PolizaRepositorio polizas, BeneficiarioRepositorio beneficiarios,
                          ClienteServicio clientes, SistemaDueno sistemaDueno) {
        this.polizas = polizas;
        this.beneficiarios = beneficiarios;
        this.clientes = clientes;
        this.sistemaDueno = sistemaDueno;
    }

    /**
     * Alta de poliza.
     *
     * @param datos datos de la nueva poliza
     * @return datos de la poliza registrada
     */
    @Transactional
    public DatosPoliza registrar(DatosPoliza datos) {
        Cliente cliente = clientes.sincronizar(datos.curpCliente());
        Poliza poliza = new Poliza(datos.clave(), datos.tipo(), datos.montoAsegurado(),
                datos.descripcion(), cliente);
        if (sistemaDueno.consultarPoliza(poliza.getClave()).isPresent()) {
            throw new ReglaNegocioException("La poliza ya existe.");
        }
        sistemaDueno.registrarPoliza(aDatos(poliza));
        return aDatos(polizas.save(poliza));
    }

    /**
     * Consulta de poliza por clave.
     *
     * @param clave clave de la poliza
     * @return datos de la poliza
     */
    @Transactional
    public DatosPoliza consultar(String clave) {
        return aDatos(sincronizar(clave));
    }

    /**
     * Consulta de polizas con filtros opcionales.
     *
     * @param curp CURP del cliente, o nulo
     * @param tipo tipo de poliza, o nulo
     * @return polizas que cumplen el filtro
     */
    public List<DatosPoliza> listar(String curp, String tipo) {
        if (curp != null && !curp.isBlank()) {
            return sistemaDueno.listarPolizasPorCurp(curp);
        }
        if (tipo != null && !tipo.isBlank()) {
            return sistemaDueno.listarPolizasPorTipo(tipo);
        }
        return sistemaDueno.listarPolizas();
    }

    /**
     * Modificacion de poliza.
     *
     * @param clave clave de la poliza
     * @param datos datos nuevos
     * @return datos de la poliza modificada
     */
    @Transactional
    public DatosPoliza modificar(String clave, DatosPoliza datos) {
        Poliza poliza = sincronizar(clave);
        poliza.actualizar(datos.tipo(), datos.montoAsegurado(), datos.descripcion());
        sistemaDueno.modificarPoliza(aDatos(poliza));
        return aDatos(polizas.save(poliza));
    }

    /**
     * Baja de poliza.
     *
     * @param clave clave de la poliza
     */
    @Transactional
    public void eliminar(String clave) {
        sincronizar(clave);
        sistemaDueno.eliminarPoliza(clave);
        beneficiarios.deleteByPolizaClave(clave);
        polizas.deleteById(clave);
    }

    /**
     * Obtiene la poliza del Sistema Dueno y refresca la copia local.
     *
     * @param clave clave de la poliza
     * @return la entidad local actualizada
     */
    Poliza sincronizar(String clave) {
        DatosPoliza remota = sistemaDueno.consultarPoliza(clave)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La poliza " + clave + " no existe."));
        Cliente cliente = clientes.sincronizar(remota.curpCliente());
        Poliza local = polizas.findById(clave).orElse(null);
        if (local == null) {
            local = new Poliza(remota.clave(), remota.tipo(), remota.montoAsegurado(),
                    remota.descripcion(), cliente);
        } else {
            local.actualizar(remota.tipo(), remota.montoAsegurado(), remota.descripcion());
        }
        return polizas.save(local);
    }

    private static DatosPoliza aDatos(Poliza poliza) {
        return new DatosPoliza(poliza.getClave(), poliza.getTipo(),
                poliza.getMontoAsegurado(), poliza.getDescripcion(),
                poliza.getCliente().getCurp());
    }
}