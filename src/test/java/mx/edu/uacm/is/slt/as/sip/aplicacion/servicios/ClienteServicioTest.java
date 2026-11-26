package mx.edu.uacm.is.slt.as.sip.aplicacion.servicios;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.ClienteRepositorio;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puertos.SistemaDueno;
import mx.edu.uacm.is.slt.as.sip.dominio.Cliente;
import mx.edu.uacm.is.slt.as.sip.dominio.ClienteNoRegistradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;

/**
 * Comprueba las operaciones del servicio de clientes.
 */
class ClienteServicioTest {

    private static final String CURP = "broe550101hdfabc00";

    private final ClienteRepositorio clientes =
            Mockito.mock(ClienteRepositorio.class);

    private final SistemaDueno remoto =
            Mockito.mock(SistemaDueno.class);

    private final ClienteServicio servicio =
            new ClienteServicio(clientes, remoto);

    /**
     * Confirma el registro mediante una consulta posterior.
     */
    @Test
    void registrarConfirmaElResultadoRemoto() {
        DatosCliente solicitud = datosValidos();

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.empty(), Optional.of(solicitud));

        Mockito.when(clientes.findById(CURP))
                .thenReturn(Optional.empty());

        guardarLocal();

        DatosCliente resultado = servicio.registrar(solicitud);

        Assertions.assertEquals(solicitud, resultado);

        Mockito.verify(remoto).registrarCliente(solicitud);
        Mockito.verify(remoto, Mockito.times(2))
                .consultarCliente(CURP);
        Mockito.verify(clientes).save(Mockito.any(Cliente.class));
    }

    /**
     * Impide registrar un cliente que ya existe.
     */
    @Test
    void registrarRechazaClienteExistente() {
        DatosCliente solicitud = datosValidos();

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.of(solicitud));

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(solicitud));

        Mockito.verify(remoto, Mockito.never())
                .registrarCliente(Mockito.any(DatosCliente.class));

        Mockito.verifyNoInteractions(clientes);
    }

    /**
     * Rechaza datos invalidos antes de consultar al servidor.
     */
    @Test
    void registrarRechazaDatosInvalidos() {
        DatosCliente solicitud = new DatosCliente(
                CURP, " ", "Lopez", "Perez",
                "Direccion de prueba", LocalDate.of(1990, 1, 1));

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(solicitud));

        Mockito.verifyNoInteractions(remoto);
        Mockito.verifyNoInteractions(clientes);
    }

    /**
     * Rechaza un alta cuyos datos no coinciden con la consulta posterior.
     */
    @Test
    void registrarRechazaConfirmacionInconsistente() {
        DatosCliente solicitud = datosValidos();

        DatosCliente diferente = new DatosCliente(
                CURP, "Ana Maria", "Lopez", "Perez",
                "Otra direccion", LocalDate.of(1990, 1, 1));

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.empty(), Optional.of(diferente));

        Mockito.when(clientes.findById(CURP))
                .thenReturn(Optional.empty());

        guardarLocal();

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(solicitud));

        Mockito.verify(remoto).registrarCliente(solicitud);
    }

    /**
     * Confirma una modificacion mediante consulta posterior.
     */
    @Test
    void modificarConfirmaElResultadoRemoto() {
        DatosCliente anterior = datosValidos();

        DatosCliente solicitud = new DatosCliente(
                CURP, "Ana Maria", "Lopez", "Perez",
                "Direccion actualizada", LocalDate.of(1990, 1, 1));

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.of(anterior), Optional.of(solicitud));

        Mockito.when(clientes.findById(CURP))
                .thenReturn(Optional.empty());

        guardarLocal();

        DatosCliente resultado = servicio.modificar(CURP, solicitud);

        Assertions.assertEquals(solicitud, resultado);

        Mockito.verify(remoto).modificarCliente(solicitud);
        Mockito.verify(remoto, Mockito.times(2))
                .consultarCliente(CURP);
        Mockito.verify(clientes).save(Mockito.any(Cliente.class));
    }

    /**
     * Impide cambiar la CURP durante una modificacion.
     */
    @Test
    void modificarRechazaCambioDeCurp() {
        DatosCliente solicitud = new DatosCliente(
                "otro550101hdfabc00", "Ana Maria", "Lopez", "Perez",
                "Direccion de prueba", LocalDate.of(1990, 1, 1));

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.modificar(CURP, solicitud));

        Mockito.verifyNoInteractions(remoto);
        Mockito.verifyNoInteractions(clientes);
    }

    /**
     * Impide modificar un cliente que no existe.
     */
    @Test
    void modificarRechazaClienteInexistente() {
        DatosCliente solicitud = datosValidos();

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(ClienteNoRegistradoException.class,
                () -> servicio.modificar(CURP, solicitud));

        Mockito.verify(remoto, Mockito.never())
                .modificarCliente(Mockito.any(DatosCliente.class));

        Mockito.verifyNoInteractions(clientes);
    }

    /**
     * Rechaza una modificacion cuyos datos no coinciden con la consulta.
     */
    @Test
    void modificarRechazaConfirmacionInconsistente() {
        DatosCliente anterior = datosValidos();

        DatosCliente solicitud = new DatosCliente(
                CURP, "Ana Maria", "Lopez", "Perez",
                "Direccion actualizada", LocalDate.of(1990, 1, 1));

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.of(anterior), Optional.of(anterior));

        Mockito.when(clientes.findById(CURP))
                .thenReturn(Optional.empty());

        guardarLocal();

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.modificar(CURP, solicitud));

        Mockito.verify(remoto).modificarCliente(solicitud);
    }

    /**
     * Consulta un cliente y crea su copia local.
     */
    @Test
    void consultarCreaLaCopiaLocal() {
        DatosCliente datos = datosValidos();

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.of(datos));

        Mockito.when(clientes.findById(CURP))
                .thenReturn(Optional.empty());

        guardarLocal();

        DatosCliente resultado = servicio.consultar(CURP);

        Assertions.assertEquals(datos, resultado);

        Mockito.verify(clientes).save(Mockito.any(Cliente.class));
    }

    /**
     * Rechaza una respuesta remota con otra CURP.
     */
    @Test
    void consultarRechazaCurpQueNoCoincide() {
        DatosCliente diferente = new DatosCliente(
                "otro550101hdfabc00", "Ana Maria", "Lopez", "Perez",
                "Direccion de prueba", LocalDate.of(1990, 1, 1));

        Mockito.when(remoto.consultarCliente(CURP))
                .thenReturn(Optional.of(diferente));

        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.consultar(CURP));

        Mockito.verifyNoInteractions(clientes);
    }

    /**
     * Rechaza una consulta sin CURP.
     */
    @Test
    void consultarRechazaCurpVacia() {
        Assertions.assertThrows(ReglaNegocioException.class,
                () -> servicio.consultar(" "));

        Mockito.verifyNoInteractions(remoto);
        Mockito.verifyNoInteractions(clientes);
    }

    /**
     * Configura el repositorio simulado para guardar la entidad recibida.
     */
    private void guardarLocal() {
        Mockito.when(clientes.save(Mockito.any(Cliente.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    /**
     * Construye datos validos para las pruebas.
     *
     * @return datos del cliente
     */
    private static DatosCliente datosValidos() {
        return new DatosCliente(
                CURP, "Ana Maria", "Lopez", "Perez",
                "Direccion de prueba", LocalDate.of(1990, 1, 1));
    }
}