
package mx.edu.uacm.is.slt.as.sip.interfaz;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosBeneficiario;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosCliente;
import mx.edu.uacm.is.slt.as.sip.aplicacion.datos.DatosPoliza;

/**
 * Interfaz JavaFX del Sistema Intermediario de Polizas.
 *
 * Conecta los modulos de clientes, polizas y beneficiarios
 * con la API REST SIP.
 */
public class SipAplicacion extends Application {

    private final Label estado = new Label(
            "SIP: selecciona una operacion.");

    private final ClienteSipApi api = new ClienteSipApi();

    private final EjecutorAsincrono ejecutor
            = new EjecutorAsincrono(estado);

    private final TextField campoUrlApi
            = new TextField(api.getUrlBase());

    // Campos de clientes.

    private TextField clienteCurp;
    private TextField clienteNombres;
    private TextField clientePrimerApellido;
    private TextField clienteSegundoApellido;
    private TextField clienteDireccion;
    private TextField clienteFecha;

    private TextArea resultadoClientes;

    // Campos de polizas.

    private TextField polizaClave;
    private TextField polizaCurp;
    private TextField polizaTipo;
    private TextField polizaMonto;
    private TextField polizaDescripcion;

    private TextField filtroCurp;
    private TextField filtroTipo;
    private TextField filtroPagina;
    private TextField filtroTamano;

    private TextArea resultadoPolizas;

    // Campos de beneficiarios.

    private TextField beneficiarioClave;
    private TextField beneficiarioNombres;
    private TextField beneficiarioPrimerApellido;
    private TextField beneficiarioSegundoApellido;
    private TextField beneficiarioFecha;
    private TextField beneficiarioPorcentaje;

    private TextArea resultadoBeneficiarios;

    /**
     * Inicia la aplicacion.
     *
     * @param args argumentos de ejecucion
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Construye la ventana principal.
     *
     * @param ventana ventana de JavaFX
     */
    @Override
    public void start(Stage ventana) {

        Label titulo = new Label("SIP");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitulo = new Label(
                "Sistema Intermediario de Polizas");
        subtitulo.setStyle("-fx-font-size: 16px;");

        Label aviso = new Label(
                "Gestion de clientes, polizas y beneficiarios.");

        Button configurar = new Button("Aplicar direccion");
        configurar.setOnAction(evento -> configurarApi());

        campoUrlApi.setPrefWidth(350);

        HBox direccionApi = new HBox(
                10, new Label("API SIP:"), campoUrlApi, configurar);

        VBox encabezado = new VBox(
                8, titulo, subtitulo, aviso, direccionApi);

        encabezado.setPadding(new Insets(20));
        encabezado.setStyle("-fx-background-color: #e8eef7;");

        TabPane pestanas = new TabPane(
                crearClientes(),
                crearPolizas(),
                crearBeneficiarios());

        pestanas.setTabClosingPolicy(
                TabPane.TabClosingPolicy.UNAVAILABLE);

        estado.setWrapText(true);
        estado.setPadding(new Insets(15));

        BorderPane raiz = new BorderPane();
        raiz.setTop(encabezado);
        raiz.setCenter(pestanas);
        raiz.setBottom(estado);

        ventana.setTitle("SIP - Sistema Intermediario de Polizas");
        ventana.setScene(new Scene(raiz, 1100, 800));
        ventana.setMinWidth(850);
        ventana.setMinHeight(650);
        ventana.show();
    }

    // MODULO DE CLIENTES

