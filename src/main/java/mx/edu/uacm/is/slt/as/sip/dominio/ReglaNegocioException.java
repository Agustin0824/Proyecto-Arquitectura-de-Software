package mx.edu.uacm.is.slt.as.sip.dominio;

/**
 * Indica que una operacion incumple una regla de negocio.
 *
 * @author Luis Angel
 */
public class ReglaNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con la descripción del problema.
     *
     * @param mensaje descripción de la regla incumplida
     */
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
