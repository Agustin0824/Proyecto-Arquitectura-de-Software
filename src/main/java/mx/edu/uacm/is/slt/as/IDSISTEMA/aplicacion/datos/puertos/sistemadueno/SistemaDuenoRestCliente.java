package mx.edu.uacm.is.slt.as.IDSISTEMA.adaptadores.sistemadueno;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.IDSISTEMA.dominio.SistemaDuenoNoDisponibleException;

/**
 * Adaptador REST hacia el Sistema Dueno.
 *
 * <p>Las rutas y el formato JSON de las escrituras y de los beneficiarios son
 * supuestos que deben confirmarse con la documentacion de la API del Sistema Dueno.
 */
@Component
public class SistemaDuenoRestCliente implements SistemaDueno {

    private final RestClient http;

    /**
     * Crea el cliente con la URL base configurada.
     *
     * @param urlBase valor de la propiedad sistema-dueno.url
     * @param segundosEspera tiempo maximo de espera en segundos
     */
    public SistemaDuenoRestCliente(@Value("${sistema-dueno.url}") String urlBase,
            @Value("${sistema-dueno.timeout-segundos:5}") long segundosEspera) {
        Duration espera = Duration.ofSeconds(segundosEspera);
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(espera).build());
        fabrica.setReadTimeout(espera);
        this.http = RestClient.builder().baseUrl(urlBase).requestFactory(fabrica).build();
    }

    @Override
    public Optional<DatosCliente> consultarCliente(String curp) {
        return obtener(() -> http.get().uri("/cliente/{curp}", curp).retrieve()
                .body(DatosCliente.class));
    }

    @Override
    public void registrarCliente(DatosCliente cliente) {
        enviar(() -> http.post().uri("/cliente").body(cliente).retrieve().toBodilessEntity());
    }

    @Override
    public void modificarCliente(DatosCliente cliente) {
        enviar(() -> http.put().uri("/cliente/{curp}", cliente.curp()).body(cliente)
                .retrieve().toBodilessEntity());
    }

    @Override
    public Optional<DatosPoliza> consultarPoliza(String clave) {
        return obtener(() -> http.get().uri("/poliza/{clave}", clave).retrieve()
                .body(DatosPoliza.class));
    }

    @Override
    public List<DatosPoliza> listarPolizas() {
        return lista(() -> http.get().uri("/polizas").retrieve().body(DatosPoliza[].class));
    }

    @Override
    public List<DatosPoliza> listarPolizasPorCurp(String curp) {
        return lista(() -> http.get().uri("/polizas/{curp}", curp).retrieve()
                .body(DatosPoliza[].class));
    }

    @Override
    public List<DatosPoliza> listarPolizasPorTipo(String tipo) {
        return lista(() -> http.get().uri("/polizas/{tipo}", tipo).retrieve()
                .body(DatosPoliza[].class));
    }

    @Override
    public void registrarPoliza(DatosPoliza poliza) {
        enviar(() -> http.post().uri("/poliza").body(poliza).retrieve().toBodilessEntity());
    }

    @Override
    public void modificarPoliza(DatosPoliza poliza) {
        enviar(() -> http.put().uri("/poliza/{clave}", poliza.clave()).body(poliza)
                .retrieve().toBodilessEntity());
    }

    @Override
    public void eliminarPoliza(String clave) {
        enviar(() -> http.delete().uri("/poliza/{clave}", clave).retrieve()
                .toBodilessEntity());
    }

    @Override
    public List<DatosBeneficiario> listarBeneficiarios(String clavePoliza) {
        return lista(() -> http.get().uri("/poliza/{clave}/beneficiarios", clavePoliza)
                .retrieve().body(DatosBeneficiario[].class));
    }

    @Override
    public void registrarBeneficiario(DatosBeneficiario beneficiario) {
        enviar(() -> http.post()
                .uri("/poliza/{clave}/beneficiarios", beneficiario.clavePoliza())
                .body(beneficiario).retrieve().toBodilessEntity());
    }

    private <T> Optional<T> obtener(Supplier<T> consulta) {
        try {
            return Optional.ofNullable(consulta.get());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new SistemaDuenoNoDisponibleException(e);
        }
    }

    private <T> List<T> lista(Supplier<T[]> consulta) {
    
