package pe.edu.utp.Grupo06.repository;
import pe.edu.utp.Grupo06.model.EventoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EventoPedidoRepository extends JpaRepository<EventoPedido,Long> {
 List<EventoPedido> findByPedidoIdOrderByFechaAscIdAsc(Long id);
}
