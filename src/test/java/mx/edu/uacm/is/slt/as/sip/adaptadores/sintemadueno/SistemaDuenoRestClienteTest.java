package mx.edu.uacm.is.slt.as.sip.adaptadores.sistemadueno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.sip.dominio.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.sip.dominio.SistemaDuenoNoDisponibleException;

/**
 * Pruebas del cliente REST contra un servidor HTTP local simulado.
 */
class SistemaDuenoRestClienteTest {

    private static final String POLIZA_JSON = "{\"clave\":\"P-1\",\"tipo\":1,"
            + "\"monto\":1500.50,\"descripcion\":\"Seguro de vida\","
            + "\"curpCliente\":\"CURP123\"}";

    private final List<String> peticiones = new CopyOnWriteArrayList<>();
    private final Deque<Integer> estados = new ConcurrentLinkedDeque<>();
    private final Deque<String> cuerpos = new ConcurrentLinkedDeque<>();

    private volatile int retraso;
    private HttpServer servidor;
    private SistemaDuenoRestCliente cliente;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> {
            peticiones.add(intercambio.getRequestMethod() + " "
                    + intercambio.getRequestURI().getRawPath());

            if (retraso > 0) {
                try {
                    Thread.sleep(retraso);
                } catch (InterruptedException excepcion) {
                    Thread.currentThread().interrupt();
                }
            }

            int estado = estados.isEmpty() ? 200 : estados.poll();
            byte[] cuerpo = (cuerpos.isEmpty() ? "" : cuerpos.poll())
                    .getBytes(StandardCharsets.UTF_8);

            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estado, cuerpo.length == 0 ? -1 : cuerpo.length);
            if (cuerpo.length > 0) {
                intercambio.getResponseBody().write(cuerpo);
            }
            intercambio.close();
        });
        servidor.setExecutor(Executors.newCachedThreadPool(tarea -> {
            Thread hilo = new Thread(tarea);
            hilo.setDaemon(true);
            return hilo;
        }));
        servidor.start();

        cliente = new SistemaDuenoRestCliente(
                "http://127.0.0.1:" + servidor.getAddress().getPort(), 2, 10);
    }

    @AfterEach
    void detenerServidor() {
        servidor.stop(0);
    }

    private void responder(int estado, String cuerpo) {
        estados.add(estado);
        cuerpos.add(cuerpo);
    }

    private static DatosPoliza poliza() {
        return new DatosPoliza("P-1", "1", new BigDecimal("1500.50"),
                "Seguro de vida", "CURP123");
    }

    @Test
    void registrarPolizaEnviaPostConParametrosCodificados() {
        responder(200, "");

        cliente.registrarPoliza(poliza());

        assertThat(peticiones)
                .containsExactly("POST /poliza/P-1/1/1500.50/Seguro%20de%20vida/CURP123");
    }

    @Test
    void modificarPolizaEnviaPut() {
        responder(200, "");

        cliente.modificarPoliza(poliza());

        assertThat(peticiones)
                .containsExactly("PUT /poliza/P-1/1/1500.50/Seguro%20de%20vida/CURP123");
    }

    @Test
    void eliminarPolizaEnviaDelete() {
        responder(200, "");

        cliente.eliminarPoliza("P-1");

        assertThat(peticiones).containsExactly("DELETE /poliza/P-1");
    }

    @Test
    void descripcionConDiagonalSeRechazaSinEnviarNada() {
        DatosPoliza invalida = new DatosPoliza("P-1", "1", BigDecimal.TEN,
                "auto/moto", "CURP123");

        assertThatThrownBy(() -> cliente.registrarPoliza(invalida))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(peticiones).isEmpty();
    }

    @Test
    void descripcionVaciaSeRechaza() {
        DatosPoliza invalida = new DatosPoliza("P-1", "1", BigDecimal.TEN,
                " ", "CURP123");

        assertThatThrownBy(() -> cliente.modificarPoliza(invalida))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(peticiones).isEmpty();
    }

    @Test
    void escrituraConErrorDelServidorNoSeReintenta() {
        responder(503, "");
        responder(200, "");

        assertThatThrownBy(() -> cliente.registrarPoliza(poliza()))
                .isInstanceOf(SistemaDuenoNoDisponibleException.class);
        assertThat(peticiones).hasSize(1);
    }

    @Test
    void escrituraRechazadaPorElDuenoEsReglaDeNegocio() {
        responder(409, "");

        assertThatThrownBy(() -> cliente.registrarPoliza(poliza()))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(peticiones).hasSize(1);
    }

    @Test
    void eliminarPolizaInexistenteInformaNoEncontrado() {
        responder(404, "");

        assertThatThrownBy(() -> cliente.eliminarPoliza("P-9"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void lecturaSeReintentaUnaVezYTieneExito() {
        responder(503, "");
        responder(200, POLIZA_JSON);

        var resultado = cliente.consultarPoliza("P-1");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().clave()).isEqualTo("P-1");
        assertThat(resultado.get().tipo()).isEqualTo("1");
        assertThat(peticiones).hasSize(2);
    }

    @Test
    void lecturaFallidaHaceMaximoDosPeticiones() {
        responder(503, "");
        responder(503, "");
        responder(200, POLIZA_JSON);

        assertThatThrownBy(() -> cliente.consultarPoliza("P-1"))
                .isInstanceOf(SistemaDuenoNoDisponibleException.class);
        assertThat(peticiones).hasSize(2);
    }

    @Test
    void lecturaNoEncontradaNoSeReintentaYDevuelveVacio() {
        responder(404, "");

        assertThat(cliente.consultarPoliza("P-9")).isEmpty();
        assertThat(peticiones).hasSize(1);
    }

    @Test
    void filtroPorAseguradoUsaLaRutaOficial() {
        responder(200, "[" + POLIZA_JSON + "]");

        List<DatosPoliza> resultado = cliente.listarPolizasPorNombreAsegurado(
                "Juan Carlos", "Perez", "Lopez");

        assertThat(resultado).hasSize(1);
        assertThat(peticiones)
                .containsExactly("GET /polizas/c/Juan%20Carlos/Perez/Lopez");
    }

    @Test
    void filtroPorAseguradoSinSegundoApellidoEnviaNull() {
        responder(200, "[]");
        responder(200, "[]");

        cliente.listarPolizasPorNombreAsegurado("Juan", "Perez", null);
        cliente.listarPolizasPorNombreAsegurado("Juan", "Perez", " ");

        assertThat(peticiones).containsExactly(
                "GET /polizas/c/Juan/Perez/null", "GET /polizas/c/Juan/Perez/null");
    }

    @Test
    void filtroPorBeneficiarioUsaLaRutaOficial() {
        responder(200, "[]");

        assertThat(cliente.listarPolizasPorNombreBeneficiario("Ana", "Ruiz", "Soto"))
                .isEmpty();
        assertThat(peticiones).containsExactly("GET /polizas/b/Ana/Ruiz/Soto");
    }

    @Test
    void filtroPorFechaDeNacimientoDelBeneficiarioUsaLaRutaOficial() {
        responder(200, "[" + POLIZA_JSON + "]");

        List<DatosPoliza> resultado = cliente
                .listarPolizasPorFechaNacimientoBeneficiario(LocalDate.of(2000, 1, 31));

        assertThat(resultado).hasSize(1);
        assertThat(peticiones).containsExactly("GET /polizas/b/2000-01-31");
    }

    @Test
    void filtrosRechazanDatosObligatoriosVacios() {
        assertThatThrownBy(() -> cliente.listarPolizasPorNombreAsegurado(
                " ", "Perez", null)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> cliente.listarPolizasPorNombreBeneficiario(
                "Ana", null, null)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> cliente
                .listarPolizasPorFechaNacimientoBeneficiario(null))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(peticiones).isEmpty();
    }

    @Test
    void escrituraSinConexionNoSeReintenta() {
        servidor.stop(0);

        assertThatThrownBy(() -> cliente.registrarPoliza(poliza()))
                .isInstanceOf(SistemaDuenoNoDisponibleException.class);
        assertThat(peticiones).isEmpty();
    }

    @Test
    void escrituraConRespuestaLentaNoSeRepite() throws IOException {
        retraso = 5000;
        SistemaDuenoRestCliente lento = new SistemaDuenoRestCliente(
                "http://127.0.0.1:" + servidor.getAddress().getPort(), 1, 3);

        assertThatThrownBy(() -> lento.modificarPoliza(poliza()))
                .isInstanceOf(SistemaDuenoNoDisponibleException.class);
        assertThat(peticiones).hasSize(1);
    }

    @Test
    void lecturaLentaRespetaElLimiteTotal() {
        retraso = 8000;
        SistemaDuenoRestCliente lento = new SistemaDuenoRestCliente(
                "http://127.0.0.1:" + servidor.getAddress().getPort(), 1, 4);

        long inicio = System.nanoTime();
        assertThatThrownBy(() -> lento.consultarPoliza("P-1"))
                .isInstanceOf(SistemaDuenoNoDisponibleException.class);
        long segundos = (System.nanoTime() - inicio) / 1_000_000_000L;

        assertThat(segundos).isLessThanOrEqualTo(4L);
        assertThat(peticiones).hasSize(1);
    }

    @Test
    void consultasExistentesSiguenFuncionando() {
        responder(200, "[]");
        responder(200, "[]");
        responder(200, "[]");

        cliente.listarPolizas();
        cliente.listarPolizasPorCurp("CURP123");
        cliente.listarPolizasPorTipo("1");

        assertThat(new ArrayList<>(peticiones)).containsExactly(
                "GET /polizas", "GET /polizas/CURP123", "GET /polizas/1");
    }
}
