package pe.edu.utp.Grupo06.dto.auth;

import jakarta.validation.constraints.*;

/** El cliente no puede elegir rol, ID ni estado de su cuenta. */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record RegistroClienteDTO(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,50}") String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 100) String nombreCompleto,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Pattern(regexp = "[0-9+ ()-]{7,15}") String telefono) {
    @Override
    public String toString() {
        return "RegistroClienteDTO[datos protegidos]";
    }
}
