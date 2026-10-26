package pe.edu.utp.Grupo06.service;

import pe.edu.utp.Grupo06.model.Usuario;
import java.util.List;

public interface IUsuarioService {
    List<Usuario> listarTodos();
    List<Usuario> listarActivos();
    Usuario buscarPorId(Long id);
    Usuario buscarPorUsername(String username);
    Usuario registrar(Usuario usuario);
    Usuario actualizar(Long id, Usuario usuario);
    void cambiarEstado(Long id, Boolean activo);

    /**
     * Valida username + password (en texto plano) contra el hash almacenado.
     * Pensado para ser usado luego por el AuthController / login de la API REST.
     *
     * @return el Usuario si las credenciales son correctas y está activo.
     * @throws RuntimeException si el usuario no existe, está inactivo o la
     *                          contraseña no coincide.
     */
    Usuario validarCredenciales(String username, String password);

    Usuario validarCredencialesPersonal(String username, String password);

    Usuario actualizarPerfil(Long id, String nombreCompleto, String email, String telefono, String passwordActual, String nuevaPassword);
}
