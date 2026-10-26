package pe.edu.utp.Grupo06.dto.compra;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompraResponseDTO {
    private Long id;
    private String numeroComprobante;
    private String tipoComprobante;
    private String estadoCompra;
    private LocalDateTime fechaCompra;
    private BigDecimal total;
    private Long proveedorId;
    private String proveedorRazonSocial;
    private Long usuarioId;
    private String usuarioNombre;
    private List<DetalleCompraResponseDTO> detalles;
}