    /**
     * Construye la pestana de clientes.
     *
     * @return pestana de clientes
     */
    private Tab crearClientes() {

        GridPane formulario = crearFormulario();

        clienteCurp = agregarCampo(
                formulario, 0, "CURP");

        clienteNombres = agregarCampo(
                formulario, 1, "Nombres");

        clientePrimerApellido = agregarCampo(
                formulario, 2, "Primer apellido");

        clienteSegundoApellido = agregarCampo(
                formulario, 3, "Segundo apellido (opcional)");

        clienteDireccion = agregarCampo(
                formulario, 4, "Direccion");

        clienteFecha = agregarCampo(
                formulario, 5, "Fecha de nacimiento (aaaa-mm-dd)");

        resultadoClientes = crearResultados();

        Button consultar = new Button("Consultar cliente");
        consultar.setOnAction(
                evento -> consultarCliente(consultar));

        Button registrar = new Button("Registrar cliente");
        registrar.setOnAction(
                evento -> registrarCliente(registrar));

        Button modificar = new Button("Modificar cliente");
        modificar.setOnAction(
                evento -> modificarCliente(modificar));

        HBox acciones = new HBox(
                10, consultar, registrar, modificar);

        VBox botones = new VBox(10, acciones);

        return new Tab("Clientes", crearContenido(
                "Gestion de clientes", formulario,
                botones, resultadoClientes));
    }

    /**
     * Consulta un cliente por CURP.
     *
     * @param boton boton de consulta
     */
    private void consultarCliente(Button boton) {

        if (!configurarApi()) {
            return;
        }

        String curp = clienteCurp.getText();

        ejecutor.ejecutar(
                "Consultar cliente",
                () -> api.consultarCliente(curp),
                this::mostrarCliente,
                error -> resultadoClientes.setText(error.getMessage()),
                boton, clienteCurp);
    }

    /**
     * Registra un cliente.
     *
     * @param boton boton de registro
     */
    private void registrarCliente(Button boton) {

        if (!configurarApi()) {
            return;
        }

        DatosCliente datos;

        try {
            datos = leerCliente();
        } catch (RuntimeException excepcion) {
            estado.setText("Datos del cliente: "
                    + excepcion.getMessage());
            return;
        }

        ejecutor.ejecutar(
                "Registrar cliente",
                () -> api.registrarCliente(datos),
                this::mostrarCliente,
                error -> resultadoClientes.setText(error.getMessage()),
                boton);
    }

    /**
     * Modifica un cliente.
     *
     * @param boton boton de modificacion
     */
    private void modificarCliente(Button boton) {

        if (!configurarApi()) {
            return;
        }

        DatosCliente datos;

        try {
            datos = leerCliente();
        } catch (RuntimeException excepcion) {
            estado.setText("Datos del cliente: "
                    + excepcion.getMessage());
            return;
        }

        ejecutor.ejecutar(
                "Modificar cliente",
                () -> api.modificarCliente(datos.curp(), datos),
                this::mostrarCliente,
                error -> resultadoClientes.setText(error.getMessage()),
                boton);
    }

    /**
     * Construye el DTO de un cliente con los campos de la interfaz.
     *
     * @return datos del cliente
     */
    private DatosCliente leerCliente() {

        return new DatosCliente(
                requerido(clienteCurp, "La CURP"),
                requerido(clienteNombres, "Los nombres"),
                requerido(clientePrimerApellido, "El primer apellido"),
                opcional(clienteSegundoApellido),
                requerido(clienteDireccion, "La direccion"),
                leerFecha(clienteFecha, "La fecha de nacimiento"));
    }

    /**
     * Muestra los datos de un cliente.
     *
     * @param datos cliente consultado o registrado
     */
    private void mostrarCliente(DatosCliente datos) {

        clienteCurp.setText(datos.curp());
        clienteNombres.setText(datos.nombres());
        clientePrimerApellido.setText(datos.primerApellido());
        clienteSegundoApellido.setText(
                datos.segundoApellido() == null
                        ? "" : datos.segundoApellido());
        clienteDireccion.setText(datos.direccion());
        clienteFecha.setText(datos.fechaNacimiento().toString());

        resultadoClientes.setText(
                "Operacion realizada correctamente.\n\n" + datos);
    }

    // MODULO DE POLIZAS

