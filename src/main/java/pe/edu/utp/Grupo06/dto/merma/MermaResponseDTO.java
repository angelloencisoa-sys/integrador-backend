package pe.edu.utp.Grupo06.dto.merma;

import lombok.*;
import pe.edu.utp.Grupo06.model.enums.MotivoMerma;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MermaResponseDTO {

    private Long id;
    private Long productoId;
    private String productoNombre;
    private String productoCodigo;
    private String productoMarca;
    private Long usuarioId;
    private String usuarioNombre;
    private Integer cantidad;
    private MotivoMerma motivo;
    private String motivoDescripcion;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotalPerdida;
    private LocalDateTime fechaMerma;
    private LocalDate fechaVencimiento;
    private String observacion;
}
