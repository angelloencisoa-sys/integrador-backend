package pe.edu.utp.Grupo06.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ImpresionUtil {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Imprime un ticket en formato térmico o estándar usando la API nativa de Windows/JavaFX.
     */
    public static boolean imprimirTicket(String textoTicket, Window parentWindow) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Impresora no detectada", "No se encontró ningún servicio o impresora configurada en el sistema.");
            return false;
        }

        boolean proceed = job.showPrintDialog(parentWindow);
        if (!proceed) {
            return false;
        }

        // Crear nodo imprimible con estilo monoespaciado tipo boleta térmica
        Text textNode = new Text(textoTicket);
        textNode.setFont(Font.font("Consolas", FontWeight.NORMAL, 9.5));

        VBox printContainer = new VBox(textNode);
        printContainer.setPadding(new Insets(10));
        printContainer.setStyle("-fx-background-color: white;");

        // Ajustar escala al ancho de la página seleccionada
        PageLayout pageLayout = job.getJobSettings().getPageLayout();
        double printableWidth = pageLayout.getPrintableWidth();
        double nodeWidth = printContainer.getBoundsInParent().getWidth();

        if (nodeWidth > printableWidth && nodeWidth > 0) {
            double scaleFactor = printableWidth / nodeWidth;
            printContainer.getTransforms().add(new Scale(scaleFactor, scaleFactor));
        }

        boolean printed = job.printPage(printContainer);
        if (printed) {
            job.endJob();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Impresión Exitosa", "El ticket ha sido enviado correctamente a la cola de impresión.");
            return true;
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Impresión", "No se pudo completar el envío del documento a la impresora.");
            return false;
        }
    }

    /**
     * Imprime un Reporte Ejecutivo formal membretado en hoja A4 con recuadros de firma.
     */
    public static boolean imprimirReporteEjecutivo(String titulo, String subtitulo, String resumenHtml, String detalleTexto, Window parentWindow) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Impresora no detectada", "No se encontró ningún servicio o impresora configurada en el sistema.");
            return false;
        }

        // Configurar por defecto hoja A4 vertical
        Printer printer = job.getPrinter();
        PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
        job.getJobSettings().setPageLayout(pageLayout);

        boolean proceed = job.showPrintDialog(parentWindow);
        if (!proceed) {
            return false;
        }

        // Contenedor principal de la hoja A4
        VBox a4Sheet = new VBox(12);
        a4Sheet.setPadding(new Insets(25, 30, 25, 30));
        a4Sheet.setStyle("-fx-background-color: white;");
        a4Sheet.setPrefWidth(pageLayout.getPrintableWidth());

        // 1. Membrete oficial
        Label lblEmpresa = new Label("BODEGA & MINIMARKET SGCIVORP");
        lblEmpresa.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblEmpresa.setStyle("-fx-text-fill: #0f172a;");

        Label lblRuc = new Label("RUC: 20601234567  |  Av. Principal 123, Lima  |  Tel: (01) 456-7890");
        lblRuc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        lblRuc.setStyle("-fx-text-fill: #475569;");

        VBox headerBox = new VBox(2, lblEmpresa, lblRuc);
        headerBox.setAlignment(Pos.CENTER);

        // 2. Título del Reporte y Rango
        Label lblTitulo = new Label(titulo.toUpperCase());
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTitulo.setStyle("-fx-text-fill: #0284c7;");

        Label lblSub = new Label(subtitulo + "  |  Emitido: " + LocalDateTime.now().format(DTF));
        lblSub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        lblSub.setStyle("-fx-text-fill: #64748b;");

        VBox titleBox = new VBox(2, lblTitulo, lblSub);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        // 3. Cuadro Resumen Financiero
        Label lblResumen = new Label(resumenHtml);
        lblResumen.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10.5));
        lblResumen.setStyle("-fx-padding: 8; -fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");

        // 4. Detalle de Datos / Tabla
        Text txtDetalle = new Text(detalleTexto);
        txtDetalle.setFont(Font.font("Consolas", FontWeight.NORMAL, 9));

        VBox detalleBox = new VBox(txtDetalle);
        detalleBox.setPadding(new Insets(6));
        detalleBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 4;");

        // 5. Pie de Página con Firmas de Conformidad
        Label lblFirmaAdmin = new Label("_________________________________\nFirma de Administración\nBodega SGCIVORP");
        lblFirmaAdmin.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 9.5));
        lblFirmaAdmin.setAlignment(Pos.CENTER);

        Label lblFirmaContador = new Label("_________________________________\nFirma de Contabilidad / Auditoría\nConformidad");
        lblFirmaContador.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 9.5));
        lblFirmaContador.setAlignment(Pos.CENTER);

        HBox firmasBox = new HBox(120, lblFirmaAdmin, lblFirmaContador);
        firmasBox.setAlignment(Pos.CENTER);
        firmasBox.setPadding(new Insets(35, 0, 10, 0));

        a4Sheet.getChildren().addAll(
                headerBox,
                new Separator(),
                titleBox,
                lblResumen,
                detalleBox,
                firmasBox
        );

        // Escalar si fuera necesario
        double printableWidth = pageLayout.getPrintableWidth();
        double nodeWidth = a4Sheet.getBoundsInParent().getWidth();
        if (nodeWidth > printableWidth && nodeWidth > 0) {
            double scale = printableWidth / nodeWidth;
            a4Sheet.getTransforms().add(new Scale(scale, scale));
        }

        boolean printed = job.printPage(a4Sheet);
        if (printed) {
            job.endJob();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Reporte Enviado", "El reporte formal A4 ha sido enviado exitosamente a la impresora / PDF.");
            return true;
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Impresión", "No se pudo imprimir el reporte.");
            return false;
        }
    }

    private static void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
