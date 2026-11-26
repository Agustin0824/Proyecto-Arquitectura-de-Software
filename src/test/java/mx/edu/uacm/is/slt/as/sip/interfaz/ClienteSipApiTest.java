
package mx.edu.uacm.is.slt.as.sip.interfaz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;

/**
 * Pruebas del cliente HTTP de la interfaz contra una API SIP simulada.
 */
class ClienteSipApiTest {

    private final List<String> peticiones = new CopyOnWriteArrayList<>();
    private final List<String> cuerpos = new CopyOnWriteArrayList<>();

    private HttpServer servidor;
    private int estado;
    private String cuerpo;

    @BeforeEach
    void iniciarServidor() throws IOException {
        estado = 200;
        cuerpo = "";

        servidor = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);

        servidor.createContext("/", intercambio -> {
            peticiones.add(intercambio.getRequestMethod() + " "
                    + intercambio.getRequestURI().toString());

            cuerpos.add(new String(
                    intercambio.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8));

            byte[] datos = cuerpo.getBytes(StandardCharsets.UTF_8);

            intercambio.getResponseHeaders().add(
                    "Content-Type", "application/json");

            if (estado == 204) {
                intercambio.sendResponseHeaders(estado, -1);
            } else {
                intercambio.sendResponseHeaders(estado, datos.length);
                intercambio.getResponseBody().write(datos);
            }

            intercambio.close();
        });

