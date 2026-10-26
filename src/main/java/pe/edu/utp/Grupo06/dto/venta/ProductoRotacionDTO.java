package pe.edu.utp.Grupo06.dto.venta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRotacionDTO {
    private Long productoId;
    private String nombre;
    private String codigo;
    private Long cantidadTotalVendida;
    private BigDecimal totalRecaudado;
}
