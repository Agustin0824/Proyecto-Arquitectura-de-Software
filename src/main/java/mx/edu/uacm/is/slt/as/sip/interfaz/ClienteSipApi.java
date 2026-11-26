
package mx.edu.uacm.is.slt.as.sip.interfaz;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cliente HTTP reutilizable de la API SIP para JavaFX.
 *
 * Las operaciones son asincronas y no bloquean la ventana.
 */
public class ClienteSipApi {

    public static final String URL_PREDETERMINADA = "http://localhost:8080";
    public static final String PROPIEDAD_URL = "sip.api.url";
    public static final String VARIABLE_URL = "SIP_API_URL";

    private static final Duration LIMITE_CONEXION = Duration.ofSeconds(10);
    private static final Duration LIMITE_RESPUESTA = Duration.ofSeconds(180);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(LIMITE_CONEXION)
            .build();

    private final JsonMapper json = JsonMapper.builder().build();

    private volatile String urlBase;

    private record CuerpoError(String codigo, String mensaje) {
    }

    public ClienteSipApi() {
        this(direccionConfigurada());
    }

    public ClienteSipApi(String urlBase) {
        configurarUrl(urlBase);
    }

    /**
     * Configura la direccion de la API SIP.
     *
     * @param url direccion HTTP o HTTPS
     */
    public final void configurarUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "La direccion de la API SIP es obligatoria.");
        }

        String limpia = url.trim();

        while (limpia.endsWith("/")) {
            limpia = limpia.substring(0, limpia.length() - 1);
        }

        URI uri;

        try {
            uri = URI.create(limpia);
        } catch (IllegalArgumentException excepcion) {
            throw new IllegalArgumentException(
                    "La direccion de la API SIP no es valida.", excepcion);
        }

        String esquema = uri.getScheme();

        if (uri.getHost() == null || esquema == null
                || !(esquema.equals("http") || esquema.equals("https"))) {
            throw new IllegalArgumentException(
                    "La direccion debe iniciar con http:// o https://");
        }

        this.urlBase = limpia;
    }

    /**
     * Obtiene la direccion configurada.
     *
     * @return direccion de la API SIP
     */
    public String getUrlBase() {
        return urlBase;
    }

    // OPERACIONES DE CLIENTES

    /**
     * Consulta un cliente por CURP.
     *
     * @param curp identificador del cliente
     * @return datos del cliente
     */
    public CompletableFuture<DatosCliente> consultarCliente(String curp) {
        if (!obligatorio(curp)) {
            return solicitudInvalida("La CURP es obligatoria.");
        }

        return solicitar("GET",
                "/api/clientes/" + codificar(curp.trim()),
                DatosCliente.class);
    }

    /**
     * Registra un cliente.
     *
     * @param datos datos del cliente
     * @return cliente registrado
     */
    public CompletableFuture<DatosCliente> registrarCliente(
            DatosCliente datos) {

        if (datos == null) {
            return solicitudInvalida("Los datos del cliente son obligatorios.");
        }

        return solicitar("POST", "/api/clientes",
                datos, DatosCliente.class);
    }

    /**
     * Modifica un cliente.
     *
     * @param curp identificador del cliente
     * @param datos datos actualizados
     * @return cliente modificado
     */
    public CompletableFuture<DatosCliente> modificarCliente(
            String curp, DatosCliente datos) {

        if (!obligatorio(curp) || datos == null) {
            return solicitudInvalida("Los datos del cliente son incompletos.");
        }

        String ruta = "/api/clientes/" + codificar(curp.trim());

        return solicitar("PUT", ruta, datos, DatosCliente.class);
    }

    // OPERACIONES DE POLIZAS

    /**
     * Consulta una poliza por clave.
     *
     * @param clave clave de la poliza
     * @return poliza encontrada
     */
    public CompletableFuture<DatosPoliza> consultarPoliza(String clave) {
        if (!obligatorio(clave)) {
            return solicitudInvalida("La clave de la poliza es obligatoria.");
        }

        return solicitar("GET",
                "/api/polizas/" + codificar(clave.trim()),
                DatosPoliza.class);
    }

    /**
     * Lista polizas utilizando filtros y paginacion opcionales.
     *
     * @param curp CURP del titular
     * @param tipo tipo de poliza
     * @param pagina numero de pagina
     * @param tamano cantidad de registros
     * @return lista de polizas
     */
    public CompletableFuture<List<DatosPoliza>> listarPolizas(
            String curp, String tipo, Integer pagina, Integer tamano) {

        StringBuilder parametros = new StringBuilder();

        if (curp != null && !curp.isBlank()) {
            agregarParametro(parametros, "curp", curp.trim());
        }

        if (tipo != null && !tipo.isBlank()) {
            agregarParametro(parametros, "tipo", tipo.trim());
        }

        if (pagina != null) {
            agregarParametro(parametros, "pagina", pagina.toString());
        }

        if (tamano != null) {
            agregarParametro(parametros, "tamano", tamano.toString());
        }

        String ruta = "/api/polizas";

        if (!parametros.isEmpty()) {
            ruta += "?" + parametros;
        }

        return solicitar("GET", ruta, DatosPoliza[].class)
                .thenApply(respuesta -> Arrays.stream(respuesta).toList());
    }

    /**
     * Registra una poliza.
     *
     * @param datos datos de la poliza
     * @return poliza registrada
     */
    public CompletableFuture<DatosPoliza> registrarPoliza(
            DatosPoliza datos) {

        if (datos == null) {
            return solicitudInvalida("Los datos de la poliza son obligatorios.");
        }

        return solicitar("POST", "/api/polizas",
                datos, DatosPoliza.class);
    }

    /**
     * Modifica una poliza.
     *
     * @param clave identificador de la poliza
     * @param datos datos actualizados
     * @return poliza modificada
     */
    public CompletableFuture<DatosPoliza> modificarPoliza(
            String clave, DatosPoliza datos) {

        if (!obligatorio(clave) || datos == null) {
            return solicitudInvalida("Los datos de la poliza son incompletos.");
        }

        String ruta = "/api/polizas/" + codificar(clave.trim());

        return solicitar("PUT", ruta, datos, DatosPoliza.class);
    }

    /**
     * Elimina una poliza.
     *
     * @param clave clave de la poliza
     * @return finalizacion de la operacion
     */
    public CompletableFuture<Void> eliminarPoliza(String clave) {
        if (!obligatorio(clave)) {
            return solicitudInvalida("La clave de la poliza es obligatoria.");
        }

        String ruta = "/api/polizas/" + codificar(clave.trim());

        return solicitar("DELETE", ruta, Void.class);
    }

    // OPERACIONES DE BENEFICIARIOS

    /**
     * Consulta un beneficiario mediante sus datos de identificacion.
     *
     * @param clave clave de la poliza
     * @param fechaNacimiento fecha de nacimiento
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido
     * @param segundoApellido segundo apellido opcional
     * @return beneficiario encontrado
     */
    public CompletableFuture<DatosBeneficiario> consultarBeneficiario(
            String clave, LocalDate fechaNacimiento,
            String nombres, String primerApellido, String segundoApellido) {

        if (!obligatorio(clave) || fechaNacimiento == null
                || !obligatorio(nombres) || !obligatorio(primerApellido)) {
            return solicitudInvalida(
                    "Los datos para consultar al beneficiario son incompletos.");
        }

        StringBuilder parametros = new StringBuilder();

        agregarParametro(parametros, "fechaNacimiento",
                fechaNacimiento.toString());
        agregarParametro(parametros, "nombres", nombres.trim());
        agregarParametro(parametros, "primerApellido",
                primerApellido.trim());

        if (segundoApellido != null && !segundoApellido.isBlank()) {
            agregarParametro(parametros, "segundoApellido",
                    segundoApellido.trim());
        }

        String ruta = "/api/polizas/" + codificar(clave.trim())
                + "/beneficiarios?" + parametros;

        return solicitar("GET", ruta, DatosBeneficiario.class);
    }

    /**
     * Registra un beneficiario.
     *
     * @param clave clave de la poliza
     * @param datos datos del beneficiario
     * @return beneficiario registrado
     */
    public CompletableFuture<DatosBeneficiario> registrarBeneficiario(
            String clave, DatosBeneficiario datos) {

        if (!obligatorio(clave) || datos == null) {
            return solicitudInvalida(
                    "Los datos del beneficiario son incompletos.");
        }

        String ruta = "/api/polizas/" + codificar(clave.trim())
                + "/beneficiarios";

        return solicitar("POST", ruta, datos, DatosBeneficiario.class);
    }

    // COMUNICACION HTTP

    /**
     * Envia una solicitud sin cuerpo.
     *
     * @param metodo metodo HTTP
     * @param ruta ruta de la API
     * @param tipo clase de la respuesta
     * @param <T> tipo de respuesta
     * @return resultado asincrono
     */
    public <T> CompletableFuture<T> solicitar(
            String metodo, String ruta, Class<T> tipo) {

        return solicitar(metodo, ruta, null, tipo);
    }

    /**
     * Envia una solicitud con cuerpo JSON opcional.
     *
     * @param metodo metodo HTTP
     * @param ruta ruta de la API
     * @param datos cuerpo de la solicitud
     * @param tipo clase de la respuesta
     * @param <T> tipo de respuesta
     * @return resultado asincrono
     */
    public <T> CompletableFuture<T> solicitar(
            String metodo, String ruta, Object datos, Class<T> tipo) {

        if (metodo == null || metodo.isBlank()
                || ruta == null || !ruta.startsWith("/") || tipo == null) {
            return solicitudInvalida("La solicitud HTTP no es valida.");
        }

        String cuerpoJson = null;

        if (datos != null) {
            try {
                cuerpoJson = json.writeValueAsString(datos);
            } catch (RuntimeException excepcion) {
                return CompletableFuture.failedFuture(new ErrorSipApi(
                        "REGLA-NEGOCIO",
                        "No fue posible preparar los datos de la solicitud.",
                        0, excepcion));
            }
        }

        HttpRequest solicitud;

        try {
            HttpRequest.Builder constructor = HttpRequest.newBuilder(
                    URI.create(urlBase + ruta))
                    .timeout(LIMITE_RESPUESTA)
                    .header("Accept", "application/json");

            HttpRequest.BodyPublisher cuerpo = cuerpoJson == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(cuerpoJson);

            if (cuerpoJson != null) {
                constructor.header("Content-Type", "application/json");
            }

            solicitud = constructor.method(metodo, cuerpo).build();

        } catch (IllegalArgumentException excepcion) {
            return CompletableFuture.failedFuture(new ErrorSipApi(
                    "REGLA-NEGOCIO",
                    "La solicitud no es valida.",
                    0, excepcion));
        }

        return http.sendAsync(solicitud, HttpResponse.BodyHandlers.ofString())
                .handle((respuesta, fallo) ->
                        procesarRespuesta(respuesta, fallo, tipo));
    }

    /**
     * Procesa la respuesta recibida o informa el fallo de comunicacion.
     *
     * @param respuesta respuesta HTTP
     * @param fallo error de comunicacion, si existe
     * @param tipo clase esperada
     * @param <T> tipo de respuesta
     * @return resultado convertido
     */
    private <T> T procesarRespuesta(
            HttpResponse<String> respuesta, Throwable fallo, Class<T> tipo) {

        if (fallo != null) {
            throw new ErrorSipApi(
                    ErrorSipApi.NO_DISPONIBLE,
                    "No fue posible comunicarse con la API SIP en "
                            + urlBase + ".",
                    0, fallo);
        }

        return interpretar(respuesta, tipo);
    }

    /**
     * Interpreta la respuesta HTTP.
     *
     * @param respuesta respuesta del servidor
     * @param tipo clase esperada
     * @param <T> tipo de respuesta
     * @return resultado convertido
     */
    private <T> T interpretar(
            HttpResponse<String> respuesta, Class<T> tipo) {

        int estado = respuesta.statusCode();
        String cuerpo = respuesta.body();

        if (estado >= 200 && estado < 300) {

            if (tipo == Void.class) {
                return null;
            }

            if (cuerpo == null || cuerpo.isBlank()) {
                throw new ErrorSipApi(
                        ErrorSipApi.RESPUESTA_INVALIDA,
                        "La API SIP devolvio una respuesta vacia.",
                        estado, null);
            }

            try {
                return json.readValue(cuerpo, tipo);
            } catch (RuntimeException excepcion) {
                throw new ErrorSipApi(
                        ErrorSipApi.RESPUESTA_INVALIDA,
                        "La respuesta de la API SIP no se pudo interpretar.",
                        estado, excepcion);
            }
        }

        CuerpoError error = null;

        try {
            error = json.readValue(cuerpo, CuerpoError.class);
        } catch (RuntimeException excepcion) {
            // Se utiliza el estado HTTP si no existe un cuerpo interpretable.
        }

        if (error != null && error.codigo() != null) {
            throw new ErrorSipApi(error.codigo(),
                    error.mensaje(), estado, null);
        }

        throw new ErrorSipApi("HTTP-" + estado,
                "La API SIP respondio con el estado HTTP " + estado + ".",
                estado, null);
    }

    /**
     * Agrega un parametro codificado a la consulta.
     *
     * @param parametros parametros actuales
     * @param nombre nombre del parametro
     * @param valor valor del parametro
     */
    private static void agregarParametro(
            StringBuilder parametros, String nombre, String valor) {

        if (!parametros.isEmpty()) {
            parametros.append("&");
        }

        parametros.append(codificar(nombre))
                .append("=")
                .append(codificar(valor));
    }

    /**
     * Codifica un valor para usarlo en una ruta o consulta.
     *
     * @param valor valor original
     * @return valor codificado
     */
    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    /**
     * Comprueba que un dato obligatorio tenga contenido.
     *
     * @param valor dato a comprobar
     * @return verdadero si tiene contenido
     */
    private static boolean obligatorio(String valor) {
        return valor != null && !valor.isBlank();
    }

    /**
     * Construye una solicitud fallida por datos invalidos.
     *
     * @param mensaje descripcion del error
     * @param <T> tipo de respuesta
     * @return futuro fallido
     */
    private static <T> CompletableFuture<T> solicitudInvalida(
            String mensaje) {

        return CompletableFuture.failedFuture(new ErrorSipApi(
                "REGLA-NEGOCIO", mensaje, 0, null));
    }

    /**
     * Obtiene la direccion configurada mediante propiedad o variable.
     *
     * @return direccion de la API SIP
     */
    private static String direccionConfigurada() {
        String valor = System.getProperty(PROPIEDAD_URL);

        if (valor == null || valor.isBlank()) {
            valor = System.getenv(VARIABLE_URL);
        }

        return valor == null || valor.isBlank()
                ? URL_PREDETERMINADA : valor;
    }
}
