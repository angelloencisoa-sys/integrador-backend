package pe.edu.utp.Grupo06.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.Grupo06.model.Pago;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByVentaId(Long ventaId);

    @org.springframework.data.jpa.repository.Query("SELECT p.metodoPago, SUM(p.monto) " +
            "FROM Pago p " +
            "WHERE p.venta.fechaVenta BETWEEN :inicio AND :fin AND p.venta.demo = false AND p.venta.estado = pe.edu.utp.Grupo06.model.enums.EstadoVenta.EMITIDA " +
            "GROUP BY p.metodoPago")
    List<Object[]> findMetodosPagoEntreFechas(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
                                             @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin);
}