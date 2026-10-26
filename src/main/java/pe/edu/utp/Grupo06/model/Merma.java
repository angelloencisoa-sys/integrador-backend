package pe.edu.utp.Grupo06.model;

import jakarta.persistence.*;
import lombok.*;
import pe.edu.utp.Grupo06.model.enums.MotivoMerma;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "mermas",
    indexes = {
        @Index(name = "idx_merma_producto", columnList = "producto_id"),
        @Index(name = "idx_merma_fecha", columnList = "fecha_merma"),
        @Index(name = "idx_merma_motivo", columnList = "motivo")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"producto", "usuario"})
public class Merma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 30)
    private MotivoMerma motivo;

    @Column(name = "costo_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal costoUnitario = BigDecimal.ZERO;

    @Column(name = "costo_total_perdida", precision = 10, scale = 2, nullable = false)
    private BigDecimal costoTotalPerdida = BigDecimal.ZERO;

    @Column(name = "fecha_merma", nullable = false)
    private LocalDateTime fechaMerma = LocalDateTime.now();

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(length = 255)
    private String observacion;
}
