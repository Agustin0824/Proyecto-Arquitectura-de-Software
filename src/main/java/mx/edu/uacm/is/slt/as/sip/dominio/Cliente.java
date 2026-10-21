package mx.edu.uacm.is.slt.as.sip.dominio;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa al cliente titular de una poliza.
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    private String curp;

    @Column(nullable = false)
    private String nombres;

    @Column(name = "primer_apellido", nullable = false)
    private String primerApellido;

    @Column(name = "segundo_apellido")
    private String segundoApellido;

    @Column(nullable = false)
    private String direccion;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    /**
     * Constructor requerido por Hibernate.
     */
    protected Cliente() {
    }

    /**
     * Crea un cliente con sus datos validados.
     *
     * @param curp identificador del cliente
     * @param nombres nombres del cliente
     * @param primerApellido primer apellido
     * @param segundoApellido segundo apellido, opcional
     * @param direccion direccion del cliente
     * @param fechaNacimiento fecha de nacimiento
     */
    public Cliente(String curp, String nombres, String primerApellido,
            String segundoApellido, String direccion, LocalDate fechaNacimiento) {
        if (curp == null || curp.isBlank()) {
            throw new ReglaNegocioException("La CURP es obligatoria.");
        }

        this.curp = curp.strip();
        actualizar(nombres, primerApellido, segundoApellido, direccion, fechaNacimiento);
    }

    /**
     * Actualiza los datos del cliente conservando su CURP.
     *
     * @param nuevosNombres nombres del cliente
     * @param nuevoPrimerApellido primer apellido
     * @param nuevoSegundoApellido segundo apellido, opcional
     * @param nuevaDireccion direccion del cliente
     * @param nuevaFechaNacimiento fecha de nacimiento
     */
    public final void actualizar(String nuevosNombres, String nuevoPrimerApellido,
            String nuevoSegundoApellido, String nuevaDireccion,
            LocalDate nuevaFechaNacimiento) {
        if (nuevosNombres == null || nuevosNombres.isBlank()) {
            throw new ReglaNegocioException("Los nombres son obligatorios.");
        }
        if (nuevoPrimerApellido == null || nuevoPrimerApellido.isBlank()) {
            throw new ReglaNegocioException("El primer apellido es obligatorio.");
        }
        if (nuevaDireccion == null || nuevaDireccion.isBlank()) {
            throw new ReglaNegocioException("La direccion es obligatoria.");
        }
        if (nuevaFechaNacimiento == null) {
            throw new ReglaNegocioException("La fecha de nacimiento es obligatoria.");
        }
        if (nuevaFechaNacimiento.isAfter(LocalDate.now())) {
            throw new ReglaNegocioException(
                    "La fecha de nacimiento no puede ser futura.");
        }

        this.nombres = nuevosNombres.strip();
        this.primerApellido = nuevoPrimerApellido.strip();
        this.segundoApellido = nuevoSegundoApellido == null
                ? null : nuevoSegundoApellido.strip();
        this.direccion = nuevaDireccion.strip();
        this.fechaNacimiento = nuevaFechaNacimiento;
    }

    public String getCurp() {
        return curp;
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

    public String getDireccion() {
        return direccion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
}