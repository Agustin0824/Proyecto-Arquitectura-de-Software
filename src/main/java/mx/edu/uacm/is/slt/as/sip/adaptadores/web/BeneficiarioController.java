package mx.edu.uacm.is.slt.as.sip.adaptadores.web;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.sip.aplicacion.servicios.BeneficiarioServicio;

/**
 * Expone las operaciones de beneficiarios de una poliza.
 */
@RestController
@RequestMapping("/api/polizas/{clave}/beneficiarios")
public class BeneficiarioController {

    private final BeneficiarioServicio servicio;

    /**
     * Crea el controlador con su servicio.
     *
     * @param servicio operaciones de beneficiarios
     */
    public BeneficiarioController(BeneficiarioServicio servicio) {
        this.servicio = servicio;
    }

    /**
     * Consulta un beneficiario mediante sus datos de identificacion.
     *
     * @param clave identificador de la poliza
     * @param fechaNacimiento fecha en formato yyyy-MM-dd
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido
     * @param segundoApellido segundo apellido, opcional
     * @return beneficiario encontrado
     */
    @GetMapping
    public DatosBeneficiario consultar(
            @PathVariable("clave") String clave,
            @RequestParam("fechaNacimiento")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
            @RequestParam("nombres") String nombres,
            @RequestParam("primerApellido") String primerApellido,
            @RequestParam(value = "segundoApellido", required = false)
            String segundoApellido) {
        return servicio.consultar(clave, fechaNacimiento, nombres,
                primerApellido, segundoApellido);
    }

    /**
     * Expone el registro pendiente de implementacion.
     *
     * @param clave identificador de la poliza
     * @param datos datos del beneficiario
     * @return beneficiario cuando se implemente la operacion
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DatosBeneficiario registrar(
            @PathVariable("clave") String clave,
            @RequestBody DatosBeneficiario datos) {
        return servicio.registrar(clave, datos);
    }
}