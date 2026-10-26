package pe.edu.utp.Grupo06.dto.producto;

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
public class ProductoResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String marca;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stockActual;
    private Integer stockMinimo;
    private Boolean estado;
    private Long categoriaId;
    private String categoriaNombre;
    private Long proveedorId;
    private String proveedorRazonSocial;
    private UnidadMedida unidadMedida;
    private Boolean bajoStock;
}
