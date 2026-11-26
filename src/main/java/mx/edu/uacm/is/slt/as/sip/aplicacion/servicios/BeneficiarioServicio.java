
package mx.edu.uacm.is.slt.as.sip.aplicacion.servicios;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.dominio.Beneficiario;
import mx.edu.uacm.is.slt.as.sip.dominio.Poliza;
import mx.edu.uacm.is.slt.as.sip.dominio.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.sip.dominio.TipoPoliza;

/**
 * Coordina las operaciones de beneficiarios con el Sistema Dueno.
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
     * Registra un beneficiario y comprueba el resultado remoto.
     *
     * @param clavePoliza identificador de la poliza
     * @param datos datos del beneficiario
     * @return datos del beneficiario registrado y confirmado
     */
    public DatosBeneficiario registrar(
            String clavePoliza, DatosBeneficiario datos) {

        if (clavePoliza == null || clavePoliza.isBlank()) {
            throw new ReglaNegocioException(
                    "La clave de la poliza es obligatoria.");
        }

        String identificador = clavePoliza.strip();

        DatosBeneficiario solicitud = validarDatos(identificador, datos);

        DatosPoliza datosPoliza = sistemaDueno.consultarPoliza(identificador)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La poliza solicitada no existe."));

        if (!identificador.equals(datosPoliza.clave())) {
            throw new ReglaNegocioException(
                    "La clave recibida no coincide con la poliza solicitada.");
        }

        TipoPoliza tipo = normalizarTipo(datosPoliza.tipo());

        Poliza poliza = new Poliza(
                datosPoliza.clave(),
                tipo,
                datosPoliza.montoAsegurado(),
                datosPoliza.descripcion(),
                datosPoliza.curpCliente());

        Beneficiario beneficiario = new Beneficiario(
                solicitud.nombres(),
                solicitud.primerApellido(),
                solicitud.segundoApellido(),
                solicitud.fechaNacimiento(),
                solicitud.porcentaje(),
                poliza);

        DatosBeneficiario validado = convertirADatos(
                beneficiario, identificador);

        Optional<DatosBeneficiario> existente
                = sistemaDueno.consultarBeneficiario(
                        identificador,
                        validado.fechaNacimiento(),
                        validado.nombres(),
                        validado.primerApellido(),
                        validado.segundoApellido());

        if (existente.isPresent()) {
            throw new ReglaNegocioException(
                    "El beneficiario ya esta registrado en esta poliza.");
        }

        sistemaDueno.registrarBeneficiario(validado);

        DatosBeneficiario registrado = sistemaDueno.consultarBeneficiario(
                identificador,
                validado.fechaNacimiento(),
                validado.nombres(),
                validado.primerApellido(),
                validado.segundoApellido())
                .orElseThrow(() -> new ReglaNegocioException(
                        "No se pudo confirmar el registro del beneficiario."
                        + " Consulta su estado antes de repetir la solicitud."));

        if (!coinciden(validado, registrado)) {
            throw new ReglaNegocioException(
                    "Los datos del beneficiario registrado no coinciden "
                    + "con la solicitud. Consulta su estado antes de "
                    + "repetir la solicitud.");
        }

        return convertirADatos(registrado, identificador);
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
            throw new ReglaNegocioException(
                    "La clave de la poliza es obligatoria.");
        }
        if (fechaNacimiento == null) {
            throw new ReglaNegocioException(
                    "La fecha de nacimiento es obligatoria.");
        }
        if (nombres == null || nombres.isBlank()
                || primerApellido == null || primerApellido.isBlank()) {
            throw new ReglaNegocioException(
                    "Los nombres y el primer apellido son obligatorios.");
        }

        String apellidoOpcional = normalizarApellido(segundoApellido);

        return sistemaDueno.consultarBeneficiario(
                clavePoliza.strip(),
                fechaNacimiento,
                nombres.strip(),
                primerApellido.strip(),
                apellidoOpcional)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El beneficiario solicitado no existe en la poliza."));
    }

    /**
     * Valida los datos recibidos para registrar un beneficiario.
     *
     * @param clavePoliza identificador de la poliza
     * @param datos datos recibidos
     * @return datos normalizados
     */
    private static DatosBeneficiario validarDatos(
            String clavePoliza, DatosBeneficiario datos) {

        if (datos == null) {
            throw new ReglaNegocioException(
                    "Los datos del beneficiario son obligatorios.");
        }

        if (datos.clavePoliza() != null
                && !datos.clavePoliza().isBlank()
                && !clavePoliza.equals(datos.clavePoliza().strip())) {
            throw new ReglaNegocioException(
                    "La clave de la poliza no coincide con la solicitud.");
        }

        if (datos.nombres() == null || datos.nombres().isBlank()
                || datos.primerApellido() == null
                || datos.primerApellido().isBlank()) {
            throw new ReglaNegocioException(
                    "Los nombres y el primer apellido son obligatorios.");
        }

        if (datos.fechaNacimiento() == null) {
            throw new ReglaNegocioException(
                    "La fecha de nacimiento es obligatoria.");
        }

        if (datos.porcentaje() == null) {
            throw new ReglaNegocioException(
                    "El porcentaje de beneficio es obligatorio.");
        }

        return new DatosBeneficiario(
                null,
                datos.nombres().strip(),
                datos.primerApellido().strip(),
                normalizarApellido(datos.segundoApellido()),
                datos.fechaNacimiento(),
                datos.porcentaje(),
                clavePoliza);
    }

    /**
     * Valida el tipo de poliza recibido del Sistema Dueno.
     *
     * @param tipo codigo remoto
     * @return tipo de poliza validado
     */
    private static TipoPoliza normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new ReglaNegocioException(
                    "El tipo de poliza recibido no es valido.");
        }

        try {
            return TipoPoliza.desdeCodigo(Integer.parseInt(tipo.strip()));
        } catch (NumberFormatException excepcion) {
            throw new ReglaNegocioException(
                    "El tipo de poliza recibido no es valido.");
        }
    }

    /**
     * Normaliza el segundo apellido opcional.
     *
     * @param segundoApellido segundo apellido
     * @return apellido normalizado o nulo
     */
    private static String normalizarApellido(String segundoApellido) {
        if (segundoApellido == null || segundoApellido.isBlank()) {
            return null;
        }
        return segundoApellido.strip();
    }

    /**
     * Comprueba que la respuesta remota coincida con los datos enviados.
     *
     * @param solicitud datos enviados
     * @param registrado datos consultados
     * @return verdadero si los datos coinciden
     */
    private static boolean coinciden(
            DatosBeneficiario solicitud, DatosBeneficiario registrado) {

        return solicitud.nombres().equals(registrado.nombres())
                && solicitud.primerApellido().equals(registrado.primerApellido())
                && java.util.Objects.equals(
                    normalizarApellido(solicitud.segundoApellido()),
                    normalizarApellido(registrado.segundoApellido()))
                && solicitud.fechaNacimiento().equals(
                    registrado.fechaNacimiento())
                && solicitud.porcentaje().compareTo(registrado.porcentaje()) == 0;
    }

    /**
     * Convierte una entidad de dominio a datos de aplicacion.
     *
     * @param beneficiario entidad validada
     * @param clavePoliza identificador de la poliza
     * @return datos del beneficiario
     */
    private static DatosBeneficiario convertirADatos(
            Beneficiario beneficiario, String clavePoliza) {

        return new DatosBeneficiario(
                null,
                beneficiario.getNombres(),
                beneficiario.getPrimerApellido(),
                beneficiario.getSegundoApellido(),
                beneficiario.getFechaNacimiento(),
                beneficiario.getPorcentaje(),
                clavePoliza);
    }

    /**
     * Asegura que la respuesta de confirmacion conserve la clave de la poliza.
     *
     * @param beneficiario datos remotos
     * @param clavePoliza identificador de la poliza
     * @return datos con la clave de la poliza
     */
    private static DatosBeneficiario convertirADatos(
            DatosBeneficiario beneficiario, String clavePoliza) {

        return new DatosBeneficiario(
                beneficiario.id(),
                beneficiario.nombres(),
                beneficiario.primerApellido(),
                beneficiario.segundoApellido(),
                beneficiario.fechaNacimiento(),
                beneficiario.porcentaje(),
                clavePoliza);
    }
}
