package pe.edu.utp.Grupo06;

/**
 * Clase intermediaria (Launcher) requerida en Java 11+ para que
 * la JVM cargue los componentes gráficos de JavaFX en el Classpath
 * sin conflictos de módulos.
 */
public class AppLauncher {
    public static void main(String[] args) {
        javafx.application.Application.launch(pe.edu.utp.Grupo06.view.JavaFxApplication.class, args);
    }
}
