package pe.edu.utp.Grupo06.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.utp.Grupo06.model.enums.RolNombre;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private RolNombre rol;
    private String mensaje;
    private String accessToken;
    private String tokenType;
    private long expiresIn;
}
