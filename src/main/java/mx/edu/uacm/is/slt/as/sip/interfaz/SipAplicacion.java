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
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;

/**
 * Muestra ejecutable de la propuesta de interfaz del SIP.
 */
public class SipAplicacion extends Application {

    private final Label estado = new Label(
            "Prototipo de practica 2. Selecciona una operacion.");
    private final ClienteSipApi api = new ClienteSipApi();
    private final EjecutorAsincrono ejecutor = new EjecutorAsincrono(estado);
    private final TextField campoUrlApi = new TextField(api.getUrlBase());

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
                "Muestra de interfaz: solo la consulta de poliza por clave esta conectada.");

        campoUrlApi.setPrefWidth(320);
        HBox direccionApi = new HBox(10, new Label("API SIP:"), campoUrlApi);

        VBox encabezado = new VBox(6, titulo, subtitulo, aviso, direccionApi);
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
        TextField campoClave = agregarCampo(formulario, 0, "Clave de poliza");
        TextField campoCurp = agregarCampo(formulario, 1, "CURP del titular");
        TextField campoTipo = agregarCampo(formulario, 2,
                "Tipo (0: auto, 1: vida, 2: medico)");
        TextField campoMonto = agregarCampo(formulario, 3, "Monto asegurado");
        TextField campoDescripcion = agregarCampo(formulario, 4, "Descripcion");

        Button consultarClave = new Button("Consultar por clave");
        consultarClave.setOnAction(evento -> consultarPolizaPorClave(
                campoClave, campoCurp, campoTipo, campoMonto, campoDescripcion,
                consultarClave));

        HBox consultas = new HBox(10,
                consultarClave,
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
     * Consulta una poliza por clave en la API SIP y muestra el resultado.
     * Es el ejemplo del mecanismo comun: cada modulo conecta sus controles
     * igual, con ClienteSipApi y EjecutorAsincrono.
     *
     * @param campoClave campo con la clave a consultar
     * @param campoCurp campo donde se muestra la CURP
     * @param campoTipo campo donde se muestra el tipo
     * @param campoMonto campo donde se muestra el monto
     * @param campoDescripcion campo donde se muestra la descripcion
     * @param boton boton de la operacion, desactivado mientras dura
     */
    private void consultarPolizaPorClave(TextField campoClave,
            TextField campoCurp, TextField campoTipo, TextField campoMonto,
            TextField campoDescripcion, Button boton) {
        try {
            api.configurarUrl(campoUrlApi.getText());
        } catch (IllegalArgumentException excepcion) {
            estado.setText(excepcion.getMessage());
            return;
        }

        String clave = campoClave.getText();
        ejecutor.<DatosPoliza>ejecutar("Consultar poliza",
                () -> api.consultarPoliza(clave),
                poliza -> {
                    campoCurp.setText(poliza.curpCliente());
                    campoTipo.setText(poliza.tipo());
                    campoMonto.setText(poliza.montoAsegurado() == null
                            ? "" : poliza.montoAsegurado().toPlainString());
                    campoDescripcion.setText(poliza.descripcion());
                },
                error -> { },
                boton, campoClave);
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
     * @return campo creado
     */
    private TextField agregarCampo(GridPane formulario, int fila, String texto) {
        TextField campo = new TextField();
        campo.setPromptText(texto);
        campo.setPrefWidth(420);
        formulario.add(new Label(texto), 0, fila);
        formulario.add(campo, 1, fila);
        return campo;
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