package mx.edu.uacm.is.slt.as.sip.adaptadores.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.sip.aplicacion.servicios.BeneficiarioServicio;

/**
 * Operaciones REST de beneficiarios de una poliza.
 */
@RestController
@RequestMapping("/api/polizas/{clave}/beneficiarios")
public class BeneficiarioController {

    private final BeneficiarioServicio servicio;

    /**
     * Crea el controlador.
     *
     * @param servicio casos de uso de beneficiarios
     */
    public BeneficiarioController(BeneficiarioServicio servicio) {
        this.servicio = servicio;
    }

    /**
     * Lista los beneficiarios de una poliza.
     *
     * @param clave clave de la poliza
     * @return beneficiarios registrados
     */
    @GetMapping
    public List<DatosBeneficiario> listar(@PathVariable String clave) {
        return servicio.listar(clave);
    }

    /**
     * Consulta un beneficiario.
     *
     * @param clave clave de la poliza
     * @param id identificador del beneficiario
     * @return el beneficiario
     */
    @GetMapping("/{id}")
    public DatosBeneficiario consultar(@PathVariable String clave,
            @PathVariable Long id) {
        return servicio.consultar(clave, id);
    }

    /**
     * Registra un beneficiario.
     *
     * @param clave clave de la poliza
     * @param datos datos del beneficiario
     * @return el beneficiario registrado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DatosBeneficiario registrar(@PathVariable String clave,
            @RequestBody DatosBeneficiario datos) {
        return servicio.registrar(clave, datos);
    }
}