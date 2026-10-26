package pe.edu.utp.Grupo06.model;

import lombok.*;
import jakarta.persistence.*;
import pe.edu.utp.Grupo06.model.enums.EstadoAlerta;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "alertas_reposicion",
    indexes = {
        @Index(name = "idx_alertas_producto_estado", columnList = "producto_id, estado"),
        @Index(name = "idx_alertas_estado", columnList = "estado")
    }
)
@org.hibernate.annotations.Check(constraints = "stock_registrado >= 0 AND stock_minimo >= 0")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AlertaReposicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "stock_registrado", nullable = false)
    private Integer stockRegistrado;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "cantidad_sugerida")
    private Integer cantidadSugerida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAlerta estado = EstadoAlerta.PENDIENTE;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Column(length = 255)
    private String observacion;
}
