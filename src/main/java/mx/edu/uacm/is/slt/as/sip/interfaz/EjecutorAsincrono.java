package mx.edu.uacm.is.slt.as.sip.interfaz;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * Ejecuta operaciones de la API SIP sin bloquear la ventana.
 *
 * <p>Informa en la barra de estado que la operacion esta en curso, desactiva
 * los controles indicados y rechaza una segunda operacion mientras haya una
 * activa, para evitar envios duplicados. Debe usarse solo desde el hilo de
 * JavaFX; los resultados tambien se entregan en ese hilo.</p>
 */
public class EjecutorAsincrono {

    private final Label estado;
    private boolean enCurso;

    /**
     * Crea el ejecutor.
     *
     * @param estado barra de estado donde se informa el avance
     */
    public EjecutorAsincrono(Label estado) {
        this.estado = estado;
    }

    /**
     * Indica si hay una operacion en curso.
     *
     * @return verdadero mientras una operacion no termina
     */
    public boolean isEnCurso() {
        return enCurso;
    }

    /**
     * Inicia una operacion si no hay otra en curso.
     *
     * @param nombre nombre de la operacion para los mensajes
     * @param operacion inicia la solicitud a la API SIP
     * @param alExito recibe el resultado, en el hilo de JavaFX
     * @param alError recibe el error, en el hilo de JavaFX
     * @param controles controles que se desactivan mientras dura la operacion
     * @param <T> tipo del resultado
     * @return falso si se rechazo por haber otra operacion en curso
     */
    public <T> boolean ejecutar(String nombre,
            Supplier<CompletableFuture<T>> operacion,
            Consumer<T> alExito, Consumer<ErrorSipApi> alError,
            Node... controles) {
        if (enCurso) {
            estado.setText(nombre + ": espera a que termine la operacion en curso.");
            return false;
        }

        enCurso = true;
        estado.setText(nombre + ": en curso...");
        for (Node control : controles) {
            control.setDisable(true);
        }

        CompletableFuture<T> futuro;
        try {
            futuro = operacion.get();
        } catch (RuntimeException excepcion) {
            futuro = CompletableFuture.failedFuture(excepcion);
        }

        futuro.whenComplete((resultado, fallo) -> Platform.runLater(() -> {
            enCurso = false;
            for (Node control : controles) {
                control.setDisable(false);
            }

            if (fallo == null) {
                estado.setText(nombre + ": listo.");
                alExito.accept(resultado);
            } else {
                ErrorSipApi error = convertir(fallo);
                estado.setText(nombre + ": " + error.getCodigo() + " - "
                        + error.getMessage());
                alError.accept(error);
            }
        }));
        return true;
    }

    /**
     * Convierte cualquier fallo en un ErrorSipApi.
     *
     * @param fallo fallo recibido
     * @return error de la API SIP
     */
    private static ErrorSipApi convertir(Throwable fallo) {
        Throwable causa = fallo;
        while (causa instanceof CompletionException && causa.getCause() != null) {
            causa = causa.getCause();
        }
        if (causa instanceof ErrorSipApi error) {
            return error;
        }
        return new ErrorSipApi(ErrorSipApi.NO_DISPONIBLE,
                "Ocurrio un error inesperado: " + causa.getMessage(), 0, causa);
    }
}
