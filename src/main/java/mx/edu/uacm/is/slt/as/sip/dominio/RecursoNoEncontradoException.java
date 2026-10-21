package mx.edu.uacm.is.slt.as.sip.dominio;

/**
 * Indica que el recurso solicitado no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion con la descripcion del problema.
     *
     * @param mensaje descripcion del recurso no encontrado
     */
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}