package mx.edu.uacm.is.slt.as.sip.infraestructura.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import mx.edu.uacm.is.slt.as.sip.aplicacion.puerto.ConsultaPolizas;
import mx.edu.uacm.is.slt.as.sip.dominio.Poliza;
import mx.edu.uacm.is.slt.as.sip.dominio.TipoPoliza;

/**
 * Consulta las polizas mediante la API del Sistema Dueno.
 *
 * @author Luis Angel
 */
public class ConsultaPolizasRest implements ConsultaPolizas {

    private final RestClient cliente;

    /**
     * Crea el adaptador con el cliente HTTP configurado.
     *
     * @param cliente cliente HTTP con la URL del Sistema Dueno
     */
    public ConsultaPolizasRest(RestClient cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente HTTP es obligatorio.");
        }
        this.cliente = cliente;
    }

    @Override
    public Optional<Poliza> buscarPorClave(String clave) {
        try {
            PolizaRespuesta respuesta = cliente.get()
                    .uri("/poliza/{clave}", clave)
                    .retrieve()
                    .body(PolizaRespuesta.class);

            return Optional.ofNullable(respuesta)
                    .map(PolizaRespuesta::convertirADominio);
        } catch (HttpClientErrorException.NotFound excepcion) {
            return Optional.empty();
        }
    }

    @Override
    public List<Poliza> consultarTodas() {
        return consultarListado("/polizas");
    }

    @Override
    public List<Poliza> buscarPorCurp(String curp) {
        return consultarListado("/polizas/{curp}", curp);
    }

    @Override
    public List<Poliza> buscarPorTipo(TipoPoliza tipo) {
        return consultarListado("/polizas/{tipo}", tipo.getCodigo());
    }

    /**
     * Recupera un listado y convierte sus registros al dominio.
     *
     * @param ruta ruta de la consulta
     * @param parametros valores de las variables de la ruta
     * @return polizas recibidas
     */
    private List<Poliza> consultarListado(String ruta, Object... parametros) {
        PolizaRespuesta[] respuestas = cliente.get()
                .uri(ruta, parametros)
                .retrieve()
                .body(PolizaRespuesta[].class);

        if (respuestas == null) {
            throw new RestClientException(
                    "El Sistema Dueno devolvio una respuesta sin listado.");
        }

        return Arrays.stream(respuestas)
                .map(PolizaRespuesta::convertirADominio)
                .toList();
    }
}