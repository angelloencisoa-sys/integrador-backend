package pe.edu.utp.Grupo06.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.utp.Grupo06.model.Categoria;
import pe.edu.utp.Grupo06.model.Proveedor;
import pe.edu.utp.Grupo06.model.Rol;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.model.enums.RolNombre;
import pe.edu.utp.Grupo06.repository.RolRepository;
import pe.edu.utp.Grupo06.service.ICategoriaService;
import pe.edu.utp.Grupo06.service.IProveedorService;
import pe.edu.utp.Grupo06.service.IUsuarioService;

import java.util.List;

@Component
public class AdminPanelViewController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IProveedorService proveedorService;

    @Autowired
    private RolRepository rolRepository;

    // Tabla Usuarios
    @FXML
    private TextField txtBuscarUsuario;
    @FXML
    private Label lblTotalUsuarios;
    @FXML
    private TableView<Usuario> tblUsuarios;
    @FXML
    private TableColumn<Usuario, Long> colUsuId;
    @FXML
    private TableColumn<Usuario, String> colUsuNombre;
    @FXML
    private TableColumn<Usuario, String> colUsuUsername;
    @FXML
    private TableColumn<Usuario, String> colUsuEmail;
    @FXML
    private TableColumn<Usuario, String> colUsuTelefono;
    @FXML
    private TableColumn<Usuario, String> colUsuRol;
    @FXML
    private TableColumn<Usuario, Boolean> colUsuEstado;
    @FXML
    private TableColumn<Usuario, Void> colUsuAcciones;

    // Tabla Categorias
    @FXML
    private TextField txtBuscarCategoria;
    @FXML
    private Label lblTotalCategorias;
    @FXML
    private TableView<Categoria> tblCategorias;
    @FXML
    private TableColumn<Categoria, Long> colCatId;
    @FXML
    private TableColumn<Categoria, String> colCatNombre;
    @FXML
    private TableColumn<Categoria, String> colCatDesc;
    @FXML
    private TableColumn<Categoria, Boolean> colCatEstado;
    @FXML
    private TableColumn<Categoria, Void> colCatAcciones;

    // Tabla Proveedores
    @FXML
    private TextField txtBuscarProveedor;
    @FXML
    private Label lblTotalProveedores;
    @FXML
    private TableView<Proveedor> tblProveedores;
    @FXML
    private TableColumn<Proveedor, String> colProvRuc;
    @FXML
    private TableColumn<Proveedor, String> colProvRazon;
    @FXML
    private TableColumn<Proveedor, String> colProvContacto;
    @FXML
    private TableColumn<Proveedor, String> colProvTelefono;
    @FXML
    private TableColumn<Proveedor, String> colProvEmail;
    @FXML
    private TableColumn<Proveedor, Boolean> colProvEstado;
    @FXML
    private TableColumn<Proveedor, Void> colProvAcciones;

    private ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();
    private FilteredList<Usuario> filteredUsuarios;

    private ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    private FilteredList<Categoria> filteredCategorias;

    private ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();
    private FilteredList<Proveedor> filteredProveedores;

    @FXML
    public void initialize() {
        configurarTablaUsuarios();
        configurarTablaCategorias();
        configurarTablaProveedores();
        handleRefrescarTodo();
    }

    private void configurarTablaUsuarios() {
        colUsuId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsuNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colUsuUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUsuEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUsuTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colUsuRol.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getRol() != null ? c.getValue().getRol().getNombre().name() : "N/A"));
        
        colUsuEstado.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getActivo()));
        colUsuEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                if (empty || activo == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(activo ? "ACTIVO" : "INACTIVO");
                    badge.setStyle(activo
                            ? "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;"
                            : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        colUsuAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnToggle = new Button();
            private final HBox box = new HBox(6, btnEditar, btnToggle);

            {
                box.setAlignment(Pos.CENTER);
                btnEditar.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
                btnEditar.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    handleEditarUsuario(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    boolean activo = Boolean.TRUE.equals(u.getActivo());
                    btnToggle.setText(activo ? "Desactivar" : "Activar");
                    btnToggle.setStyle(activo
                            ? "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;"
                            : "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
                    btnToggle.setOnAction(e -> handleToggleEstadoUsuario(u));
                    setGraphic(box);
                }
            }
        });

        filteredUsuarios = new FilteredList<>(listaUsuarios, p -> true);
        if (txtBuscarUsuario != null) {
            txtBuscarUsuario.textProperty().addListener((obs, oldVal, newVal) -> {
                String term = newVal == null ? "" : newVal.trim().toLowerCase();
                filteredUsuarios.setPredicate(u -> {
                    if (term.isEmpty()) return true;
                    boolean mNom = u.getNombreCompleto() != null && u.getNombreCompleto().toLowerCase().contains(term);
                    boolean mUser = u.getUsername() != null && u.getUsername().toLowerCase().contains(term);
                    boolean mEmail = u.getEmail() != null && u.getEmail().toLowerCase().contains(term);
                    boolean mRol = u.getRol() != null && u.getRol().getNombre().name().toLowerCase().contains(term);
                    return mNom || mUser || mEmail || mRol;
                });
                actualizarConteoUsuarios();
            });
        }

        SortedList<Usuario> sorted = new SortedList<>(filteredUsuarios);
        sorted.comparatorProperty().bind(tblUsuarios.comparatorProperty());
        tblUsuarios.setItems(sorted);
    }

    private void configurarTablaCategorias() {
        colCatId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCatNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCatDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        colCatEstado.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getEstado()));
        colCatEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(estado ? "ACTIVO" : "INACTIVO");
                    badge.setStyle(estado
                            ? "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;"
                            : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        colCatAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnToggle = new Button();
            private final HBox box = new HBox(6, btnEditar, btnToggle);

            {
                box.setAlignment(Pos.CENTER);
                btnEditar.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
                btnEditar.setOnAction(e -> {
                    Categoria c = getTableView().getItems().get(getIndex());
                    handleEditarCategoria(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Categoria c = getTableView().getItems().get(getIndex());
                    boolean activo = Boolean.TRUE.equals(c.getEstado());
                    btnToggle.setText(activo ? "Desactivar" : "Activar");
                    btnToggle.setStyle(activo
                            ? "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;"
                            : "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
                    btnToggle.setOnAction(e -> handleToggleEstadoCategoria(c));
                    setGraphic(box);
                }
            }
        });

        filteredCategorias = new FilteredList<>(listaCategorias, p -> true);
        if (txtBuscarCategoria != null) {
            txtBuscarCategoria.textProperty().addListener((obs, oldVal, newVal) -> {
                String term = newVal == null ? "" : newVal.trim().toLowerCase();
                filteredCategorias.setPredicate(c -> {
                    if (term.isEmpty()) return true;
                    boolean mNom = c.getNombre() != null && c.getNombre().toLowerCase().contains(term);
                    boolean mDesc = c.getDescripcion() != null && c.getDescripcion().toLowerCase().contains(term);
                    return mNom || mDesc;
                });
                actualizarConteoCategorias();
            });
        }

        SortedList<Categoria> sorted = new SortedList<>(filteredCategorias);
        sorted.comparatorProperty().bind(tblCategorias.comparatorProperty());
        tblCategorias.setItems(sorted);
    }

    private void configurarTablaProveedores() {
        colProvRuc.setCellValueFactory(new PropertyValueFactory<>("ruc"));
        colProvRazon.setCellValueFactory(new PropertyValueFactory<>("razonSocial"));
        colProvContacto.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colProvTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colProvEmail.setCellValueFactory(new PropertyValueFactory<>("correo"));

        colProvEstado.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getEstado()));
        colProvEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(estado ? "ACTIVO" : "INACTIVO");
                    badge.setStyle(estado
                            ? "-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;"
                            : "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                    setText(null);
                }
            }
        });

        colProvAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnToggle = new Button();
            private final HBox box = new HBox(6, btnEditar, btnToggle);

            {
                box.setAlignment(Pos.CENTER);
                btnEditar.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
                btnEditar.setOnAction(e -> {
                    Proveedor p = getTableView().getItems().get(getIndex());
                    handleEditarProveedor(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Proveedor p = getTableView().getItems().get(getIndex());
                    boolean activo = Boolean.TRUE.equals(p.getEstado());
                    btnToggle.setText(activo ? "Desactivar" : "Activar");
                    btnToggle.setStyle(activo
                            ? "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;"
                            : "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
                    btnToggle.setOnAction(e -> handleToggleEstadoProveedor(p));
                    setGraphic(box);
                }
            }
        });

        filteredProveedores = new FilteredList<>(listaProveedores, p -> true);
        if (txtBuscarProveedor != null) {
            txtBuscarProveedor.textProperty().addListener((obs, oldVal, newVal) -> {
                String term = newVal == null ? "" : newVal.trim().toLowerCase();
                filteredProveedores.setPredicate(p -> {
                    if (term.isEmpty()) return true;
                    boolean mRuc = p.getRuc() != null && p.getRuc().toLowerCase().contains(term);
                    boolean mRazon = p.getRazonSocial() != null && p.getRazonSocial().toLowerCase().contains(term);
                    boolean mMail = p.getCorreo() != null && p.getCorreo().toLowerCase().contains(term);
                    boolean mTel = p.getTelefono() != null && p.getTelefono().toLowerCase().contains(term);
                    return mRuc || mRazon || mMail || mTel;
                });
                actualizarConteoProveedores();
            });
        }

        SortedList<Proveedor> sorted = new SortedList<>(filteredProveedores);
        sorted.comparatorProperty().bind(tblProveedores.comparatorProperty());
        tblProveedores.setItems(sorted);
    }

    @FXML
    public void handleRefrescarTodo() {
        try {
            listaUsuarios.setAll(usuarioService.listarTodos());
            listaCategorias.setAll(categoriaService.listarTodas());
            listaProveedores.setAll(proveedorService.listarTodos());

            actualizarConteoUsuarios();
            actualizarConteoCategorias();
            actualizarConteoProveedores();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actualizarConteoUsuarios() {
        if (lblTotalUsuarios != null) {
            long activos = listaUsuarios.stream().filter(u -> Boolean.TRUE.equals(u.getActivo())).count();
            lblTotalUsuarios.setText(String.format("Mostrando %d de %d usuarios (%d activos)",
                    filteredUsuarios.size(), listaUsuarios.size(), activos));
        }
    }

    private void actualizarConteoCategorias() {
        if (lblTotalCategorias != null) {
            long activas = listaCategorias.stream().filter(c -> Boolean.TRUE.equals(c.getEstado())).count();
            lblTotalCategorias.setText(String.format("Mostrando %d de %d categorías (%d activas)",
                    filteredCategorias.size(), listaCategorias.size(), activas));
        }
    }

    private void actualizarConteoProveedores() {
        if (lblTotalProveedores != null) {
            long activos = listaProveedores.stream().filter(p -> Boolean.TRUE.equals(p.getEstado())).count();
            lblTotalProveedores.setText(String.format("Mostrando %d de %d proveedores (%d activos)",
                    filteredProveedores.size(), listaProveedores.size(), activos));
        }
    }

    // ==================== USUARIOS ====================

    @FXML
    public void handleNuevoUsuario() {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Crear Nuevo Usuario / Empleado");
        dialog.setHeaderText("Registro de cuenta de acceso al sistema");

        ButtonType btnGuardar = new ButtonType("Guardar Usuario", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        VBox form = new VBox(8);
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Ej: Juan Pérez");
        TextField txtUser = new TextField();
        txtUser.setPromptText("Ej: jperez");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Mínimo 6 caracteres");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("juan@bodega.com");
        TextField txtTel = new TextField();
        txtTel.setPromptText("999888777");

        ComboBox<RolNombre> cbRol = new ComboBox<>();
        cbRol.getItems().addAll(RolNombre.VENDEDOR, RolNombre.ADMINISTRADOR);
        cbRol.setValue(RolNombre.VENDEDOR);

        form.getChildren().addAll(
                new Label("Nombre Completo:"), txtNombre,
                new Label("Usuario (Username):"), txtUser,
                new Label("Contraseña Inicial:"), txtPass,
                new Label("Correo Electrónico:"), txtEmail,
                new Label("Teléfono:"), txtTel,
                new Label("Rol de Acceso:"), cbRol
        );
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    Usuario u = new Usuario();
                    u.setNombreCompleto(txtNombre.getText().trim());
                    u.setUsername(txtUser.getText().trim());
                    u.setPassword(txtPass.getText());
                    u.setEmail(txtEmail.getText().trim());
                    u.setTelefono(txtTel.getText().trim());
                    u.setActivo(true);

                    Rol rol = rolRepository.findByNombre(cbRol.getValue())
                            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                    u.setRol(rol);
                    return u;
                } catch (Exception ex) {
                    mostrarAlerta("Datos Inválidos", "Complete todos los campos obligatorios.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(u -> {
            try {
                usuarioService.registrar(u);
                handleRefrescarTodo();
                mostrarAlerta("Usuario Creado", "La cuenta fue registrada con éxito.");
            } catch (Exception ex) {
                mostrarAlertaError("Error al registrar", ex.getMessage());
            }
        });
    }

    private void handleEditarUsuario(Usuario u) {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Editar Usuario");
        dialog.setHeaderText("Modificar datos de la cuenta: @" + u.getUsername());

        ButtonType btnGuardar = new ButtonType("Guardar Cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        VBox form = new VBox(8);
        TextField txtNombre = new TextField(u.getNombreCompleto());
        TextField txtEmail = new TextField(u.getEmail() != null ? u.getEmail() : "");
        TextField txtTel = new TextField(u.getTelefono() != null ? u.getTelefono() : "");

        ComboBox<RolNombre> cbRol = new ComboBox<>();
        cbRol.getItems().addAll(RolNombre.VENDEDOR, RolNombre.ADMINISTRADOR);
        if (u.getRol() != null) {
            cbRol.setValue(u.getRol().getNombre());
        }

        CheckBox chkCambiarPass = new CheckBox("Restablecer / Asignar nueva contraseña");
        PasswordField txtNuevaPass = new PasswordField();
        txtNuevaPass.setPromptText("Nueva contraseña (mínimo 6 caracteres)");
        txtNuevaPass.setDisable(true);

        chkCambiarPass.selectedProperty().addListener((obs, oldV, isSel) -> {
            txtNuevaPass.setDisable(!isSel);
            if (!isSel) txtNuevaPass.clear();
        });

        form.getChildren().addAll(
                new Label("Nombre Completo:"), txtNombre,
                new Label("Correo Electrónico:"), txtEmail,
                new Label("Teléfono:"), txtTel,
                new Label("Rol de Acceso:"), cbRol,
                new Separator(),
                chkCambiarPass,
                txtNuevaPass
        );
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    Usuario editado = new Usuario();
                    editado.setNombreCompleto(txtNombre.getText().trim());
                    editado.setEmail(txtEmail.getText().trim());
                    editado.setTelefono(txtTel.getText().trim());

                    Rol rol = rolRepository.findByNombre(cbRol.getValue())
                            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                    editado.setRol(rol);

                    if (chkCambiarPass.isSelected()) {
                        editado.setPassword(txtNuevaPass.getText());
                    } else {
                        editado.setPassword("");
                    }
                    return editado;
                } catch (Exception ex) {
                    mostrarAlertaError("Datos Inválidos", "Revise los campos ingresados.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(editado -> {
            try {
                usuarioService.actualizar(u.getId(), editado);
                handleRefrescarTodo();
                mostrarAlerta("Usuario Actualizado", "Los datos de @" + u.getUsername() + " han sido actualizados.");
            } catch (Exception ex) {
                mostrarAlertaError("Error al actualizar", ex.getMessage());
            }
        });
    }

    private void handleToggleEstadoUsuario(Usuario u) {
        Usuario usuarioSesion = LoginViewController.getUsuarioSesion();
        if (usuarioSesion != null && usuarioSesion.getId().equals(u.getId())) {
            mostrarAlertaError("Acción denegada", "No puedes desactivar tu propia cuenta mientras estás en sesión.");
            return;
        }

        boolean nuevoEstado = !Boolean.TRUE.equals(u.getActivo());
        String accion = nuevoEstado ? "activar" : "desactivar";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar cambio de estado");
        confirm.setHeaderText("¿Seguro que desea " + accion + " al usuario @" + u.getUsername() + "?");
        confirm.setContentText(nuevoEstado
                ? "El usuario podrá volver a ingresar al sistema con sus credenciales."
                : "El usuario ya no podrá iniciar sesión en el sistema.");

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    usuarioService.cambiarEstado(u.getId(), nuevoEstado);
                    handleRefrescarTodo();
                    mostrarAlerta("Estado actualizado", "El usuario @" + u.getUsername() + " ahora está " + (nuevoEstado ? "ACTIVO" : "INACTIVO") + ".");
                } catch (Exception ex) {
                    mostrarAlertaError("Error al cambiar estado", ex.getMessage());
                }
            }
        });
    }

    // ==================== CATEGORÍAS ====================

    @FXML
    public void handleNuevaCategoria() {
        Dialog<Categoria> dialog = new Dialog<>();
        dialog.setTitle("Nueva Categoría");
        dialog.setHeaderText("Registro de categoría de productos");

        ButtonType btnGuardar = new ButtonType("Guardar Categoría", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        VBox form = new VBox(8);
        TextField txtNom = new TextField();
        txtNom.setPromptText("Ej: Bebidas, Lácteos, Abarrotes");
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Descripción opcional de la categoría");
        txtDesc.setPrefRowCount(3);

        form.getChildren().addAll(
                new Label("Nombre de la Categoría:"), txtNom,
                new Label("Descripción:"), txtDesc
        );
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                if (txtNom.getText().isBlank()) {
                    mostrarAlertaError("Campo requerido", "El nombre de la categoría es obligatorio.");
                    return null;
                }
                Categoria c = new Categoria();
                c.setNombre(txtNom.getText().trim());
                c.setDescripcion(txtDesc.getText().trim());
                c.setEstado(true);
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            try {
                categoriaService.registrar(c);
                handleRefrescarTodo();
                mostrarAlerta("Categoría Creada", "Categoría '" + c.getNombre() + "' registrada con éxito.");
            } catch (Exception ex) {
                mostrarAlertaError("Error al registrar", ex.getMessage());
            }
        });
    }

    private void handleEditarCategoria(Categoria c) {
        Dialog<Categoria> dialog = new Dialog<>();
        dialog.setTitle("Editar Categoría");
        dialog.setHeaderText("Modificar categoría: " + c.getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar Cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        VBox form = new VBox(8);
        TextField txtNom = new TextField(c.getNombre());
        TextArea txtDesc = new TextArea(c.getDescripcion() != null ? c.getDescripcion() : "");
        txtDesc.setPrefRowCount(3);

        form.getChildren().addAll(
                new Label("Nombre de la Categoría:"), txtNom,
                new Label("Descripción:"), txtDesc
        );
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                if (txtNom.getText().isBlank()) {
                    mostrarAlertaError("Campo requerido", "El nombre de la categoría no puede estar vacío.");
                    return null;
                }
                Categoria editada = new Categoria();
                editada.setNombre(txtNom.getText().trim());
                editada.setDescripcion(txtDesc.getText().trim());
                editada.setEstado(c.getEstado());
                return editada;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(editada -> {
            try {
                categoriaService.actualizar(c.getId(), editada);
                handleRefrescarTodo();
                mostrarAlerta("Categoría Actualizada", "Los datos de la categoría han sido guardados.");
            } catch (Exception ex) {
                mostrarAlertaError("Error al actualizar", ex.getMessage());
            }
        });
    }

    private void handleToggleEstadoCategoria(Categoria c) {
        boolean nuevoEstado = !Boolean.TRUE.equals(c.getEstado());
        String accion = nuevoEstado ? "activar" : "desactivar";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar cambio de estado");
        confirm.setHeaderText("¿Seguro que desea " + accion + " la categoría '" + c.getNombre() + "'?");
        confirm.setContentText(nuevoEstado
                ? "La categoría volverá a estar disponible para asignar nuevos productos."
                : "Se validará que no tenga productos activos asociados antes de desactivar.");

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    if (!nuevoEstado) {
                        categoriaService.eliminar(c.getId()); // Realiza la baja lógica con validación de productos
                    } else {
                        Categoria cat = new Categoria();
                        cat.setNombre(c.getNombre());
                        cat.setDescripcion(c.getDescripcion());
                        cat.setEstado(true);
                        categoriaService.actualizar(c.getId(), cat);
                    }
                    handleRefrescarTodo();
                    mostrarAlerta("Estado actualizado", "La categoría '" + c.getNombre() + "' ahora está " + (nuevoEstado ? "ACTIVA" : "INACTIVA") + ".");
                } catch (Exception ex) {
                    mostrarAlertaError("Operación denegada", ex.getMessage());
                }
            }
        });
    }

    // ==================== PROVEEDORES ====================

    @FXML
    public void handleNuevoProveedor() {
        Dialog<Proveedor> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Proveedor");
        dialog.setHeaderText("Registro de empresa proveedora de mercadería");

        ButtonType btnGuardar = new ButtonType("Guardar Proveedor", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        VBox form = new VBox(8);
        TextField txtRuc = new TextField();
        txtRuc.setPromptText("11 dígitos (Ej: 20123456789)");
        TextField txtRazon = new TextField();
        txtRazon.setPromptText("Razón Social (Ej: Gloria S.A.)");
        TextField txtDir = new TextField();
        txtDir.setPromptText("Dirección comercial");
        TextField txtTel = new TextField();
        txtTel.setPromptText("Teléfono");
        TextField txtCorreo = new TextField();
        txtCorreo.setPromptText("ventas@empresa.com");

        form.getChildren().addAll(
                new Label("RUC:"), txtRuc,
                new Label("Razón Social:"), txtRazon,
                new Label("Dirección:"), txtDir,
                new Label("Teléfono:"), txtTel,
                new Label("Correo Electrónico:"), txtCorreo
        );
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    Proveedor p = new Proveedor();
                    p.setRuc(txtRuc.getText().trim());
                    p.setRazonSocial(txtRazon.getText().trim());
                    p.setDireccion(txtDir.getText().trim());
                    p.setTelefono(txtTel.getText().trim());
                    p.setCorreo(txtCorreo.getText().trim());
                    p.setEstado(true);
                    return p;
                } catch (Exception ex) {
                    mostrarAlertaError("Datos Inválidos", "Complete los campos correctamente.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            try {
                proveedorService.registrar(p);
                handleRefrescarTodo();
                mostrarAlerta("Proveedor Creado", "Proveedor '" + p.getRazonSocial() + "' registrado.");
            } catch (Exception ex) {
                mostrarAlertaError("Error al registrar", ex.getMessage());
            }
        });
    }

    private void handleEditarProveedor(Proveedor p) {
        Dialog<Proveedor> dialog = new Dialog<>();
        dialog.setTitle("Editar Proveedor");
        dialog.setHeaderText("Modificar datos de la empresa proveedora");

        ButtonType btnGuardar = new ButtonType("Guardar Cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        VBox form = new VBox(8);
        TextField txtRuc = new TextField(p.getRuc());
        TextField txtRazon = new TextField(p.getRazonSocial());
        TextField txtDir = new TextField(p.getDireccion() != null ? p.getDireccion() : "");
        TextField txtTel = new TextField(p.getTelefono() != null ? p.getTelefono() : "");
        TextField txtCorreo = new TextField(p.getCorreo() != null ? p.getCorreo() : "");

        form.getChildren().addAll(
                new Label("RUC:"), txtRuc,
                new Label("Razón Social:"), txtRazon,
                new Label("Dirección Comercial:"), txtDir,
                new Label("Teléfono:"), txtTel,
                new Label("Correo Electrónico:"), txtCorreo
        );
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    Proveedor editado = new Proveedor();
                    editado.setRuc(txtRuc.getText().trim());
                    editado.setRazonSocial(txtRazon.getText().trim());
                    editado.setDireccion(txtDir.getText().trim());
                    editado.setTelefono(txtTel.getText().trim());
                    editado.setCorreo(txtCorreo.getText().trim());
                    editado.setEstado(p.getEstado());
                    return editado;
                } catch (Exception ex) {
                    mostrarAlertaError("Datos Inválidos", "Revise los campos ingresados.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(editado -> {
            try {
                proveedorService.actualizar(p.getId(), editado);
                handleRefrescarTodo();
                mostrarAlerta("Proveedor Actualizado", "Los datos de '" + editado.getRazonSocial() + "' han sido guardados.");
            } catch (Exception ex) {
                mostrarAlertaError("Error al actualizar", ex.getMessage());
            }
        });
    }

    private void handleToggleEstadoProveedor(Proveedor p) {
        boolean nuevoEstado = !Boolean.TRUE.equals(p.getEstado());
        String accion = nuevoEstado ? "activar" : "desactivar";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar cambio de estado");
        confirm.setHeaderText("¿Seguro que desea " + accion + " al proveedor '" + p.getRazonSocial() + "'?");
        confirm.setContentText(nuevoEstado
                ? "El proveedor volverá a estar disponible para compras y catálogo."
                : "Se validará que no tenga productos activos asignados antes de desactivar.");

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    if (!nuevoEstado) {
                        proveedorService.eliminar(p.getId()); // Realiza baja lógica con validación de productos
                    } else {
                        Proveedor prov = new Proveedor();
                        prov.setRuc(p.getRuc());
                        prov.setRazonSocial(p.getRazonSocial());
                        prov.setDireccion(p.getDireccion());
                        prov.setTelefono(p.getTelefono());
                        prov.setCorreo(p.getCorreo());
                        prov.setEstado(true);
                        proveedorService.actualizar(p.getId(), prov);
                    }
                    handleRefrescarTodo();
                    mostrarAlerta("Estado actualizado", "El proveedor '" + p.getRazonSocial() + "' ahora está " + (nuevoEstado ? "ACTIVO" : "INACTIVO") + ".");
                } catch (Exception ex) {
                    mostrarAlertaError("Operación denegada", ex.getMessage());
                }
            }
        });
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
