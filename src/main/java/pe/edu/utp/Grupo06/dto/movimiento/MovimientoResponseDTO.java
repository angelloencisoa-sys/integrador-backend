package pe.edu.utp.Grupo06.dto.movimiento;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.utp.Grupo06.model.enums.TipoMovimiento;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Long usuarioId;
    private String usuarioNombre;
    private TipoMovimiento tipoMovimiento;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockPosterior;
    private LocalDateTime fechaMovimiento;
    private String motivo;
}
