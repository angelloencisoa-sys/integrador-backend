package pe.edu.utp.Grupo06.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.Grupo06.model.MovimientoInventario;
import pe.edu.utp.Grupo06.model.enums.TipoMovimiento;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    @EntityGraph(attributePaths = {"producto", "usuario"})
    List<MovimientoInventario> findByProductoIdOrderByFechaMovimientoDesc(Long productoId);

    @EntityGraph(attributePaths = {"producto", "usuario"})
    List<MovimientoInventario> findByTipoMovimientoOrderByFechaMovimientoDesc(TipoMovimiento tipoMovimiento);

    @EntityGraph(attributePaths = {"producto", "usuario"})
    List<MovimientoInventario> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(LocalDateTime inicio, LocalDateTime fin);
}