package mx.edu.uacm.is.slt.as.sip.aplicacion.datos;

import java.time.LocalDate;

/**
 * Datos de un cliente que viajan entre capas.
 *
 * @param curp identificador del cliente
 * @param nombres nombres del cliente
 * @param primerApellido primer apellido
 * @param segundoApellido segundo apellido, opcional
 * @param direccion direccion del cliente
 * @param fechaNacimiento fecha de nacimiento
 */
public record DatosCliente(
        String curp,
        String nombres,
        String primerApellido,
        String segundoApellido,
        String direccion,
        LocalDate fechaNacimiento) {
}