package mx.edu.uacm.is.slt.as.sip.adaptadores.sistemadueno;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.dominio.Poliza;
import mx.edu.uacm.is.slt.as.sip.dominio.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.sip.dominio.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.sip.infraestructura.rest.ClienteRespuesta;
import mx.edu.uacm.is.slt.as.sip.infraestructura.rest.PolizaRespuesta;

/**
 * Realiza peticiones REST al Sistema Dueno.
 *
 * <p>Politica de reintentos: solo las lecturas (GET) se reintentan, un maximo
 * de una vez y siempre dentro del limite total de tiempo de lectura (180
 * segundos). Las escrituras nunca se repiten automaticamente, porque un
 * resultado incierto podria duplicar o deshacer el cambio.</p>
 */
@Component
public class SistemaDuenoRestCliente implements SistemaDueno {

    /** Reintentos permitidos para una lectura. */
    static final int MAXIMO_REINTENTOS_LECTURA = 1;

    /** Ruta de polizas por nombre del asegurado (documentacion del Dueno). */
    static final String RUTA_POR_ASEGURADO
            = "/polizas/c/{nombres}/{primer}/{segundo}";

    /** Ruta de polizas por nombre del beneficiario (documentacion del Dueno). */
    static final String RUTA_POR_BENEFICIARIO
            = "/polizas/b/{nombres}/{primer}/{segundo}";

    /** Ruta de polizas por nacimiento del beneficiario (documentacion del Dueno). */
    static final String RUTA_POR_NACIMIENTO_BENEFICIARIO = "/polizas/b/{fecha}";

    /** Valor del segundo apellido ausente en la ruta (igual que beneficiarios). */
    private static final String APELLIDO_AUSENTE = "null";

    /** Ruta de escritura de una poliza. */
    private static final String RUTA_ESCRITURA_POLIZA
            = "/poliza/{clave}/{tipo}/{monto}/{descripcion}/{curp}";

    private final HttpClient clienteHttp;
    private final String urlBase;
    private final Duration limiteTotal;
    private final Duration limiteConexion;
    private final RestClient http;

    /**
     * Configura el cliente HTTP.
     *
     * @param urlBase direccion del Sistema Dueno
     * @param segundosConexion tiempo para establecer la conexion
     * @param segundosLectura limite total de una lectura, incluidos el tiempo de
     *        conexion y el reintento
     */
    public SistemaDuenoRestCliente(
            @Value("${sistema.dueno.url}") String urlBase,
            @Value("${sistema.dueno.timeout-conexion-segundos}") long segundosConexion,
            @Value("${sistema.dueno.timeout-lectura-segundos}") long segundosLectura) {

        this.limiteConexion = Duration.ofSeconds(segundosConexion);
        this.clienteHttp = HttpClient.newBuilder()
                .connectTimeout(limiteConexion)
                .build();
        this.urlBase = urlBase;
        this.limiteTotal = Duration.ofSeconds(segundosLectura);
        this.http = crearCliente(limiteTotal);
    }

    @Override
    public Optional<DatosCliente> consultarCliente(String curp) {
        return obtener(cliente -> cliente.get()
                .uri("/cliente/{curp}", curp)
                .retrieve()
                .body(ClienteRespuesta.class))
                .map(ClienteRespuesta::convertirADatos);
    }

    @Override
    public void registrarCliente(DatosCliente cliente) {
        throw new UnsupportedOperationException(
                "El alta de clientes aun no esta implementada.");
    }

    @Override
    public void modificarCliente(DatosCliente cliente) {
        throw new UnsupportedOperationException(
                "La modificacion de clientes aun no esta implementada.");
    }

    @Override
    public Optional<DatosPoliza> consultarPoliza(String clave) {
        return obtener(cliente -> cliente.get()
                .uri("/poliza/{clave}", clave)
                .retrieve()
                .body(PolizaRespuesta.class))
                .map(SistemaDuenoRestCliente::convertirPoliza);
    }

    @Override
    public List<DatosPoliza> listarPolizas() {
        return consultarPolizas("/polizas");
    }

    @Override
    public List<DatosPoliza> listarPolizasPorCurp(String curp) {
        return consultarPolizas("/polizas/{curp}", curp);
    }

    @Override
    public List<DatosPoliza> listarPolizasPorTipo(String tipo) {
        return consultarPolizas("/polizas/{tipo}", tipo);
    }

    @Override
    public List<DatosPoliza> listarPolizasPorNombreAsegurado(
            String nombres, String primerApellido, String segundoApellido) {
        return consultarPolizas(RUTA_POR_ASEGURADO,
                validarSegmento(nombres, "Los nombres del asegurado"),
                validarSegmento(primerApellido, "El primer apellido del asegurado"),
                apellidoOpcional(segundoApellido));
    }

