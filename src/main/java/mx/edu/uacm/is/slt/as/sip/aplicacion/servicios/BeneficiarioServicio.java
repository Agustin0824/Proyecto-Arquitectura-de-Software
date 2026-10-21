package mx.edu.uacm.is.slt.as.sip.aplicacion.servicios;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.dominio.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;

/**
 * Coordina la consulta de beneficiarios al Sistema Dueno.
 */
@Service
public class BeneficiarioServicio {

    private final SistemaDueno sistemaDueno;

    /**
     * Crea el servicio con su proveedor de consultas.
     *
     * @param sistemaDueno acceso al Sistema Dueno
     */
    public BeneficiarioServicio(SistemaDueno sistemaDueno) {
        this.sistemaDueno = sistemaDueno;
    }

    /**
     * Representa el registro pendiente de implementacion.
     *
     * @param clavePoliza identificador de la poliza
     * @param datos datos del beneficiario
     * @return beneficiario cuando se implemente la operacion
     */
    public DatosBeneficiario registrar(String clavePoliza, DatosBeneficiario datos) {
        throw new UnsupportedOperationException(
                "El alta de beneficiarios aun no esta implementada.");
    }

    /**
     * Consulta un beneficiario mediante sus datos de identificacion.
     *
     * @param clavePoliza identificador de la poliza
     * @param fechaNacimiento fecha de nacimiento
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido
     * @param segundoApellido segundo apellido, opcional
     * @return beneficiario encontrado
     */
    public DatosBeneficiario consultar(String clavePoliza,
            LocalDate fechaNacimiento, String nombres, String primerApellido,
            String segundoApellido) {
        if (clavePoliza == null || clavePoliza.isBlank()) {
            throw new ReglaNegocioException("La clave de la poliza es obligatoria.");
        }
        if (fechaNacimiento == null) {
            throw new ReglaNegocioException("La fecha de nacimiento es obligatoria.");
        }
        if (nombres == null || nombres.isBlank()
                || primerApellido == null || primerApellido.isBlank()) {
            throw new ReglaNegocioException(
                    "Los nombres y el primer apellido son obligatorios.");
        }

        String apellidoOpcional = segundoApellido == null
                ? null : segundoApellido.strip();

        return sistemaDueno.consultarBeneficiario(
                clavePoliza.strip(), fechaNacimiento, nombres.strip(),
                primerApellido.strip(), apellidoOpcional)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El beneficiario solicitado no existe en la poliza."));
    }
}