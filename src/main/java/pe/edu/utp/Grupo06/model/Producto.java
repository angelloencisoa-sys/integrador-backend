package pe.edu.utp.Grupo06.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import pe.edu.utp.Grupo06.model.enums.UnidadMedida;

import java.math.BigDecimal;

@Entity
@Table(
    name = "productos",
    indexes = {
        @Index(name = "idx_producto_categoria", columnList = "categoria_id"),
        @Index(name = "idx_producto_proveedor", columnList = "proveedor_id")
    }
)
@org.hibernate.annotations.Check(constraints = "precio_compra >= 0 AND precio_venta > 0 AND stock_actual >= 0 AND stock_minimo >= 0")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código del producto es obligatorio")
    @Size(max = 50, message = "El código no puede superar los 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @Size(max = 80, message = "La marca no puede superar los 80 caracteres")
    @Column(length = 80)
    private String marca;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    @Column(length = 255)
    private String descripcion;

    @NotNull(message = "El precio de compra es obligatorio")
    @PositiveOrZero(message = "El precio de compra no puede ser negativo")
    @Column(name = "precio_compra", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor a 0")
    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @PositiveOrZero(message = "El stock actual no puede ser negativo")
    @org.hibernate.annotations.ColumnDefault("0")
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Column(name = "stock_reservado", nullable = false)
    private Integer stockReservado = 0;

    @Transient
    public int getStockDisponible() { return Math.max(0, stockActual - stockReservado); }

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    @org.hibernate.annotations.ColumnDefault("5")
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 5;

    @org.hibernate.annotations.ColumnDefault("true")
    @Column(nullable = false)
    private Boolean estado = true;

    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @NotNull(message = "La unidad de medida es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false, length = 20)
    private UnidadMedida unidadMedida;

    // Control de concurrencia optimista: evita que dos ventas/compras
    // simultáneas sobre el mismo producto se "pisen" al actualizar el stock
    // (importante en cuanto haya varias estaciones conectadas a Supabase).
    @Version
    @Column(nullable = false)
    private Long version = 0L;
}
