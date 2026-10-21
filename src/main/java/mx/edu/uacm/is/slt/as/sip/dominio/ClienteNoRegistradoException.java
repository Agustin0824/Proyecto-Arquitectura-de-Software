package mx.edu.uacm.is.slt.as.sip.dominio;

/**
 * Indica que el cliente solicitado no esta registrado.
 */
public class ClienteNoRegistradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion para el cliente solicitado.
     *
     * @param curp identificador del cliente
     */
    public ClienteNoRegistradoException(String curp) {
        super("El cliente con CURP " + curp + " no esta registrado.");
    }
}