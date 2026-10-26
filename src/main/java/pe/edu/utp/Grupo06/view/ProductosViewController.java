package pe.edu.utp.Grupo06.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.utp.Grupo06.model.Categoria;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.PresentacionProducto;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.model.enums.MotivoMerma;
import pe.edu.utp.Grupo06.model.enums.UnidadMedida;
import pe.edu.utp.Grupo06.service.ICategoriaService;
import pe.edu.utp.Grupo06.service.IExportacionService;
import pe.edu.utp.Grupo06.service.IMermaService;
import pe.edu.utp.Grupo06.service.IProductoService;
import pe.edu.utp.Grupo06.service.IPresentacionProductoService;
import pe.edu.utp.Grupo06.service.IUsuarioService;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.stage.Window;

@Component
public class ProductosViewController {

    @Autowired
    private IProductoService productoService;

    @Autowired
    private IPresentacionProductoService presentacionService;

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IMermaService mermaService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IExportacionService exportacionService;

    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<Categoria> cbCategoriaFiltro;

    @FXML
    private CheckBox chkSoloBajoStock;

    @FXML
    private TableView<Producto> tblProductos;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, String> colMarca;

    @FXML
    private TableColumn<Producto, String> colCategoria;

    @FXML
    private TableColumn<Producto, BigDecimal> colPrecioCompra;

    @FXML
    private TableColumn<Producto, BigDecimal> colPrecioVenta;

    @FXML
    private TableColumn<Producto, Integer> colStockActual;

    @FXML
    private TableColumn<Producto, Integer> colStockMinimo;

    @FXML
    private TableColumn<Producto, String> colEstadoStock;

    @FXML
    private TableColumn<Producto, Void> colAcciones;

    private final ObservableList<Producto> listaProductosBase = FXCollections.observableArrayList();
    private final Categoria categoriaTodas = new Categoria();
    private FilteredList<Producto> filteredProductos;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarCategoriasFiltro();

