package pe.edu.utp.Grupo06;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.NONE)
class PedidosFxmlTest {
 @Autowired ApplicationContext context;
 @Test void cargaVistaPedidosSinAbrirVentana()throws Exception{
  try{Platform.startup(()->{});}catch(IllegalStateException alreadyStarted){}
  var latch=new CountDownLatch(1);var error=new AtomicReference<Throwable>();
  Platform.runLater(()->{try{
   var loader=new FXMLLoader(getClass().getResource("/fxml/pedidos_web.fxml"));loader.setControllerFactory(context::getBean);Parent root=loader.load();new Scene(root,1050,850);root.applyCss();root.layout();
   assertNotNull(root.lookup("#tabla"));assertEquals("Aprobar pago",((Button)root.lookup("#aprobar")).getText());
   var snap=root.snapshot(null,null);var pixels=snap.getPixelReader();var img=new java.awt.image.BufferedImage((int)snap.getWidth(),(int)snap.getHeight(),java.awt.image.BufferedImage.TYPE_INT_ARGB);for(int y=0;y<img.getHeight();y++)for(int x=0;x<img.getWidth();x++)img.setRGB(x,y,pixels.getArgb(x,y));java.nio.file.Files.createDirectories(java.nio.file.Path.of(".local"));javax.imageio.ImageIO.write(img,"png",new java.io.File(".local/pedidos-panel.png"));
  }catch(Throwable e){error.set(e);}finally{latch.countDown();}});
  assertTrue(latch.await(20,TimeUnit.SECONDS));if(error.get()!=null)throw new AssertionError(error.get());
 }
}
