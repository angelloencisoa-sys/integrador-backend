package pe.edu.utp.Grupo06.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "presentacion_producto", uniqueConstraints =
        @UniqueConstraint(name = "uk_presentacion_producto_nombre", columnNames = {"producto_id", "nombre_presentacion"}))
@Getter @Setter @NoArgsConstructor
public class PresentacionProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotBlank
    @Column(name = "nombre_presentacion", nullable = false, length = 80)
    private String nombrePresentacion;

    @Positive
    @Column(name = "factor_conversion", nullable = false)
    private Integer factorConversion;

    @Column(nullable = false)
    private Boolean estado = true;
}
