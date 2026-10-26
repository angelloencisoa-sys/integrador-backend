package pe.edu.utp.Grupo06.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.utp.Grupo06.model.Venta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Venta v where v.id=:id")
    Optional<Venta> locked(Long id);
    Optional<Venta> findByNumeroTicket(String numeroTicket);
    @EntityGraph(attributePaths = {"usuario"})
    List<Venta> findByFechaVentaBetweenOrderByFechaVentaDesc(LocalDateTime inicio, LocalDateTime fin);

    @EntityGraph(attributePaths = {"usuario"})
    List<Venta> findByUsuarioIdOrderByFechaVentaDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "pagos"})
    @Query("SELECT v FROM Venta v")
    List<Venta> findAllConUsuario();

    Optional<Venta> findTopByNumeroTicketStartingWithOrderByIdDesc(String prefix);

    @EntityGraph(attributePaths = {"usuario", "pagos"})
    @Query("SELECT v FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.demo = false AND v.estado = pe.edu.utp.Grupo06.model.enums.EstadoVenta.EMITIDA ORDER BY v.fechaVenta ASC")
    List<Venta> findVentasEntreFechas(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
                                      @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin);

    @Query("SELECT v.usuario.nombreCompleto, COUNT(v), SUM(v.total) " +
           "FROM Venta v " +
           "WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.demo = false AND v.estado = pe.edu.utp.Grupo06.model.enums.EstadoVenta.EMITIDA " +
           "GROUP BY v.usuario.nombreCompleto " +
           "ORDER BY SUM(v.total) DESC")
    List<Object[]> findVentasPorVendedorEntreFechas(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
                                                    @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin);
}