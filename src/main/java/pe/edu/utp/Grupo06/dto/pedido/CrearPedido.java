package pe.edu.utp.Grupo06.dto.pedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
public record CrearPedido(@NotBlank @Pattern(regexp="[a-fA-F0-9-]{36}") String clave,
 @NotEmpty @Size(max=50) List<@Valid Item> items) {
 public record Item(@NotNull @Positive Long productoId,@Min(1) @Max(999) int cantidad,
 @NotNull @Positive @Digits(integer=8,fraction=2) BigDecimal precioEsperado) {}
}