    /**
     * Construye la pestana de polizas.
     *
     * @return pestana de polizas
     */
    private Tab crearPolizas() {

        GridPane formulario = crearFormulario();

        polizaClave = agregarCampo(
                formulario, 0, "Clave de poliza");

        polizaCurp = agregarCampo(
                formulario, 1, "CURP del titular");

        polizaTipo = agregarCampo(
                formulario, 2, "Tipo (0: auto, 1: vida, 2: medico)");

        polizaMonto = agregarCampo(
                formulario, 3, "Monto asegurado");

        polizaDescripcion = agregarCampo(
                formulario, 4, "Descripcion");

        GridPane filtros = crearFormulario();

        filtroCurp = agregarCampo(
                filtros, 0, "Filtro CURP (opcional)");

        filtroTipo = agregarCampo(
                filtros, 1, "Filtro tipo (opcional)");

        filtroPagina = agregarCampo(
                filtros, 2, "Pagina (opcional, desde cero)");

        filtroTamano = agregarCampo(
                filtros, 3, "Tamano de pagina (opcional)");

        resultadoPolizas = crearResultados();

        Button consultar = new Button("Consultar por clave");
        consultar.setOnAction(
                evento -> consultarPoliza(consultar));

        Button listar = new Button("Listar y filtrar");
        listar.setOnAction(
                evento -> listarPolizas(listar));

        Button registrar = new Button("Registrar poliza");
        registrar.setOnAction(
                evento -> registrarPoliza(registrar));

        Button modificar = new Button("Modificar poliza");
        modificar.setOnAction(
                evento -> modificarPoliza(modificar));

        Button eliminar = new Button("Eliminar poliza");
        eliminar.setOnAction(
                evento -> eliminarPoliza(eliminar));

        HBox consultas = new HBox(
                10, consultar, listar);

        HBox escrituras = new HBox(
                10, registrar, modificar, eliminar);

        VBox acciones = new VBox(
                12,
                new Label("Filtros y paginacion"),
                filtros,
                consultas,
                escrituras);

        return new Tab("Polizas", crearContenido(
                "Consulta y gestion de polizas",
                formulario, acciones, resultadoPolizas));
    }

    /**
     * Consulta una poliza por su clave.
     *
     * @param boton boton de consulta
     */
    private void consultarPoliza(Button boton) {

        if (!configurarApi()) {
            return;
        }

        String clave = polizaClave.getText();

        ejecutor.ejecutar(
                "Consultar poliza",
                () -> api.consultarPoliza(clave),
                this::mostrarPoliza,
                error -> resultadoPolizas.setText(error.getMessage()),
                boton, polizaClave);
    }

    /**
     * Lista polizas utilizando filtros y paginacion.
     *
     * @param boton boton de listado
     */
    private void listarPolizas(Button boton) {

        if (!configurarApi()) {
            return;
        }

        Integer pagina;
        Integer tamano;

        try {
            pagina = enteroOpcional(filtroPagina, "La pagina");
            tamano = enteroOpcional(filtroTamano, "El tamano");

            if (pagina != null && pagina < 0) {
                throw new IllegalArgumentException(
                        "La pagina debe ser mayor o igual a cero.");
            }

            if (tamano != null && tamano < 1) {
                throw new IllegalArgumentException(
                        "El tamano debe ser mayor que cero.");
            }

        } catch (RuntimeException excepcion) {
            estado.setText(excepcion.getMessage());
            return;
        }

        String curp = opcional(filtroCurp);
        String tipo = opcional(filtroTipo);

        ejecutor.ejecutar(
                "Listar polizas",
                () -> api.listarPolizas(curp, tipo, pagina, tamano),
                this::mostrarPolizas,
                error -> resultadoPolizas.setText(error.getMessage()),
                boton);
    }

    /**
     * Registra una poliza.
     *
     * @param boton boton de registro
     */
    private void registrarPoliza(Button boton) {

        if (!configurarApi()) {
            return;
        }

        DatosPoliza datos;

        try {
            datos = leerPoliza();
        } catch (RuntimeException excepcion) {
            estado.setText("Datos de la poliza: "
                    + excepcion.getMessage());
            return;
        }

        ejecutor.ejecutar(
                "Registrar poliza",
                () -> api.registrarPoliza(datos),
                this::mostrarPoliza,
                error -> resultadoPolizas.setText(error.getMessage()),
                boton);
    }

