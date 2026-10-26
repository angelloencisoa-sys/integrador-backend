package pe.edu.utp.Grupo06.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.utp.Grupo06.model.DetalleVenta;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    @EntityGraph(attributePaths = {"producto"})
    List<DetalleVenta> findByVentaId(Long ventaId);

    // Consulta para productos de mayor rotación (RF08)
    @Query("SELECT d.producto.id, d.producto.codigo, d.producto.nombre, SUM(d.cantidad) AS totalVendido, SUM(d.subtotal) AS totalRecaudado " +
            "FROM DetalleVenta d " +
            "WHERE d.venta.demo = false AND d.venta.estado = pe.edu.utp.Grupo06.model.enums.EstadoVenta.EMITIDA " +
            "GROUP BY d.producto.id, d.producto.codigo, d.producto.nombre " +
            "ORDER BY totalVendido DESC")
    List<Object[]> findProductosMayorRotacion();

    @Query("SELECT d.producto.nombre, SUM(d.cantidad) AS totalVendido " +
            "FROM DetalleVenta d " +
            "WHERE d.venta.fechaVenta BETWEEN :inicio AND :fin AND d.venta.demo = false AND d.venta.estado = pe.edu.utp.Grupo06.model.enums.EstadoVenta.EMITIDA " +
            "GROUP BY d.producto.id, d.producto.nombre " +
            "ORDER BY totalVendido DESC")
    List<Object[]> findTopProductosEntreFechas(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
                                               @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin);

    @Query("SELECT d.producto.id, d.producto.codigo, d.producto.nombre, SUM(d.cantidad) AS totalVendido, SUM(d.subtotal) AS totalRecaudado " +
            "FROM DetalleVenta d " +
            "WHERE d.venta.fechaVenta BETWEEN :inicio AND :fin " +
            "  AND (:categoriaId IS NULL OR d.producto.categoria.id = :categoriaId) " +
            "  AND d.venta.demo = false AND d.venta.estado = pe.edu.utp.Grupo06.model.enums.EstadoVenta.EMITIDA " +
            "GROUP BY d.producto.id, d.producto.codigo, d.producto.nombre " +
            "ORDER BY totalVendido DESC")
    List<Object[]> findProductosMayorRotacionFiltrado(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
                                                     @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin,
                                                     @org.springframework.data.repository.query.Param("categoriaId") Long categoriaId);
}