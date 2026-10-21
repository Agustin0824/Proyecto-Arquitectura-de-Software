package mx.edu.uacm.is.slt.as.sip.adaptadores.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.sip.aplicacion.servicios.ClienteServicio;

/**
 * Operaciones REST de clientes.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteServicio servicio;

    /**
     * Crea el controlador.
     *
     * @param servicio casos de uso de clientes
     */
    public ClienteController(ClienteServicio servicio) {
        this.servicio = servicio;
    }

    /**
     * Alta de cliente.
     *
     * @param datos datos del cliente
     * @return el cliente registrado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DatosCliente registrar(@RequestBody DatosCliente datos) {
        return servicio.registrar(datos);
    }

    /**
     * Modificacion de cliente.
     *
     * @param curp CURP del cliente
     * @param datos datos nuevos
     * @return el cliente modificado
     */
    @PutMapping("/{curp}")
    public DatosCliente modificar(@PathVariable String curp,
            @RequestBody DatosCliente datos) {
        return servicio.modificar(curp, datos);
    }

    /**
     * Consulta de cliente.
     *
     * @param curp CURP del cliente
     * @return el cliente
     */
    @GetMapping("/{curp}")
    public DatosCliente consultar(@PathVariable String curp) {
        return servicio.consultar(curp);
    }
}