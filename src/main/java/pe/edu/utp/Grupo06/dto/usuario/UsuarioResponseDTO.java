package pe.edu.utp.Grupo06.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.utp.Grupo06.model.enums.RolNombre;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private Boolean activo;
    private RolNombre rol;
}
