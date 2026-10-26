package pe.edu.utp.Grupo06.repository;
import pe.edu.utp.Grupo06.model.Pedido;
import pe.edu.utp.Grupo06.model.enums.EstadoPedido;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import java.time.LocalDateTime;
public interface PedidoRepository extends JpaRepository<Pedido,Long> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Pedido p where p.id=:id") Optional<Pedido> locked(Long id);
 Optional<Pedido> findByClienteIdAndClave(Long clienteId,String clave);
 List<Pedido> findTop100ByClienteIdOrderByCreadoDesc(Long id);
 List<Pedido> findTop200ByOrderByCreadoDesc();
 List<Pedido> findByEstadoAndVenceBefore(EstadoPedido estado,LocalDateTime fecha);
 boolean existsByClienteIdAndEstadoIn(Long id,Collection<EstadoPedido> estados);
 boolean existsByVentaId(Long id);
 Optional<Pedido> findByCodigoRecojo(String codigo);
}
