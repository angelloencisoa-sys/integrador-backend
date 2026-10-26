package pe.edu.utp.Grupo06.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="eventos_pedido_web") @Getter @Setter @NoArgsConstructor
public class EventoPedido {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long pedidoId;
 @Column(nullable=false) private Long actorId;
 @Column(nullable=false) private LocalDateTime fecha;
 @Column(nullable=false,length=32) private String estado;
 @Column(nullable=false,length=500) private String mensaje;
}
