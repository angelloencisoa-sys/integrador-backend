package pe.edu.utp.Grupo06.dto.venta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.utp.Grupo06.model.enums.EstadoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponseDTO {
    private Long id;
    private String numeroTicket;
    private LocalDateTime fechaVenta;
    private BigDecimal total;
    private EstadoVenta estado;
    private Long usuarioId;
    private String usuarioNombre;
    private List<DetalleVentaResponseDTO> detalles;
    private List<PagoResponseDTO> pagos;
}
