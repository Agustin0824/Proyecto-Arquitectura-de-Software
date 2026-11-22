package mx.edu.uacm.is.slt.as.sip.adaptadores.sistemadueno;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;

/**
 * Pruebas de la conversion de beneficiarios recibidos del Sistema Dueno.
 */
class SistemaDuenoRestClienteBeneficiarioTest {

    private static final String CLAVE_POLIZA
            = "f3a71d8c-6b42-4e95-9c10-2d7b8a4e6103";

    private static final String BENEFICIARIO_JSON
            = "{\"id\":{"
            + "\"nombres\":\"Beneficiario Prueba\","
            + "\"primerApellido\":\"SIP\","
            + "\"segundoApellido\":\"null\","
            + "\"fechaNacimiento\":\"1990-01-10T06:00:00.000+00:00\","
            + "\"clavePoliza\":\"" + CLAVE_POLIZA + "\"},"
            + "\"porcentaje\":100}";

    private final List<String> peticiones = new CopyOnWriteArrayList<>();

    private HttpServer servidor;
    private SistemaDuenoRestCliente cliente;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> {
            peticiones.add(intercambio.getRequestMethod() + " "
                    + intercambio.getRequestURI().getRawPath());

            byte[] cuerpo = BENEFICIARIO_JSON.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add(
                    "Content-Type", "application/json");
            intercambio.sendResponseHeaders(200, cuerpo.length);
            intercambio.getResponseBody().write(cuerpo);
            intercambio.close();
        });
        servidor.start();

        cliente = new SistemaDuenoRestCliente(
                "http://127.0.0.1:" + servidor.getAddress().getPort(), 2, 10);
    }

    @AfterEach
    void detenerServidor() {
        servidor.stop(0);
    }

    @Test
    void consultaConvierteIdentificadorCompuestoYApellidoAusente() {
        Optional<DatosBeneficiario> resultado = cliente.consultarBeneficiario(
                CLAVE_POLIZA,
                LocalDate.of(1990, 1, 10),
                "Beneficiario Prueba",
                "SIP",
                null);

        assertThat(resultado).isPresent();
        DatosBeneficiario beneficiario = resultado.orElseThrow();
        assertThat(beneficiario.id()).isNull();
        assertThat(beneficiario.nombres()).isEqualTo("Beneficiario Prueba");
        assertThat(beneficiario.primerApellido()).isEqualTo("SIP");
        assertThat(beneficiario.segundoApellido()).isNull();
        assertThat(beneficiario.fechaNacimiento())
                .isEqualTo(LocalDate.of(1990, 1, 10));
        assertThat(beneficiario.porcentaje()).isEqualByComparingTo("100");
        assertThat(beneficiario.clavePoliza()).isEqualTo(CLAVE_POLIZA);
        assertThat(peticiones).containsExactly(
                "GET /beneficiario/1990-01-10/" + CLAVE_POLIZA
                + "/Beneficiario%20Prueba/SIP/null");
    }
}