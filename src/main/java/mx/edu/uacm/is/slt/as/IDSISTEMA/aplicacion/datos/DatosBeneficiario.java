package mx.edu.uacm.is.slt.as.IDSISTEMA.aplicacion.datos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos de un beneficiario que viajan entre capas.
 *
 * @param id identificador del beneficiario
 * @param nombreCompleto nombre completo
 * @param fechaNacimiento fecha de nacimiento
 * @param porcentaje porcentaje de beneficio
 * @param clavePoliza clave de la poliza
 */
public record DatosBeneficiario(Long id, String nombreCompleto, LocalDate fechaNacimiento,
        BigDecimal porcentaje, String clavePoliza) {
}