    @Override
    public List<DatosPoliza> listarPolizasPorNombreBeneficiario(
            String nombres, String primerApellido, String segundoApellido) {
        return consultarPolizas(RUTA_POR_BENEFICIARIO,
                validarSegmento(nombres, "Los nombres del beneficiario"),
                validarSegmento(primerApellido,
                        "El primer apellido del beneficiario"),
                apellidoOpcional(segundoApellido));
    }

    @Override
    public List<DatosPoliza> listarPolizasPorFechaNacimientoBeneficiario(
            LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new ReglaNegocioException(
                    "La fecha de nacimiento del beneficiario es obligatoria.");
        }
        return consultarPolizas(RUTA_POR_NACIMIENTO_BENEFICIARIO,
                fechaNacimiento.toString());
    }

    @Override
    public void registrarPoliza(DatosPoliza poliza) {
        Object[] segmentos = segmentosEscritura(poliza);
        escribir(() -> http.post()
                .uri(RUTA_ESCRITURA_POLIZA, segmentos)
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public void modificarPoliza(DatosPoliza poliza) {
        Object[] segmentos = segmentosEscritura(poliza);
        escribir(() -> http.put()
                .uri(RUTA_ESCRITURA_POLIZA, segmentos)
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public void eliminarPoliza(String clave) {
        String claveValida = validarSegmento(clave, "La clave de la poliza");
        escribir(() -> http.delete()
                .uri("/poliza/{clave}", claveValida)
                .retrieve()
                .toBodilessEntity());
    }

    @Override
    public Optional<DatosBeneficiario> consultarBeneficiario(
            String clavePoliza, LocalDate fechaNacimiento,
            String nombres, String primerApellido, String segundoApellido) {

        return obtener(cliente -> cliente.get()
                .uri("/beneficiario/{fecha}/{clave}/{nombres}/{primer}/{segundo}",
                        fechaNacimiento.toString(), clavePoliza, nombres,
                        primerApellido, apellidoOpcional(segundoApellido))
                .retrieve()
                .body(DatosBeneficiario.class));
    }

    @Override
    public void registrarBeneficiario(DatosBeneficiario beneficiario) {
        throw new UnsupportedOperationException(
                "El alta de beneficiarios aun no esta implementada.");
    }

    /**
     * Crea un cliente REST con un tiempo maximo de lectura.
     *
     * @param disponible tiempo total disponible para conectar y leer
     * @return cliente cuya conexion mas lectura no excede ese tiempo
     */
    private RestClient crearCliente(Duration disponible) {
        Duration lectura = disponible.minus(limiteConexion);
        if (lectura.compareTo(Duration.ofSeconds(1)) < 0) {
            lectura = Duration.ofSeconds(1);
        }

        JdkClientHttpRequestFactory fabrica
                = new JdkClientHttpRequestFactory(clienteHttp);
        fabrica.setReadTimeout(lectura);

        return RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(fabrica)
                .build();
    }

    /**
     * Consulta y transforma un listado de polizas.
     *
     * @param ruta ruta del servidor
     * @param parametros valores de las variables de la ruta
     * @return polizas recibidas
     */
    private List<DatosPoliza> consultarPolizas(String ruta, Object... parametros) {
        PolizaRespuesta[] respuesta;
        try {
            respuesta = leer(cliente -> cliente.get()
                    .uri(ruta, parametros)
                    .retrieve()
                    .body(PolizaRespuesta[].class));

            if (respuesta == null) {
                throw new RestClientException(
                        "El Sistema Dueno devolvio una respuesta sin listado.");
            }
        } catch (RestClientException excepcion) {
            throw new SistemaDuenoNoDisponibleException(excepcion);
        }

        return Arrays.stream(respuesta)
                .map(SistemaDuenoRestCliente::convertirPoliza)
                .toList();
    }

    /**
     * Convierte una respuesta remota a los datos de aplicacion.
     *
     * @param respuesta registro recibido
     * @return datos de la poliza
     */
    private static DatosPoliza convertirPoliza(PolizaRespuesta respuesta) {
        Poliza poliza = respuesta.convertirADominio();

        return new DatosPoliza(poliza.getClave(),
                Integer.toString(poliza.getTipo().getCodigo()),
                poliza.getMontoAsegurado(), poliza.getDescripcion(),
                poliza.getCurpCliente());
    }

    /**
     * Ejecuta una consulta individual.
     *
     * @param consulta operacion de lectura
     * @param <T> tipo del resultado
     * @return resultado encontrado o vacio
     */
    private <T> Optional<T> obtener(Function<RestClient, T> consulta) {
        try {
            return Optional.ofNullable(leer(consulta));
        } catch (HttpClientErrorException.NotFound excepcion) {
            return Optional.empty();
        } catch (RestClientException excepcion) {
            throw new SistemaDuenoNoDisponibleException(excepcion);
        }
    }

    /**
     * Ejecuta una lectura con la politica de reintentos: un solo reintento,
     * solo ante fallos de comunicacion o errores 5xx, y sin exceder el limite
     * total de tiempo de lectura.
     *
     * @param lectura operacion de lectura
     * @param <T> tipo del resultado
     * @return resultado de la lectura
     */
    private <T> T leer(Function<RestClient, T> lectura) {
        long inicio = System.nanoTime();
        RestClientException ultimoFallo = null;

        for (int intento = 0; intento <= MAXIMO_REINTENTOS_LECTURA; intento++) {
            Duration restante = limiteTotal.minus(
                    Duration.ofNanos(System.nanoTime() - inicio));
            if (intento > 0 && restante.compareTo(
                    limiteConexion.plus(Duration.ofSeconds(1))) < 0) {
                break;
            }

            RestClient cliente = intento == 0 ? http : crearCliente(restante);
            try {
                return lectura.apply(cliente);
            } catch (RestClientException excepcion) {
                if (!esReintentable(excepcion)) {
                    throw excepcion;
                }
                ultimoFallo = excepcion;
            }
        }

        throw ultimoFallo != null ? ultimoFallo : new ResourceAccessException(
                "Se agoto el tiempo limite de la lectura.");
    }

    /**
     * Indica si un fallo de lectura puede reintentarse.
     *
     * @param excepcion fallo ocurrido
     * @return verdadero ante fallos de red, tiempos agotados y errores 5xx
     */
    private static boolean esReintentable(RestClientException excepcion) {
        return excepcion instanceof ResourceAccessException
                || excepcion instanceof HttpServerErrorException;
    }

    /**
     * Ejecuta una escritura una sola vez y traduce sus errores. Un fallo de
     * comunicacion o un 5xx deja el resultado incierto y se informa como
     * EX-DND; quien llama debe consultar antes de volver a intentar.
     *
     * @param escritura operacion de escritura
     */
    private void escribir(Runnable escritura) {
        try {
            escritura.run();
        } catch (HttpClientErrorException.NotFound excepcion) {
            throw new RecursoNoEncontradoException(
                    "El Sistema Dueno no encontro el recurso solicitado.");
        } catch (HttpClientErrorException excepcion) {
            throw new ReglaNegocioException(
                    "El Sistema Dueno rechazo la operacion (HTTP "
                    + excepcion.getStatusCode().value() + ").");
        } catch (RestClientException excepcion) {
            throw new SistemaDuenoNoDisponibleException(excepcion);
        }
    }

    /**
     * Convierte el segundo apellido opcional en el valor de la ruta.
     *
     * @param segundoApellido segundo apellido, o nulo o vacio si no tiene
     * @return el apellido, o "null" si no tiene
     */
    private static String apellidoOpcional(String segundoApellido) {
        if (segundoApellido == null || segundoApellido.isBlank()) {
            return APELLIDO_AUSENTE;
        }
        return validarSegmento(segundoApellido, "El segundo apellido");
    }

    /**
     * Valida y convierte los datos de una poliza en variables de la ruta.
     *
     * @param poliza datos de la poliza
     * @return clave, tipo, monto, descripcion y CURP
     */
    private static Object[] segmentosEscritura(DatosPoliza poliza) {
        if (poliza == null || poliza.montoAsegurado() == null) {
            throw new ReglaNegocioException(
                    "Los datos de la poliza estan incompletos.");
        }

        return new Object[] {
            validarSegmento(poliza.clave(), "La clave de la poliza"),
            validarSegmento(poliza.tipo(), "El tipo de la poliza"),
            poliza.montoAsegurado().toPlainString(),
            validarSegmento(poliza.descripcion(), "La descripcion"),
            validarSegmento(poliza.curpCliente(), "La CURP del cliente")
        };
    }

    /**
     * Valida un valor que viajara como segmento de ruta. RestClient codifica
     * los caracteres reservados al expandir la ruta, pero una diagonal no puede
     * enviarse dentro de un segmento.
     *
     * @param valor valor a validar
     * @param campo nombre del campo, para el mensaje
     * @return el mismo valor
     */
    private static String validarSegmento(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(campo + " es obligatorio.");
        }
        if (valor.indexOf('/') >= 0) {
            throw new ReglaNegocioException(
                    campo + " no puede contener diagonales.");
        }
        return valor;
    }
}
