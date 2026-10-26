package pe.edu.utp.Grupo06.view;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.concurrent.Task;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import pe.edu.utp.Grupo06.service.PedidoService;
import pe.edu.utp.Grupo06.dto.pedido.PedidoDTO;
import java.io.ByteArrayInputStream;
import java.util.function.*;

@Component @Scope("prototype")
public class PedidosWebViewController {
 private final PedidoService servicio;
 public PedidosWebViewController(PedidoService servicio){this.servicio=servicio;}
 @FXML private TableView<PedidoDTO> tabla;
 @FXML private TableColumn<PedidoDTO,String> numero,cliente,estado,total;
 @FXML private TextArea detalle;
 @FXML private Label mensaje;
 @FXML private ImageView comprobante;
 @FXML private CheckBox confirmado,reponer;
 @FXML private TextField motivo,codigo;
 @FXML private Button aprobar,rechazar,preparar,listo,reembolsar;
 private boolean ocupado;
 private Long operador;
 private Long actor(){return operador;}
 @FXML public void initialize(){
  var usuario=LoginViewController.getUsuarioSesion();operador=usuario==null?null:usuario.getId();
  numero.setCellValueFactory(c->new ReadOnlyStringWrapper((c.getValue().demo()?"DEMO · ":"")+c.getValue().numero()));
  cliente.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().cliente()));
  estado.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().estado()));
  total.setCellValueFactory(c->new ReadOnlyStringWrapper("S/ "+c.getValue().total()));
  tabla.getSelectionModel().selectedItemProperty().addListener((o,a,b)->seleccionar(b));refrescar();
 }
 private <T> void tarea(Supplier<T> trabajo,Consumer<T> ok){if(ocupado||operador==null)return;ocupado=true;mensaje.setText("Procesando…");actualizarBotones();
  Task<T> task=new Task<>(){protected T call(){return trabajo.get();}};
  task.setOnSucceeded(e->{ocupado=false;mensaje.setText("Operación completada");ok.accept(task.getValue());actualizarBotones();});
  task.setOnFailed(e->{ocupado=false;mensaje.setText(task.getException().getMessage());actualizarBotones();});
  Thread hilo=new Thread(task,"pedidos-web-ui");hilo.setDaemon(true);hilo.start();
 }
 @FXML public void refrescar(){Long anterior=tabla.getSelectionModel().getSelectedItem()==null?null:tabla.getSelectionModel().getSelectedItem().id();tarea(()->servicio.todos(actor()),lista->{tabla.getItems().setAll(lista);lista.stream().filter(p->p.id().equals(anterior)).findFirst().ifPresent(p->tabla.getSelectionModel().select(p));});}
 private void seleccionar(PedidoDTO p){comprobante.setImage(null);confirmado.setSelected(false);reponer.setSelected(false);motivo.clear();
  if(p==null){detalle.clear();actualizarBotones();return;}
  StringBuilder s=new StringBuilder(p.numero()+" · "+p.estado()+"\n"+(p.demo()?"DEMOSTRACIÓN: sin transferencia real. La venta D001 se excluye de ingresos.\n":"Verifique el abono en la cuenta Yape, no solo la captura.\n"));
  for(var l:p.lineas())s.append(l.cantidad()).append(" × ").append(l.nombre()).append(" · S/ ").append(l.precio()).append("\n");
  s.append("Total: S/ ").append(p.total()).append("\nReserva hasta: ").append(p.vence()).append("\n");
  for(var e:p.eventos())s.append(e.fecha()).append(" · ").append(e.mensaje()).append("\n");detalle.setText(s.toString());actualizarBotones();
 }
 private void actualizarBotones(){var p=tabla.getSelectionModel().getSelectedItem();String s=p==null?"":p.estado();
  aprobar.setDisable(ocupado||!s.equals("PENDIENTE_VERIFICACION"));rechazar.setDisable(aprobar.isDisabled());
  preparar.setDisable(ocupado||!s.equals("PAGADO"));listo.setDisable(ocupado||!s.equals("EN_PREPARACION"));
  boolean admin=LoginViewController.getUsuarioSesion()!=null&&LoginViewController.getUsuarioSesion().getRol().getNombre().name().equals("ADMINISTRADOR");
  reembolsar.setDisable(ocupado||!admin||!java.util.List.of("PAGADO","EN_PREPARACION","LISTO_PARA_RECOJO").contains(s));
 }
 private void accion(Function<Long,PedidoDTO> trabajo){var p=tabla.getSelectionModel().getSelectedItem();if(p==null)return;tarea(()->trabajo.apply(p.id()),actual->{int i=tabla.getItems().indexOf(p);tabla.getItems().set(i,actual);tabla.getSelectionModel().select(actual);});}
 @FXML public void verComprobante(){var p=tabla.getSelectionModel().getSelectedItem();if(p==null||!p.tieneComprobante())return;tarea(()->servicio.comprobante(p.id(),actor(),true),c->{comprobante.setImage(new Image(new ByteArrayInputStream(c.getContenido())));mensaje.setText("Referencia: "+c.getOperacion()+" · Confirme el monto y destinatario en Yape.");});}
 @FXML public void aprobar(){boolean check=confirmado.isSelected();accion(id->servicio.aprobar(id,actor(),check));}
 @FXML public void rechazar(){String razon=motivo.getText();accion(id->servicio.rechazar(id,actor(),razon));}
 @FXML public void preparar(){accion(id->servicio.preparar(id,actor()));}
 @FXML public void listo(){accion(id->servicio.listo(id,actor()));}
 @FXML public void entregar(){String valor=codigo.getText();tarea(()->servicio.entregar(actor(),valor),p->{codigo.clear();mensaje.setText("Entrega registrada: "+p.numero());tabla.getItems().removeIf(x->x.id().equals(p.id()));tabla.getItems().addFirst(p);tabla.getSelectionModel().select(p);});}
 @FXML public void reembolsar(){var dialog=new Alert(Alert.AlertType.CONFIRMATION,"Confirme únicamente después de realizar el reembolso externo (o simularlo en DEMO). La reposición requiere revisar físicamente los productos.",ButtonType.OK,ButtonType.CANCEL);dialog.setHeaderText("Confirmar reembolso total");if(dialog.showAndWait().orElse(ButtonType.CANCEL)!=ButtonType.OK)return;String ref=motivo.getText();boolean devolver=reponer.isSelected();accion(id->servicio.reembolsar(id,actor(),ref,devolver));}
}