        filteredProductos = new FilteredList<>(listaProductosBase, p -> true);
        tblProductos.setItems(filteredProductos);

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cbCategoriaFiltro.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        chkSoloBajoStock.selectedProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());

        cargarProductos();
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(cellData -> {
            String m = cellData.getValue().getMarca();
            return new SimpleStringProperty(m != null && !m.isBlank() ? m : "Genérico");
        });
        colCategoria.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategoria() != null ?
                        cellData.getValue().getCategoria().getNombre() : ""));

        colPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStockActual.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        // Badge para estado de stock
        colEstadoStock.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Producto p = getTableRow().getItem();
                    Label badge = new Label();
                    if (p.getStockActual() <= p.getStockMinimo()) {
                        badge.setText("⚠️ Reponer (" + p.getStockActual() + ")");
                        badge.getStyleClass().add("badge-danger");
                    } else {
                        badge.setText("✅ Óptimo");
                        badge.getStyleClass().add("badge-success");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Botones de Editar, Merma/Baja y Eliminar en acciones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnMerma = new Button("Baja");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox pane = new HBox(5, btnEditar, btnMerma, btnEliminar);

            {
                btnEditar.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4; -fx-font-size: 11px;");
                btnMerma.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fca5a5; -fx-border-radius: 4; -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4; -fx-font-size: 11px;");
                btnEliminar.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4; -fx-font-size: 11px;");
                pane.setAlignment(Pos.CENTER);

                btnEditar.setOnAction(event -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    editarProducto(p);
                });

                btnMerma.setOnAction(event -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    abrirDialogoMerma(p);
                });

                btnEliminar.setOnAction(event -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    eliminarProducto(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    public void cargarProductos() {
        try {
            List<Producto> productos = productoService.listarActivos();
            listaProductosBase.setAll(productos);
            aplicarFiltros();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarCategoriasFiltro() {
        try {
            Long categoriaAnteriorId = cbCategoriaFiltro.getValue() != null
                    ? cbCategoriaFiltro.getValue().getId() : null;
            List<Categoria> categorias = categoriaService.listarActivas();
            categoriaTodas.setNombre("Todas las Categorías");
            cbCategoriaFiltro.getItems().setAll(categoriaTodas);
            cbCategoriaFiltro.getItems().addAll(categorias);

            javafx.util.StringConverter<Categoria> converter = new javafx.util.StringConverter<>() {
                @Override
                public String toString(Categoria c) {
                    return c != null ? c.getNombre() : "";
                }

                @Override
                public Categoria fromString(String string) {
                    return null;
                }
            };
            cbCategoriaFiltro.setConverter(converter);
            cbCategoriaFiltro.setValue(categorias.stream()
                    .filter(c -> categoriaAnteriorId != null && categoriaAnteriorId.equals(c.getId()))
                    .findFirst().orElse(categoriaTodas));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleFiltroBajoStock() {
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
        Categoria categoriaSel = cbCategoriaFiltro.getValue();
        boolean soloBajoStock = chkSoloBajoStock.isSelected();

        filteredProductos.setPredicate(p -> {
            if (p == null) return false;

            if (!texto.isEmpty()) {
                boolean matchNom = p.getNombre() != null && p.getNombre().toLowerCase().contains(texto);
                boolean matchCod = p.getCodigo() != null && p.getCodigo().toLowerCase().contains(texto);
                boolean matchMarca = p.getMarca() != null && p.getMarca().toLowerCase().contains(texto);
                if (!matchNom && !matchCod && !matchMarca) return false;
            }

            if (categoriaSel != null && categoriaSel.getId() != null) {
                if (p.getCategoria() == null || !p.getCategoria().getId().equals(categoriaSel.getId())) {
                    return false;
                }
            }

            if (soloBajoStock) {
                if (p.getStockActual() == null || p.getStockMinimo() == null || p.getStockActual() > p.getStockMinimo()) {
                    return false;
                }
            }

            return true;
        });
    }

    @FXML
    public void handleNuevoProducto() {
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Producto");
        dialog.setHeaderText("Registrar un nuevo producto en el catálogo");

        ButtonType btnGuardarType = new ButtonType("Guardar Producto", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        VBox form = new VBox(10);

        long totalProds = productoService.listarTodos().size() + 1;
        String codigoSugerido = String.format("PROD-%04d", totalProds);

        TextField txtCod = new TextField(codigoSugerido);
        TextField txtNom = new TextField();
        txtNom.setPromptText("Nombre del producto");
        TextField txtMarca = new TextField();
        txtMarca.setPromptText("Marca (ej. Gloria, Costeño, San Fernando...)");
        TextField txtPCompra = new TextField();
        txtPCompra.setPromptText("0.00");
        TextField txtPVenta = new TextField();
        txtPVenta.setPromptText("0.00");
        TextField txtStockMin = new TextField("5");

        ComboBox<Categoria> cbCat = new ComboBox<>();
        cbCat.getItems().setAll(categoriaService.listarActivas());
        cbCat.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Categoria c) {
                return c != null ? c.getNombre() : "";
            }

            @Override
            public Categoria fromString(String string) {
                return null;
            }
        });
        if (!cbCat.getItems().isEmpty()) {
            cbCat.setValue(cbCat.getItems().get(0));
        }

        Button btnNuevaCat = new Button("➕ Crear Categoría");
        btnNuevaCat.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-cursor: hand;");
        btnNuevaCat.setOnAction(e -> {
            TextInputDialog catDialog = new TextInputDialog();
            catDialog.setTitle("Nueva Categoría");
            catDialog.setHeaderText("Crear categoría de productos");
            catDialog.setContentText("Nombre:");
            catDialog.showAndWait().ifPresent(nombreCat -> {
                if (!nombreCat.isBlank()) {
                    Categoria nuevaC = new Categoria();
                    nuevaC.setNombre(nombreCat.trim());
                    nuevaC.setDescripcion("Creada desde catálogo");
                    categoriaService.registrar(nuevaC);
                    cbCat.getItems().setAll(categoriaService.listarActivas());
                    cbCat.setValue(nuevaC);
                    cargarCategoriasFiltro();
                }
            });
        });

        HBox catBox = new HBox(8, cbCat, btnNuevaCat);
        catBox.setAlignment(Pos.CENTER_LEFT);

        ComboBox<UnidadMedida> cbUnidad = new ComboBox<>();
        cbUnidad.getItems().setAll(UnidadMedida.UNIDAD, UnidadMedida.KILOGRAMO,
                UnidadMedida.GRAMO, UnidadMedida.LITRO, UnidadMedida.MILILITRO);
        cbUnidad.setValue(UnidadMedida.UNIDAD);

        form.getChildren().addAll(
                new Label("Código (Autogenerado / Editable):"), txtCod,
                new Label("Nombre del Producto:"), txtNom,
                new Label("Marca del Producto:"), txtMarca,
                new Label("Categoría:"), catBox,
                new Label("Precio Compra (S/):"), txtPCompra,
                new Label("Precio Venta (S/):"), txtPVenta,
                new Label("Stock Mínimo de Seguridad:"), txtStockMin,
                new Label("Unidad de Medida:"), cbUnidad
        );

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardarType) {
                try {
                    Producto p = new Producto();
                    p.setCodigo(txtCod.getText().trim());
                    p.setNombre(txtNom.getText().trim());
                    p.setMarca(txtMarca.getText() != null && !txtMarca.getText().isBlank() ? txtMarca.getText().trim() : "Genérico");
                    p.setPrecioCompra(new BigDecimal(txtPCompra.getText().trim()));
                    p.setPrecioVenta(new BigDecimal(txtPVenta.getText().trim()));
                    p.setStockActual(0);
                    p.setStockMinimo(Integer.parseInt(txtStockMin.getText().trim()));
                    p.setCategoria(cbCat.getValue());
                    p.setUnidadMedida(cbUnidad.getValue());
                    p.setEstado(true);
                    return p;
                } catch (Exception e) {
                    mostrarAlertaError("Error de validación", "Verifique que los campos numéricos y obligatorios sean válidos.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nuevo -> {
            try {
                Producto guardado = productoService.registrar(nuevo);
                cargarProductos();
                administrarPresentaciones(guardado);
            } catch (Exception ex) {
                mostrarAlertaError("Error al guardar", ex.getMessage());
            }
        });
    }

    private void editarProducto(Producto p) {
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Editar Producto — " + p.getCodigo());
        dialog.setHeaderText("Modificar datos de: " + p.getNombre());

        ButtonType btnGuardarType = new ButtonType("Guardar Cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        VBox form = new VBox(10);

        TextField txtCod = new TextField(p.getCodigo());
        txtCod.setDisable(true); // El código no se altera para conservar trazabilidad
        TextField txtNom = new TextField(p.getNombre());
        TextField txtMarca = new TextField(p.getMarca() != null ? p.getMarca() : "");
        txtMarca.setPromptText("Marca (ej. Gloria, Costeño, San Fernando...)");
        TextField txtPCompra = new TextField(p.getPrecioCompra() != null ? p.getPrecioCompra().toString() : "0.00");
        TextField txtPVenta = new TextField(p.getPrecioVenta() != null ? p.getPrecioVenta().toString() : "0.00");
        TextField txtStockMin = new TextField(p.getStockMinimo() != null ? p.getStockMinimo().toString() : "5");

        ComboBox<Categoria> cbCat = new ComboBox<>();
        cbCat.getItems().setAll(categoriaService.listarActivas());
        cbCat.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Categoria c) {
                return c != null ? c.getNombre() : "";
            }

            @Override
            public Categoria fromString(String string) {
                return null;
            }
        });
        if (p.getCategoria() != null) {
            for (Categoria c : cbCat.getItems()) {
                if (c.getId().equals(p.getCategoria().getId())) {
                    cbCat.setValue(c);
                    break;
                }
            }
        }

        ComboBox<UnidadMedida> cbUnidad = new ComboBox<>();
        cbUnidad.getItems().setAll(p.getUnidadMedida());
        cbUnidad.setDisable(true); // requiere migración manual si cambia la unidad base
        cbUnidad.setValue(p.getUnidadMedida() != null ? p.getUnidadMedida() : UnidadMedida.UNIDAD);

        form.getChildren().addAll(
                new Label("Código (Inmutable):"), txtCod,
                new Label("Nombre del Producto:"), txtNom,
                new Label("Marca del Producto:"), txtMarca,
                new Label("Categoría:"), cbCat,
                new Label("Precio Compra (S/):"), txtPCompra,
                new Label("Precio Venta (S/):"), txtPVenta,
                new Label("Stock Mínimo de Seguridad:"), txtStockMin,
                new Label("Unidad de Medida:"), cbUnidad
        );

        Button btnPresentaciones = new Button("Administrar presentaciones");
        btnPresentaciones.setOnAction(e -> administrarPresentaciones(p));
        form.getChildren().addAll(new Label("Presentaciones del producto:"), btnPresentaciones);

        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardarType) {
                try {
                    Producto actualizado = new Producto();
                    actualizado.setCodigo(p.getCodigo());
                    actualizado.setNombre(txtNom.getText().trim());
                    actualizado.setMarca(txtMarca.getText() != null && !txtMarca.getText().isBlank() ? txtMarca.getText().trim() : "Genérico");
                    actualizado.setDescripcion(p.getDescripcion());
                    actualizado.setPrecioCompra(new BigDecimal(txtPCompra.getText().trim()));
                    actualizado.setPrecioVenta(new BigDecimal(txtPVenta.getText().trim()));
                    actualizado.setStockActual(p.getStockActual());
                    actualizado.setStockMinimo(Integer.parseInt(txtStockMin.getText().trim()));
                    actualizado.setCategoria(cbCat.getValue());
                    actualizado.setUnidadMedida(cbUnidad.getValue());
                    actualizado.setProveedor(p.getProveedor());
                    actualizado.setEstado(p.getEstado());
                    return actualizado;
                } catch (Exception e) {
                    mostrarAlertaError("Error de validación", "Verifique que los campos numéricos sean válidos.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(actualizado -> {
            try {
                productoService.actualizar(p.getId(), actualizado);
                cargarProductos();
            } catch (Exception ex) {
                mostrarAlertaError("Error al actualizar", ex.getMessage());
            }
        });
    }

    private void administrarPresentaciones(Producto producto) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Presentaciones: " + producto.getNombre());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        TableView<PresentacionProducto> tabla = new TableView<>();
        tabla.setPrefHeight(220);
        TableColumn<PresentacionProducto, String> nombre = new TableColumn<>("Presentación");
        nombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombrePresentacion()));
        TableColumn<PresentacionProducto, Integer> factor = new TableColumn<>("Factor a unidad base");
        factor.setCellValueFactory(new PropertyValueFactory<>("factorConversion"));
        TableColumn<PresentacionProducto, String> estado = new TableColumn<>("Estado");
        estado.setCellValueFactory(c -> new SimpleStringProperty(Boolean.TRUE.equals(c.getValue().getEstado()) ? "ACTIVO" : "INACTIVO"));
        tabla.getColumns().addAll(nombre, factor, estado);
        Button agregar = new Button("Agregar");
        Button editar = new Button("Editar seleccionado");
        Button cambiarEstado = new Button("Desactivar seleccionado");
        Runnable actualizarEstado = () -> {
            PresentacionProducto seleccionada = tabla.getSelectionModel().getSelectedItem();
            boolean inactiva = seleccionada != null && !Boolean.TRUE.equals(seleccionada.getEstado());
            cambiarEstado.setText(inactiva ? "Activar seleccionado" : "Desactivar seleccionado");
            cambiarEstado.setDisable(seleccionada == null ||
                    (!inactiva && presentacionService.esBase(producto.getId(), seleccionada.getId())));
        };
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> actualizarEstado.run());
        Runnable recargar = () -> {
            PresentacionProducto seleccionada = tabla.getSelectionModel().getSelectedItem();
            Long idSeleccionado = seleccionada == null ? null : seleccionada.getId();
            tabla.getItems().setAll(presentacionService.listar(producto.getId(), false));
            if (idSeleccionado != null) tabla.getItems().stream()
                    .filter(p -> idSeleccionado.equals(p.getId())).findFirst()
                    .ifPresent(p -> tabla.getSelectionModel().select(p));
            actualizarEstado.run();
        };
        recargar.run();
        agregar.setOnAction(e -> editarPresentacion(producto, null, recargar));
        editar.setOnAction(e -> {
            PresentacionProducto seleccionada = tabla.getSelectionModel().getSelectedItem();
            if (seleccionada != null) editarPresentacion(producto, seleccionada, recargar);
        });
        cambiarEstado.setOnAction(e -> {
            PresentacionProducto seleccionada = tabla.getSelectionModel().getSelectedItem();
            if (seleccionada == null) return;
            try {
                if (Boolean.TRUE.equals(seleccionada.getEstado())) presentacionService.desactivar(seleccionada.getId());
                else presentacionService.activar(seleccionada.getId());
                recargar.run();
            } catch (Exception ex) { mostrarAlertaError("No se pudo cambiar el estado", ex.getMessage()); }
        });
        VBox contenido = new VBox(10, new Label("Unidad base: " + producto.getUnidadMedida()),
                tabla, new HBox(8, agregar, editar, cambiarEstado));
        contenido.setPrefWidth(570);
        dialog.getDialogPane().setContent(contenido);
        dialog.showAndWait();
    }

    private void editarPresentacion(Producto producto, PresentacionProducto actual, Runnable recargar) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(actual == null ? "Agregar presentación" : "Editar presentación");
        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);
        TextField nombre = new TextField(actual == null ? "" : actual.getNombrePresentacion());
        TextField factor = new TextField(actual == null ? "" : actual.getFactorConversion().toString());
        boolean esBase = actual != null && presentacionService.esBase(producto.getId(), actual.getId());
        factor.setDisable(esBase);
        dialog.getDialogPane().setContent(new VBox(8, new Label("Nombre:"), nombre,
                new Label(esBase ? "Unidades base por presentación (la base conserva factor 1):"
                        : "Unidades base por presentación:"), factor));
        Button boton = (Button) dialog.getDialogPane().lookupButton(guardar);
        boton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            try {
                presentacionService.guardar(producto.getId(), actual == null ? null : actual.getId(),
                        nombre.getText(), Integer.valueOf(factor.getText().trim()));
                recargar.run();
            } catch (Exception ex) {
                mostrarAlertaError("Presentación inválida", ex.getMessage());
                e.consume();
            }
        });
        dialog.showAndWait();
    }

    private void eliminarProducto(Producto p) {
        if (p.getStockActual() != null && p.getStockActual() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No se puede dar de baja");
            alert.setHeaderText("El producto cuenta con existencias activas (" + p.getStockActual() + " " + (p.getUnidadMedida() != null ? p.getUnidadMedida() : "unidades") + ")");
            alert.setContentText("Para mantener la integridad contable, no se puede desactivar un producto con stock mayor a cero.\n\nPrimero debe liquidar el stock mediante ventas o registrar un movimiento de 'AJUSTE' a cero en el inventario.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Seguro que desea dar de baja al producto " + p.getNombre() + "?");
        confirm.setContentText("El producto pasará a estado inactivo (eliminación lógica).");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productoService.eliminar(p.getId());
                    cargarProductos();
                } catch (Exception ex) {
                    mostrarAlertaError("Error al dar de baja", ex.getMessage());
                }
            }
        });
    }

    private void mostrarAlertaError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    @FXML
    public void handleRegistrarMerma() {
        abrirDialogoMerma(null);
    }

    private void abrirDialogoMerma(Producto productoSeleccionado) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Registrar Merma / Baja de Inventario");
        dialog.setHeaderText("Gestión de Bajas por Vencimiento, Rotura o Deterioro\nEsta acción reduce existencias y audita la pérdida sin alterar la caja ni falsear ventas.");

        ButtonType btnRegistrarType = new ButtonType("Registrar Baja", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnRegistrarType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        ComboBox<Producto> cbProducto = new ComboBox<>();
        List<Producto> productosDisponibles = productoService.listarActivos();
        cbProducto.setItems(FXCollections.observableArrayList(productosDisponibles));
        cbProducto.setPrefWidth(380);
        cbProducto.setConverter(new StringConverter<>() {
            @Override
            public String toString(Producto p) {
                if (p == null) return "";
                String marca = (p.getMarca() != null && !p.getMarca().isBlank()) ? " (" + p.getMarca() + ")" : "";
                return "[" + p.getCodigo() + "] " + p.getNombre() + marca + " - Stock actual: " + p.getStockActual();
            }

            @Override
            public Producto fromString(String string) {
                return null;
            }
        });

        if (productoSeleccionado != null) {
            for (Producto p : productosDisponibles) {
                if (p.getId().equals(productoSeleccionado.getId())) {
                    cbProducto.setValue(p);
                    break;
                }
            }
        }

        Label lblStockInfo = new Label();
        lblStockInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #0284c7;");

        Spinner<Integer> spCantidad = new Spinner<>(1, 9999, 1);
        spCantidad.setEditable(true);
        spCantidad.setPrefWidth(130);

        ComboBox<MotivoMerma> cbMotivo = new ComboBox<>(FXCollections.observableArrayList(MotivoMerma.values()));
        cbMotivo.setValue(MotivoMerma.VENCIMIENTO);
        cbMotivo.setPrefWidth(260);

        DatePicker dpVencimiento = new DatePicker();
        dpVencimiento.setPromptText("Opcional: Fecha caducidad");
        dpVencimiento.setPrefWidth(200);

        Label lblPerdidaEstimada = new Label("Pérdida valorizada: S/ 0.00");
        lblPerdidaEstimada.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");

        TextField txtObservacion = new TextField();
        txtObservacion.setPromptText("Ej. Empaque roto en transporte / Lote vencido...");
        txtObservacion.setPrefWidth(380);

        Runnable actualizarCalculos = () -> {
            Producto p = cbProducto.getValue();
            if (p != null) {
                lblStockInfo.setText("Stock disponible: " + p.getStockActual() + " | Costo unitario: S/ " + (p.getPrecioCompra() != null ? p.getPrecioCompra() : BigDecimal.ZERO));
                int cant = spCantidad.getValue() != null ? spCantidad.getValue() : 1;
                BigDecimal costoUnit = p.getPrecioCompra() != null ? p.getPrecioCompra() : BigDecimal.ZERO;
                BigDecimal totalPerdida = costoUnit.multiply(BigDecimal.valueOf(cant));
                lblPerdidaEstimada.setText("Pérdida valorizada: S/ " + totalPerdida.setScale(2, RoundingMode.HALF_UP));
            } else {
                lblStockInfo.setText("");
                lblPerdidaEstimada.setText("Pérdida valorizada: S/ 0.00");
            }
        };

        cbProducto.valueProperty().addListener((obs, oldV, newV) -> actualizarCalculos.run());
        spCantidad.valueProperty().addListener((obs, oldV, newV) -> actualizarCalculos.run());
        spCantidad.getEditor().textProperty().addListener((obs, oldV, newV) -> {
            try {
                int val = Integer.parseInt(newV.trim());
                if (val > 0) {
                    spCantidad.getValueFactory().setValue(val);
                }
            } catch (Exception ignored) {}
            actualizarCalculos.run();
        });

        actualizarCalculos.run();

        grid.add(new Label("Producto a dar de baja:"), 0, 0);
        grid.add(cbProducto, 1, 0);
        grid.add(lblStockInfo, 1, 1);

        grid.add(new Label("Cantidad a dar de baja:"), 0, 2);
        grid.add(spCantidad, 1, 2);

        grid.add(new Label("Motivo de la merma:"), 0, 3);
        grid.add(cbMotivo, 1, 3);

        grid.add(new Label("Fecha de vencimiento:"), 0, 4);
        grid.add(dpVencimiento, 1, 4);

        grid.add(new Label("Pérdida económica:"), 0, 5);
        grid.add(lblPerdidaEstimada, 1, 5);

        grid.add(new Label("Observación / Justificación:"), 0, 6);
        grid.add(txtObservacion, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Button btnRegistrar = (Button) dialog.getDialogPane().lookupButton(btnRegistrarType);
        btnRegistrar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Producto prod = cbProducto.getValue();
            if (prod == null) {
                mostrarAlertaError("Selección requerida", "Debe seleccionar un producto del inventario.");
                event.consume();
                return;
            }
            int cant = spCantidad.getValue() != null ? spCantidad.getValue() : 0;
            if (cant <= 0) {
                mostrarAlertaError("Cantidad inválida", "La cantidad debe ser mayor a 0.");
                event.consume();
                return;
            }
            if (prod.getStockActual() < cant) {
                mostrarAlertaError("Stock insuficiente", "No puede dar de baja más unidades de las disponibles.\nStock actual: " + prod.getStockActual() + ", solicitado: " + cant);
                event.consume();
                return;
            }
        });

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnRegistrarType) {
                try {
                    Producto prod = cbProducto.getValue();
                    int cant = spCantidad.getValue();
                    MotivoMerma motivo = cbMotivo.getValue();
                    LocalDate fechaVenc = dpVencimiento.getValue();
                    String obs = txtObservacion.getText();

                    Usuario usuario = LoginViewController.getUsuarioSesion();
                    if (usuario == null) {
                        usuario = usuarioService.listarTodos().stream()
                                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No hay ningún usuario activo registrado para auditar la merma"));
                    }

                    mermaService.registrarMerma(prod.getId(), usuario.getId(), cant, motivo, fechaVenc, obs);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Merma Registrada");
                    alert.setHeaderText("Baja de mercadería procesada correctamente");
                    alert.setContentText(String.format("Se dieron de baja %d unidades de '%s'.\nNuevo stock disponible: %d unidades.\nEl movimiento fue auditado en Kardex.",
                            cant, prod.getNombre(), prod.getStockActual() - cant));
                    alert.showAndWait();

                    cargarProductos();
                } catch (Exception ex) {
                    mostrarAlertaError("Error al registrar merma", ex.getMessage());
                }
            }
        });
    }

    @FXML
    public void handleExportarInventarioExcel() {
        List<Producto> productos = productoService.listarActivos();
        if (productos.isEmpty()) {
            mostrarAlertaError("Sin datos", "No hay productos en el catálogo para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Inventario en Excel");
        fileChooser.setInitialFileName("Inventario_SGCIVORP_" + LocalDate.now() + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Libro de Excel (*.xlsx)", "*.xlsx"));

        Window window = tblProductos.getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);
        if (file != null) {
            try {
                exportacionService.exportarInventarioCsv(file, productos);
                mostrarAlertaExitoExportacion(file);
            } catch (Exception e) {
                mostrarAlertaError("Error al exportar", e.getMessage());
            }
        }
    }

    private void mostrarAlertaExitoExportacion(File file) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exportación Exitosa");
        alert.setHeaderText("Archivo generado correctamente para Microsoft Excel");
        alert.setContentText("Ubicación: " + file.getAbsolutePath() + "\n\n¿Desea abrir el archivo en Excel ahora mismo?");

        ButtonType btnAbrir = new ButtonType("Abrir en Excel", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCerrar = new ButtonType("Listo", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getDialogPane().getButtonTypes().setAll(btnAbrir, btnCerrar);

        alert.showAndWait().ifPresent(res -> {
            if (res == btnAbrir) {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(file);
                    }
                } catch (Exception ex) {
                    mostrarAlertaError("No se pudo abrir automáticamente", ex.getMessage());
                }
            }
        });
    }
}
