package mx.edu.uacm.is.slt.as.sip.dominio;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa una poliza de seguro y valida sus datos.
 *
 * @author Luis Angel
 */
@Entity
@Table(name = "poliza")
public class Poliza {

    @Id
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPoliza tipo;

    @Column(name = "monto_asegurado", nullable = false)
    private BigDecimal montoAsegurado;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "curp_cliente", nullable = false)
    private String curpCliente;

    /**
     * Constructor requerido por Hibernate.
     */
    protected Poliza() {
    }

    /**
     * Crea una poliza con los datos proporcionados.
     *
     * @param clave identificador de la poliza
     * @param tipo tipo de seguro
     * @param montoAsegurado monto mayor que cero
     * @param descripcion descripcion del seguro
     * @param curpCliente CURP del titular
     */
    public Poliza(String clave, TipoPoliza tipo, BigDecimal montoAsegurado,
            String descripcion, String curpCliente) {
        if (clave == null || clave.isBlank()) {
            throw new ReglaNegocioException("La clave de la poliza es obligatoria.");
        }
        if (curpCliente == null || curpCliente.isBlank()) {
            throw new ReglaNegocioException("La CURP del titular es obligatoria.");
        }

        this.clave = clave.strip();
        this.curpCliente = curpCliente.strip();
        actualizar(tipo, montoAsegurado, descripcion);
    }

    /**
     * Actualiza los datos modificables de la poliza.
     *
     * @param nuevoTipo tipo de seguro
     * @param nuevoMonto monto mayor que cero
     * @param nuevaDescripcion descripcion del seguro
     */
    public final void actualizar(TipoPoliza nuevoTipo, BigDecimal nuevoMonto,
            String nuevaDescripcion) {
        if (nuevoTipo == null) {
            throw new ReglaNegocioException("El tipo de poliza es obligatorio.");
        }
        if (nuevoMonto == null || nuevoMonto.signum() <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor que cero.");
        }
        if (nuevaDescripcion == null || nuevaDescripcion.isBlank()) {
            throw new ReglaNegocioException("La descripcion es obligatoria.");
        }

        this.tipo = nuevoTipo;
        this.montoAsegurado = nuevoMonto;
        this.descripcion = nuevaDescripcion.strip();
    }

    public String getClave() {
        return clave;
    }

    public TipoPoliza getTipo() {
        return tipo;
    }

    public BigDecimal getMontoAsegurado() {
        return montoAsegurado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCurpCliente() {
        return curpCliente;
    }
}