    /**
     * Modifica una poliza.
     *
     * @param boton boton de modificacion
     */
    private void modificarPoliza(Button boton) {

        if (!configurarApi()) {
            return;
        }

        DatosPoliza datos;

        try {
            datos = leerPoliza();
        } catch (RuntimeException excepcion) {
            estado.setText("Datos de la poliza: "
                    + excepcion.getMessage());
            return;
        }

        ejecutor.ejecutar(
                "Modificar poliza",
                () -> api.modificarPoliza(datos.clave(), datos),
                this::mostrarPoliza,
                error -> resultadoPolizas.setText(error.getMessage()),
                boton);
    }

    /**
     * Elimina una poliza despues de confirmar la operacion.
     *
     * @param boton boton de eliminacion
     */
    private void eliminarPoliza(Button boton) {

        if (!configurarApi()) {
            return;
        }

        String clave = polizaClave.getText();

        if (clave == null || clave.isBlank()) {
            estado.setText("Ingresa la clave de la poliza.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar baja");
        confirmacion.setHeaderText("Eliminar poliza");
        confirmacion.setContentText(
                "Se solicitara la baja de la poliza " + clave + ".");

        ButtonType respuesta = confirmacion.showAndWait()
                .orElse(ButtonType.CANCEL);

        if (respuesta != ButtonType.OK) {
            return;
        }

        ejecutor.<Void>ejecutar(
                "Eliminar poliza",
                () -> api.eliminarPoliza(clave),
                resultado -> {
                    resultadoPolizas.setText(
                            "Poliza eliminada: " + clave);
                    limpiarPoliza();
                },
                error -> resultadoPolizas.setText(error.getMessage()),
                boton, polizaClave);
    }

    /**
     * Construye el DTO de una poliza con los campos de la interfaz.
     *
     * @return datos de la poliza
     */
    private DatosPoliza leerPoliza() {

        String monto = requerido(polizaMonto, "El monto asegurado");

        return new DatosPoliza(
                requerido(polizaClave, "La clave de la poliza"),
                requerido(polizaTipo, "El tipo de poliza"),
                new BigDecimal(monto),
                requerido(polizaDescripcion, "La descripcion"),
                requerido(polizaCurp, "La CURP del titular"));
    }

    /**
     * Muestra una poliza en el formulario.
     *
     * @param datos poliza recibida
     */
    private void mostrarPoliza(DatosPoliza datos) {

        polizaClave.setText(datos.clave());
        polizaCurp.setText(datos.curpCliente());
        polizaTipo.setText(datos.tipo());

        polizaMonto.setText(
                datos.montoAsegurado() == null
                        ? "" : datos.montoAsegurado().toPlainString());

        polizaDescripcion.setText(datos.descripcion());

        resultadoPolizas.setText(
                "Operacion realizada correctamente.\n\n" + datos);
    }

    /**
     * Muestra una lista de polizas.
     *
     * @param polizas polizas recibidas
     */
    private void mostrarPolizas(List<DatosPoliza> polizas) {

        if (polizas.isEmpty()) {
            resultadoPolizas.setText(
                    "La consulta no devolvio polizas.");
            return;
        }

        StringBuilder texto = new StringBuilder();

        texto.append("Polizas encontradas: ")
                .append(polizas.size())
                .append("\n\n");

        for (DatosPoliza poliza : polizas) {
            texto.append(poliza).append("\n\n");
        }

        resultadoPolizas.setText(texto.toString());
    }

    /**
     * Limpia los campos de la poliza eliminada.
     */
    private void limpiarPoliza() {

        polizaClave.clear();
        polizaCurp.clear();
        polizaTipo.clear();
        polizaMonto.clear();
        polizaDescripcion.clear();
    }

    // MODULO DE BENEFICIARIOS

    /**
     * Construye la pestana de beneficiarios.
     *
     * @return pestana de beneficiarios
     */
    private Tab crearBeneficiarios() {

        GridPane formulario = crearFormulario();

        beneficiarioClave = agregarCampo(
                formulario, 0, "Clave de poliza");

        beneficiarioNombres = agregarCampo(
                formulario, 1, "Nombres");

        beneficiarioPrimerApellido = agregarCampo(
                formulario, 2, "Primer apellido");

        beneficiarioSegundoApellido = agregarCampo(
                formulario, 3, "Segundo apellido (opcional)");

        beneficiarioFecha = agregarCampo(
                formulario, 4, "Fecha de nacimiento (aaaa-mm-dd)");

        beneficiarioPorcentaje = agregarCampo(
                formulario, 5, "Porcentaje de beneficio");

        resultadoBeneficiarios = crearResultados();

        Button consultar = new Button("Consultar beneficiario");
        consultar.setOnAction(
                evento -> consultarBeneficiario(consultar));

        Button registrar = new Button("Registrar beneficiario");
        registrar.setOnAction(
                evento -> registrarBeneficiario(registrar));

        HBox acciones = new HBox(
                10, consultar, registrar);

        VBox botones = new VBox(10, acciones);

        return new Tab("Beneficiarios", crearContenido(
                "Gestion de beneficiarios", formulario,
                botones, resultadoBeneficiarios));
    }

    /**
     * Consulta un beneficiario por los datos de identificacion.
     *
     * @param boton boton de consulta
     */
    private void consultarBeneficiario(Button boton) {

        if (!configurarApi()) {
            return;
        }

        String clave = beneficiarioClave.getText();
        String nombres = beneficiarioNombres.getText();
        String primerApellido = beneficiarioPrimerApellido.getText();
        String segundoApellido = opcional(beneficiarioSegundoApellido);

        LocalDate fecha;

        try {
            fecha = leerFecha(
                    beneficiarioFecha, "La fecha de nacimiento");
        } catch (RuntimeException excepcion) {
            estado.setText(excepcion.getMessage());
            return;
        }

        ejecutor.ejecutar(
                "Consultar beneficiario",
                () -> api.consultarBeneficiario(
                        clave, fecha, nombres, primerApellido,
                        segundoApellido),
                this::mostrarBeneficiario,
                error -> resultadoBeneficiarios.setText(error.getMessage()),
                boton, beneficiarioClave);
    }

    /**
     * Registra un beneficiario.
     *
     * @param boton boton de registro
     */
    private void registrarBeneficiario(Button boton) {

        if (!configurarApi()) {
            return;
        }

        DatosBeneficiario datos;

        try {
            datos = leerBeneficiario();
        } catch (RuntimeException excepcion) {
            estado.setText("Datos del beneficiario: "
                    + excepcion.getMessage());
            return;
        }

        ejecutor.ejecutar(
                "Registrar beneficiario",
                () -> api.registrarBeneficiario(
                        datos.clavePoliza(), datos),
                this::mostrarBeneficiario,
                error -> resultadoBeneficiarios.setText(error.getMessage()),
                boton);
    }

    /**
     * Construye el DTO de un beneficiario.
     *
     * @return datos del beneficiario
     */
    private DatosBeneficiario leerBeneficiario() {

        String clave = requerido(
                beneficiarioClave, "La clave de la poliza");

        String porcentaje = requerido(
                beneficiarioPorcentaje, "El porcentaje");

        return new DatosBeneficiario(
                null,
                requerido(beneficiarioNombres, "Los nombres"),
                requerido(beneficiarioPrimerApellido, "El primer apellido"),
                opcional(beneficiarioSegundoApellido),
                leerFecha(beneficiarioFecha, "La fecha de nacimiento"),
                new BigDecimal(porcentaje),
                clave);
    }

    /**
     * Muestra los datos de un beneficiario.
     *
     * @param datos beneficiario recibido
     */
    private void mostrarBeneficiario(DatosBeneficiario datos) {

        beneficiarioNombres.setText(datos.nombres());
        beneficiarioPrimerApellido.setText(datos.primerApellido());

        beneficiarioSegundoApellido.setText(
                datos.segundoApellido() == null
                        ? "" : datos.segundoApellido());

        beneficiarioFecha.setText(datos.fechaNacimiento().toString());

        beneficiarioPorcentaje.setText(
                datos.porcentaje().toPlainString());

        beneficiarioClave.setText(datos.clavePoliza());

        resultadoBeneficiarios.setText(
                "Operacion realizada correctamente.\n\n" + datos);
    }

    // COMPONENTES Y VALIDACIONES COMUNES

    /**
     * Crea un formulario con espaciado.
     *
     * @return formulario nuevo
     */
    private GridPane crearFormulario() {

        GridPane formulario = new GridPane();
        formulario.setHgap(15);
        formulario.setVgap(12);

        return formulario;
    }

    /**
     * Agrega un campo de texto a un formulario.
     *
     * @param formulario formulario de destino
     * @param fila posicion del campo
     * @param texto etiqueta del campo
     * @return campo creado
     */
    private TextField agregarCampo(
            GridPane formulario, int fila, String texto) {

        TextField campo = new TextField();
        campo.setPromptText(texto);
        campo.setPrefWidth(420);

        formulario.add(new Label(texto), 0, fila);
        formulario.add(campo, 1, fila);

        return campo;
    }

    /**
     * Crea el area para mostrar resultados.
     *
     * @return area de resultados
     */
    private TextArea crearResultados() {

        TextArea resultados = new TextArea();
        resultados.setEditable(false);
        resultados.setWrapText(true);
        resultados.setPromptText(
                "Los resultados de las operaciones apareceran aqui.");
        resultados.setPrefRowCount(7);

        return resultados;
    }

    /**
     * Construye el contenido desplazable de una pestana.
     *
     * @param titulo titulo de la seccion
     * @param formulario formulario de datos
     * @param acciones botones de operaciones
     * @param resultados area de resultados
     * @return contenido de la pestana
     */
    private ScrollPane crearContenido(
            String titulo, GridPane formulario,
            VBox acciones, TextArea resultados) {

        Label encabezado = new Label(titulo);
        encabezado.setStyle(
                "-fx-font-size: 19px; -fx-font-weight: bold;");

        VBox contenido = new VBox(
                18, encabezado, formulario, acciones, resultados);

        contenido.setPadding(new Insets(20));
        contenido.setPrefWidth(1000);

        ScrollPane desplazamiento = new ScrollPane(contenido);
        desplazamiento.setFitToWidth(true);

        return desplazamiento;
    }

    /**
     * Configura la URL escrita en la interfaz.
     *
     * @return verdadero si la direccion es valida
     */
    private boolean configurarApi() {

        try {
            api.configurarUrl(campoUrlApi.getText());
            return true;
        } catch (IllegalArgumentException excepcion) {
            estado.setText(excepcion.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el contenido obligatorio de un campo.
     *
     * @param campo campo de texto
     * @param nombre nombre del dato
     * @return contenido sin espacios externos
     */
    private String requerido(TextField campo, String nombre) {

        String valor = campo.getText();

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    nombre + " es obligatorio.");
        }

        return valor.trim();
    }

    /**
     * Obtiene un contenido opcional.
     *
     * @param campo campo de texto
     * @return contenido o null si esta vacio
     */
    private String opcional(TextField campo) {

        String valor = campo.getText();

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }

    /**
     * Convierte un campo a fecha.
     *
     * @param campo campo de fecha
     * @param nombre nombre del dato
     * @return fecha indicada
     */
    private LocalDate leerFecha(TextField campo, String nombre) {

        String valor = requerido(campo, nombre);

        try {
            return LocalDate.parse(valor);
        } catch (RuntimeException excepcion) {
            throw new IllegalArgumentException(
                    nombre + " debe tener el formato aaaa-mm-dd.");
        }
    }

    /**
     * Convierte un campo opcional a entero.
     *
     * @param campo campo numerico
     * @param nombre nombre del dato
     * @return entero o null si esta vacio
     */
    private Integer enteroOpcional(TextField campo, String nombre) {

        String valor = opcional(campo);

        if (valor == null) {
            return null;
        }

        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException(
                    nombre + " debe ser un numero entero.");
        }
    }
}
