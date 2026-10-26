package pe.edu.utp.Grupo06.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import pe.edu.utp.Grupo06.dto.pedido.*;
import pe.edu.utp.Grupo06.service.*;
import java.security.Principal;
import java.util.List;
import java.io.*;
@RestController @ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET)
public class PedidoController {
 private final PedidoService pedidos;private final TiendaConfig tienda;
 public PedidoController(PedidoService pedidos,TiendaConfig tienda){this.pedidos=pedidos;this.tienda=tienda;}
 private Long id(Principal p){return Long.parseLong(p.getName());}
 @GetMapping("/api/tienda") public TiendaConfig.Datos tienda(){return tienda.datos();}
 @PostMapping("/api/pedidos") public PedidoDTO crear(Principal p,@Valid @RequestBody CrearPedido body){return pedidos.crear(id(p),body);}
 @GetMapping("/api/pedidos") public List<PedidoDTO> mios(Principal p){return pedidos.mios(id(p));}
 @GetMapping("/api/pedidos/{pedido}") public PedidoDTO ver(Principal p,@PathVariable Long pedido){return pedidos.ver(pedido,id(p));}
 @PostMapping("/api/pedidos/{pedido}/cancelar") public PedidoDTO cancelar(Principal p,@PathVariable Long pedido){return pedidos.cancelar(pedido,id(p));}
 @PostMapping(value="/api/pedidos/{pedido}/comprobante",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public PedidoDTO adjuntar(Principal p,@PathVariable Long pedido,@RequestParam MultipartFile archivo,@RequestParam String operacion)throws IOException {
  if(archivo.getSize()>3*1024*1024)throw new IllegalArgumentException("Máximo 3 MB");return pedidos.adjuntar(pedido,id(p),archivo.getBytes(),operacion);
 }
 @GetMapping("/api/pedidos/{pedido}/comprobante") public ResponseEntity<byte[]> imagen(Principal p,@PathVariable Long pedido){return imagen(pedido,id(p),false);}
 @GetMapping("/api/personal/pedidos/{pedido}/comprobante") public ResponseEntity<byte[]> imagenPersonal(Principal p,@PathVariable Long pedido){return imagen(pedido,id(p),true);}
 private ResponseEntity<byte[]> imagen(Long pedido,Long actor,boolean interno){var c=pedidos.comprobante(pedido,actor,interno);return ResponseEntity.ok().contentType(MediaType.parseMediaType(c.getTipo())).header("X-Content-Type-Options","nosniff").cacheControl(CacheControl.noStore()).body(c.getContenido());}
 @GetMapping(value="/api/pedidos/{pedido}/qr",produces=MediaType.IMAGE_PNG_VALUE) public ResponseEntity<byte[]> qr(Principal p,@PathVariable Long pedido)throws Exception {
  var dto=pedidos.ver(pedido,id(p));if(dto.codigoRecojo()==null || !List.of("PAGADO","EN_PREPARACION","LISTO_PARA_RECOJO").contains(dto.estado()))throw new IllegalArgumentException("Código de recojo no disponible");
  var matrix=new com.google.zxing.qrcode.QRCodeWriter().encode(dto.codigoRecojo(),com.google.zxing.BarcodeFormat.QR_CODE,300,300);
  var image=new java.awt.image.BufferedImage(300,300,java.awt.image.BufferedImage.TYPE_INT_RGB);
  for(int y=0;y<300;y++)for(int x=0;x<300;x++)image.setRGB(x,y,matrix.get(x,y)?0:0xffffff);
  var out=new ByteArrayOutputStream();javax.imageio.ImageIO.write(image,"png",out);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out.toByteArray());
 }
 @GetMapping("/api/personal/pedidos") public List<PedidoDTO> todos(Principal p){return pedidos.todos(id(p));}
 @GetMapping("/api/personal/pedidos/{pedido}") public PedidoDTO verPersonal(Principal p,@PathVariable Long pedido){return pedidos.verPersonal(pedido,id(p));}
 public record Accion(Boolean confirmado,String motivo,Boolean reponer){}
 @PostMapping("/api/personal/pedidos/{pedido}/{accion}") public PedidoDTO accion(Principal p,@PathVariable Long pedido,@PathVariable String accion,@RequestBody Accion body){return switch(accion){
 case "aprobar"->pedidos.aprobar(pedido,id(p),Boolean.TRUE.equals(body.confirmado()));case "rechazar"->pedidos.rechazar(pedido,id(p),body.motivo());
 case "preparar"->pedidos.preparar(pedido,id(p));case "listo"->pedidos.listo(pedido,id(p));
 case "reembolsar"->pedidos.reembolsar(pedido,id(p),body.motivo(),Boolean.TRUE.equals(body.reponer()));default->throw new IllegalArgumentException("Acción inválida");};}
 public record Entrega(String codigo){}
 @PostMapping("/api/personal/pedidos/entregar") public PedidoDTO entregar(Principal p,@RequestBody Entrega body){return pedidos.entregar(id(p),body.codigo());}
}
