package pe.edu.utp.Grupo06.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.dto.reporte.ResumenInventarioDTO;
import pe.edu.utp.Grupo06.dto.reporte.ResumenVentasDTO;
import pe.edu.utp.Grupo06.dto.venta.ProductoRotacionDTO;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.Venta;
import pe.edu.utp.Grupo06.model.enums.EstadoAlerta;
import pe.edu.utp.Grupo06.model.enums.EstadoVenta;
import pe.edu.utp.Grupo06.repository.AlertaReposicionRepository;
import pe.edu.utp.Grupo06.repository.DetalleVentaRepository;
import pe.edu.utp.Grupo06.repository.ProductoRepository;
import pe.edu.utp.Grupo06.repository.VentaRepository;
import pe.edu.utp.Grupo06.service.IReporteService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReporteServiceImpl implements IReporteService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AlertaReposicionRepository alertaRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Override
    @Transactional(readOnly = true)
    public ResumenInventarioDTO obtenerResumenInventario() {
        List<Producto> productosActivos = productoRepository.findByEstadoTrue();

        long totalActivos = productosActivos.size();
        long bajoStock = productosActivos.stream()
                .filter(p -> p.getStockActual() <= p.getStockMinimo())
                .count();

        long alertasPendientes = alertaRepository.findByEstadoOrderByFechaGeneracionDesc(EstadoAlerta.PENDIENTE).size();

        BigDecimal valorizacion = productosActivos.stream()
                .map(p -> p.getPrecioCompra().multiply(BigDecimal.valueOf(p.getStockActual())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumenInventarioDTO(totalActivos, bajoStock, alertasPendientes, valorizacion);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenVentasDTO obtenerResumenVentasPorPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        List<Venta> ventas = ventaRepository.findByFechaVentaBetweenOrderByFechaVentaDesc(inicio, fin);

        long emitidas = ventas.stream().filter(v -> !v.isDemo()).filter(v -> v.getEstado() == EstadoVenta.EMITIDA).count();
        long anuladas = ventas.stream().filter(v -> !v.isDemo()).filter(v -> v.getEstado() == EstadoVenta.ANULADA).count();

        BigDecimal totalRecaudado = ventas.stream()
                .filter(v -> !v.isDemo()).filter(v -> v.getEstado() == EstadoVenta.EMITIDA)
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumenVentasDTO(emitidas, anuladas, totalRecaudado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoRotacionDTO> obtenerProductosMayorRotacion() {
        List<Object[]> resultados = detalleVentaRepository.findProductosMayorRotacion();
        return resultados.stream().map(fila -> {
            Long productoId = ((Number) fila[0]).longValue();
            String codigo = (String) fila[1];
            String nombre = (String) fila[2];
            Long totalCantidad = ((Number) fila[3]).longValue();
            BigDecimal totalRecaudado = (BigDecimal) fila[4];
            return new ProductoRotacionDTO(
                    productoId,
                    nombre,
                    codigo,
                    totalCantidad,
                    totalRecaudado
            );
        }).collect(Collectors.toList());
    }
}
