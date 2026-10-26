package pe.edu.utp.Grupo06.dto.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PresentacionRequestDTO(@NotBlank String nombrePresentacion, @Positive Integer factorConversion) { }
