package pe.edu.utp.Grupo06.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="lineas_pedido_web") @Getter @Setter @NoArgsConstructor
public class LineaPedido {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pedido_id",nullable=false) private Pedido pedido;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="producto_id",nullable=false) private Producto producto;
 @Column(nullable=false,length=150) private String nombre;
 @Column(nullable=false) private int cantidad;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal precio;
}
