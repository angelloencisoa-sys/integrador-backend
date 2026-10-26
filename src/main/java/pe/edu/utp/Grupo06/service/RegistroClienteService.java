package pe.edu.utp.Grupo06.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.dto.auth.RegistroClienteDTO;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.model.enums.RolNombre;
import pe.edu.utp.Grupo06.repository.RolRepository;

@Service
public class RegistroClienteService {
    private final IUsuarioService usuarios;
    private final RolRepository roles;

    public RegistroClienteService(IUsuarioService usuarios, RolRepository roles) {
        this.usuarios = usuarios;
        this.roles = roles;
    }

    @Transactional
    public Usuario registrar(RegistroClienteDTO datos) {
        Usuario usuario = new Usuario();
        usuario.setUsername(datos.username());
        usuario.setPassword(datos.password());
        usuario.setNombreCompleto(datos.nombreCompleto().trim());
        usuario.setEmail(datos.email().trim());
        usuario.setTelefono(datos.telefono().trim());
        usuario.setActivo(true);
        usuario.setRol(roles.findByNombre(RolNombre.CLIENTE)
                .orElseThrow(() -> new IllegalStateException("Debe aplicar la migración del rol CLIENTE")));
        return usuarios.registrar(usuario);
    }
}
