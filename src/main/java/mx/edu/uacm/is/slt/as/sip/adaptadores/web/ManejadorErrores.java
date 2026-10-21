package mx.edu.uacm.is.slt.as.sip.adaptadores.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import mx.edu.uacm.is.slt.as.sip.dominio.ClienteNoRegistradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.sip.dominio.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.sip.dominio.SistemaDuenoNoDisponibleException;

/**
 * Traduce las excepciones a respuestas HTTP.
 */
@RestControllerAdvice
public class ManejadorErrores {

    /**
     * Cuerpo de las respuestas de error.
     *
     * @param codigo codigo corto del error
     * @param mensaje mensaje para el usuario
     */
    public record Error(String codigo, String mensaje) {
    }

    /**
     * Informa que el cliente no esta registrado.
     *
     * @param excepcion excepcion recibida
     * @return respuesta HTTP 404
     */
    @ExceptionHandler(ClienteNoRegistradoException.class)
    public ResponseEntity<Error> clienteNoRegistrado(
            ClienteNoRegistradoException excepcion) {
        return responder(HttpStatus.NOT_FOUND, "EX-CNR", excepcion.getMessage());
    }

    /**
     * Informa que el Sistema Dueno no esta disponible.
     *
     * @param excepcion excepcion recibida
     * @return respuesta HTTP 503
     */
    @ExceptionHandler(SistemaDuenoNoDisponibleException.class)
    public ResponseEntity<Error> duenoNoDisponible(
            SistemaDuenoNoDisponibleException excepcion) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "EX-DND",
                excepcion.getMessage());
    }

    /**
     * Informa que el recurso solicitado no existe.
     *
     * @param excepcion excepcion recibida
     * @return respuesta HTTP 404
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Error> noEncontrado(
            RecursoNoEncontradoException excepcion) {
        return responder(HttpStatus.NOT_FOUND, "NO-ENCONTRADO",
                excepcion.getMessage());
    }

    /**
     * Informa que se incumplio una regla de negocio.
     *
     * @param excepcion excepcion recibida
     * @return respuesta HTTP 400
     */
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Error> reglaNegocio(
            ReglaNegocioException excepcion) {
        return responder(HttpStatus.BAD_REQUEST, "REGLA-NEGOCIO",
                excepcion.getMessage());
    }


     /**
     * Informa que la operacion esta pendiente de implementacion.
     *
     * @param excepcion operacion no implementada
     * @return respuesta HTTP 501
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Error> operacionNoImplementada(
            UnsupportedOperationException excepcion) {
        return responder(HttpStatus.NOT_IMPLEMENTED, "NO-IMPLEMENTADO",
                excepcion.getMessage());
    }

    private static ResponseEntity<Error> responder(HttpStatus estado,
            String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new Error(codigo, mensaje));
    }
}