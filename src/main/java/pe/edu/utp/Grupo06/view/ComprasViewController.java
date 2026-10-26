package pe.edu.utp.Grupo06.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.utp.Grupo06.model.Compra;
import pe.edu.utp.Grupo06.model.DetalleCompra;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.Proveedor;
import pe.edu.utp.Grupo06.model.PresentacionProducto;
import pe.edu.utp.Grupo06.model.enums.EstadoCompra;
import pe.edu.utp.Grupo06.model.enums.MotivoDiferencia;
import pe.edu.utp.Grupo06.service.ICompraService;
import pe.edu.utp.Grupo06.service.IPresentacionProductoService;
import pe.edu.utp.Grupo06.service.IProductoService;
import pe.edu.utp.Grupo06.service.IProveedorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Component
public class ComprasViewController {

    @Autowired
    private ICompraService compraService;

    @Autowired
    private IProveedorService proveedorService;

    @Autowired
    private IProductoService productoService;

    @Autowired
    private IPresentacionProductoService presentacionService;

    @FXML
    private TextField txtFiltroCompra;

    @FXML
    private DatePicker dpFechaInicio;

    @FXML
    private DatePicker dpFechaFin;

    @FXML
    private Label lblTotalCompras;

    @FXML
    private TableView<Compra> tblCompras;

    @FXML
    private TableColumn<Compra, String> colComprobante;

    @FXML
    private TableColumn<Compra, String> colProveedor;

    @FXML
    private TableColumn<Compra, LocalDateTime> colFecha;

    @FXML
    private TableColumn<Compra, String> colUsuario;

    @FXML
    private TableColumn<Compra, BigDecimal> colTotal;

    @FXML
    private TableColumn<Compra, String> colEstadoCompra;

    @FXML
    private TableColumn<Compra, Void> colAcciones;

    @Autowired
    private pe.edu.utp.Grupo06.repository.DetalleCompraRepository detalleCompraRepository;

