package pe.edu.utp.Grupo06.dto.merma;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pe.edu.utp.Grupo06.model.enums.MotivoMerma;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MermaRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    // Compatibilidad con clientes anteriores; la API usa la identidad del token.
    private Long usuarioId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad a dar de baja debe ser al menos 1")
    private Integer cantidad;

    @NotNull(message = "El motivo de la merma es obligatorio")
    private MotivoMerma motivo;

    private LocalDate fechaVencimiento;

    private String observacion;
}
