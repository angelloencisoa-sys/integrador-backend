package pe.edu.utp.Grupo06.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.Grupo06.model.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findLockedById(Long id);

    @EntityGraph(attributePaths = {"rol"})
    Optional<Usuario> findByUsername(String username);

    Boolean existsByUsername(String username);

    @EntityGraph(attributePaths = {"rol"})
    List<Usuario> findByActivoTrue();

    @EntityGraph(attributePaths = {"rol"})
    Optional<Usuario> findById(Long id);

    @EntityGraph(attributePaths = {"rol"})
    @org.springframework.data.jpa.repository.Query("SELECT u FROM Usuario u")
    List<Usuario> findAllConRol();

    long countByRolNombreAndActivoTrue(pe.edu.utp.Grupo06.model.enums.RolNombre nombre);
}
