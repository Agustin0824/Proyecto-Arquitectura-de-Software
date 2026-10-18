package mx.edu.uacm.is.slt.as.sip.aplicacion;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puerto.ConsultaPolizas;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;

/**
 * Comprueba las validaciones y consultas del servicio.
 *
 * @author Luis Angel
 */
class ServicioPolizasTest {

    @Test
    void rechazaClaveVaciaSinConsultar() {
        ConsultaPolizas consultas = Mockito.mock(ConsultaPolizas.class);
        ServicioPolizas servicio = new ServicioPolizas(consultas);

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.buscarPorClave("   "));

        Mockito.verifyNoInteractions(consultas);
    }

    @Test
    void eliminaEspaciosDeLaClaveAntesDeConsultar() {
        ConsultaPolizas consultas = Mockito.mock(ConsultaPolizas.class);
        Mockito.when(consultas.buscarPorClave("POL-001"))
                .thenReturn(Optional.empty());
        ServicioPolizas servicio = new ServicioPolizas(consultas);

        Assertions.assertTrue(servicio.buscarPorClave("  POL-001  ").isEmpty());

        Mockito.verify(consultas).buscarPorClave("POL-001");
    }

    @Test
    void rechazaTipoNuloSinConsultar() {
        ConsultaPolizas consultas = Mockito.mock(ConsultaPolizas.class);
        ServicioPolizas servicio = new ServicioPolizas(consultas);

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.buscarPorTipo(null));

        Mockito.verifyNoInteractions(consultas);
    }
}