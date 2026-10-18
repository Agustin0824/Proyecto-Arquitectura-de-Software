package mx.edu.uacm.is.slt.as.sip.dominio;

/**
 * Tipos de poliza y sus identificadores en el Sistema Dueno.
 *
 * @author Luis Angel
 */
public enum TipoPoliza {

    AUTO(0),
    VIDA(1),
    MEDICO(2);

    private final int codigo;

    TipoPoliza(int codigo) {
        this.codigo = codigo;
    }

    /**
     * Obtiene el identificador utilizado por el Sistema Dueño.
     *
     * @return código del tipo de póliza
     */
    public int getCodigo() {
        return codigo;
    }

    /**
     * Obtiene el tipo correspondiente a un código.
     *
     * @param codigo identificador del Sistema Dueño
     * @return tipo de póliza correspondiente
     * @throws ReglaNegocioException si el código no existe
     */
    public static TipoPoliza desdeCodigo(int codigo) {
        for (TipoPoliza tipo : values()) {
            if (tipo.codigo == codigo) {
                return tipo;
            }
        }
        throw new ReglaNegocioException(
                "Tipo de póliza inválido: " + codigo);
    }
}
