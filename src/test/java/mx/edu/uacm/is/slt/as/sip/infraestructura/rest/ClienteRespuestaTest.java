package mx.edu.uacm.is.slt.as.sip.infraestructura.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;

/**
 * Pruebas para la conversion de respuestas de clientes.
 */
class ClienteRespuestaTest {

    @Test
    void convierteTextoNullEnSegundoApellidoNulo() {
        ClienteRespuesta respuesta = new ClienteRespuesta(
                "Cliente Prueba",
                "Prueba",
                "null",
                "Calle de Prueba 456",
                "ABCD991231HDFLRS09",
                "1995-06-15");

        DatosCliente resultado = respuesta.convertirADatos();

        assertThat(resultado.segundoApellido()).isNull();
        assertThat(resultado.fechaNacimiento())
                .isEqualTo(LocalDate.of(1995, 6, 15));
    }

    @Test
    void conservaSegundoApellidoCuandoTieneValor() {
        ClienteRespuesta respuesta = new ClienteRespuesta(
                "Cliente Prueba",
                "Prueba",
                "Lopez",
                "Calle de Prueba 456",
                "ABCD991231HDFLRS09",
                "1995-06-15");

        DatosCliente resultado = respuesta.convertirADatos();

        assertThat(resultado.segundoApellido()).isEqualTo("Lopez");
    }
}