package pe.edu.utp.Grupo06.dto.producto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.utp.Grupo06.model.enums.UnidadMedida;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {

    @NotBlank(message = "El código del producto es obligatorio")
    @Size(max = 50, message = "El código no puede superar los 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
    private String nombre;

    @Size(max = 80, message = "La marca no puede superar los 80 caracteres")
    private String marca;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String descripcion;

    @NotNull(message = "El precio de compra es obligatorio")
    @PositiveOrZero(message = "El precio de compra no puede ser negativo")
    private BigDecimal precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor a 0")
    private BigDecimal precioVenta;

    @PositiveOrZero(message = "El stock actual no puede ser negativo")
    private Integer stockActual = 0;

    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 5;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    private Long proveedorId;

    @NotNull(message = "La unidad de medida es obligatoria")
    private UnidadMedida unidadMedida;
}
