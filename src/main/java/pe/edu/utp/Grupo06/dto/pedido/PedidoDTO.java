package pe.edu.utp.Grupo06.dto.pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record PedidoDTO(Long id,String numero,String estado,boolean demo,String cliente,LocalDateTime creado,LocalDateTime vence,
 BigDecimal total,String codigoRecojo,String direccion,String horario,String titular,String telefonoYape,String observacion,
 boolean tieneComprobante,List<Linea> lineas,List<Evento> eventos) {
 public record Linea(Long productoId,String nombre,int cantidad,BigDecimal precio) {}
 public record Evento(LocalDateTime fecha,String estado,String mensaje) {}
}
