package mx.edu.uacm.is.slt.as.sip.aplicacion.datos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos de un beneficiario que viajan entre capas.
 *
 * @param id identificador local, opcional para datos remotos
 * @param nombres nombres del beneficiario
 * @param primerApellido primer apellido
 * @param segundoApellido segundo apellido, opcional
 * @param fechaNacimiento fecha de nacimiento
 * @param porcentaje porcentaje de beneficio
 * @param clavePoliza identificador de la poliza
 */
public record DatosBeneficiario(
        Long id,
        String nombres,
        String primerApellido,
        String segundoApellido,
        LocalDate fechaNacimiento,
        BigDecimal porcentaje,
        String clavePoliza) {
}