package mx.edu.uacm.is.slt.as.sip.interfaz;

/**
 * Error al usar la API SIP desde la interfaz: la API respondio con error o no
 * fue posible comunicarse con ella.
 */
public class ErrorSipApi extends RuntimeException {

    /** Codigo cuando la API SIP no responde o la conexion falla. */
    public static final String NO_DISPONIBLE = "SIP-NO-DISPONIBLE";

    /** Codigo cuando la respuesta de la API no se puede interpretar. */
    public static final String RESPUESTA_INVALIDA = "SIP-RESPUESTA-INVALIDA";

    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final int estadoHttp;

    /**
     * Crea el error.
     *
     * @param codigo codigo corto (por ejemplo EX-DND o NO-ENCONTRADO)
     * @param mensaje mensaje para el usuario
     * @param estadoHttp estado HTTP recibido, o 0 si no hubo respuesta
     * @param causa causa original, opcional
     */
    public ErrorSipApi(String codigo, String mensaje, int estadoHttp,
            Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
        this.estadoHttp = estadoHttp;
    }

    /**
     * Obtiene el codigo corto del error.
     *
     * @return codigo del error
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Obtiene el estado HTTP de la respuesta.
     *
     * @return estado HTTP, o 0 si no hubo respuesta
     */
    public int getEstadoHttp() {
        return estadoHttp;
    }
}
