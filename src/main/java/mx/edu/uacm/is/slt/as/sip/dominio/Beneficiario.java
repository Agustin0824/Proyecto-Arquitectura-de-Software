package mx.edu.uacm.is.slt.as.sip.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Representa al beneficiario de una poliza.
 */
@Entity
@Table(name = "beneficiario")
public class Beneficiario {

    public static final BigDecimal MAXIMO_TOTAL = new BigDecimal("100");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombres;

    @Column(name = "primer_apellido", nullable = false)
    private String primerApellido;

    @Column(name = "segundo_apellido")
    private String segundoApellido;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @ManyToOne(optional = false)
    @JoinColumn(name = "clave_poliza", nullable = false)
    private Poliza poliza;

    /**
     * Constructor requerido por Hibernate.
     */
    protected Beneficiario() {
    }

    /**
     * Crea un beneficiario con sus datos validados.
     *
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido
     * @param segundoApellido segundo apellido, opcional
     * @param fechaNacimiento fecha de nacimiento
     * @param porcentaje porcentaje de beneficio
     * @param poliza poliza correspondiente
     */
    public Beneficiario(String nombres, String primerApellido,
            String segundoApellido, LocalDate fechaNacimiento,
            BigDecimal porcentaje, Poliza poliza) {
        if (nombres == null || nombres.isBlank()) {
            throw new ReglaNegocioException("Los nombres son obligatorios.");
        }
        if (primerApellido == null || primerApellido.isBlank()) {
            throw new ReglaNegocioException("El primer apellido es obligatorio.");
        }
        if (fechaNacimiento == null) {
            throw new ReglaNegocioException("La fecha de nacimiento es obligatoria.");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new ReglaNegocioException(
                    "La fecha de nacimiento no puede ser futura.");
        }
        if (porcentaje == null || porcentaje.signum() <= 0
                || porcentaje.compareTo(MAXIMO_TOTAL) > 0) {
            throw new ReglaNegocioException(
                    "El porcentaje debe ser mayor que cero y no superar 100.");
        }
        BigDecimal porcentajeNormalizado = porcentaje.stripTrailingZeros();
        if (porcentajeNormalizado.scale() > 2) {
            throw new ReglaNegocioException(
                    "El porcentaje admite como maximo dos decimales.");
        }
        if (poliza == null) {
            throw new ReglaNegocioException("La poliza es obligatoria.");
        }

        this.nombres = nombres.strip();
        this.primerApellido = primerApellido.strip();
        this.segundoApellido = segundoApellido == null
                ? null : segundoApellido.strip();
        this.fechaNacimiento = fechaNacimiento;
        this.porcentaje = porcentajeNormalizado;
        this.poliza = poliza;
    }

    public Long getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public Poliza getPoliza() {
        return poliza;
    }
}