        servidor.start();
    }

    @AfterEach
    void detenerServidor() {
        servidor.stop(0);
    }

    private ClienteSipApi cliente() {
        return new ClienteSipApi(
                "http://127.0.0.1:" + servidor.getAddress().getPort());
    }

    // PRUEBAS ORIGINALES

    @Test
    void consultaPolizaPorClaveConvierteLaRespuesta() throws Exception {
        estado = 200;
        cuerpo = "{\"clave\":\"P 1\",\"tipo\":\"1\","
                + "\"montoAsegurado\":1500.50,"
                + "\"descripcion\":\"Vida\",\"curpCliente\":\"CURP123\"}";

        DatosPoliza poliza = cliente().consultarPoliza("P 1")
                .get(10, TimeUnit.SECONDS);

        assertThat(poliza.clave()).isEqualTo("P 1");
        assertThat(poliza.curpCliente()).isEqualTo("CURP123");
        assertThat(peticiones).containsExactly(
                "GET /api/polizas/P%201");
    }

    @Test
    void errorDeLaApiSeConvierteEnErrorSipApi() {
        estado = 503;
        cuerpo = "{\"codigo\":\"EX-DND\","
                + "\"mensaje\":\"Sistema Dueno no disponible\"}";

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

        assertThat(cliente.getUrlBase())
                .isEqualTo("http://localhost:9090");
    }

    // PRUEBAS DE CLIENTES

    @Test
    void consultaClientePorCurpConvierteLaRespuesta() throws Exception {
        estado = 200;
        cuerpo = "{\"curp\":\"CURP123\",\"nombres\":\"Ana\","
                + "\"primerApellido\":\"Lopez\","
                + "\"segundoApellido\":null,"
                + "\"direccion\":\"Calle 1\","
                + "\"fechaNacimiento\":\"1990-01-02\"}";

        DatosCliente datos = cliente().consultarCliente("CURP123")
                .get(10, TimeUnit.SECONDS);

        assertThat(datos.curp()).isEqualTo("CURP123");
        assertThat(datos.nombres()).isEqualTo("Ana");
        assertThat(datos.fechaNacimiento())
                .isEqualTo(LocalDate.of(1990, 1, 2));

        assertThat(peticiones).containsExactly(
                "GET /api/clientes/CURP123");
    }

    @Test
    void registraClienteEnviaJson() throws Exception {
        estado = 201;
        cuerpo = "{\"curp\":\"CURP123\",\"nombres\":\"Ana\","
                + "\"primerApellido\":\"Lopez\","
                + "\"segundoApellido\":null,"
                + "\"direccion\":\"Calle 1\","
                + "\"fechaNacimiento\":\"1990-01-02\"}";

        DatosCliente solicitud = new DatosCliente(
                "CURP123", "Ana", "Lopez", null,
                "Calle 1", LocalDate.of(1990, 1, 2));

        DatosCliente resultado = cliente().registrarCliente(solicitud)
                .get(10, TimeUnit.SECONDS);

        assertThat(resultado.curp()).isEqualTo("CURP123");
        assertThat(peticiones).containsExactly(
                "POST /api/clientes");

        assertThat(cuerpos.get(0))
                .contains("\"curp\":\"CURP123\"")
                .contains("\"fechaNacimiento\":\"1990-01-02\"");
    }

    @Test
    void modificaClienteEnviaPutConJson() throws Exception {
        estado = 200;
        cuerpo = "{\"curp\":\"CURP123\",\"nombres\":\"Ana\","
                + "\"primerApellido\":\"Lopez\","
                + "\"segundoApellido\":null,"
                + "\"direccion\":\"Calle 2\","
                + "\"fechaNacimiento\":\"1990-01-02\"}";

        DatosCliente solicitud = new DatosCliente(
                "CURP123", "Ana", "Lopez", null,
                "Calle 2", LocalDate.of(1990, 1, 2));

        DatosCliente resultado = cliente()
                .modificarCliente("CURP123", solicitud)
                .get(10, TimeUnit.SECONDS);

        assertThat(resultado.direccion()).isEqualTo("Calle 2");
        assertThat(peticiones).containsExactly(
                "PUT /api/clientes/CURP123");

        assertThat(cuerpos.get(0)).contains("\"direccion\":\"Calle 2\"");
    }

    // PRUEBAS DE POLIZAS

    @Test
    void listaPolizasConFiltrosYPaginacion() throws Exception {
        estado = 200;
        cuerpo = "[{\"clave\":\"P-1\",\"tipo\":\"1\","
                + "\"montoAsegurado\":1500.00,"
                + "\"descripcion\":\"Vida\","
                + "\"curpCliente\":\"CURP123\"}]";

        List<DatosPoliza> polizas = cliente()
                .listarPolizas("CURP 123", "1", 0, 10)
                .get(10, TimeUnit.SECONDS);

        assertThat(polizas).hasSize(1);
        assertThat(polizas.get(0).clave()).isEqualTo("P-1");

        assertThat(peticiones).containsExactly(
                "GET /api/polizas?curp=CURP%20123&tipo=1&pagina=0&tamano=10");
    }

    @Test
    void registraPolizaEnviaJson() throws Exception {
        estado = 201;
        cuerpo = "{\"clave\":\"P-1\",\"tipo\":\"1\","
                + "\"montoAsegurado\":1500.00,"
                + "\"descripcion\":\"Vida\","
                + "\"curpCliente\":\"CURP123\"}";

        DatosPoliza solicitud = new DatosPoliza(
                "P-1", "1", new java.math.BigDecimal("1500.00"),
                "Vida", "CURP123");

        DatosPoliza resultado = cliente().registrarPoliza(solicitud)
                .get(10, TimeUnit.SECONDS);

        assertThat(resultado.clave()).isEqualTo("P-1");
        assertThat(peticiones).containsExactly(
                "POST /api/polizas");

        assertThat(cuerpos.get(0))
                .contains("\"clave\":\"P-1\"")
                .contains("\"curpCliente\":\"CURP123\"");
    }

    @Test
    void modificaPolizaEnviaPutConJson() throws Exception {
        estado = 200;
        cuerpo = "{\"clave\":\"P-1\",\"tipo\":\"1\","
                + "\"montoAsegurado\":2000.00,"
                + "\"descripcion\":\"Vida actualizada\","
                + "\"curpCliente\":\"CURP123\"}";

        DatosPoliza solicitud = new DatosPoliza(
                "P-1", "1", new java.math.BigDecimal("2000.00"),
                "Vida actualizada", "CURP123");

        DatosPoliza resultado = cliente()
                .modificarPoliza("P-1", solicitud)
                .get(10, TimeUnit.SECONDS);

        assertThat(resultado.montoAsegurado())
                .isEqualByComparingTo("2000.00");

        assertThat(peticiones).containsExactly(
                "PUT /api/polizas/P-1");
    }

    @Test
    void eliminaPolizaAceptaRespuestaSinContenido() throws Exception {
        estado = 204;
        cuerpo = "";

        Void resultado = cliente().eliminarPoliza("P-1")
                .get(10, TimeUnit.SECONDS);

        assertThat(resultado).isNull();
        assertThat(peticiones).containsExactly(
                "DELETE /api/polizas/P-1");
    }

    // PRUEBAS DE BENEFICIARIOS

    @Test
    void consultaBeneficiarioConParametros() throws Exception {
        estado = 200;
        cuerpo = "{\"id\":1,\"nombres\":\"Luis\","
                + "\"primerApellido\":\"Perez\","
                + "\"segundoApellido\":null,"
                + "\"fechaNacimiento\":\"1995-05-10\","
                + "\"porcentaje\":50.00,"
                + "\"clavePoliza\":\"P-1\"}";

        DatosBeneficiario resultado = cliente().consultarBeneficiario(
                "P-1", LocalDate.of(1995, 5, 10),
                "Luis", "Perez", null)
                .get(10, TimeUnit.SECONDS);

        assertThat(resultado.nombres()).isEqualTo("Luis");
        assertThat(resultado.clavePoliza()).isEqualTo("P-1");

        assertThat(peticiones).containsExactly(
                "GET /api/polizas/P-1/beneficiarios"
                + "?fechaNacimiento=1995-05-10"
                + "&nombres=Luis&primerApellido=Perez");
    }

    @Test
    void registraBeneficiarioEnviaJson() throws Exception {
        estado = 201;
        cuerpo = "{\"id\":1,\"nombres\":\"Luis\","
                + "\"primerApellido\":\"Perez\","
                + "\"segundoApellido\":null,"
                + "\"fechaNacimiento\":\"1995-05-10\","
                + "\"porcentaje\":50.00,"
                + "\"clavePoliza\":\"P-1\"}";

        DatosBeneficiario solicitud = new DatosBeneficiario(
                null, "Luis", "Perez", null,
                LocalDate.of(1995, 5, 10),
                new java.math.BigDecimal("50.00"), "P-1");

        DatosBeneficiario resultado = cliente()
                .registrarBeneficiario("P-1", solicitud)
                .get(10, TimeUnit.SECONDS);

        assertThat(resultado.id()).isEqualTo(1L);
        assertThat(peticiones).containsExactly(
                "POST /api/polizas/P-1/beneficiarios");

        assertThat(cuerpos.get(0))
                .contains("\"nombres\":\"Luis\"")
                .contains("\"clavePoliza\":\"P-1\"");
    }
}
