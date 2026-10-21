package mx.edu.uacm.is.slt.as.sip.adaptadores.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;
import mx.edu.uacm.is.slt.as.sip.aplicacion.servicios.PolizaServicio;

/**
 * Operaciones REST de polizas.
 */
@RestController
@RequestMapping("/api/polizas")
public class PolizaController {

    private final PolizaServicio servicio;

    /**
     * Crea el controlador.
     *
     * @param servicio casos de uso de polizas
     */
    public PolizaController(PolizaServicio servicio) {
        this.servicio = servicio;
    }

    /**
     * Consulta polizas con filtros opcionales.
     *
     * @param curp CURP del cliente, opcional
     * @param tipo tipo de poliza, opcional
     * @return polizas encontradas
     */
    @GetMapping
    public List<DatosPoliza> listar(@RequestParam(required = false) String curp,
            @RequestParam(required = false) String tipo) {
        return servicio.listar(curp, tipo);
    }

    /**
     * Consulta una poliza por clave.
     *
     * @param clave clave de la poliza
     * @return la poliza
     */
    @GetMapping("/{clave}")
    public DatosPoliza consultar(@PathVariable String clave) {
        return servicio.consultar(clave);
    }

    /**
     * Registra una poliza.
     *
     * @param datos datos de la poliza
     * @return la poliza registrada
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DatosPoliza registrar(@RequestBody DatosPoliza datos) {
        return servicio.registrar(datos);
    }

    /**
     * Modifica una poliza.
     *
     * @param clave clave de la poliza
     * @param datos datos nuevos
     * @return la poliza modificada
     */
    @PutMapping("/{clave}")
    public DatosPoliza modificar(@PathVariable String clave,
            @RequestBody DatosPoliza datos) {
        return servicio.modificar(clave, datos);
    }

    /**
     * Elimina una poliza.
     *
     * @param clave clave de la poliza
     */
    @DeleteMapping("/{clave}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String clave) {
        servicio.eliminar(clave);
    }
}