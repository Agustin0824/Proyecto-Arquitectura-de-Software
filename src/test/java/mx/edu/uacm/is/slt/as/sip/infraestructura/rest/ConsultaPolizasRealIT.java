package mx.edu.uacm.is.slt.as.sip.infraestructura.rest;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.sip.aplicacion.ServicioPolizas;
import mx.edu.uacm.is.slt.as.sip.dominio.Poliza;
import mx.edu.uacm.is.slt.as.sip.infraestructura.configuracion.ConfiguracionPolizas;

/**
 * Comprueba una consulta contra el Sistema Dueno real.
 *
 * @author Luis Angel
 */
class ConsultaPolizasRealIT {

    @Test
    void consultaPolizasDelServidor() {
        ConfiguracionPolizas configuracion = new ConfiguracionPolizas();
        ServicioPolizas servicio = configuracion.servicioPolizas(
                "http://nachintoch.mx:8080", 10, 180);

        List<Poliza> polizas = servicio.consultarTodas();

        Assertions.assertNotNull(polizas);
        for (Poliza poliza : polizas) {
            Assertions.assertNotNull(poliza);
            Assertions.assertNotNull(poliza.getTipo());
            Assertions.assertTrue(poliza.getMontoAsegurado().signum() > 0);
        }

        System.out.println("Polizas recibidas del Sistema Dueno: " + polizas.size());
    }
}