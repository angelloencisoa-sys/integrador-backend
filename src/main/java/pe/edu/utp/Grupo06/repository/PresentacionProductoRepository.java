package pe.edu.utp.Grupo06.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.utp.Grupo06.model.PresentacionProducto;
import java.util.List;
import java.util.Optional;

public interface PresentacionProductoRepository extends JpaRepository<PresentacionProducto, Long> {
    List<PresentacionProducto> findByProductoIdOrderById(Long productoId);
    List<PresentacionProducto> findByProductoIdAndEstadoTrueOrderById(Long productoId);
    Optional<PresentacionProducto> findByProductoIdAndNombrePresentacionIgnoreCase(Long productoId, String nombre);
    Optional<PresentacionProducto> findFirstByProductoIdAndFactorConversionOrderByIdAsc(Long productoId, Integer factorConversion);
}
