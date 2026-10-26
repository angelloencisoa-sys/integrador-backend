package pe.edu.utp.Grupo06.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.utp.Grupo06.model.DetalleVenta;
import pe.edu.utp.Grupo06.model.Pago;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.Venta;
import pe.edu.utp.Grupo06.model.enums.MetodoPago;
import pe.edu.utp.Grupo06.service.IProductoService;
import pe.edu.utp.Grupo06.service.IVentaService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class VentasPosViewController {

    @Autowired
    private IProductoService productoService;

    @Autowired
    private IVentaService ventaService;

    @FXML
    private TextField txtBuscarProducto;

    @FXML
    private Spinner<Integer> spnCantidad;

    @FXML
    private TableView<Producto> tblCatalogo;

    @FXML
    private TableColumn<Producto, String> colCatCodigo;

    @FXML
    private TableColumn<Producto, String> colCatNombre;

    @FXML
    private TableColumn<Producto, String> colCatMarca;

    @FXML
    private TableColumn<Producto, BigDecimal> colCatPrecio;

    @FXML
    private TableColumn<Producto, Integer> colCatStock;

    @FXML
    private TableView<DetalleVenta> tblCarrito;

    @FXML
    private TableColumn<DetalleVenta, String> colCarProducto;

    @FXML
    private TableColumn<DetalleVenta, Integer> colCarCant;

    @FXML
    private TableColumn<DetalleVenta, BigDecimal> colCarPrecio;

    @FXML
    private TableColumn<DetalleVenta, BigDecimal> colCarSubtotal;

    @FXML
    private Label lblSubtotalVenta;

    @FXML
    private Label lblIgvVenta;

    @FXML
    private Label lblTotalVenta;

    @FXML
    private TextField txtMontoEfectivo;

    @FXML
    private TextField txtMontoYape;

    @FXML
    private TextField txtMontoPlin;

    private ObservableList<Producto> listaCatalogo = FXCollections.observableArrayList();
    private ObservableList<DetalleVenta> listaCarrito = FXCollections.observableArrayList();
    private BigDecimal totalVenta = BigDecimal.ZERO;

    @FXML
    public void initialize() {
        spnCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1));
        configurarTablas();
        cargarCatalogo();

        txtBuscarProducto.textProperty().addListener((obs, oldVal, newVal) -> filtrarCatalogo(newVal));
    }

    private void configurarTablas() {
        // Catálogo
        colCatCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colCatNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCatMarca.setCellValueFactory(cellData -> {
            String m = cellData.getValue().getMarca();
            return new SimpleStringProperty(m != null && !m.isBlank() ? m : "Genérico");
        });
        colCatPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colCatStock.setCellValueFactory(new PropertyValueFactory<>("stockDisponible"));

        // Carrito
        colCarProducto.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProducto().getNombre()));
        colCarCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCarPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colCarSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tblCarrito.setItems(listaCarrito);
    }

    private void cargarCatalogo() {
        try {
            listaCatalogo.setAll(productoService.listarActivos());
            tblCatalogo.setItems(listaCatalogo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filtrarCatalogo(String texto) {
        if (texto == null || texto.isBlank()) {
            tblCatalogo.setItems(listaCatalogo);
            return;
        }
        String query = texto.toLowerCase().trim();
        List<Producto> filtrados = listaCatalogo.stream()
                .filter(p -> (p.getNombre() != null && p.getNombre().toLowerCase().contains(query)) ||
                             (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(query)) ||
                             (p.getMarca() != null && p.getMarca().toLowerCase().contains(query)))
                .toList();
        tblCatalogo.setItems(FXCollections.observableArrayList(filtrados));
    }

    @FXML
    public void handleAnadirAlCarrito() {
        Producto seleccionado = tblCatalogo.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Seleccione un producto de la tabla para añadirlo.");
            return;
        }

        if (seleccionado.getStockDisponible() <= 0) {
            mostrarAlerta(Alert.AlertType.ERROR, "Sin Stock", "El producto seleccionado no tiene unidades disponibles.");
            return;
        }

        int cantidadAAgregar = spnCantidad.getValue() != null ? spnCantidad.getValue() : 1;

        // Buscar si ya está en el carrito
        DetalleVenta existente = listaCarrito.stream()
                .filter(d -> d.getProducto().getId().equals(seleccionado.getId()))
                .findFirst()
                .orElse(null);

        int cantidadFinal = (existente != null ? existente.getCantidad() : 0) + cantidadAAgregar;

        if (cantidadFinal > seleccionado.getStockDisponible()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Stock Límite",
                    "No puede agregar " + cantidadFinal + " unidades. Stock disponible: " + seleccionado.getStockDisponible() + ".");
            return;
        }

        if (existente != null) {
            existente.setCantidad(cantidadFinal);
            existente.setSubtotal(existente.getPrecioUnitario().multiply(BigDecimal.valueOf(cantidadFinal)));
            tblCarrito.refresh();
        } else {
            DetalleVenta nuevo = new DetalleVenta();
            nuevo.setProducto(seleccionado);
            nuevo.setCantidad(cantidadAAgregar);
            nuevo.setPrecioUnitario(seleccionado.getPrecioVenta());
            nuevo.setSubtotal(seleccionado.getPrecioVenta().multiply(BigDecimal.valueOf(cantidadAAgregar)));
            listaCarrito.add(nuevo);
        }

        recalcularTotal();
        spnCantidad.getValueFactory().setValue(1); // Reset a 1
    }

    private void recalcularTotal() {
        totalVenta = listaCarrito.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotalSinIgv = totalVenta.divide(BigDecimal.valueOf(1.18), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal igvCalculado = totalVenta.subtract(subtotalSinIgv);

        if (lblSubtotalVenta != null) {
            lblSubtotalVenta.setText("S/ " + subtotalSinIgv);
        }
        if (lblIgvVenta != null) {
            lblIgvVenta.setText("S/ " + igvCalculado);
        }

        lblTotalVenta.setText("S/ " + totalVenta.setScale(2, java.math.RoundingMode.HALF_UP).toString());
        txtMontoEfectivo.setText(totalVenta.setScale(2, java.math.RoundingMode.HALF_UP).toString());
        txtMontoYape.setText("0.00");
        txtMontoPlin.setText("0.00");
    }

    private BigDecimal parsearMonto(String texto) {
        if (texto == null || texto.isBlank()) return BigDecimal.ZERO;
        String limpio = texto.trim().replace(",", ".");
        return new BigDecimal(limpio);
    }

    @FXML
    public void handleLimpiarCarrito() {
        listaCarrito.clear();
        recalcularTotal();
    }

    @FXML
    public void handleEmitirVenta() {
        if (listaCarrito.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Carrito Vacío", "Añada al menos un producto para registrar la venta.");
            return;
        }

        try {
            BigDecimal ef = parsearMonto(txtMontoEfectivo.getText());
            BigDecimal yap = parsearMonto(txtMontoYape.getText());
            BigDecimal pli = parsearMonto(txtMontoPlin.getText());

            BigDecimal sumaPagos = ef.add(yap).add(pli);
            if (sumaPagos.compareTo(totalVenta) != 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Pagos Descuadrados",
                        "La suma de los métodos de pago (S/ " + sumaPagos + ") debe ser exactamente igual al total de la venta (S/ " + totalVenta + ").");
                return;
            }

            Venta venta = new Venta();
            venta.setNumeroTicket(ventaService.generarSiguienteNumeroTicket());
            venta.setUsuario(LoginViewController.getUsuarioSesion());

            List<DetalleVenta> detalles = new ArrayList<>(listaCarrito);
            venta.setDetalles(detalles);

            List<Pago> pagos = new ArrayList<>();
            if (ef.compareTo(BigDecimal.ZERO) > 0) {
                Pago p = new Pago();
                p.setMetodoPago(MetodoPago.EFECTIVO);
                p.setMonto(ef);
                pagos.add(p);
            }
            if (yap.compareTo(BigDecimal.ZERO) > 0) {
                Pago p = new Pago();
                p.setMetodoPago(MetodoPago.YAPE);
                p.setMonto(yap);
                pagos.add(p);
            }
            if (pli.compareTo(BigDecimal.ZERO) > 0) {
                Pago p = new Pago();
                p.setMetodoPago(MetodoPago.PLIN);
                p.setMonto(pli);
                pagos.add(p);
            }
            venta.setPagos(pagos);

            Venta emitida = ventaService.registrarVenta(venta);

            mostrarBoletaFormal(emitida);

            handleLimpiarCarrito();
            cargarCatalogo();
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al procesar la venta", ex.getMessage());
        }
    }

    private void mostrarBoletaFormal(Venta v) {
        Dialog<ButtonType> boletaDialog = new Dialog<>();
        boletaDialog.setTitle("Comprobante de Pago — SGCIVORP");
        boletaDialog.setHeaderText(null);

        ButtonType btnImprimir = new ButtonType("🖨️ Imprimir Ticket", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        boletaDialog.getDialogPane().getButtonTypes().addAll(btnImprimir, btnCerrar);
        boletaDialog.setResultConverter(btn -> btn);

        VBox root = new VBox(10);
        root.setStyle("-fx-font-family: 'Courier New', monospace; -fx-padding: 15px; -fx-background-color: #ffffff;");
        root.setPrefWidth(380);

        Label lblCabecera = new Label(
                "==========================================\n" +
                "           BODEGA SGCIVORP                \n" +
                "       RUC: 20123456789 - LIMA PERÚ       \n" +
                "==========================================\n" +
                "Ticket N°: " + v.getNumeroTicket() + "\n" +
                "Fecha: " + v.getFechaVenta().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "\n" +
                "Cajero: " + (v.getUsuario() != null ? v.getUsuario().getNombreCompleto() : "N/A") + "\n" +
                "------------------------------------------\n" +
                "CANT  DESCRIPCIÓN             P.U    TOTAL\n" +
                "------------------------------------------"
        );

        StringBuilder sbItems = new StringBuilder();
        for (DetalleVenta d : v.getDetalles()) {
            String nom = d.getProducto().getNombre();
            if (nom.length() > 20) nom = nom.substring(0, 17) + "...";
            sbItems.append(String.format("%-4d  %-22s %5.2f  %6.2f\n",
                    d.getCantidad(), nom, d.getPrecioUnitario(), d.getSubtotal()));
        }

        Label lblItems = new Label(sbItems.toString());

        BigDecimal subtotal = v.getSubtotal() != null && v.getSubtotal().compareTo(BigDecimal.ZERO) > 0
                ? v.getSubtotal()
                : v.getTotal().divide(BigDecimal.valueOf(1.18), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal igv = v.getIgv() != null && v.getIgv().compareTo(BigDecimal.ZERO) > 0
                ? v.getIgv()
                : v.getTotal().subtract(subtotal);

        StringBuilder sbPagos = new StringBuilder();
        sbPagos.append("------------------------------------------\n");
        sbPagos.append(String.format("OP. GRAVADA (Subtotal):         S/ %7.2f\n", subtotal));
        sbPagos.append(String.format("I.G.V. (18%%):                   S/ %7.2f\n", igv));
        sbPagos.append(String.format("TOTAL A PAGAR:                  S/ %7.2f\n", v.getTotal()));
        sbPagos.append("------------------------------------------\n");
        sbPagos.append("MÉTODOS DE PAGO:\n");
        for (Pago p : v.getPagos()) {
            sbPagos.append(String.format(" - %-10s:                    S/ %7.2f\n", p.getMetodoPago(), p.getMonto()));
        }
        sbPagos.append("==========================================\n");
        sbPagos.append("       ¡GRACIAS POR SU COMPRA!           \n");
        sbPagos.append("==========================================");

        Label lblPie = new Label(sbPagos.toString());

        root.getChildren().addAll(lblCabecera, lblItems, lblPie);
        boletaDialog.getDialogPane().setContent(root);

        String textoCompleto = lblCabecera.getText() + "\n" + sbItems + lblPie.getText();
        boletaDialog.showAndWait().ifPresent(res -> {
            if (res == btnImprimir) {
                pe.edu.utp.Grupo06.util.ImpresionUtil.imprimirTicket(
                        textoCompleto,
                        tblCarrito.getScene().getWindow()
                );
            }
        });
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}

