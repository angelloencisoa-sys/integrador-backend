package pe.edu.utp.Grupo06.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import pe.edu.utp.Grupo06.model.enums.EstadoPedido;
@Entity @Table(name="pedidos_web", uniqueConstraints=@UniqueConstraint(columnNames={"cliente_id","clave"}))
@Getter @Setter @NoArgsConstructor
public class Pedido {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="cliente_id",nullable=false) private Usuario cliente;
 @Column(nullable=false,length=36) private String clave;
 @Column(nullable=false,length=64) private String huella;
 @Column(nullable=false,unique=true,length=30) private String numero;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) private EstadoPedido estado;
 @Column(nullable=false) private boolean demo;
 @Column(nullable=false) private LocalDateTime creado;
 @Column(nullable=false) private LocalDateTime vence;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal total;
 @Column(length=40,unique=true) private String codigoRecojo;
 @Column(nullable=false,length=255) private String direccion;
 @Column(nullable=false,length=150) private String horario;
 @Column(nullable=false,length=100) private String titular;
 @Column(nullable=false,length=30) private String telefonoYape;
 @Column(length=500) private String observacion;
 @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="venta_id",unique=true) private Venta venta;
 @OneToMany(mappedBy="pedido",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("id asc") private List<LineaPedido> lineas=new ArrayList<>();
 @Version private Long version;
}

