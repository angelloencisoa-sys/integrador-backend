package pe.edu.utp.Grupo06.dto.compra;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import pe.edu.utp.Grupo06.model.enums.MotivoDiferencia;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCompraRequestDTO {

    @NotNull(message = "El producto es obligatorio en el detalle")
    private Long productoId;

    private Integer cantidad;

    private BigDecimal precioUnitario;

    private Long presentacionId;
    @Positive private Integer cantidadPresentaciones;
    @PositiveOrZero private BigDecimal precioPresentacion;
    @PositiveOrZero private Integer unidadesRecibidas;
    @PositiveOrZero private Integer unidadesRechazadas;
    private MotivoDiferencia motivoDiferencia;
    private String observacion;
}
