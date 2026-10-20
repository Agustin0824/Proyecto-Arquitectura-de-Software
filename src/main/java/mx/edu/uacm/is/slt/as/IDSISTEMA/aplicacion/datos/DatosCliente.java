package mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.datos;

import java.time.LocalDate;

/**
 * Datos de un cliente que viajan entre capas.
 *
 * @param curp CURP del cliente
 * @param nombre nombre completo
 * @param direccion direccion
 * @param fechaNacimiento fecha de nacimiento
 */
public record DatosCliente(String curp, String nombre, String direccion,
        LocalDate fechaNacimiento) {
}
