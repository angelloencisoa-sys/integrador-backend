package pe.edu.utp.Grupo06.dto.compra;

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
public class CompraRequestDTO {

    @NotBlank(message = "El número de comprobante es obligatorio")
    private String numeroComprobante;

    private String tipoComprobante;

    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    // Compatibilidad con clientes anteriores; la API usa la identidad del token.
    private Long usuarioId;

    @NotEmpty(message = "La compra debe incluir al menos un detalle")
    @Valid
    private List<DetalleCompraRequestDTO> detalles;
}
