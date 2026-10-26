package pe.edu.utp.Grupo06.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.utp.Grupo06.model.*;
import pe.edu.utp.Grupo06.model.enums.*;
import pe.edu.utp.Grupo06.repository.*;
import pe.edu.utp.Grupo06.dto.pedido.*;
import pe.edu.utp.Grupo06.exception.PedidoConflictException;
import java.math.*;
import java.time.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import pe.edu.utp.Grupo06.util.DatosPedidoSeguros;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestiona el ciclo BOPIS y sus transacciones de reserva, pago y entrega.
 * Revalida el actor, la propiedad del pedido y el estado en cada operacion.
 * La aprobacion representa una verificacion humana; no consulta la cuenta Yape.
 */
@Service @Transactional
public class PedidoService {
 private static final Logger log = LoggerFactory.getLogger(PedidoService.class);
 private final PedidoRepository pedidos;
 private final ProductoRepository productos;
 private final UsuarioRepository usuarios;
 private final ComprobantePedidoRepository comprobantes;
 private final EventoPedidoRepository eventos;
 private final VentaRepository ventas;
 private final IMovimientoInventarioService movimientos;
 private final TicketService tickets;
 private final TiendaConfig tienda;
 public PedidoService(PedidoRepository pedidos,ProductoRepository productos,UsuarioRepository usuarios,
  ComprobantePedidoRepository comprobantes,EventoPedidoRepository eventos,VentaRepository ventas,
  IMovimientoInventarioService movimientos,TicketService tickets,TiendaConfig tienda) {
  this.pedidos=pedidos;this.productos=productos;this.usuarios=usuarios;this.comprobantes=comprobantes;
  this.eventos=eventos;this.ventas=ventas;this.movimientos=movimientos;this.tickets=tickets;this.tienda=tienda;
 }
 private Usuario usuario(Long id) { return usuarios.findById(id).filter(u->Boolean.TRUE.equals(u.getActivo()))
  .orElseThrow(()->new AccessDeniedException("Cuenta no disponible")); }
 private Usuario personal(Long id) { var u=usuario(id);if(u.getRol().getNombre()==RolNombre.CLIENTE) throw new AccessDeniedException("Solo personal");return u; }
 private Pedido locked(Long id) { return pedidos.locked(id).orElseThrow(()->new IllegalArgumentException("Pedido no encontrado")); }
 private void propio(Pedido p,Long id) { if(!p.getCliente().getId().equals(id)) throw new AccessDeniedException("Pedido de otra cuenta"); }
 private void exigir(Pedido p,EstadoPedido... estados) { if(!Arrays.asList(estados).contains(p.getEstado())) throw new PedidoConflictException("El pedido está en " + p.getEstado()+". Actualice la vista."); }
 private String texto(String s,int min,int max) { return DatosPedidoSeguros.textoRequerido(s,min,max); }
 private void evento(Pedido p,Long actor,String mensaje) {
  var e=new EventoPedido();e.setPedidoId(p.getId());e.setActorId(actor);e.setFecha(LocalDateTime.now());e.setEstado(p.getEstado().name());e.setMensaje(mensaje);eventos.save(e);
  log.info("Pedido {} actualizado a {}", p.getId(), p.getEstado());
 }
 private static String hash(byte[] bytes) { return DatosPedidoSeguros.huellaSha256(bytes); }
 private Map<Long,Producto> bloquearProductos(Pedido p) {
  Map<Long,Producto> result=new TreeMap<>();p.getLineas().stream().map(l->l.getProducto().getId()).distinct().sorted()
   .forEach(id->result.put(id,productos.findLockedById(id).orElseThrow()));return result;
 }
 /**
  * Crea o recupera un pedido idempotente y reserva stock durante diez minutos.
  * @param clienteId cuenta activa con rol CLIENTE
  * @param solicitud clave, productos, cantidades y precios esperados
  * @return pedido con total recalculado y fecha de vencimiento
  * @throws PedidoConflictException si cambian precios, stock o intencion de compra
  */
 public PedidoDTO crear(Long clienteId,CrearPedido solicitud) {
  var cliente=usuarios.findLockedById(clienteId).orElseThrow(()->new AccessDeniedException("Cuenta no disponible"));
  if(!Boolean.TRUE.equals(cliente.getActivo()) || cliente.getRol().getNombre()!=RolNombre.CLIENTE) throw new AccessDeniedException("Utilice una cuenta de cliente");
  if(solicitud==null||solicitud.clave()==null||!solicitud.clave().matches("[a-fA-F0-9-]{36}")||solicitud.items()==null||solicitud.items().isEmpty()||solicitud.items().size()>50) throw new IllegalArgumentException("Carrito inválido");
  SortedMap<Long,CrearPedido.Item> items=new TreeMap<>();
  for(var i:solicitud.items()) {
   if(i.productoId()==null||i.productoId()<=0||i.cantidad()<1||i.cantidad()>999||i.precioEsperado()==null||i.precioEsperado().signum()<=0||items.put(i.productoId(),i)!=null) throw new IllegalArgumentException("Producto o cantidad inválidos o repetidos");
  }
  String huella=hash(items.values().stream().map(i->i.productoId()+":"+i.cantidad()+":"+i.precioEsperado().stripTrailingZeros().toPlainString()).reduce("",(a,b)->a+";"+b).getBytes(StandardCharsets.UTF_8));
  var anterior=pedidos.findByClienteIdAndClave(clienteId,solicitud.clave());
  if(anterior.isPresent()) { if(!anterior.get().getHuella().equals(huella)) throw new PedidoConflictException("La clave corresponde a otra compra");return dto(anterior.get()); }
  // Serialize creation per client. An expired reservation is released by the scheduler or explicitly cancelled.
  if(pedidos.existsByClienteIdAndEstadoIn(clienteId,List.of(EstadoPedido.PENDIENTE_PAGO,EstadoPedido.PENDIENTE_VERIFICACION))) throw new PedidoConflictException("Ya tiene un pedido pendiente. Revíselo en Mis pedidos.");
  Pedido p=new Pedido();p.setCliente(cliente);p.setClave(solicitud.clave());p.setHuella(huella);
  p.setNumero("NOVA-"+UUID.randomUUID().toString().substring(0,18).toUpperCase(Locale.ROOT));
  p.setEstado(EstadoPedido.PENDIENTE_PAGO);p.setDemo(tienda.demo);p.setCreado(LocalDateTime.now());p.setVence(p.getCreado().plusMinutes(10));
  p.setTitular(tienda.titular);p.setTelefonoYape(tienda.telefono);p.setDireccion(tienda.direccion);p.setHorario(tienda.horario);
  BigDecimal total=BigDecimal.ZERO;
  for(var i:items.values()) {
   var producto=productos.findLockedById(i.productoId()).orElseThrow(()->new IllegalArgumentException("Producto no encontrado"));
   if(!Boolean.TRUE.equals(producto.getEstado())||!Boolean.TRUE.equals(producto.getCategoria().getEstado())) throw new PedidoConflictException("Producto no disponible: "+producto.getNombre());
   if(producto.getPrecioVenta().compareTo(i.precioEsperado())!=0) throw new PedidoConflictException("Cambió el precio de "+producto.getNombre()+". Actualice su carrito.");
   if(producto.getStockDisponible()<i.cantidad()) throw new PedidoConflictException("Stock insuficiente: "+producto.getNombre());
   producto.setStockReservado(Math.addExact(producto.getStockReservado(),i.cantidad()));
   var l=new LineaPedido();l.setPedido(p);l.setProducto(producto);l.setNombre(producto.getNombre());l.setCantidad(i.cantidad());l.setPrecio(producto.getPrecioVenta());p.getLineas().add(l);
   total=total.add(l.getPrecio().multiply(BigDecimal.valueOf(l.getCantidad())));
  }
  if(total.compareTo(new BigDecimal("99999999.99"))>0) throw new IllegalArgumentException("Total fuera del límite");
  p.setTotal(total);p=pedidos.saveAndFlush(p);evento(p,clienteId,p.isDemo()?"Pedido de demostración creado. No realice transferencias.":"Pedido creado. Reserva de 10 minutos.");return dto(p);
 }
 @Transactional(readOnly=true) public List<PedidoDTO> mios(Long actor){usuario(actor);return pedidos.findTop100ByClienteIdOrderByCreadoDesc(actor).stream().map(this::dto).toList();}
 @Transactional(readOnly=true) public List<PedidoDTO> todos(Long actor){personal(actor);return pedidos.findTop200ByOrderByCreadoDesc().stream().map(this::dto).toList();}
 @Transactional(readOnly=true) public PedidoDTO ver(Long id,Long actor){usuario(actor);var p=pedidos.findById(id).orElseThrow(()->new IllegalArgumentException("Pedido no encontrado"));propio(p,actor);return dto(p);}
 @Transactional(readOnly=true) public PedidoDTO verPersonal(Long id,Long actor){personal(actor);return dto(pedidos.findById(id).orElseThrow(()->new IllegalArgumentException("Pedido no encontrado")));}
 private void liberar(Pedido p){var ps=bloquearProductos(p);for(var l:p.getLineas()){var prod=ps.get(l.getProducto().getId());prod.setStockReservado(prod.getStockReservado()-l.getCantidad());}}
 public PedidoDTO cancelar(Long id,Long actor){usuario(actor);var p=locked(id);propio(p,actor);if(p.getEstado()==EstadoPedido.CANCELADO)return dto(p);exigir(p,EstadoPedido.PENDIENTE_PAGO);liberar(p);p.setEstado(EstadoPedido.CANCELADO);evento(p,actor,"Cancelado por el cliente. Reserva liberada.");return dto(p);}
 public void expirar(Long id){var p=locked(id);if(p.getEstado()==EstadoPedido.PENDIENTE_PAGO && p.getVence().isBefore(LocalDateTime.now())){liberar(p);p.setEstado(EstadoPedido.EXPIRADO);evento(p,p.getCliente().getId(),"Venció el plazo sin comprobante. Reserva liberada.");}}
 @Transactional(readOnly=true) public List<Long> vencidos(){return pedidos.findByEstadoAndVenceBefore(EstadoPedido.PENDIENTE_PAGO,LocalDateTime.now()).stream().map(Pedido::getId).toList();}
 /**
  * Valida y almacena privadamente un comprobante PNG o JPEG, sin confirmar el pago.
  * @param id identificador del pedido pendiente
  * @param actor propietario del pedido
  * @param bytes imagen de hasta 3 MB con dimensiones acotadas
  * @param operacion referencia que el personal verificara externamente
  * @return pedido pendiente de verificacion
  */
 public PedidoDTO adjuntar(Long id,Long actor,byte[] bytes,String operacion) {
  usuario(actor);var p=locked(id);propio(p,actor);exigir(p,EstadoPedido.PENDIENTE_PAGO);
  if(p.getVence().isBefore(LocalDateTime.now())) throw new PedidoConflictException("Venció la reserva. Cancele este pedido y cree uno nuevo. Si ya pagó, contacte a la tienda.");
  operacion=texto(operacion,3,100);String tipo=validarImagen(bytes);String sha=hash(bytes);
  if(comprobantes.existsByHash(sha)) throw new PedidoConflictException("Este comprobante ya fue utilizado");
  var c=new ComprobantePedido();c.setPedidoId(id);c.setHash(sha);c.setContenido(bytes);c.setTipo(tipo);c.setOperacion(operacion);comprobantes.saveAndFlush(c);
  p.setEstado(EstadoPedido.PENDIENTE_VERIFICACION);evento(p,actor,"Comprobante recibido. Pendiente de verificación del personal.");return dto(p);
 }
 private String validarImagen(byte[] bytes) {
  if(bytes==null||bytes.length<16||bytes.length>3*1024*1024) throw new IllegalArgumentException("La imagen debe ocupar como máximo 3 MB");
  try(ImageInputStream in=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
   var readers=ImageIO.getImageReaders(in);if(!readers.hasNext()) throw new IllegalArgumentException("Adjunte una imagen PNG o JPEG válida");
   var reader=readers.next();try {reader.setInput(in);String formato=reader.getFormatName().toLowerCase(Locale.ROOT);
    if(!List.of("png","jpeg","jpg").contains(formato)) throw new IllegalArgumentException("Solo PNG o JPEG");
    int w=reader.getWidth(0),h=reader.getHeight(0);if(w<1||h<1||(long)w*h>16000000||w>6000||h>6000) throw new IllegalArgumentException("Dimensiones de imagen no admitidas");
    if(reader.read(0)==null) throw new IllegalArgumentException("Imagen no válida");return formato.equals("png")?"image/png":"image/jpeg";
   }finally{reader.dispose();}
  }catch(IOException ex){throw new IllegalArgumentException("Imagen no válida");}
 }
 @Transactional(readOnly=true) public ComprobantePedido comprobante(Long id,Long actor,boolean interno){if(interno)personal(actor);else{usuario(actor);propio(pedidos.findById(id).orElseThrow(()->new IllegalArgumentException("Pedido no encontrado")),actor);}return comprobantes.findById(id).orElseThrow(()->new IllegalArgumentException("Comprobante no encontrado"));}
 /**
  * Confirma la revision humana y registra una sola venta y salida de inventario.
  * @param id pedido con comprobante recibido
  * @param actor administrador o vendedor activo
  * @param confirmado confirmacion explicita de la revision externa o DEMO
  * @return pedido pagado con codigo de recojo
  */
 public PedidoDTO aprobar(Long id,Long actor,boolean confirmado) {
  var empleado=personal(actor);var p=locked(id);
  if(p.getVenta()!=null)return dto(p);
  exigir(p,EstadoPedido.PENDIENTE_VERIFICACION);
  if(!confirmado)throw new IllegalArgumentException("Confirme la revisión del pago en la cuenta receptora (o la simulación DEMO)");
  if(!comprobantes.existsById(id))throw new IllegalArgumentException("Falta comprobante");
  var ps=bloquearProductos(p);var v=new Venta();v.setNumeroTicket(tickets.siguiente(p.isDemo()?"D001":"W001"));v.setUsuario(empleado);v.setDemo(p.isDemo());v.setTotal(p.getTotal());
  v.setSubtotal(p.getTotal().divide(new BigDecimal("1.18"),2,RoundingMode.HALF_UP));v.setIgv(p.getTotal().subtract(v.getSubtotal()));
  for(var l:p.getLineas()){var prod=ps.get(l.getProducto().getId());prod.setStockReservado(prod.getStockReservado()-l.getCantidad());
   var d=new DetalleVenta();d.setVenta(v);d.setProducto(prod);d.setCantidad(l.getCantidad());d.setPrecioUnitario(l.getPrecio());d.setSubtotal(l.getPrecio().multiply(BigDecimal.valueOf(l.getCantidad())));v.getDetalles().add(d);}
  var pago=new Pago();pago.setVenta(v);pago.setMetodoPago(MetodoPago.YAPE);pago.setMonto(p.getTotal());v.getPagos().add(pago);ventas.saveAndFlush(v);
  for(var l:p.getLineas()) movimientos.registrarMovimiento(l.getProducto().getId(),actor,TipoMovimiento.SALIDA,l.getCantidad(),(p.isDemo()?"DEMO - ":"")+p.getNumero(),null,v);
  p.setVenta(v);p.setEstado(EstadoPedido.PAGADO);byte[] codigo=new byte[8];new SecureRandom().nextBytes(codigo);p.setCodigoRecojo("NV-"+HexFormat.of().formatHex(codigo).toUpperCase(Locale.ROOT));
  evento(p,actor,p.isDemo()?"Pago simulado aprobado por el personal. No hubo cobro real.":"Pago verificado en cuenta receptora por el personal.");return dto(p);
 }
 public PedidoDTO rechazar(Long id,Long actor,String motivo){personal(actor);var p=locked(id);exigir(p,EstadoPedido.PENDIENTE_VERIFICACION);motivo=texto(motivo,5,400);liberar(p);p.setEstado(EstadoPedido.RECHAZADO);p.setObservacion(motivo);evento(p,actor,"Comprobante rechazado: "+motivo);return dto(p);}
 public PedidoDTO preparar(Long id,Long actor){personal(actor);var p=locked(id);exigir(p,EstadoPedido.PAGADO);p.setEstado(EstadoPedido.EN_PREPARACION);evento(p,actor,"Preparando los productos.");return dto(p);}
 public PedidoDTO listo(Long id,Long actor){personal(actor);var p=locked(id);exigir(p,EstadoPedido.EN_PREPARACION);p.setEstado(EstadoPedido.LISTO_PARA_RECOJO);evento(p,actor,"Listo para recoger en tienda. Presente su código.");return dto(p);}
 public PedidoDTO entregar(Long actor,String codigo){personal(actor);if(codigo==null||codigo.length()>40)throw new IllegalArgumentException("Código inválido");var encontrado=pedidos.findByCodigoRecojo(codigo.trim().toUpperCase(Locale.ROOT)).orElseThrow(()->new IllegalArgumentException("Código no encontrado"));var p=locked(encontrado.getId());exigir(p,EstadoPedido.LISTO_PARA_RECOJO);p.setEstado(EstadoPedido.ENTREGADO);evento(p,actor,"Entregado en tienda. Código consumido.");return dto(p);}
 // A refund is an explicit, audited confirmation of an external operation; never an automatic transfer.
 public PedidoDTO reembolsar(Long id,Long actor,String referencia,boolean reponer){var u=personal(actor);if(u.getRol().getNombre()!=RolNombre.ADMINISTRADOR)throw new AccessDeniedException("Solo administrador");var p=locked(id);exigir(p,EstadoPedido.PAGADO,EstadoPedido.EN_PREPARACION,EstadoPedido.LISTO_PARA_RECOJO);referencia=texto(referencia,5,400);var ps=bloquearProductos(p);
  if(reponer)for(var l:p.getLineas())movimientos.registrarMovimiento(l.getProducto().getId(),actor,TipoMovimiento.ENTRADA,l.getCantidad(),"Devolución "+p.getNumero(),null,p.getVenta());
  p.getVenta().setEstado(EstadoVenta.ANULADA);p.setEstado(EstadoPedido.REEMBOLSADO);p.setObservacion(referencia);evento(p,actor,(p.isDemo()?"Reembolso simulado: ":"Reembolso externo confirmado: ")+referencia+(reponer?". Mercadería revisada y repuesta.":". Sin reposición de mercadería."));return dto(p);
 }
 private PedidoDTO dto(Pedido p){return new PedidoDTO(p.getId(),p.getNumero(),p.getEstado().name(),p.isDemo(),p.getCliente().getNombreCompleto(),p.getCreado(),p.getVence(),p.getTotal(),p.getCodigoRecojo(),p.getDireccion(),p.getHorario(),p.getTitular(),p.getTelefonoYape(),p.getObservacion(),comprobantes.existsById(p.getId()),p.getLineas().stream().map(l->new PedidoDTO.Linea(l.getProducto().getId(),l.getNombre(),l.getCantidad(),l.getPrecio())).toList(),eventos.findByPedidoIdOrderByFechaAscIdAsc(p.getId()).stream().map(e->new PedidoDTO.Evento(e.getFecha(),e.getEstado(),e.getMensaje())).toList());}
}

