package pe.edu.utp.Grupo06.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenInventarioDTO {
    private Long totalProductosActivos;
    private Long productosBajoStock;
    private Long alertasPendientes;
    private BigDecimal valorizacionTotalInventario;
}
