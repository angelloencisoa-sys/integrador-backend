package pe.edu.utp.Grupo06.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.service.IUsuarioService;

@Component
public class MainLayoutController {

    @Autowired
    private IUsuarioService usuarioService;

    @FXML
    private Label lblUsuarioNombre;

    @FXML
    private Label lblUsuarioRol;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnPos;

    @FXML private Button btnPedidos;

    @FXML
    private Button btnProductos;

    @FXML
    private Button btnAlertas;

    @FXML
    private Button btnCompras;

    @FXML
    private Button btnReportes;

    @FXML
    private Button btnAdmin;

    @FXML
    public void initialize() {
        Usuario usuario = LoginViewController.getUsuarioSesion();
        if (usuario != null) {
            lblUsuarioNombre.setText(usuario.getNombreCompleto());
            lblUsuarioRol.setText("Rol: " + (usuario.getRol() != null ? usuario.getRol().getNombre().name() : "N/A"));

            // Restricciones por Rol (RNF03):
            // Si es VENDEDOR, solo accede a Punto de Venta e Inventario;
            // Ocultamos Compras, Reportes y Administración (solo para ADMINISTRADOR).
            boolean esAdmin = usuario.getRol() != null && usuario.getRol().getNombre() == pe.edu.utp.Grupo06.model.enums.RolNombre.ADMINISTRADOR;
            
            btnCompras.setVisible(esAdmin);
            btnCompras.setManaged(esAdmin);
            
            btnReportes.setVisible(esAdmin);
            btnReportes.setManaged(esAdmin);

            btnAdmin.setVisible(esAdmin);
            btnAdmin.setManaged(esAdmin);
        }

        // Cargar vista inicial
        mostrarPos();
    }

    @FXML
    public void mostrarPos() {
        activarBoton(btnPos);
        cargarVista("/fxml/ventas_pos.fxml");
    }

    @FXML public void mostrarPedidos() { activarBoton(btnPedidos); cargarVista("/fxml/pedidos_web.fxml"); }

    @FXML
    public void mostrarProductos() {
        activarBoton(btnProductos);
        cargarVista("/fxml/productos.fxml");
    }

    @FXML
    public void mostrarAlertas() {
        activarBoton(btnAlertas);
        cargarVista("/fxml/alertas.fxml");
    }

    @FXML
    public void mostrarCompras() {
        activarBoton(btnCompras);
        cargarVista("/fxml/compras.fxml");
    }

    @FXML
    public void mostrarReportes() {
        activarBoton(btnReportes);
        cargarVista("/fxml/reportes.fxml");
    }

    @FXML
    public void mostrarAdmin() {
        activarBoton(btnAdmin);
        cargarVista("/fxml/admin_panel.fxml");
    }

    @FXML
    public void handleAbrirPerfil() {
        Usuario usuario = LoginViewController.getUsuarioSesion();
        if (usuario == null) return;

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Mi Perfil");
        dialog.setHeaderText("Configuración de perfil y credenciales");

        ButtonType btnGuardar = new ButtonType("Guardar Cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        VBox form = new VBox(10);
        form.setPrefWidth(380);

        Label lblUser = new Label("Usuario: @" + usuario.getUsername());
        lblUser.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");

        Label lblRolBadge = new Label("Rol: " + (usuario.getRol() != null ? usuario.getRol().getNombre().name() : "N/A"));
        lblRolBadge.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;");

        TextField txtNombre = new TextField(usuario.getNombreCompleto());
        TextField txtEmail = new TextField(usuario.getEmail() != null ? usuario.getEmail() : "");
        TextField txtTel = new TextField(usuario.getTelefono() != null ? usuario.getTelefono() : "");

        CheckBox chkPass = new CheckBox("Modificar contraseña de acceso");
        VBox passBox = new VBox(6);
        PasswordField txtPassActual = new PasswordField();
        txtPassActual.setPromptText("Contraseña actual");
        PasswordField txtNuevaPass = new PasswordField();
        txtNuevaPass.setPromptText("Nueva contraseña (mínimo 6 caracteres)");
        PasswordField txtConfirmPass = new PasswordField();
        txtConfirmPass.setPromptText("Confirmar nueva contraseña");

        passBox.getChildren().addAll(
                new Label("Contraseña Actual:"), txtPassActual,
                new Label("Nueva Contraseña:"), txtNuevaPass,
                new Label("Confirmar Nueva Contraseña:"), txtConfirmPass
        );
        passBox.setDisable(true);

        chkPass.selectedProperty().addListener((obs, oldV, isSel) -> passBox.setDisable(!isSel));

        form.getChildren().addAll(
                new HBox(10, lblUser, lblRolBadge),
                new Separator(),
                new Label("Nombre Completo:"), txtNombre,
                new Label("Correo Electrónico:"), txtEmail,
                new Label("Teléfono:"), txtTel,
                new Separator(),
                chkPass,
                passBox
        );
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    String nombre = txtNombre.getText().trim();
                    String email = txtEmail.getText().trim();
                    String tel = txtTel.getText().trim();
                    String passActual = null;
                    String nuevaPass = null;

                    if (chkPass.isSelected()) {
                        passActual = txtPassActual.getText();
                        nuevaPass = txtNuevaPass.getText();
                        String confirm = txtConfirmPass.getText();

                        if (passActual == null || passActual.isBlank()) {
                            mostrarAlerta("Validación", "Debe ingresar su contraseña actual.", Alert.AlertType.ERROR);
                            return null;
                        }
                        if (nuevaPass == null || nuevaPass.length() < 6) {
                            mostrarAlerta("Validación", "La nueva contraseña debe tener al menos 6 caracteres.", Alert.AlertType.ERROR);
                            return null;
                        }
                        if (!nuevaPass.equals(confirm)) {
                            mostrarAlerta("Validación", "Las contraseñas nuevas no coinciden.", Alert.AlertType.ERROR);
                            return null;
                        }
                    }

                    Usuario actualizado = usuarioService.actualizarPerfil(usuario.getId(), nombre, email, tel, passActual, nuevaPass);
                    // Actualizar datos en la sesión local
                    usuario.setNombreCompleto(actualizado.getNombreCompleto());
                    usuario.setEmail(actualizado.getEmail());
                    usuario.setTelefono(actualizado.getTelefono());
                    lblUsuarioNombre.setText(actualizado.getNombreCompleto());

                    return true;
                } catch (Exception ex) {
                    mostrarAlerta("Error al actualizar perfil", ex.getMessage(), Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(guardado -> {
            if (guardado) {
                mostrarAlerta("Perfil Actualizado", "Tus datos personales y credenciales han sido actualizados con éxito.", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void handleCerrarSesion() {
        LoginViewController.cerrarSesion();
    }

    private void activarBoton(Button botonActivo) {
        Button[] botones = {btnPos, btnPedidos, btnProductos, btnAlertas, btnCompras, btnReportes, btnAdmin};
        for (Button b : botones) {
            if (b != null) {
                b.getStyleClass().remove("sidebar-btn-active");
                if (!b.getStyleClass().contains("sidebar-btn")) {
                    b.getStyleClass().add("sidebar-btn");
                }
            }
        }
        if (botonActivo != null) {
            botonActivo.getStyleClass().remove("sidebar-btn");
            botonActivo.getStyleClass().add("sidebar-btn-active");
        }
    }

    private void cargarVista(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(JavaFxApplication.getContext()::getBean);
            Node vista = loader.load();
            contentArea.getChildren().setAll(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
