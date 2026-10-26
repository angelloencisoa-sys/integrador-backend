package pe.edu.utp.Grupo06.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.utp.Grupo06.model.AlertaReposicion;
import pe.edu.utp.Grupo06.model.enums.EstadoAlerta;
import pe.edu.utp.Grupo06.service.IAlertaReposicionService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AlertasViewController {

    @Autowired
    private IAlertaReposicionService alertaService;

    @FXML
    private ComboBox<String> cbFiltroEstado;

    @FXML
    private TableView<AlertaReposicion> tblAlertas;

    @FXML
    private TableColumn<AlertaReposicion, Long> colAlertaId;

    @FXML
    private TableColumn<AlertaReposicion, String> colProducto;

    @FXML
    private TableColumn<AlertaReposicion, Integer> colStockReg;

    @FXML
    private TableColumn<AlertaReposicion, Integer> colStockMin;

    @FXML
    private TableColumn<AlertaReposicion, Integer> colSugerida;

    @FXML
    private TableColumn<AlertaReposicion, LocalDateTime> colFecha;

    @FXML
    private TableColumn<AlertaReposicion, String> colEstado;

    @FXML
    private TableColumn<AlertaReposicion, Void> colAcciones;

    private final ObservableList<AlertaReposicion> listaAlertas = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        configurarFiltroSelector();
        configurarColumnas();
        cargarAlertas();
    }

    private void configurarFiltroSelector() {
        cbFiltroEstado.getItems().addAll(
                "⏳ Solo Pendientes",
                "✅ Solo Atendidas",
                "🚫 Solo Descartadas",
                "📋 Todas las Alertas (Historial)"
        );
        cbFiltroEstado.setValue("⏳ Solo Pendientes");
        cbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> cargarAlertas());
    }

    private void configurarColumnas() {
        colAlertaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProducto.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProducto() != null ?
                        cellData.getValue().getProducto().getNombre() + " (" + cellData.getValue().getProducto().getCodigo() + ")" : ""));

        colStockReg.setCellValueFactory(new PropertyValueFactory<>("stockRegistrado"));
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colSugerida.setCellValueFactory(new PropertyValueFactory<>("cantidadSugerida"));

        // Ordenamiento cronológico real
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaGeneracion"));
        colFecha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstado() != null ?
                        cellData.getValue().getEstado().name() : ""));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnAtender = new Button("✔️ Atender");
            private final Label lblObs = new Label();

            {
                btnAtender.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;");
                btnAtender.setOnAction(event -> {
                    AlertaReposicion alerta = getTableView().getItems().get(getIndex());
                    atenderAlerta(alerta);
                });
                lblObs.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AlertaReposicion alerta = getTableView().getItems().get(getIndex());
                    if (alerta.getEstado() == EstadoAlerta.PENDIENTE) {
                        HBox pane = new HBox(btnAtender);
                        pane.setAlignment(Pos.CENTER);
                        setGraphic(pane);
                    } else {
                        String obs = alerta.getObservacion() != null ? alerta.getObservacion() : alerta.getEstado().name();
                        if (obs.length() > 22) obs = obs.substring(0, 19) + "...";
                        lblObs.setText(obs);
                        Tooltip.install(lblObs, new Tooltip(alerta.getObservacion()));
                        HBox pane = new HBox(lblObs);
                        pane.setAlignment(Pos.CENTER);
                        setGraphic(pane);
                    }
                }
            }
        });

        tblAlertas.setItems(listaAlertas);
    }

    @FXML
    public void cargarAlertas() {
        try {
            String filtro = cbFiltroEstado.getValue();
            if (filtro == null) filtro = "⏳ Solo Pendientes";

            List<AlertaReposicion> alertas;
            switch (filtro) {
                case "✅ Solo Atendidas":
                    alertas = alertaService.listarPorEstado(EstadoAlerta.ATENDIDA);
                    break;
                case "🚫 Solo Descartadas":
                    alertas = alertaService.listarPorEstado(EstadoAlerta.DESCARTADA);
                    break;
                case "📋 Todas las Alertas (Historial)":
                    alertas = alertaService.listarTodas();
                    break;
                case "⏳ Solo Pendientes":
                default:
                    alertas = alertaService.listarPendientes();
                    break;
            }

            listaAlertas.setAll(alertas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void atenderAlerta(AlertaReposicion alerta) {
        TextInputDialog dialog = new TextInputDialog("Compra de reposición realizada");
        dialog.setTitle("Atender Alerta de Reposición");
        dialog.setHeaderText("Registrar atención de reposición para:\n" + alerta.getProducto().getNombre());
        dialog.setContentText("Observación o N° Comprobante de Compra:");

        dialog.showAndWait().ifPresent(obs -> {
            try {
                alertaService.atenderAlerta(alerta.getId(), obs);
                cargarAlertas();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
