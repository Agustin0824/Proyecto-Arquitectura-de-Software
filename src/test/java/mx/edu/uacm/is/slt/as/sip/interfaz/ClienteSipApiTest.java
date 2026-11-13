package mx.edu.uacm.is.slt.as.sip.interfaz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;

/**
 * Pruebas del cliente HTTP de la interfaz contra una API SIP simulada.
 */
class ClienteSipApiTest {

    private final List<String> peticiones = new CopyOnWriteArrayList<>();
    private HttpServer servidor;
    private int estado;
    private String cuerpo;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> {
            peticiones.add(intercambio.getRequestMethod() + " "
                    + intercambio.getRequestURI().getRawPath());
            byte[] datos = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estado, datos.length);
            intercambio.getResponseBody().write(datos);
            intercambio.close();
        });
        servidor.start();
    }

    @AfterEach
    void detenerServidor() {
        servidor.stop(0);
    }

    private ClienteSipApi cliente() {
        return new ClienteSipApi("http://127.0.0.1:" + servidor.getAddress().getPort());
    }

    @Test
    void consultaPolizaPorClaveConvierteLaRespuesta() throws Exception {
        estado = 200;
        cuerpo = "{\"clave\":\"P 1\",\"tipo\":\"1\",\"montoAsegurado\":1500.50,"
                + "\"descripcion\":\"Vida\",\"curpCliente\":\"CURP123\"}";

        DatosPoliza poliza = cliente().consultarPoliza("P 1")
                .get(10, TimeUnit.SECONDS);

        assertThat(poliza.clave()).isEqualTo("P 1");
        assertThat(poliza.curpCliente()).isEqualTo("CURP123");
        assertThat(peticiones).containsExactly("GET /api/polizas/P%201");
    }

    @Test
    void errorDeLaApiSeConvierteEnErrorSipApi() {
        estado = 503;
        cuerpo = "{\"codigo\":\"EX-DND\",\"mensaje\":\"Sistema Dueno no disponible\"}";

        assertThatThrownBy(() -> cliente().consultarPoliza("P-1")
                .get(10, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isInstanceOf(ErrorSipApi.class)
                .hasMessage("Sistema Dueno no disponible");
    }

    @Test
    void apiSinConexionSeInformaComoNoDisponible() {
        ClienteSipApi cliente = new ClienteSipApi("http://127.0.0.1:1");

        assertThatThrownBy(() -> cliente.consultarPoliza("P-1")
                .get(30, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isInstanceOf(ErrorSipApi.class)
                .extracting(causa -> ((ErrorSipApi) causa).getCodigo())
                .isEqualTo(ErrorSipApi.NO_DISPONIBLE);
    }

    @Test
    void claveVaciaFallaSinEnviarSolicitud() {
        assertThatThrownBy(() -> cliente().consultarPoliza(" ")
                .get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class);
        assertThat(peticiones).isEmpty();
    }

    @Test
    void direccionInvalidaSeRechaza() {
        ClienteSipApi cliente = cliente();

        assertThatThrownBy(() -> cliente.configurarUrl("localhost:8080"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cliente.configurarUrl(" "))
                .isInstanceOf(IllegalArgumentException.class);
        cliente.configurarUrl("http://localhost:9090/");
        assertThat(cliente.getUrlBase()).isEqualTo("http://localhost:9090");
    }
}
