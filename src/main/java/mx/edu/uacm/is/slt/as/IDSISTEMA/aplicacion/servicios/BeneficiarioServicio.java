package mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.servicios;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.puertos.BeneficiarioRepositorio;
import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.IDSISTEMA.dominio.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.dominio.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.dominio.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.dominio.ReglaNegocioException;

/**
 * Casos de uso: alta y consulta de beneficiarios de una poliza.
 *
 * <p>Regla: la suma de porcentajes de una poliza no puede superar 100.
 */
@Service
public class BeneficiarioServicio {

    private final BeneficiarioRepositorio beneficiarios;

    private final PolizaServicio polizas;

    private final SistemaDueno sistemaDueno;

    /**
     * Crea el servicio con sus dependencias.
     *
     * @param beneficiarios almacenamiento local de beneficiarios
     * @param polizas casos de uso de polizas
     * @param sistemaDueno puerto hacia el Sistema Dueno
     */
    public BeneficiarioServicio(BeneficiarioRepositorio beneficiarios,
                                PolizaServicio polizas, SistemaDueno sistemaDueno) {
        this.beneficiarios = beneficiarios;
        this.polizas = polizas;
        this.sistemaDueno = sistemaDueno;
    }

    /**
     * Alta de beneficiario.
     *
     * @param clavePoliza clave de la poliza
     * @param datos datos del beneficiario
     * @return datos del beneficiario registrado
     */
    @Transactional
    public DatosBeneficiario registrar(String clavePoliza, DatosBeneficiario datos) {
        Poliza poliza = polizas.sincronizar(clavePoliza);
        Beneficiario nuevo = new Beneficiario(datos.nombreCompleto(), datos.fechaNacimiento(),
                datos.porcentaje(), poliza);
        BigDecimal total = sistemaDueno.listarBeneficiarios(clavePoliza).stream()
                .map(DatosBeneficiario::porcentaje)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(nuevo.getPorcentaje());
        if (total.compareTo(Beneficiario.MAXIMO_TOTAL) > 0) {
            throw new ReglaNegocioException(
                    "La suma de porcentajes de la poliza no puede superar 100.");
        }
        sistemaDueno.registrarBeneficiario(aDatos(nuevo));
        return aDatos(beneficiarios.save(nuevo));
    }

    /**
     * Lista los beneficiarios de una poliza.
     *
     * @param clavePoliza clave de la poliza
     * @return beneficiarios registrados
     */
    public List<DatosBeneficiario> listar(String clavePoliza) {
        polizas.consultar(clavePoliza);
        return sistemaDueno.listarBeneficiarios(clavePoliza);
    }

    /**
     * Consulta de beneficiario.
     *
     * @param clavePoliza clave de la poliza
     * @param id identificador del beneficiario
     * @return datos del beneficiario
     */
    public DatosBeneficiario consultar(String clavePoliza, Long id) {
        return listar(clavePoliza).stream()
                .filter(b -> id.equals(b.id()))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El beneficiario " + id + " no existe en la poliza."));
    }

    private static DatosBeneficiario aDatos(Beneficiario b) {
        return new DatosBeneficiario(b.getId(), b.getNombreCompleto(),
                b.getFechaNacimiento(), b.getPorcentaje(), b.getPoliza().getClave());
    }
}