package pe.edu.utp.Grupo06.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.repository.UsuarioRepository;
import pe.edu.utp.Grupo06.service.IUsuarioService;
import pe.edu.utp.Grupo06.util.Validador;

import java.util.List;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    private static final int PASSWORD_MIN_LENGTH = 6;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Validador validador;

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAllConRol();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con username: " + username));
    }

    @Override
    @Transactional
    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya se encuentra registrado: " + usuario.getUsername());
        }

        validarPasswordEnTextoPlano(usuario.getPassword());

        // Se encripta ANTES de validar la entidad completa (la validación de
        // @NotBlank sobre el campo password sigue pasando porque el hash
        // nunca queda vacío) y ANTES de guardar.
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        validador.validar(usuario);

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario actualizar(Long id, Usuario usuarioActualizado) {
        Usuario existente = buscarPorId(id);
        existente.setNombreCompleto(usuarioActualizado.getNombreCompleto());
        existente.setEmail(usuarioActualizado.getEmail());
        existente.setTelefono(usuarioActualizado.getTelefono());
        if (Boolean.TRUE.equals(existente.getActivo())
                && existente.getRol() != null
                && existente.getRol().getNombre()
                == pe.edu.utp.Grupo06.model.enums.RolNombre.ADMINISTRADOR
                && usuarioActualizado.getRol() != null
                && usuarioActualizado.getRol().getNombre()
                != pe.edu.utp.Grupo06.model.enums.RolNombre.ADMINISTRADOR) {

            long administradoresActivos = usuarioRepository
                    .countByRolNombreAndActivoTrue(
                            pe.edu.utp.Grupo06.model.enums.RolNombre.ADMINISTRADOR
                    );

            if (administradoresActivos <= 1) {
                throw new RuntimeException(
                        "No se puede cambiar el rol del único administrador activo"
                );
            }
        }

        existente.setRol(usuarioActualizado.getRol());

        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isBlank()) {
            validarPasswordEnTextoPlano(usuarioActualizado.getPassword());
            existente.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
        }

        validador.validar(existente);

        return usuarioRepository.save(existente);
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, Boolean activo) {
        Usuario usuario = buscarPorId(id);
        if (Boolean.FALSE.equals(activo)) {
            if (usuario.getRol() != null && usuario.getRol().getNombre() == pe.edu.utp.Grupo06.model.enums.RolNombre.ADMINISTRADOR) {
                long adminsActivos = usuarioRepository.countByRolNombreAndActivoTrue(pe.edu.utp.Grupo06.model.enums.RolNombre.ADMINISTRADOR);
                if (adminsActivos <= 1) {
                    throw new RuntimeException("No se puede desactivar al único Administrador activo del sistema. Debe existir al menos un administrador habilitado.");
                }
            }
        }
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario actualizarPerfil(Long id, String nombreCompleto, String email, String telefono, String passwordActual, String nuevaPassword) {
        Usuario usuario = buscarPorId(id);

        if (nombreCompleto != null && !nombreCompleto.isBlank()) {
            usuario.setNombreCompleto(nombreCompleto.trim());
        }
        if (email != null && !email.isBlank()) {
            usuario.setEmail(email.trim());
        }
        if (telefono != null) {
            usuario.setTelefono(telefono.trim());
        }

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            if (passwordActual == null || passwordActual.isBlank()) {
                throw new RuntimeException("Debe ingresar su contraseña actual para establecer una nueva.");
            }
            if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
                throw new RuntimeException("La contraseña actual ingresada es incorrecta.");
            }
            validarPasswordEnTextoPlano(nuevaPassword);
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        }

        validador.validar(usuario);
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario validarCredenciales(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Usuario o contraseña incorrectos"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Usuario o contraseña incorrectos");
        }

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Usuario o contraseña incorrectos");
        }

        return usuario;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario validarCredencialesPersonal(String username, String password) {
        Usuario usuario = validarCredenciales(username, password);
        if (usuario.getRol().getNombre() == pe.edu.utp.Grupo06.model.enums.RolNombre.CLIENTE) {
            throw new org.springframework.security.access.AccessDeniedException("Esta cuenta pertenece a la tienda web. El panel es exclusivo del personal.");
        }
        return usuario;
    }

    private void validarPasswordEnTextoPlano(String password) {
        if (password == null || password.isBlank()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }
        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new RuntimeException("La contraseña debe tener al menos " + PASSWORD_MIN_LENGTH + " caracteres");
        }
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña no puede superar 72 bytes en UTF-8");
        }
    }
}
