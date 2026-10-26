package pe.edu.utp.Grupo06.dto.venta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequestDTO {

    @NotBlank(message = "El número de ticket es obligatorio")
    private String numeroTicket;

    // Compatibilidad con clientes anteriores; la API usa la identidad del token.
    private Long usuarioId;

    @NotEmpty(message = "La venta debe incluir al menos un producto")
    @Valid
    private List<DetalleVentaRequestDTO> detalles;

    @NotEmpty(message = "La venta debe incluir al menos un método de pago")
    @Valid
    private List<PagoRequestDTO> pagos;
}
