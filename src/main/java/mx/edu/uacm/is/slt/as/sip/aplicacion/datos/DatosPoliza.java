package mx.edu.uacm.is.slt.as.sip.aplicacion.datos;

import java.math.BigDecimal;

/**
 * Datos de una poliza que viajan entre capas.
 *
 * @param clave clave de la poliza
 * @param tipo tipo de poliza
 * @param montoAsegurado monto asegurado
 * @param descripcion descripcion
 * @param curpCliente CURP del cliente titular
 */
public record DatosPoliza(String clave, String tipo, BigDecimal montoAsegurado,
        String descripcion, String curpCliente) {
}
