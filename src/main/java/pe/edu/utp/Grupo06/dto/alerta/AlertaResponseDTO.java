package pe.edu.utp.Grupo06.dto.alerta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.utp.Grupo06.model.enums.EstadoAlerta;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertaResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private String productoCodigo;
    private Integer stockRegistrado;
    private Integer stockMinimo;
    private Integer cantidadSugerida;
    private EstadoAlerta estado;
    private LocalDateTime fechaGeneracion;
    private String observacion;
}
