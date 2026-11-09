package mx.edu.uacm.is.slt.as.sip.aplicacion.servicios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.aplicacion.servicios.PolizaServicio;
import mx.edu.uacm.is.slt.as.sip.dominio.ClienteNoRegistradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;

/**
 * Comprueba el alta de polizas sin realizar solicitudes remotas.
 */
class PolizaServicioTest {

    private static final String CURP = "broe550101hdfabc00";

    /**
     * Confirma el alta mediante una consulta posterior.
     */
    @Test
    void registrarConfirmaElResultadoRemoto() {
        SistemaDueno remoto = Mockito.mock(SistemaDueno.class);
        PolizaServicio servicio = new PolizaServicio(remoto);
        DatosPoliza solicitud = datosValidos();

        DatosCliente cliente = new DatosCliente(
                CURP, "Cliente", "Prueba", null,
                "Direccion de prueba", LocalDate.of(1990, 1, 1));

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.of(cliente));
        Mockito.when(remoto.consultarPoliza(solicitud.clave()))
                .thenReturn(Optional.empty(), Optional.of(solicitud));

        DatosPoliza resultado = servicio.registrar(solicitud);

        Assertions.assertEquals(solicitud, resultado);
        Mockito.verify(remoto).registrarPoliza(solicitud);
        Mockito.verify(remoto, Mockito.times(2))
                .consultarPoliza(solicitud.clave());
    }

    /**
     * Rechaza datos invalidos antes de consultar o escribir.
     */
    @Test
    void registrarRechazaMontoInvalido() {
        SistemaDueno remoto = Mockito.mock(SistemaDueno.class);
        PolizaServicio servicio = new PolizaServicio(remoto);

        DatosPoliza solicitud = new DatosPoliza(
                "SIP-PRUEBA-001", "0", BigDecimal.ZERO,
                "Poliza de prueba", CURP);

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(solicitud));

        Mockito.verifyNoInteractions(remoto);
    }

    /**
     * Impide el alta cuando el cliente no existe.
     */
    @Test
    void registrarRechazaClienteInexistente() {
        SistemaDueno remoto = Mockito.mock(SistemaDueno.class);
        PolizaServicio servicio = new PolizaServicio(remoto);
        DatosPoliza solicitud = datosValidos();

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ClienteNoRegistradoException.class,
                () -> servicio.registrar(solicitud));

        Mockito.verify(remoto, Mockito.never()).registrarPoliza(
                Mockito.any(DatosPoliza.class));
        Mockito.verify(remoto, Mockito.never()).consultarPoliza(
                Mockito.anyString());
    }

    /**
     * No confirma un alta cuyos datos remotos son diferentes.
     */
    @Test
    void registrarRechazaConfirmacionInconsistente() {
        SistemaDueno remoto = Mockito.mock(SistemaDueno.class);
        PolizaServicio servicio = new PolizaServicio(remoto);
        DatosPoliza solicitud = datosValidos();

        DatosCliente cliente = new DatosCliente(
                CURP, "Cliente", "Prueba", null,
                "Direccion de prueba", LocalDate.of(1990, 1, 1));

        DatosPoliza distinta = new DatosPoliza(
                solicitud.clave(), "0", new BigDecimal("500.00"),
                solicitud.descripcion(), CURP);

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.of(cliente));
        Mockito.when(remoto.consultarPoliza(solicitud.clave()))
                .thenReturn(Optional.empty(), Optional.of(distinta));

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(solicitud));

        Mockito.verify(remoto, Mockito.times(1))
                .registrarPoliza(solicitud);
    }

    /**
     * Confirma una modificacion mediante consulta posterior.
     */
    @Test
    void modificarConfirmaElResultadoRemoto() {
        SistemaDueno remoto = Mockito.mock(SistemaDueno.class);
        PolizaServicio servicio = new PolizaServicio(remoto);
        DatosPoliza anterior = datosValidos();

        DatosPoliza solicitud = new DatosPoliza(
                anterior.clave(), "1", new BigDecimal("2000.00"),
                "Cobertura actualizada", CURP);

        DatosCliente cliente = new DatosCliente(
                CURP, "Cliente", "Prueba", null,
                "Direccion de prueba", LocalDate.of(1990, 1, 1));

        Mockito.when(remoto.consultarPoliza(anterior.clave()))
                .thenReturn(Optional.of(anterior), Optional.of(solicitud));
        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.of(cliente));

        DatosPoliza resultado = servicio.modificar(
                anterior.clave(), solicitud);

        Assertions.assertEquals(solicitud, resultado);
        Mockito.verify(remoto).modificarPoliza(solicitud);
        Mockito.verify(remoto, Mockito.times(2))
                .consultarPoliza(anterior.clave());
    }

    /**
     * Impide cambiar la clave durante una modificacion.
     */
    @Test
    void modificarRechazaCambioDeClave() {
        SistemaDueno remoto = Mockito.mock(SistemaDueno.class);
        PolizaServicio servicio = new PolizaServicio(remoto);
        DatosPoliza solicitud = datosValidos();

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.modificar("OTRA-CLAVE", solicitud));

        Mockito.verifyNoInteractions(remoto);
    }

    /**
     * No confirma una modificacion que devuelve los datos anteriores.
     */
    @Test
    void modificarRechazaConfirmacionInconsistente() {
        SistemaDueno remoto = Mockito.mock(SistemaDueno.class);
        PolizaServicio servicio = new PolizaServicio(remoto);
        DatosPoliza anterior = datosValidos();

        DatosPoliza solicitud = new DatosPoliza(
                anterior.clave(), "0", new BigDecimal("3000.00"),
                anterior.descripcion(), CURP);

        DatosCliente cliente = new DatosCliente(
                CURP, "Cliente", "Prueba", null,
                "Direccion de prueba", LocalDate.of(1990, 1, 1));

        Mockito.when(remoto.consultarPoliza(anterior.clave()))
                .thenReturn(Optional.of(anterior));
        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.of(cliente));

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.modificar(anterior.clave(), solicitud));

        Mockito.verify(remoto, Mockito.times(1))
                .modificarPoliza(solicitud);
    }

    /**
     * Confirma la baja mediante la ausencia del registro remoto.
     */
    @Test
    void eliminarConfirmaAusenciaRemota() {
        SistemaDueno remoto = Mockito.mock(SistemaDueno.class);
        PolizaServicio servicio = new PolizaServicio(remoto);
        DatosPoliza existente = datosValidos();

        Mockito.when(remoto.consultarPoliza(existente.clave()))
                .thenReturn(Optional.of(existente), Optional.empty());

        servicio.eliminar(existente.clave());

        Mockito.verify(remoto, Mockito.times(1))
                .eliminarPoliza(existente.clave());
        Mockito.verify(remoto, Mockito.times(2))
                .consultarPoliza(existente.clave());
    }

    /**
     * Impide solicitar la baja de una poliza inexistente.
     */
    @Test
    void eliminarRechazaPolizaInexistente() {
        SistemaDueno remoto = Mockito.mock(SistemaDueno.class);
        PolizaServicio servicio = new PolizaServicio(remoto);
        String clave = "SIP-INEXISTENTE";

        Mockito.when(remoto.consultarPoliza(clave))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.eliminar(clave));

        Mockito.verify(remoto, Mockito.never())
                .eliminarPoliza(Mockito.anyString());
    }

    /**
     * No confirma una baja si el registro continua existiendo.
     */
    @Test
    void eliminarRechazaRegistroPersistente() {
        SistemaDueno remoto = Mockito.mock(SistemaDueno.class);
        PolizaServicio servicio = new PolizaServicio(remoto);
        DatosPoliza existente = datosValidos();

        Mockito.when(remoto.consultarPoliza(existente.clave()))
                .thenReturn(Optional.of(existente));

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.eliminar(existente.clave()));

        Mockito.verify(remoto, Mockito.times(1))
                .eliminarPoliza(existente.clave());
    }

    /**
     * Construye una solicitud valida para las pruebas.
     *
     * @return datos de prueba
     */
    private static DatosPoliza datosValidos() {
        return new DatosPoliza(
                "SIP-PRUEBA-001", "0", new BigDecimal("1000.00"),
                "Poliza de prueba", CURP);
    }
}