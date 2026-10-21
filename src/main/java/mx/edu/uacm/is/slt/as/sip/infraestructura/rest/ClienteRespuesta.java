package mx.edu.uacm.is.slt.as.sip.infraestructura.rest;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;

/**
 * Representa los datos de cliente recibidos del Sistema Dueno.
 *
 * @param nombres nombres del cliente
 * @param primerApellido primer apellido
 * @param segundoApellido segundo apellido, opcional
 * @param direccion direccion del cliente
 * @param curp identificador del cliente
 * @param fechaNacimiento fecha recibida del servidor
 */
public record ClienteRespuesta(
        String nombres,
        String primerApellido,
        String segundoApellido,
        String direccion,
        String curp,
        String fechaNacimiento) {

    /**
     * Convierte la respuesta a los datos de aplicacion.
     *
     * @return datos del cliente con su fecha de nacimiento
     */
    public DatosCliente convertirADatos() {
        LocalDate fecha = null;

        if (fechaNacimiento != null) {
            if (fechaNacimiento.length() == 10) {
                fecha = LocalDate.parse(fechaNacimiento);
            } else {
                fecha = OffsetDateTime.parse(fechaNacimiento).toLocalDate();
            }
        }

        return new DatosCliente(curp, nombres, primerApellido,
                segundoApellido, direccion, fecha);
    }
}