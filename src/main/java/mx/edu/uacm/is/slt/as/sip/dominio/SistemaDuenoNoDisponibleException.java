package mx.edu.uacm.is.slt.as.sip.dominio;

/**
 * Indica un fallo al comunicarse con el Sistema Dueno.
 */
public class SistemaDuenoNoDisponibleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion conservando la causa original.
     *
     * @param causa fallo de comunicacion
     */
    public SistemaDuenoNoDisponibleException(Throwable causa) {
        super("No fue posible completar la comunicacion con el Sistema Dueno.", causa);
    }
}