package pe.edu.utp.Grupo06.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.service.IUsuarioService;

@Component
public class LoginViewController {

    @Autowired
    private IUsuarioService usuarioService;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    @FXML
    private Button btnLogin;

    @FXML
    private ProgressIndicator progressIndicator;

    private static Usuario usuarioSesion;

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    @FXML
    public void handleLogin() {
        String username = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
        String password = txtPassword.getText() != null ? txtPassword.getText() : "";

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor ingrese usuario y contraseña");
            return;
        }

        btnLogin.setDisable(true);
        progressIndicator.setVisible(true);
        progressIndicator.setManaged(true);
        lblError.setVisible(false);
        lblError.setManaged(false);

        new Thread(() -> {
            try {
                Usuario usuario = usuarioService.validarCredencialesPersonal(username, password);
                usuarioSesion = usuario;

                Platform.runLater(() -> {
                    JavaFxApplication.mostrarVentanaPrincipal();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    mostrarError(ex.getMessage() != null ? ex.getMessage() : "Error al iniciar sesión");
                    btnLogin.setDisable(false);
                    progressIndicator.setVisible(false);
                    progressIndicator.setManaged(false);
                });
            }
        }).start();
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    public static Usuario getUsuarioSesion() {
        return usuarioSesion;
    }

    public static void cerrarSesion() {
        usuarioSesion = null;
        JavaFxApplication.mostrarLogin();
    }
}
