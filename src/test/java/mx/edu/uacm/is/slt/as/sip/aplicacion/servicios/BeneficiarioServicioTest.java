
package mx.edu.uacm.is.slt.as.sip.aplicacion.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.dominio.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;

/**
 * Pruebas unitarias del servicio de beneficiarios.
 */
class BeneficiarioServicioTest {

    private static final String CLAVE = "P-1";

    private static final LocalDate FECHA = LocalDate.of(1990, 1, 1);

    private final SistemaDueno sistemaDueno =
            Mockito.mock(SistemaDueno.class);

    private final BeneficiarioServicio servicio =
            new BeneficiarioServicio(sistemaDueno);

    /**
     * Construye una poliza valida para las pruebas.
     *
     * @return datos de la poliza
     */
    private static DatosPoliza poliza() {
        return new DatosPoliza(
                CLAVE,
                "1",
                new BigDecimal("1500.00"),
                "Seguro de vida",
                "CURP123456HDFABC01");
    }

    /**
     * Construye datos validos de un beneficiario.
     *
     * @return datos del beneficiario
     */
    private static DatosBeneficiario beneficiario() {
        return new DatosBeneficiario(
                null,
                "Ana Maria",
                "Lopez",
                "Perez",
                FECHA,
                new BigDecimal("50.00"),
                CLAVE);
    }

    /**
     * Registra un beneficiario y confirma el resultado remoto.
     */
    @Test
    void registrarConfirmaElBeneficiario() {
        DatosBeneficiario solicitud = beneficiario();
        DatosBeneficiario registrado = beneficiario();

        Mockito.when(sistemaDueno.consultarPoliza(CLAVE))
                .thenReturn(Optional.of(poliza()));

        Mockito.when(sistemaDueno.consultarBeneficiario(
                CLAVE, FECHA, "Ana Maria", "Lopez", "Perez"))
                .thenReturn(Optional.empty(), Optional.of(registrado));

        DatosBeneficiario resultado =
                servicio.registrar(CLAVE, solicitud);

        assertEquals("Ana Maria", resultado.nombres());
        assertEquals("Lopez", resultado.primerApellido());
        assertEquals(CLAVE, resultado.clavePoliza());

        Mockito.verify(sistemaDueno).registrarBeneficiario(
                Mockito.any(DatosBeneficiario.class));

        Mockito.verify(sistemaDueno, Mockito.times(2))
                .consultarBeneficiario(
                        CLAVE, FECHA, "Ana Maria", "Lopez", "Perez");
    }

    /**
     * Rechaza el registro de un beneficiario existente.
     */
    @Test
    void registrarRechazaBeneficiarioDuplicado() {
        DatosBeneficiario solicitud = beneficiario();

        Mockito.when(sistemaDueno.consultarPoliza(CLAVE))
                .thenReturn(Optional.of(poliza()));

        Mockito.when(sistemaDueno.consultarBeneficiario(
                CLAVE, FECHA, "Ana Maria", "Lopez", "Perez"))
                .thenReturn(Optional.of(solicitud));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(CLAVE, solicitud));

        Mockito.verify(sistemaDueno, Mockito.never())
                .registrarBeneficiario(
                        Mockito.any(DatosBeneficiario.class));
    }

    /**
     * Rechaza el registro cuando la poliza no existe.
     */
    @Test
    void registrarRechazaPolizaInexistente() {
        Mockito.when(sistemaDueno.consultarPoliza(CLAVE))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.registrar(CLAVE, beneficiario()));

        Mockito.verify(sistemaDueno, Mockito.never())
                .registrarBeneficiario(
                        Mockito.any(DatosBeneficiario.class));
    }

    /**
     * Rechaza un porcentaje que no cumple las reglas del dominio.
     */
    @Test
    void registrarRechazaPorcentajeInvalido() {
        DatosBeneficiario invalido = new DatosBeneficiario(
                null,
                "Ana Maria",
                "Lopez",
                "Perez",
                FECHA,
                BigDecimal.ZERO,
                CLAVE);

        Mockito.when(sistemaDueno.consultarPoliza(CLAVE))
                .thenReturn(Optional.of(poliza()));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(CLAVE, invalido));

        Mockito.verify(sistemaDueno, Mockito.never())
                .registrarBeneficiario(
                        Mockito.any(DatosBeneficiario.class));
    }

    /**
     * Rechaza datos incompletos.
     */
    @Test
    void registrarRechazaNombresVacios() {
        DatosBeneficiario invalido = new DatosBeneficiario(
                null,
                " ",
                "Lopez",
                "Perez",
                FECHA,
                new BigDecimal("50"),
                CLAVE);

        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(CLAVE, invalido));

        Mockito.verifyNoInteractions(sistemaDueno);
    }

    /**
     * Rechaza una confirmacion remota que no encuentra el beneficiario.
     */
    @Test
    void registrarRechazaConfirmacionVacia() {
        Mockito.when(sistemaDueno.consultarPoliza(CLAVE))
                .thenReturn(Optional.of(poliza()));

        Mockito.when(sistemaDueno.consultarBeneficiario(
                CLAVE, FECHA, "Ana Maria", "Lopez", "Perez"))
                .thenReturn(Optional.empty());

        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(CLAVE, beneficiario()));

        Mockito.verify(sistemaDueno).registrarBeneficiario(
                Mockito.any(DatosBeneficiario.class));

        Mockito.verify(sistemaDueno, Mockito.times(2))
                .consultarBeneficiario(
                        CLAVE, FECHA, "Ana Maria", "Lopez", "Perez");
    }

    /**
     * Rechaza una confirmacion con datos diferentes a los solicitados.
     */
    @Test
    void registrarRechazaConfirmacionConDatosDiferentes() {
        DatosBeneficiario diferente = new DatosBeneficiario(
                null,
                "Ana Maria",
                "Lopez",
                "Perez",
                FECHA,
                new BigDecimal("60"),
                CLAVE);

        Mockito.when(sistemaDueno.consultarPoliza(CLAVE))
                .thenReturn(Optional.of(poliza()));

        Mockito.when(sistemaDueno.consultarBeneficiario(
                CLAVE, FECHA, "Ana Maria", "Lopez", "Perez"))
                .thenReturn(Optional.empty(), Optional.of(diferente));

        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(CLAVE, beneficiario()));
    }

    /**
     * Consulta un beneficiario y normaliza los datos de busqueda.
     */
    @Test
    void consultarBeneficiarioUsaDatosNormalizados() {
        DatosBeneficiario esperado = beneficiario();

        Mockito.when(sistemaDueno.consultarBeneficiario(
                CLAVE, FECHA, "Ana Maria", "Lopez", null))
                .thenReturn(Optional.of(esperado));

        DatosBeneficiario resultado = servicio.consultar(
                " P-1 ",
                FECHA,
                " Ana Maria ",
                " Lopez ",
                " ");

        assertEquals(esperado, resultado);

        Mockito.verify(sistemaDueno).consultarBeneficiario(
                CLAVE, FECHA, "Ana Maria", "Lopez", null);
    }

    /**
     * Rechaza una consulta sin los datos obligatorios.
     */
    @Test
    void consultarRechazaDatosObligatoriosVacios() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.consultar(
                        CLAVE, FECHA, " ", "Lopez", null));

        Mockito.verifyNoInteractions(sistemaDueno);
    }
}
