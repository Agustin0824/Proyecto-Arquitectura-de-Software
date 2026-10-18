package mx.edu.uacm.is.slt.as.sip.infraestructura.rest;

import java.math.BigDecimal;
import mx.edu.uacm.is.slt.as.sip.dominio.Poliza;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.sip.dominio.TipoPoliza;

/**
 * Representa una poliza recibida del Sistema Dueno.
 *
 * @param clave identificador de la poliza
 * @param tipo codigo del tipo de seguro
 * @param monto monto asegurado
 * @param descripcion descripcion del seguro
 * @param curpCliente CURP del titular
 * @author Luis Angel
 */
public record PolizaRespuesta(
        String clave,
        Integer tipo,
        BigDecimal monto,
        String descripcion,
        String curpCliente) {

    /**
     * Convierte la respuesta al modelo de dominio.
     *
     * @return poliza con sus datos validados
     */
    public Poliza convertirADominio() {
        if (tipo == null) {
            throw new ReglaNegocioException(
                    "El Sistema Dueno devolvio una poliza sin tipo.");
        }

        return new Poliza(
                clave,
                TipoPoliza.desdeCodigo(tipo),
                monto,
                descripcion,
                curpCliente);
    }
}