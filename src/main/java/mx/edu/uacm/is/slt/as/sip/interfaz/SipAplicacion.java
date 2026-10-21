package mx.edu.uacm.is.slt.as.sip.interfaz;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Muestra ejecutable de la propuesta de interfaz del SIP.
 */
public class SipAplicacion extends Application {

    private final Label estado = new Label(
            "Prototipo de practica 2. Selecciona una operacion.");

    /**
     * Inicia la muestra de interfaz.
     *
     * @param args argumentos de ejecucion
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage ventana) {
        Label titulo = new Label("SIP");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitulo = new Label("Sistema Intermediario de Polizas");
        subtitulo.setStyle("-fx-font-size: 16px;");

        Label aviso = new Label(
                "Muestra de interfaz: las operaciones no estan conectadas en esta ventana.");

        VBox encabezado = new VBox(6, titulo, subtitulo, aviso);
        encabezado.setPadding(new Insets(20));
        encabezado.setStyle("-fx-background-color: #e8eef7;");

        TabPane pestanas = new TabPane(
                crearClientes(), crearPolizas(), crearBeneficiarios());
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        estado.setWrapText(true);
        estado.setPadding(new Insets(15));

        BorderPane raiz = new BorderPane();
        raiz.setTop(encabezado);
        raiz.setCenter(pestanas);
        raiz.setBottom(estado);

        ventana.setTitle("SIP - Prototipo de practica 2");
        ventana.setScene(new Scene(raiz, 950, 700));
        ventana.setMinWidth(750);
        ventana.setMinHeight(600);
        ventana.show();
    }

    /**
     * Construye la propuesta de operaciones de clientes.
     *
     * @return pestana de clientes
     */
    private Tab crearClientes() {
        GridPane formulario = crearFormulario();
        agregarCampo(formulario, 0, "CURP");
        agregarCampo(formulario, 1, "Nombres");
        agregarCampo(formulario, 2, "Primer apellido");
        agregarCampo(formulario, 3, "Segundo apellido (opcional)");
        agregarCampo(formulario, 4, "Direccion");
        agregarCampo(formulario, 5, "Fecha de nacimiento (aaaa-mm-dd)");

        HBox acciones = new HBox(10,
                crearBoton("Consultar cliente"),
                crearBoton("Registrar cliente"),
                crearBoton("Modificar cliente"));

        return new Tab("Clientes", crearContenido(
                "Datos del cliente", formulario, acciones));
    }

    /**
     * Construye la propuesta de operaciones de polizas.
     *
     * @return pestana de polizas
     */
    private Tab crearPolizas() {
        GridPane formulario = crearFormulario();
        agregarCampo(formulario, 0, "Clave de poliza");
        agregarCampo(formulario, 1, "CURP del titular");
        agregarCampo(formulario, 2, "Tipo (0: auto, 1: vida, 2: medico)");
        agregarCampo(formulario, 3, "Monto asegurado");
        agregarCampo(formulario, 4, "Descripcion");

        HBox consultas = new HBox(10,
                crearBoton("Consultar por clave"),
                crearBoton("Consultar listado y filtros"));

        HBox escrituras = new HBox(10,
                crearBoton("Registrar poliza"),
                crearBoton("Modificar poliza"),
                crearBoton("Eliminar poliza"));

        VBox contenido = crearContenido(
                "Consulta y gestion de polizas", formulario, consultas);
        contenido.getChildren().add(escrituras);

        return new Tab("Polizas", contenido);
    }

    /**
     * Construye la propuesta de operaciones de beneficiarios.
     *
     * @return pestana de beneficiarios
     */
    private Tab crearBeneficiarios() {
        GridPane formulario = crearFormulario();
        agregarCampo(formulario, 0, "Clave de poliza");
        agregarCampo(formulario, 1, "Nombres");
        agregarCampo(formulario, 2, "Primer apellido");
        agregarCampo(formulario, 3, "Segundo apellido (opcional)");
        agregarCampo(formulario, 4, "Fecha de nacimiento (aaaa-mm-dd)");
        agregarCampo(formulario, 5, "Porcentaje de beneficio");

        HBox acciones = new HBox(10,
                crearBoton("Consultar beneficiario"),
                crearBoton("Registrar beneficiario"));

        return new Tab("Beneficiarios", crearContenido(
                "Beneficiario de una poliza", formulario, acciones));
    }

    /**
     * Crea una distribucion para etiquetas y campos.
     *
     * @return formulario vacio
     */
    private GridPane crearFormulario() {
        GridPane formulario = new GridPane();
        formulario.setHgap(15);
        formulario.setVgap(12);
        return formulario;
    }

    /**
     * Agrega un campo a un formulario.
     *
     * @param formulario formulario de destino
     * @param fila posicion del campo
     * @param texto etiqueta del campo
     */
    private void agregarCampo(GridPane formulario, int fila, String texto) {
        TextField campo = new TextField();
        campo.setPromptText(texto);
        campo.setPrefWidth(420);
        formulario.add(new Label(texto), 0, fila);
        formulario.add(campo, 1, fila);
    }

    /**
     * Crea un boton que identifica la operacion propuesta.
     *
     * @param texto nombre de la operacion
     * @return boton de muestra
     */
    private Button crearBoton(String texto) {
        Button boton = new Button(texto);
        boton.setOnAction(evento -> estado.setText(
                texto + ": propuesta visual; sin ejecucion desde esta muestra."));
        return boton;
    }

    /**
     * Construye el contenido de una pestana.
     *
     * @param titulo titulo de la seccion
     * @param formulario campos de la seccion
     * @param acciones botones disponibles
     * @return contenido de la pestana
     */
    private VBox crearContenido(String titulo, GridPane formulario, HBox acciones) {
        Label encabezado = new Label(titulo);
        encabezado.setStyle("-fx-font-size: 19px; -fx-font-weight: bold;");

        TextArea resultados = new TextArea();
        resultados.setEditable(false);
        resultados.setPromptText("Area propuesta para resultados y mensajes.");
        resultados.setPrefRowCount(5);

        VBox contenido = new VBox(18,
                encabezado, formulario, acciones, resultados);
        contenido.setPadding(new Insets(20));
        return contenido;
    }
}