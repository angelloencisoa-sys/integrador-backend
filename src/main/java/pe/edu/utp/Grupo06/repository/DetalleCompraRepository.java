package pe.edu.utp.Grupo06.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.utp.Grupo06.model.DetalleCompra;

import java.util.List;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {
    @EntityGraph(attributePaths = {"producto", "presentacion"})
    List<DetalleCompra> findByCompraId(Long compraId);

    @org.springframework.data.jpa.repository.Query("SELECT d.producto.nombre, SUM(d.unidadesIngresadas) AS totalComprado, SUM(d.subtotal) AS totalGastado " +
            "FROM DetalleCompra d " +
            "WHERE d.compra.fechaCompra BETWEEN :inicio AND :fin AND d.compra.estadoCompra = pe.edu.utp.Grupo06.model.enums.EstadoCompra.REGISTRADA " +
            "GROUP BY d.producto.id, d.producto.nombre " +
            "ORDER BY totalComprado DESC")
    List<Object[]> findTopProductosCompradosEntreFechas(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
                                                       @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin);

    @org.springframework.data.jpa.repository.Query("SELECT d.producto.codigo, d.producto.nombre, d.compra.proveedor.razonSocial, SUM(d.unidadesIngresadas), SUM(d.subtotal) " +
            "FROM DetalleCompra d " +
            "WHERE d.compra.fechaCompra BETWEEN :inicio AND :fin AND d.compra.estadoCompra = pe.edu.utp.Grupo06.model.enums.EstadoCompra.REGISTRADA " +
            "GROUP BY d.producto.codigo, d.producto.nombre, d.compra.proveedor.razonSocial " +
            "ORDER BY SUM(d.subtotal) DESC")
    List<Object[]> findResumenProductosCompradosEntreFechas(@org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
                                                           @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin);
}