    private ObservableList<Compra> listaCompras = FXCollections.observableArrayList();
    private javafx.collections.transformation.FilteredList<Compra> filteredCompras;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarCompras();
    }

    private void configurarColumnas() {
        colComprobante.setCellValueFactory(new PropertyValueFactory<>("numeroComprobante"));
        colProveedor.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProveedor() != null ?
                        cellData.getValue().getProveedor().getRazonSocial() : ""));

        // Ordenamiento cronológico real
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCompra"));
        colFecha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(formatter));
                }
            }
        });

        colUsuario.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsuario() != null ?
                        cellData.getValue().getUsuario().getNombreCompleto() : ""));

        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colEstadoCompra.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoCompra().name()));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("Ver Detalle");
            private final Button btnAnular = new Button("Anular");

            {
                btnVer.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-font-weight: bold; -fx-cursor: hand;");
                btnVer.setOnAction(event -> {
                    Compra c = getTableView().getItems().get(getIndex());
                    mostrarDetalleFactura(c);
                });
                btnAnular.setOnAction(event -> anularCompra(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Compra compra = getTableView().getItems().get(getIndex());
                    btnAnular.setDisable(compra.getEstadoCompra() == EstadoCompra.ANULADA);
                    HBox pane = new HBox(5, btnVer, btnAnular);
                    pane.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(pane);
                }
            }
        });

        filteredCompras = new javafx.collections.transformation.FilteredList<>(listaCompras, p -> true);
        javafx.collections.transformation.SortedList<Compra> sorted = new javafx.collections.transformation.SortedList<>(filteredCompras);
        sorted.comparatorProperty().bind(tblCompras.comparatorProperty());
        tblCompras.setItems(sorted);

        txtFiltroCompra.textProperty().addListener((obs, old, n) -> aplicarFiltroCompras());
    }

    private void anularCompra(Compra compra) {
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "Se revertirán " + compra.getNumeroComprobante() + " y sus unidades ingresadas. Si ya se vendieron y el stock no alcanza, se cancelará la anulación.",
                ButtonType.OK, ButtonType.CANCEL);
        confirmar.setTitle("Anular compra");
        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        TextInputDialog motivo = new TextInputDialog();
        motivo.setTitle("Motivo de anulación");
        motivo.setHeaderText("Indique el motivo de la anulación");
        motivo.showAndWait().ifPresent(texto -> {
            try {
                compraService.anularCompra(compra.getId(), LoginViewController.getUsuarioSesion().getId(), texto);
                cargarCompras();
            } catch (Exception ex) { mostrarAlerta("No se pudo anular", ex.getMessage()); }
        });
    }

    @FXML
    public void handleFiltrarCompras() {
        aplicarFiltroCompras();
    }

    @FXML
    public void handleLimpiarFiltroCompras() {
        txtFiltroCompra.clear();
        dpFechaInicio.setValue(null);
        dpFechaFin.setValue(null);
        aplicarFiltroCompras();
    }

    private void aplicarFiltroCompras() {
        String texto = txtFiltroCompra.getText() != null ? txtFiltroCompra.getText().trim().toLowerCase() : "";
        LocalDate fIni = dpFechaInicio.getValue();
        LocalDate fFin = dpFechaFin.getValue();

        filteredCompras.setPredicate(c -> {
            if (c == null) return false;

            if (!texto.isEmpty()) {
                boolean matchComp = c.getNumeroComprobante() != null && c.getNumeroComprobante().toLowerCase().contains(texto);
                boolean matchProv = c.getProveedor() != null && c.getProveedor().getRazonSocial() != null &&
                        c.getProveedor().getRazonSocial().toLowerCase().contains(texto);
                if (!matchComp && !matchProv) return false;
            }

            if (c.getFechaCompra() != null) {
                LocalDate fComp = c.getFechaCompra().toLocalDate();
                if (fIni != null && fComp.isBefore(fIni)) return false;
                if (fFin != null && fComp.isAfter(fFin)) return false;
            }

            return true;
        });

        BigDecimal total = filteredCompras.stream()
                .filter(c -> c.getEstadoCompra() == EstadoCompra.REGISTRADA)
                .map(Compra::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalCompras.setText(String.format("Total Compras: S/ %.2f", total));
    }

    private void mostrarDetalleFactura(Compra c) {
        Dialog<Void> factDialog = new Dialog<>();
        factDialog.setTitle("Comprobante de Compra — " + c.getNumeroComprobante());
        factDialog.setHeaderText(null);

        ButtonType btnOk = new ButtonType("Cerrar", ButtonBar.ButtonData.OK_DONE);
        factDialog.getDialogPane().getButtonTypes().add(btnOk);

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 15px; -fx-background-color: #ffffff;");
        root.setPrefWidth(550);

        Label lblHeader = new Label(
                "COMPROBANTE DE COMPRA A PROVEEDOR\n" +
                "N° Comprobante: " + c.getNumeroComprobante() + "\n" +
                "Tipo: " + c.getTipoComprobante() + " | Estado: " + c.getEstadoCompra() + "\n" +
                "Proveedor: " + (c.getProveedor() != null ? c.getProveedor().getRazonSocial() + " (RUC: " + c.getProveedor().getRuc() + ")" : "N/A") + "\n" +
                "Fecha: " + (c.getFechaCompra() != null ? c.getFechaCompra().format(formatter) : "") + "\n" +
                "Recepcionado por: " + (c.getUsuario() != null ? c.getUsuario().getNombreCompleto() : "")
        );
        lblHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        List<DetalleCompra> detalles = detalleCompraRepository.findByCompraId(c.getId());
        TableView<DetalleCompra> tblDet = new TableView<>(FXCollections.observableArrayList(detalles));
        tblDet.setPrefHeight(180);

        TableColumn<DetalleCompra, String> colNom = new TableColumn<>("Producto");
        colNom.setCellValueFactory(d -> {
            if (d.getValue().getProducto() == null) return new SimpleStringProperty("");
            String nom = d.getValue().getProducto().getNombre();
            String marca = d.getValue().getProducto().getMarca();
            return new SimpleStringProperty(nom + (marca != null && !marca.isBlank() ? " (" + marca + ")" : ""));
        });
        colNom.setPrefWidth(220);

        TableColumn<DetalleCompra, Integer> colC = new TableColumn<>("Cantidad");
        colC.setCellValueFactory(new PropertyValueFactory<>("cantidadPresentaciones"));
        colC.setPrefWidth(80);

        TableColumn<DetalleCompra, BigDecimal> colP = new TableColumn<>("P. Presentación (S/)");
        colP.setCellValueFactory(new PropertyValueFactory<>("precioPresentacion"));
        colP.setPrefWidth(100);

        TableColumn<DetalleCompra, BigDecimal> colS = new TableColumn<>("Subtotal (S/)");
        colS.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colS.setPrefWidth(110);

        TableColumn<DetalleCompra, String> colPres = new TableColumn<>("Presentación");
        colPres.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombrePresentacion() + " x" + d.getValue().getFactorConversion()));
        TableColumn<DetalleCompra, Integer> colEsp = new TableColumn<>("Esperadas");
        colEsp.setCellValueFactory(new PropertyValueFactory<>("unidadesEsperadas"));
        TableColumn<DetalleCompra, Integer> colRec = new TableColumn<>("Recibidas");
        colRec.setCellValueFactory(new PropertyValueFactory<>("unidadesRecibidas"));
        TableColumn<DetalleCompra, Integer> colRech = new TableColumn<>("Rechazadas");
        colRech.setCellValueFactory(new PropertyValueFactory<>("unidadesRechazadas"));
        TableColumn<DetalleCompra, Integer> colIng = new TableColumn<>("Ingresadas");
        colIng.setCellValueFactory(new PropertyValueFactory<>("unidadesIngresadas"));
        TableColumn<DetalleCompra, String> colMot = new TableColumn<>("Motivo");
        colMot.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivoDiferencia().name()));
        TableColumn<DetalleCompra, String> colObs = new TableColumn<>("Observación");
        colObs.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObservacion()));
        tblDet.getColumns().addAll(colNom, colPres, colC, colEsp, colRec, colRech, colIng, colP, colS, colMot, colObs);
        root.setPrefWidth(1100);

        BigDecimal tot = c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO;
        BigDecimal sub = c.getSubtotal() != null && c.getSubtotal().compareTo(BigDecimal.ZERO) > 0
                ? c.getSubtotal()
                : tot.divide(BigDecimal.valueOf(1.18), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal igv = c.getIgv() != null && c.getIgv().compareTo(BigDecimal.ZERO) > 0
                ? c.getIgv()
                : tot.subtract(sub);

        VBox totBox = new VBox(4);
        totBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label lblSub = new Label("Base Imponible: S/ " + sub.setScale(2, java.math.RoundingMode.HALF_UP));
        lblSub.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
        Label lblIgv = new Label("I.G.V. (18%): S/ " + igv.setScale(2, java.math.RoundingMode.HALF_UP));
        lblIgv.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
        Label lblTot = new Label("TOTAL FACTURADO: S/ " + tot.setScale(2, java.math.RoundingMode.HALF_UP));
        lblTot.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #15803d;");
        totBox.getChildren().addAll(lblSub, lblIgv, lblTot);

        root.getChildren().addAll(lblHeader, new Separator(), tblDet, totBox);
        factDialog.getDialogPane().setContent(root);
        factDialog.showAndWait();
    }

    public void cargarCompras() {
        try {
            List<Compra> compras = compraService.listarCompras();
            listaCompras.setAll(compras);
            aplicarFiltroCompras();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNuevaCompra() {
        Dialog<Compra> dialog = new Dialog<>();
        dialog.setTitle("Registrar Factura / Boleta de Compra");
        dialog.setHeaderText("Ingreso de mercadería al almacén por proveedor");

        ButtonType btnGuardar = new ButtonType("💾 Confirmar Compra", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        VBox root = new VBox(12);
        root.setPrefWidth(680);
        root.setStyle("-fx-padding: 10px;");

        // Fila 1: Comprobante y Proveedor
        HBox row1 = new HBox(15);
        VBox boxComp = new VBox(4, new Label("N° Comprobante:"), new TextField());
        TextField txtComp = (TextField) boxComp.getChildren().get(1);
        txtComp.setPromptText("Ej: F001-000456");
        txtComp.setPrefWidth(220);

        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("FACTURA", "BOLETA", "OTRO"));
        cbTipo.setValue("FACTURA");

        ComboBox<Proveedor> cbProv = new ComboBox<>();
        cbProv.getItems().setAll(proveedorService.listarActivos());
        cbProv.setPrefWidth(350);
        cbProv.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Proveedor p) {
                return p != null ? p.getRazonSocial() + " (RUC: " + p.getRuc() + ")" : "";
            }

            @Override
            public Proveedor fromString(String s) {
                return null;
            }
        });
        if (!cbProv.getItems().isEmpty()) cbProv.setValue(cbProv.getItems().get(0));

        VBox boxProv = new VBox(4, new Label("Proveedor:"), cbProv);
        row1.getChildren().addAll(new VBox(4, new Label("Tipo:"), cbTipo), boxComp, boxProv);

        // Fila 2: Selector de Producto a añadir a la compra
        HBox row2 = new HBox(10);
        row2.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        row2.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 10px; -fx-background-radius: 6px;");

        ComboBox<Producto> cbProd = new ComboBox<>();
        cbProd.getItems().setAll(productoService.listarActivos());
        cbProd.setPrefWidth(240);
        cbProd.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Producto p) {
                if (p == null) return "";
                String marca = p.getMarca() != null && !p.getMarca().isBlank() ? " (" + p.getMarca() + ")" : "";
                return p.getNombre() + marca;
            }

            @Override
            public Producto fromString(String s) {
                return null;
            }
        });
        if (!cbProd.getItems().isEmpty()) cbProd.setValue(cbProd.getItems().get(0));

        ComboBox<PresentacionProducto> cbPresentacion = new ComboBox<>();
        cbPresentacion.setPrefWidth(155);
        cbPresentacion.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(PresentacionProducto p) {
                return p == null ? "" : p.getNombrePresentacion() + " x" + p.getFactorConversion();
            }
            @Override public PresentacionProducto fromString(String s) { return null; }
        });
        TextField txtCantidad = new TextField("1");
        txtCantidad.setPrefWidth(70);
        TextField txtRecibidas = new TextField("1");
        txtRecibidas.setPrefWidth(80);
        TextField txtRechazadas = new TextField("0");
        txtRechazadas.setPrefWidth(80);
        Label lblEsperadas = new Label("1");
        Label lblIngresadas = new Label("1");
        ComboBox<MotivoDiferencia> cbMotivo = new ComboBox<>(FXCollections.observableArrayList(MotivoDiferencia.values()));
        cbMotivo.setValue(MotivoDiferencia.SIN_DIFERENCIA);
        TextField txtObservacion = new TextField();
        txtObservacion.setPromptText("Detalle de faltante, daño u otra diferencia");
        txtObservacion.setPrefWidth(300);

        TextField txtPrecio = new TextField();
        txtPrecio.setPrefWidth(95);
        txtPrecio.setPromptText("P. Compra");

        Runnable recalcular = () -> {
            try {
                int cantidad = Integer.parseInt(txtCantidad.getText().trim());
                int factor = cbPresentacion.getValue() == null ? 1 : cbPresentacion.getValue().getFactorConversion();
                int esperadas = Math.multiplyExact(cantidad, factor);
                lblEsperadas.setText(String.valueOf(esperadas));
                lblIngresadas.setText(String.valueOf(Integer.parseInt(txtRecibidas.getText().trim()) - Integer.parseInt(txtRechazadas.getText().trim())));
            } catch (Exception ex) { lblEsperadas.setText("—"); lblIngresadas.setText("—"); }
        };
        cbProd.setOnAction(e -> {
            if (cbProd.getValue() != null) {
                txtPrecio.setText(cbProd.getValue().getPrecioCompra().setScale(2, java.math.RoundingMode.HALF_UP).toString());
                cbPresentacion.getItems().setAll(presentacionService.listar(cbProd.getValue().getId(), true));
                cbPresentacion.setValue(cbPresentacion.getItems().isEmpty() ? null : cbPresentacion.getItems().get(0));
            }
            recalcular.run();
        });
        if (cbProd.getValue() != null) {
            txtPrecio.setText(cbProd.getValue().getPrecioCompra().setScale(2, java.math.RoundingMode.HALF_UP).toString());
            cbPresentacion.getItems().setAll(presentacionService.listar(cbProd.getValue().getId(), true));
            cbPresentacion.setValue(cbPresentacion.getItems().isEmpty() ? null : cbPresentacion.getItems().get(0));
        }
        cbPresentacion.valueProperty().addListener((o, a, b) -> {
            if (b != null && cbProd.getValue() != null && cbProd.getValue().getPrecioCompra() != null)
                txtPrecio.setText(cbProd.getValue().getPrecioCompra().multiply(BigDecimal.valueOf(b.getFactorConversion())).setScale(2, java.math.RoundingMode.HALF_UP).toString());
            recalcular.run();
            if (!lblEsperadas.getText().equals("—")) txtRecibidas.setText(lblEsperadas.getText());
        });
        txtCantidad.textProperty().addListener((o, a, b) -> { recalcular.run(); if (!lblEsperadas.getText().equals("—")) txtRecibidas.setText(lblEsperadas.getText()); });
        txtRecibidas.textProperty().addListener((o, a, b) -> recalcular.run());
        txtRechazadas.textProperty().addListener((o, a, b) -> recalcular.run());

        Button btnAdd = new Button("➕ Añadir Ítem");
        btnAdd.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        row2.getChildren().addAll(
                new VBox(3, new Label("Producto:"), cbProd),
                new VBox(3, new Label("Presentación:"), cbPresentacion),
                new VBox(3, new Label("Cantidad:"), txtCantidad),
                new VBox(3, new Label("Precio por presentación:"), txtPrecio),
                btnAdd
        );
        HBox rowRecepcion = new HBox(12,
                new VBox(3, new Label("Unidades esperadas:"), lblEsperadas),
                new VBox(3, new Label("Recibidas:"), txtRecibidas),
                new VBox(3, new Label("Rechazadas:"), txtRechazadas),
                new VBox(3, new Label("Ingresarán:"), lblIngresadas),
                new VBox(3, new Label("Motivo:"), cbMotivo));
        HBox rowObservacion = new HBox(8, new Label("Observación:"), txtObservacion);

        // Tabla de ítems incluidos en esta compra
        ObservableList<DetalleCompra> itemsCompra = FXCollections.observableArrayList();
        TableView<DetalleCompra> tblItems = new TableView<>(itemsCompra);
        tblItems.setPrefHeight(190);

        TableColumn<DetalleCompra, String> colPName = new TableColumn<>("Producto");
        colPName.setCellValueFactory(c -> {
            if (c.getValue().getProducto() == null) return new SimpleStringProperty("");
            String nom = c.getValue().getProducto().getNombre();
            String marca = c.getValue().getProducto().getMarca();
            return new SimpleStringProperty(nom + (marca != null && !marca.isBlank() ? " (" + marca + ")" : ""));
        });
        colPName.setPrefWidth(240);

        TableColumn<DetalleCompra, Integer> colPCant = new TableColumn<>("Cantidad");
        colPCant.setCellValueFactory(new PropertyValueFactory<>("cantidadPresentaciones"));
        colPCant.setPrefWidth(80);

        TableColumn<DetalleCompra, BigDecimal> colPPrice = new TableColumn<>("P. Presentación (S/)");
        colPPrice.setCellValueFactory(new PropertyValueFactory<>("precioPresentacion"));
        colPPrice.setPrefWidth(95);

        TableColumn<DetalleCompra, BigDecimal> colPSub = new TableColumn<>("Subtotal (S/)");
        colPSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colPSub.setPrefWidth(100);

        Label lblTotal = new Label("Total Compra: S/ 0.00");
        lblTotal.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");

        Runnable actualizarTotal = () -> {
            BigDecimal totalSum = itemsCompra.stream()
                    .map(DetalleCompra::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal subtotalSinIgv = totalSum.divide(BigDecimal.valueOf(1.18), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal igvCalculado = totalSum.subtract(subtotalSinIgv);
            lblTotal.setText(String.format("Base: S/ %.2f | IGV (18%%): S/ %.2f | Total: S/ %.2f",
                    subtotalSinIgv.doubleValue(), igvCalculado.doubleValue(), totalSum.doubleValue()));
        };

        TableColumn<DetalleCompra, Void> colPQuitar = new TableColumn<>("Acción");
        colPQuitar.setPrefWidth(85);
        colPQuitar.setStyle("-fx-alignment: CENTER;");
        colPQuitar.setCellFactory(col -> new TableCell<>() {
            private final Button btnQuitar = new Button("❌ Quitar");
            {
                btnQuitar.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
                btnQuitar.setOnAction(e -> {
                    DetalleCompra d = getTableView().getItems().get(getIndex());
                    itemsCompra.remove(d);
                    actualizarTotal.run();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(btnQuitar);
                }
            }
        });

        TableColumn<DetalleCompra, String> colPPres = new TableColumn<>("Presentación");
        colPPres.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombrePresentacion() + " x" + c.getValue().getFactorConversion()));
        TableColumn<DetalleCompra, Integer> colPEsp = new TableColumn<>("Esperadas");
        colPEsp.setCellValueFactory(new PropertyValueFactory<>("unidadesEsperadas"));
        TableColumn<DetalleCompra, Integer> colPRec = new TableColumn<>("Recibidas");
        colPRec.setCellValueFactory(new PropertyValueFactory<>("unidadesRecibidas"));
        TableColumn<DetalleCompra, Integer> colPRech = new TableColumn<>("Rechazadas");
        colPRech.setCellValueFactory(new PropertyValueFactory<>("unidadesRechazadas"));
        TableColumn<DetalleCompra, Integer> colPIng = new TableColumn<>("Ingresadas");
        colPIng.setCellValueFactory(new PropertyValueFactory<>("unidadesIngresadas"));
        tblItems.getColumns().addAll(colPName, colPPres, colPCant, colPEsp, colPRec, colPRech, colPIng, colPPrice, colPSub, colPQuitar);
        root.setPrefWidth(1100);

        btnAdd.setOnAction(e -> {
            try {
                Producto prodSel = cbProd.getValue();
                if (prodSel == null) {
                    mostrarAlerta("Seleccione producto", "Debe seleccionar un producto de la lista.");
                    return;
                }
                PresentacionProducto presentacion = cbPresentacion.getValue();
                if (presentacion == null) throw new IllegalArgumentException("Seleccione presentación");
                int cant = Integer.parseInt(txtCantidad.getText().trim());
                int recibidas = Integer.parseInt(txtRecibidas.getText().trim());
                int rechazadas = Integer.parseInt(txtRechazadas.getText().trim());
                int esperadas = Math.multiplyExact(cant, presentacion.getFactorConversion());
                if (cant <= 0 || recibidas < 0 || rechazadas < 0 || rechazadas > recibidas)
                    throw new IllegalArgumentException("Cantidad, recibidas o rechazadas inválidas");
                if ((esperadas != recibidas || rechazadas > 0) && cbMotivo.getValue() == MotivoDiferencia.SIN_DIFERENCIA)
                    throw new IllegalArgumentException("Seleccione un motivo de diferencia");
                BigDecimal pu = new BigDecimal(txtPrecio.getText().trim().replace(",", "."));
                if (pu.compareTo(BigDecimal.ZERO) < 0) {
                    mostrarAlerta("Precio inválido", "El precio unitario no puede ser negativo.");
                    return;
                }

                DetalleCompra det = new DetalleCompra();
                det.setProducto(prodSel);
                det.setPresentacion(presentacion);
                det.setNombrePresentacion(presentacion.getNombrePresentacion());
                det.setFactorConversion(presentacion.getFactorConversion());
                det.setCantidadPresentaciones(cant);
                det.setPrecioPresentacion(pu);
                det.setUnidadesEsperadas(esperadas);
                det.setUnidadesRecibidas(recibidas);
                det.setUnidadesRechazadas(rechazadas);
                det.setUnidadesIngresadas(recibidas - rechazadas);
                det.setMotivoDiferencia(cbMotivo.getValue());
                det.setObservacion(txtObservacion.getText().trim());
                det.setSubtotal(pu.multiply(BigDecimal.valueOf(cant)));
                itemsCompra.add(det);

                actualizarTotal.run();
            } catch (Exception ex) {
                mostrarAlerta("Datos inválidos", ex.getMessage());
            }
        });

        Button btnVaciar = new Button("🗑️ Vaciar Lista");
        btnVaciar.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 4;");
        btnVaciar.setOnAction(e -> {
            itemsCompra.clear();
            actualizarTotal.run();
        });

        HBox botRow = new HBox(15, btnVaciar, new Region(), lblTotal);
        HBox.setHgrow(botRow.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        botRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        root.getChildren().addAll(row1, row2, rowRecepcion, rowObservacion, tblItems, botRow);
        dialog.getDialogPane().setContent(root);

        // Prevenir que la ventana se cierre si faltan datos
        Button btnConfirmar = (Button) dialog.getDialogPane().lookupButton(btnGuardar);
        final boolean[] registrada = {false};
        btnConfirmar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (txtComp.getText().isBlank()) {
                mostrarAlerta("Dato Requerido", "Ingrese el número de comprobante (factura/boleta de compra).");
                event.consume();
                return;
            }
            if (cbProv.getValue() == null) {
                mostrarAlerta("Dato Requerido", "Seleccione la empresa proveedora.");
                event.consume();
                return;
            }
            if (itemsCompra.isEmpty()) {
                mostrarAlerta("Lista Vacía", "Debe añadir al menos un producto a la compra antes de guardar.");
                event.consume();
                return;
            }
            Compra compra = new Compra();
            compra.setNumeroComprobante(txtComp.getText().trim());
            compra.setTipoComprobante(cbTipo.getValue());
            compra.setProveedor(cbProv.getValue());
            compra.setUsuario(LoginViewController.getUsuarioSesion());
            compra.setDetalles(new java.util.ArrayList<>(itemsCompra));
            try {
                compraService.registrarCompra(compra);
                registrada[0] = true;
            } catch (Exception ex) {
                mostrarAlerta("Error al registrar compra", ex.getMessage());
                event.consume(); // conserva los ítems y el diálogo para corregir
            }
        });
        dialog.showAndWait();
        if (registrada[0]) {
            cargarCompras();
            mostrarAlerta("Compra registrada", "Se ingresaron al stock las unidades recibidas y aceptadas.");
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
