package pe.edu.utp.Grupo06.controller;

import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.auth.*;
import pe.edu.utp.Grupo06.dto.usuario.UsuarioResponseDTO;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.service.*;

@RestController
@RequestMapping("/api/auth")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthController {
    private final IUsuarioService usuarios;
    private final RegistroClienteService registro;
    private final TokenService tokens;

    public AuthController(IUsuarioService usuarios, RegistroClienteService registro, TokenService tokens) {
        this.usuarios = usuarios;
        this.registro = registro;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO datos) {
        return ResponseEntity.ok(respuesta(usuarios.validarCredenciales(datos.getUsername(), datos.getPassword())));
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponseDTO> registrar(@Valid @RequestBody RegistroClienteDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta(registro.registrar(datos)));
    }

    @GetMapping("/me")
    public UsuarioResponseDTO perfil(Authentication authentication) {
        Usuario u = usuarios.buscarPorId(Long.parseLong(authentication.getName()));
        return new UsuarioResponseDTO(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getEmail(),
                u.getTelefono(), u.getActivo(), u.getRol().getNombre());
    }

    private LoginResponseDTO respuesta(Usuario u) {
        return new LoginResponseDTO(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getEmail(),
                u.getRol().getNombre(), "Autenticación exitosa", tokens.emitir(u), "Bearer", TokenService.DURACION_SEGUNDOS);
    }
}
