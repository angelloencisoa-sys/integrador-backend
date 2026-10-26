package pe.edu.utp.Grupo06.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.model.DetalleVenta;
import pe.edu.utp.Grupo06.model.Pago;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.Venta;
import pe.edu.utp.Grupo06.model.enums.EstadoVenta;
import pe.edu.utp.Grupo06.model.enums.TipoMovimiento;
import pe.edu.utp.Grupo06.repository.DetalleVentaRepository;
import pe.edu.utp.Grupo06.repository.ProductoRepository;
import pe.edu.utp.Grupo06.repository.VentaRepository;
import pe.edu.utp.Grupo06.service.IMovimientoInventarioService;
import pe.edu.utp.Grupo06.service.IVentaService;
import pe.edu.utp.Grupo06.util.Validador;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaServiceImpl implements IVentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private IMovimientoInventarioService movimientoService;

    @Autowired
    private Validador validador;
    @Autowired private pe.edu.utp.Grupo06.service.TicketService tickets;
    @Autowired private pe.edu.utp.Grupo06.repository.PedidoRepository pedidos;

    @Override
    @Transactional
    public Venta registrarVenta(Venta venta) {
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new RuntimeException("La venta debe contener al menos un detalle");
        }
        if (venta.getPagos() == null || venta.getPagos().isEmpty()) {
            throw new RuntimeException("La venta debe registrar al menos un método de pago");
        }

        venta.setDemo(false);
        venta.getDetalles().stream().map(d -> d.getProducto().getId()).distinct().sorted()
            .forEach(id -> productoRepository.findLockedById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado")));
        BigDecimal totalCalculado = BigDecimal.ZERO;
        venta.setFechaVenta(LocalDateTime.now());
        venta.setEstado(EstadoVenta.EMITIDA);

        for (DetalleVenta detalle : venta.getDetalles()) {
            validador.validar(detalle);

            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + detalle.getProducto().getId()));

            if (producto.getStockDisponible() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre() +
                        ". Disponible: " + producto.getStockDisponible() + ", Requerido: " + detalle.getCantidad());
            }

            detalle.setPrecioUnitario(producto.getPrecioVenta());
            BigDecimal subtotal = producto.getPrecioVenta().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            detalle.setSubtotal(subtotal);
            detalle.setVenta(venta);

            totalCalculado = totalCalculado.add(subtotal);
        }

        BigDecimal subtotalSinIgv = totalCalculado.divide(BigDecimal.valueOf(1.18), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal igvCalculado = totalCalculado.subtract(subtotalSinIgv);

        venta.setSubtotal(subtotalSinIgv);
        venta.setIgv(igvCalculado);
        venta.setTotal(totalCalculado);

        // RNF06 (integridad): la suma de los pagos (efectivo + Yape + Plin, etc.)
        // debe coincidir exactamente con el total calculado de la venta.
        BigDecimal totalPagado = BigDecimal.ZERO;
        for (Pago pago : venta.getPagos()) {
            if (pago.getMonto() == null || pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Cada pago debe tener un monto mayor a 0");
            }
            pago.setVenta(venta);
            totalPagado = totalPagado.add(pago.getMonto());
        }

        if (totalPagado.compareTo(totalCalculado) != 0) {
            throw new RuntimeException("El total de los pagos (" + totalPagado +
                    ") no coincide con el total de la venta (" + totalCalculado + ")");
        }

        Venta ventaGuardada = ventaRepository.save(venta);

        // Recién aquí, con la venta validada y guardada, se registran los
        // movimientos de salida de stock asociados directamente a la venta.
        for (DetalleVenta detalle : ventaGuardada.getDetalles()) {
            movimientoService.registrarMovimiento(
                    detalle.getProducto().getId(),
                    ventaGuardada.getUsuario().getId(),
                    TipoMovimiento.SALIDA,
                    detalle.getCantidad(),
                    "Venta con ticket: " + ventaGuardada.getNumeroTicket(),
                    null,
                    ventaGuardada
            );
        }

        return ventaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarVentas() {
        return ventaRepository.findAllConUsuario();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaVentaBetweenOrderByFechaVentaDesc(inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public Venta buscarPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Venta buscarPorTicket(String ticket) {
        return ventaRepository.findByNumeroTicket(ticket)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ticket: " + ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> reporteMayorRotacion() {
        return detalleVentaRepository.findProductosMayorRotacion();
    }

    @Override
    @Transactional
    public Venta anularVenta(Long ventaId, Long usuarioId, String motivo) {
        if (pedidos.existsByVentaId(ventaId)) throw new IllegalArgumentException("Gestione la devolución desde Pedidos web");
        Venta venta = ventaRepository.locked(ventaId).orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new RuntimeException("La venta con ticket " + venta.getNumeroTicket() + " ya se encuentra anulada");
        }

        // Devuelve al stock la cantidad de cada producto vendido.
        for (DetalleVenta detalle : venta.getDetalles()) {
            movimientoService.registrarMovimiento(
                    detalle.getProducto().getId(),
                    usuarioId,
                    TipoMovimiento.ENTRADA,
                    detalle.getCantidad(),
                    "Anulación de venta con ticket: " + venta.getNumeroTicket() +
                            (motivo != null && !motivo.isBlank() ? " — Motivo: " + motivo : ""),
                    null,
                    venta
            );
        }

        venta.setEstado(EstadoVenta.ANULADA);
        return ventaRepository.save(venta);
    }

    @Override
    @Transactional
    public String generarSiguienteNumeroTicket() {
        return tickets.siguiente("B001");
    }
}
