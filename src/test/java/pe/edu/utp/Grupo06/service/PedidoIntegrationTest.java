package pe.edu.utp.Grupo06.service;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import pe.edu.utp.Grupo06.model.*;
import pe.edu.utp.Grupo06.model.enums.*;
import pe.edu.utp.Grupo06.repository.*;
import pe.edu.utp.Grupo06.dto.pedido.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.net.http.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class PedidoIntegrationTest {
 @Autowired IReporteService reportes;@Autowired PagoRepository pagos;
 @Autowired PedidoService service;@Autowired ProductoRepository productos;@Autowired UsuarioRepository usuarios;
 @Autowired RolRepository roles;@Autowired CategoriaRepository categorias;@Autowired VentaRepository ventas;
 @Autowired IMovimientoInventarioService movimientos;@Autowired IVentaService ventaService;
 @Autowired TransactionTemplate tx;@Autowired JdbcTemplate jdbc;
 @Autowired org.springframework.security.oauth2.jwt.JwtEncoder encoder;
 @LocalServerPort int port;
 Long cliente,otro,staff,producto;BigDecimal precio=new BigDecimal("7.50");
 @BeforeEach void setup(){tx.executeWithoutResult(t->{
  cliente=user(RolNombre.CLIENTE);otro=user(RolNombre.CLIENTE);staff=user(RolNombre.ADMINISTRADOR);
  var p=new Producto();p.setCodigo("ORDER-"+UUID.randomUUID());p.setNombre("Producto pedido test");p.setPrecioCompra(new BigDecimal("4.00"));p.setPrecioVenta(precio);p.setStockActual(3);p.setStockMinimo(0);p.setCategoria(categorias.findByEstadoTrue().getFirst());p.setUnidadMedida(UnidadMedida.UNIDAD);producto=productos.saveAndFlush(p).getId();
 });}
 Long user(RolNombre rol){var u=new Usuario();u.setUsername("order_"+UUID.randomUUID().toString().substring(0,20));u.setNombreCompleto("Prueba pedidos");u.setPassword("not-used-http");u.setRol(roles.findByNombre(rol).orElseThrow());return usuarios.saveAndFlush(u).getId();}
 @AfterEach void cleanup(){tx.executeWithoutResult(t->{
  var ids=jdbc.queryForList("select id from pedidos_web where cliente_id in (?,?)",Long.class,cliente,otro);
  var vs=jdbc.queryForList("select venta_id from pedidos_web where cliente_id in (?,?) and venta_id is not null",Long.class,cliente,otro);
  for(Long id:ids){jdbc.update("delete from eventos_pedido_web where pedido_id=?",id);jdbc.update("delete from comprobantes_pedido_web where pedido_id=?",id);jdbc.update("delete from lineas_pedido_web where pedido_id=?",id);jdbc.update("delete from pedidos_web where id=?",id);}
  jdbc.update("delete from movimientos_inventario where producto_id=?",producto);
  for(Long id:vs){jdbc.update("delete from pagos where venta_id=?",id);jdbc.update("delete from detalles_venta where venta_id=?",id);jdbc.update("delete from ventas where id=?",id);}
  jdbc.update("delete from alertas_reposicion where producto_id=?",producto);jdbc.update("delete from productos where id=?",producto);jdbc.update("delete from usuarios where id in (?,?,?)",cliente,otro,staff);
 });}
 CrearPedido request(int cantidad){return new CrearPedido(UUID.randomUUID().toString(),List.of(new CrearPedido.Item(producto,cantidad,precio)));}
 PedidoDTO crear(int cantidad){return service.crear(cliente,request(cantidad));}
 byte[] imagen()throws Exception{var image=new BufferedImage(8,8,BufferedImage.TYPE_INT_RGB);image.setRGB(1,1,new java.util.Random().nextInt());var out=new ByteArrayOutputStream();javax.imageio.ImageIO.write(image,"png",out);return out.toByteArray();}
 PedidoDTO pagado()throws Exception{var p=crear(2);service.adjuntar(p.id(),cliente,imagen(),"DEMO-001");return service.aprobar(p.id(),staff,true);}
 @Test void idempotenciaYReserva(){var req=request(2);var a=service.crear(cliente,req);assertEquals(a.id(),service.crear(cliente,req).id());assertEquals(2,productos.findById(producto).orElseThrow().getStockReservado());assertEquals(1,productos.findById(producto).orElseThrow().getStockDisponible());assertThrows(RuntimeException.class,()->service.crear(cliente,new CrearPedido(req.clave(),List.of(new CrearPedido.Item(producto,1,precio)))));}
 @Test void cancelarLiberaUnaSolaVez(){var p=crear(2);service.cancelar(p.id(),cliente);service.cancelar(p.id(),cliente);assertEquals(0,productos.findById(producto).orElseThrow().getStockReservado());assertEquals(3,productos.findById(producto).orElseThrow().getStockActual());}
 @Test void precioYAcceso(){assertThrows(RuntimeException.class,()->service.crear(cliente,new CrearPedido(UUID.randomUUID().toString(),List.of(new CrearPedido.Item(producto,1,new BigDecimal("0.01"))))));var p=crear(1);assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.ver(p.id(),otro));assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.todos(cliente));assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.crear(staff,request(1)));}
 @Test void reservaProtegeCajaYAjuste(){crear(2);assertThrows(RuntimeException.class,()->movimientos.registrarMovimiento(producto,staff,TipoMovimiento.SALIDA,2,"Prueba",null,null));assertThrows(RuntimeException.class,()->movimientos.registrarMovimiento(producto,staff,TipoMovimiento.AJUSTE,1,"Prueba",null,null));assertEquals(3,productos.findById(producto).orElseThrow().getStockActual());}
 @Test void venceUnaSolaVez(){var p=crear(2);jdbc.update("update pedidos_web set vence=? where id=?",LocalDateTime.now().minusMinutes(1),p.id());service.expirar(p.id());service.expirar(p.id());assertEquals("EXPIRADO",service.ver(p.id(),cliente).estado());assertEquals(0,productos.findById(producto).orElseThrow().getStockReservado());}
 @Test void comprobanteSeConservaDuranteRevision()throws Exception{var p=crear(2);service.adjuntar(p.id(),cliente,imagen(),"DEMO-001");jdbc.update("update pedidos_web set vence=? where id=?",LocalDateTime.now().minusMinutes(1),p.id());service.expirar(p.id());assertEquals("PENDIENTE_VERIFICACION",service.ver(p.id(),cliente).estado());assertEquals(2,productos.findById(producto).orElseThrow().getStockReservado());assertThrows(RuntimeException.class,()->service.cancelar(p.id(),cliente));}
 @Test void imagenNoEsPagoYNoAdmiteContenidoFalso()throws Exception{var p=crear(1);assertThrows(RuntimeException.class,()->service.adjuntar(p.id(),cliente,"<script>alert(1)</script>".getBytes(),"DEMO-001"));service.adjuntar(p.id(),cliente,imagen(),"DEMO-001");assertThrows(RuntimeException.class,()->service.aprobar(p.id(),staff,false));assertEquals("PENDIENTE_VERIFICACION",service.ver(p.id(),cliente).estado());assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.comprobante(p.id(),otro,false));}
 @Test void aprobarConservaPrecioYNoDuplicaVenta()throws Exception{var p=crear(2);service.adjuntar(p.id(),cliente,imagen(),"DEMO-001");jdbc.update("update productos set precio_venta=9.00 where id=?",producto);var a=service.aprobar(p.id(),staff,true);service.aprobar(p.id(),staff,true);assertEquals(new BigDecimal("15.00"),a.total());assertEquals(1,productos.findById(producto).orElseThrow().getStockActual());assertEquals(0,productos.findById(producto).orElseThrow().getStockReservado());assertEquals(1,jdbc.queryForObject("select count(*) from ventas where usuario_id=?",Integer.class,staff));assertEquals(true,jdbc.queryForObject("select demo from ventas where usuario_id=?",Boolean.class,staff));}
 @Test void recojoExigePreparacionYCodigoUnSoloUso()throws Exception{var p=pagado();assertThrows(RuntimeException.class,()->service.entregar(staff,p.codigoRecojo()));service.preparar(p.id(),staff);service.listo(p.id(),staff);assertEquals("ENTREGADO",service.entregar(staff,p.codigoRecojo()).estado());assertThrows(RuntimeException.class,()->service.entregar(staff,p.codigoRecojo()));}
 @Test void rechazoLiberaYReembolsoAuditado()throws Exception{var p=crear(2);service.adjuntar(p.id(),cliente,imagen(),"DEMO-001");service.rechazar(p.id(),staff,"Imagen de prueba rechazada");assertEquals(3,productos.findById(producto).orElseThrow().getStockDisponible());var pago=pagado();assertEquals("REEMBOLSADO",service.reembolsar(pago.id(),staff,"DEMO-REEMBOLSO-01",true).estado());assertEquals(3,productos.findById(producto).orElseThrow().getStockActual());assertThrows(RuntimeException.class,()->service.reembolsar(pago.id(),staff,"DEMO-OTRO",true));}
 @Test void dosClientesCompitenPorUltimaUnidad()throws Exception{
  jdbc.update("update productos set stock_actual=1 where id=?",producto);var ready=new CountDownLatch(2);var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
  try{List<Future<Boolean>> results=new ArrayList<>();for(Long actor:List.of(cliente,otro))results.add(pool.submit(()->{ready.countDown();start.await();try{service.crear(actor,request(1));return true;}catch(RuntimeException e){return false;}}));assertTrue(ready.await(5,TimeUnit.SECONDS));start.countDown();int wins=0;for(var f:results)if(f.get(10,TimeUnit.SECONDS))wins++;assertEquals(1,wins);assertEquals(1,productos.findById(producto).orElseThrow().getStockReservado());}finally{pool.shutdownNow();}
 }
 @Test void demoNoSumaIngresosNiPagosReales()throws Exception{var inicio=LocalDateTime.now().minusDays(1);var fin=LocalDateTime.now().plusDays(1);var antes=reportes.obtenerResumenVentasPorPeriodo(inicio,fin).getTotalRecaudado();BigDecimal metodosAntes=pagos.findMetodosPagoEntreFechas(inicio,fin).stream().map(f->(BigDecimal)f[1]).reduce(BigDecimal.ZERO,BigDecimal::add);var p=pagado();assertEquals(antes,reportes.obtenerResumenVentasPorPeriodo(inicio,fin).getTotalRecaudado());assertEquals(metodosAntes,pagos.findMetodosPagoEntreFechas(inicio,fin).stream().map(f->(BigDecimal)f[1]).reduce(BigDecimal.ZERO,BigDecimal::add));assertTrue(ventas.findVentasEntreFechas(inicio,fin).stream().noneMatch(Venta::isDemo));}
 @Test void ticketAtomicoNoSeRepite(){assertNotEquals(ventaService.generarSiguienteNumeroTicket(),ventaService.generarSiguienteNumeroTicket());}
 private String token(Long actor){var now=java.time.Instant.now();var claims=org.springframework.security.oauth2.jwt.JwtClaimsSet.builder().issuer(pe.edu.utp.Grupo06.config.ApiSecurityConfig.ISSUER).subject(actor.toString()).issuedAt(now).expiresAt(now.plusSeconds(600)).build();return encoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(org.springframework.security.oauth2.jwt.JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build(),claims)).getTokenValue();}
 private HttpResponse<String> http(String method,String path,String body,Long actor)throws Exception{return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).header("Authorization","Bearer "+token(actor)).header("Content-Type","application/json").method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());}
 @Test void autorizacionHttpYCamposOpcionales()throws Exception{var p=crear(1);assertEquals(403,http("GET","/api/pedidos/"+p.id(),null,otro).statusCode());assertEquals(403,http("GET","/api/personal/pedidos",null,cliente).statusCode());assertEquals(403,http("GET","/api/pedidos",null,staff).statusCode());service.adjuntar(p.id(),cliente,imagen(),"DEMO-HTTP-001");assertEquals(200,http("POST","/api/personal/pedidos/"+p.id()+"/aprobar","{\"confirmado\":true}",staff).statusCode());assertEquals(200,http("POST","/api/personal/pedidos/"+p.id()+"/preparar","{}",staff).statusCode());assertEquals(200,http("POST","/api/personal/pedidos/"+p.id()+"/listo","{}",staff).statusCode());assertEquals(200,http("GET","/api/pedidos/"+p.id()+"/qr",null,cliente).statusCode());}
 @Test void rutasProtegidas()throws Exception{var http=HttpClient.newHttpClient();for(String path:List.of("/api/pedidos","/api/personal/pedidos","/api/pedidos/1/comprobante","/api/pedidos/1/qr")){var response=http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).GET().build(),HttpResponse.BodyHandlers.ofString());assertEquals(401,response.statusCode(),path);}assertEquals(200,http.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/tienda")).GET().build(),HttpResponse.BodyHandlers.ofString()).statusCode());}
}
