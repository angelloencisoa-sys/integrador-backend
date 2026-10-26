package pe.edu.utp.Grupo06.model;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="comprobantes_pedido_web") @Getter @Setter @NoArgsConstructor
public class ComprobantePedido {
 @Id private Long pedidoId;
 @Column(nullable=false,unique=true,length=64) private String hash;
 @Column(nullable=false,length=20) private String tipo;
 @Column(nullable=false,length=100) private String operacion;
 @Column(nullable=false) private byte[] contenido;
}
