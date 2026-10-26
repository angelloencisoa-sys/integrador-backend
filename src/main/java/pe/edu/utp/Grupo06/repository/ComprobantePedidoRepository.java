package pe.edu.utp.Grupo06.repository;
import pe.edu.utp.Grupo06.model.ComprobantePedido;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ComprobantePedidoRepository extends JpaRepository<ComprobantePedido,Long> {
 boolean existsByHash(String hash);
}
