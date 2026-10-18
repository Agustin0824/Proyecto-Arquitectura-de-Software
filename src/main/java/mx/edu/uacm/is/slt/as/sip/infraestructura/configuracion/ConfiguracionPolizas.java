package mx.edu.uacm.is.slt.as.sip.infraestructura.configuracion;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import mx.edu.uacm.is.slt.as.sip.aplicacion.ServicioPolizas;
import mx.edu.uacm.is.slt.as.sip.infraestructura.rest.ConsultaPolizasRest;

/**
 * Conecta el servicio de polizas con el Sistema Dueno.
 *
 * @author Luis Angel
 */
@Configuration
public class ConfiguracionPolizas {

    /**
     * Configura el adaptador y el servicio de consultas.
     *
     * @param url direccion del Sistema Dueno
     * @param conexion segundos para establecer la conexion
     * @param lectura segundos para esperar la respuesta
     * @return servicio conectado al adaptador REST
     */
    @Bean
    public ServicioPolizas servicioPolizas(
            @Value("${sistema.dueno.url}") String url,
            @Value("${sistema.dueno.timeout-conexion-segundos}") int conexion,
            @Value("${sistema.dueno.timeout-lectura-segundos}") int lectura) {

        HttpClient clienteHttp = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(conexion))
                .build();

        JdkClientHttpRequestFactory solicitudes
                = new JdkClientHttpRequestFactory(clienteHttp);
        solicitudes.setReadTimeout(Duration.ofSeconds(lectura));

        RestClient clienteRest = RestClient.builder()
                .baseUrl(url)
                .requestFactory(solicitudes)
                .build();

        return new ServicioPolizas(new ConsultaPolizasRest(clienteRest));
    }
}