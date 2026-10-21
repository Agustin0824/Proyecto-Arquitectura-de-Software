package mx.edu.uacm.is.slt.as.sip.adaptadores.sistemadueno;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.dominio.Poliza;
import mx.edu.uacm.is.slt.as.sip.dominio.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.sip.infraestructura.rest.PolizaRespuesta;
import mx.edu.uacm.is.slt.as.sip.infraestructura.rest.ClienteRespuesta;

/**
 * Realiza consultas REST al Sistema Dueno.
 */
@Component
public class SistemaDuenoRestCliente implements SistemaDueno {

    private final RestClient http;

    /**
     * Configura el cliente HTTP.
     *
     * @param urlBase direccion del Sistema Dueno
     * @param segundosConexion tiempo para establecer la conexion
     * @param segundosLectura tiempo para esperar la respuesta
     */
    public SistemaDuenoRestCliente(
            @Value("${sistema.dueno.url}") String urlBase,
            @Value("${sistema.dueno.timeout-conexion-segundos}") long segundosConexion,
            @Value("${sistema.dueno.timeout-lectura-segundos}") long segundosLectura) {

        HttpClient clienteHttp = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(segundosConexion))
                .build();

        JdkClientHttpRequestFactory fabrica
                = new JdkClientHttpRequestFactory(clienteHttp);
        fabrica.setReadTimeout(Duration.ofSeconds(segundosLectura));

        this.http = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(fabrica)
                .build();
    }

    @Override
    public Optional<DatosCliente> consultarCliente(String curp) {
        return obtener(() -> http.get()
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
        return obtener(() -> http.get()
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
    public void registrarPoliza(DatosPoliza poliza) {
        throw new UnsupportedOperationException(
                "El alta de polizas aun no esta implementada.");
    }

    @Override
    public void modificarPoliza(DatosPoliza poliza) {
        throw new UnsupportedOperationException(
                "La modificacion de polizas aun no esta implementada.");
    }

    @Override
    public void eliminarPoliza(String clave) {
        throw new UnsupportedOperationException(
                "La baja de polizas aun no esta implementada.");
    }

    @Override
    public Optional<DatosBeneficiario> consultarBeneficiario(
            String clavePoliza, LocalDate fechaNacimiento,
            String nombres, String primerApellido, String segundoApellido) {

        String apellidoOpcional = segundoApellido == null || segundoApellido.isBlank()
                ? "null" : segundoApellido;

        return obtener(() -> http.get()
                .uri("/beneficiario/{fecha}/{clave}/{nombres}/{primer}/{segundo}",
                        fechaNacimiento.toString(), clavePoliza, nombres,
                        primerApellido, apellidoOpcional)
                .retrieve()
                .body(DatosBeneficiario.class));
    }

    @Override
    public void registrarBeneficiario(DatosBeneficiario beneficiario) {
        throw new UnsupportedOperationException(
                "El alta de beneficiarios aun no esta implementada.");
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
            respuesta = http.get()
                    .uri(ruta, parametros)
                    .retrieve()
                    .body(PolizaRespuesta[].class);

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
    private <T> Optional<T> obtener(Supplier<T> consulta) {
        try {
            return Optional.ofNullable(consulta.get());
        } catch (HttpClientErrorException.NotFound excepcion) {
            return Optional.empty();
        } catch (RestClientException excepcion) {
            throw new SistemaDuenoNoDisponibleException(excepcion);
        }
    }
}