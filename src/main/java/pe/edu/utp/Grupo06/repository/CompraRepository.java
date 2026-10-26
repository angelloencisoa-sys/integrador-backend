package pe.edu.utp.Grupo06.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import pe.edu.utp.Grupo06.model.Compra;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    boolean existsByProveedorIdAndNumeroComprobanteIgnoreCase(Long proveedorId, String numeroComprobante);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compra c WHERE c.id = :id")
    Optional<Compra> findLockedById(@org.springframework.data.repository.query.Param("id") Long id);
    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    List<Compra> findByProveedorIdOrderByFechaCompraDesc(Long proveedorId);

    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    List<Compra> findByFechaCompraBetweenOrderByFechaCompraDesc(LocalDateTime inicio, LocalDateTime fin);

    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    @Query("SELECT c FROM Compra c")
    List<Compra> findAllConProveedorYUsuario();

    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    @Query("SELECT c FROM Compra c WHERE c.fechaCompra BETWEEN :inicio AND :fin ORDER BY c.fechaCompra ASC")
    List<Compra> findComprasEntreFechas(@org.springframework.data.repository.query.Param("inicio") LocalDateTime inicio,
                                       @org.springframework.data.repository.query.Param("fin") LocalDateTime fin);

    @EntityGraph(attributePaths = {"proveedor", "usuario"})
    @Query("SELECT c FROM Compra c WHERE c.fechaCompra BETWEEN :inicio AND :fin AND c.estadoCompra = pe.edu.utp.Grupo06.model.enums.EstadoCompra.REGISTRADA ORDER BY c.fechaCompra ASC")
    List<Compra> findComprasRegistradasEntreFechas(@org.springframework.data.repository.query.Param("inicio") LocalDateTime inicio,
                                                   @org.springframework.data.repository.query.Param("fin") LocalDateTime fin);

    @Query("SELECT c.proveedor.razonSocial, SUM(c.total) " +
           "FROM Compra c " +
           "WHERE c.fechaCompra BETWEEN :inicio AND :fin AND c.estadoCompra = pe.edu.utp.Grupo06.model.enums.EstadoCompra.REGISTRADA " +
           "GROUP BY c.proveedor.razonSocial " +
           "ORDER BY SUM(c.total) DESC")
    List<Object[]> findGastoPorProveedorEntreFechas(@org.springframework.data.repository.query.Param("inicio") LocalDateTime inicio,
                                                  @org.springframework.data.repository.query.Param("fin") LocalDateTime fin);
}
