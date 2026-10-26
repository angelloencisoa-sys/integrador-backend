package pe.edu.utp.Grupo06.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import pe.edu.utp.Grupo06.model.enums.MotivoDiferencia;

import java.math.BigDecimal;

@Entity
@Table(
    name = "detalles_compra",
    indexes = {
        @Index(name = "idx_detalles_compra_compra", columnList = "compra_id"),
        @Index(name = "idx_detalles_compra_producto", columnList = "producto_id")
    }
)
@org.hibernate.annotations.Check(constraints = "cantidad > 0 AND precio_unitario >= 0 AND subtotal >= 0 AND cantidad_presentaciones > 0 AND factor_conversion > 0 AND unidades_esperadas = cantidad_presentaciones * factor_conversion AND unidades_recibidas >= 0 AND unidades_rechazadas >= 0 AND unidades_rechazadas <= unidades_recibidas AND unidades_ingresadas = unidades_recibidas - unidades_rechazadas AND precio_presentacion >= 0 AND costo_unitario >= 0")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @NotNull(message = "El producto es obligatorio en cada detalle de compra")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @PositiveOrZero(message = "El precio unitario no puede ser negativo")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presentacion_id", nullable = false)
    private PresentacionProducto presentacion;

    @Column(name = "nombre_presentacion", nullable = false, length = 80)
    private String nombrePresentacion;

    @Column(name = "cantidad_presentaciones", nullable = false)
    private Integer cantidadPresentaciones;

    @Column(name = "factor_conversion", nullable = false)
    private Integer factorConversion;

    @Column(name = "unidades_esperadas", nullable = false)
    private Integer unidadesEsperadas;

    @Column(name = "unidades_recibidas", nullable = false)
    private Integer unidadesRecibidas;

    @Column(name = "unidades_rechazadas", nullable = false)
    private Integer unidadesRechazadas;

    @Column(name = "unidades_ingresadas", nullable = false)
    private Integer unidadesIngresadas;

    @Column(name = "precio_presentacion", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPresentacion;

    @Column(name = "costo_unitario", nullable = false, precision = 14, scale = 6)
    private BigDecimal costoUnitario;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_diferencia", nullable = false, length = 30)
    private MotivoDiferencia motivoDiferencia = MotivoDiferencia.SIN_DIFERENCIA;

    @Column(length = 500)
    private String observacion;
}
