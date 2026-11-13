package mx.edu.uacm.is.slt.as.sip.interfaz;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import tools.jackson.databind.json.JsonMapper;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;

/**
 * Cliente HTTP reutilizable de la API SIP para la interfaz JavaFX.
 *
 * <p>Todas las operaciones son asincronas: devuelven un CompletableFuture y no
 * bloquean la ventana. Los fallos llegan como {@link ErrorSipApi}. Para
 * conectar un control nuevo basta agregar un metodo que llame a
 * {@link #solicitar(String, String, Class)} y usarlo con
 * {@link EjecutorAsincrono}.</p>
 */
public class ClienteSipApi {

    /** Direccion de la API SIP cuando no se configura otra. */
    public static final String URL_PREDETERMINADA = "http://localhost:8080";

    /** Propiedad de sistema (-Dsip.api.url) con la direccion de la API. */
    public static final String PROPIEDAD_URL = "sip.api.url";

    /** Variable de entorno con la direccion de la API. */
    public static final String VARIABLE_URL = "SIP_API_URL";

    private static final Duration LIMITE_CONEXION = Duration.ofSeconds(10);
    private static final Duration LIMITE_RESPUESTA = Duration.ofSeconds(180);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(LIMITE_CONEXION)
            .build();
    private final JsonMapper json = JsonMapper.builder().build();
    private volatile String urlBase;

    /**
     * Cuerpo de error de la API SIP (ver ManejadorErrores).
     *
     * @param codigo codigo corto del error
     * @param mensaje mensaje para el usuario
     */
    private record CuerpoError(String codigo, String mensaje) {
    }

    /**
     * Crea el cliente con la direccion configurada en la propiedad
     * sip.api.url, la variable SIP_API_URL o, si faltan, la predeterminada.
     */
    public ClienteSipApi() {
        this(direccionConfigurada());
    }

    /**
     * Crea el cliente.
     *
     * @param urlBase direccion de la API SIP, por ejemplo http://localhost:8080
     */
    public ClienteSipApi(String urlBase) {
        configurarUrl(urlBase);
    }

    /**
     * Cambia la direccion de la API SIP para las siguientes solicitudes.
     *
     * @param url direccion http o https de la API
     * @throws IllegalArgumentException si la direccion no es valida
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
                    "La direccion de la API SIP no es valida: " + url, excepcion);
        }

        String esquema = uri.getScheme();
        if (uri.getHost() == null || esquema == null
                || !(esquema.equals("http") || esquema.equals("https"))) {
            throw new IllegalArgumentException(
                    "La direccion de la API SIP debe iniciar con http:// o https://");
        }
        this.urlBase = limpia;
    }

    /**
     * Obtiene la direccion actual de la API SIP.
     *
     * @return direccion sin diagonal final
     */
    public String getUrlBase() {
        return urlBase;
    }

    /**
     * Consulta una poliza por clave (GET /api/polizas/{clave}).
     *
     * @param clave clave de la poliza
     * @return poliza encontrada; falla con ErrorSipApi si no existe o la API
     *         no esta disponible
     */
    public CompletableFuture<DatosPoliza> consultarPoliza(String clave) {
        if (clave == null || clave.isBlank()) {
            return CompletableFuture.failedFuture(new ErrorSipApi(
                    "REGLA-NEGOCIO", "La clave de la poliza es obligatoria.",
                    0, null));
        }
        return solicitar("GET", "/api/polizas/" + codificar(clave.trim()),
                DatosPoliza.class);
    }

    /**
     * Envia una solicitud sin cuerpo y convierte la respuesta JSON.
     *
     * @param metodo metodo HTTP
     * @param ruta ruta de la API, iniciando con diagonal y ya codificada
     * @param tipo clase del resultado
     * @param <T> tipo del resultado
     * @return resultado, o fallo con ErrorSipApi
     */
    public <T> CompletableFuture<T> solicitar(
            String metodo, String ruta, Class<T> tipo) {
        HttpRequest solicitud;
        try {
            solicitud = HttpRequest.newBuilder(URI.create(urlBase + ruta))
                    .timeout(LIMITE_RESPUESTA)
                    .header("Accept", "application/json")
                    .method(metodo, HttpRequest.BodyPublishers.noBody())
                    .build();
        } catch (IllegalArgumentException excepcion) {
            return CompletableFuture.failedFuture(new ErrorSipApi(
                    "REGLA-NEGOCIO", "La solicitud no es valida.", 0, excepcion));
        }

        return http.sendAsync(solicitud, HttpResponse.BodyHandlers.ofString())
                .handle((respuesta, fallo) -> {
                    if (fallo != null) {
                        throw new ErrorSipApi(ErrorSipApi.NO_DISPONIBLE,
                                "No fue posible comunicarse con la API SIP en "
                                + urlBase + ".", 0, fallo);
                    }
                    return interpretar(respuesta, tipo);
                });
    }

    /**
     * Convierte una respuesta HTTP en resultado o error.
     *
     * @param respuesta respuesta recibida
     * @param tipo clase del resultado
     * @param <T> tipo del resultado
     * @return resultado convertido
     */
    private <T> T interpretar(HttpResponse<String> respuesta, Class<T> tipo) {
        int estado = respuesta.statusCode();
        String cuerpo = respuesta.body();

        if (estado >= 200 && estado < 300) {
            try {
                return json.readValue(cuerpo, tipo);
            } catch (RuntimeException excepcion) {
                throw new ErrorSipApi(ErrorSipApi.RESPUESTA_INVALIDA,
                        "La respuesta de la API SIP no se pudo interpretar.",
                        estado, excepcion);
            }
        }

        CuerpoError error = null;
        try {
            error = json.readValue(cuerpo, CuerpoError.class);
        } catch (RuntimeException excepcion) {
            // El cuerpo no tiene el formato de error esperado; se usa el estado.
        }

        if (error != null && error.codigo() != null) {
            throw new ErrorSipApi(error.codigo(), error.mensaje(), estado, null);
        }
        throw new ErrorSipApi("HTTP-" + estado,
                "La API SIP respondio con el estado HTTP " + estado + ".",
                estado, null);
    }

    /**
     * Codifica un valor para usarlo como segmento de ruta.
     *
     * @param valor valor sin codificar
     * @return valor codificado (espacios como %20)
     */
    static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    private static String direccionConfigurada() {
        String valor = System.getProperty(PROPIEDAD_URL);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(VARIABLE_URL);
        }
        return valor == null || valor.isBlank() ? URL_PREDETERMINADA : valor;
    }
}
