package pe.edu.utp.Grupo06.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import pe.edu.utp.Grupo06.Grupo06Application;

public class JavaFxApplication extends Application {

    private static ConfigurableApplicationContext applicationContext;
    private static Stage primaryStage;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(Grupo06Application.class)
                .web(org.springframework.boot.WebApplicationType.NONE)
                .headless(false)
                .run(getParameters().getRaw().toArray(String[]::new));
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        mostrarLogin();
    }

    public static void mostrarLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(JavaFxApplication.class.getResource("/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root, 480, 560);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(JavaFxApplication.class.getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("SGCIVORP — Iniciar Sesión");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mostrarVentanaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(JavaFxApplication.class.getResource("/fxml/main_layout.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1180, 720);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            scene.getStylesheets().add(JavaFxApplication.class.getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("SGCIVORP — Sistema de Control de Inventario y Ventas");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static ConfigurableApplicationContext getContext() {
        return applicationContext;
    }

    @Override
    public void stop() {
        if (applicationContext != null) {
            applicationContext.close();
        }
        Platform.exit();
    }
}
