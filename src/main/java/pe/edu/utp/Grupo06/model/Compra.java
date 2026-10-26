package pe.edu.utp.Grupo06.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.edu.utp.Grupo06.model.enums.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "compras",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_compras_proveedor_comprobante", columnNames = {"proveedor_id", "numero_comprobante"})
    },
    indexes = {
        @Index(name = "idx_compras_proveedor", columnList = "proveedor_id"),
        @Index(name = "idx_compras_fecha", columnList = "fecha_compra")
    }
)
@org.hibernate.annotations.Check(constraints = "total >= 0")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El número de comprobante es obligatorio")
    @Column(name = "numero_comprobante", nullable = false, length = 50)
    private String numeroComprobante;

    @Column(name = "tipo_comprobante", nullable = false, length = 30)
    private String tipoComprobante = "OTRO";

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_compra", nullable = false, length = 20)
    private EstadoCompra estadoCompra = EstadoCompra.REGISTRADA;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra = LocalDateTime.now();

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @NotNull(message = "El proveedor es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> detalles = new ArrayList<>();
}
