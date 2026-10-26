package pe.edu.utp.Grupo06.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.model.Compra;
import pe.edu.utp.Grupo06.model.DetalleCompra;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.PresentacionProducto;
import pe.edu.utp.Grupo06.model.enums.EstadoCompra;
import pe.edu.utp.Grupo06.model.enums.MotivoDiferencia;
import pe.edu.utp.Grupo06.model.enums.TipoMovimiento;
import pe.edu.utp.Grupo06.repository.CompraRepository;
import pe.edu.utp.Grupo06.repository.ProductoRepository;
import pe.edu.utp.Grupo06.repository.PresentacionProductoRepository;
import pe.edu.utp.Grupo06.service.ICompraService;
import pe.edu.utp.Grupo06.service.IMovimientoInventarioService;
import pe.edu.utp.Grupo06.util.Validador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraServiceImpl implements ICompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PresentacionProductoRepository presentacionRepository;

    @Autowired
    private IMovimientoInventarioService movimientoService;

    @Autowired
    private Validador validador;

    @Override
    @Transactional
    public Compra registrarCompra(Compra compra) {
        if (compra.getDetalles() == null || compra.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La compra debe incluir al menos un detalle");
        }
        if (compra.getProveedor() == null || compra.getUsuario() == null)
            throw new IllegalArgumentException("Proveedor y usuario son obligatorios");
        String numero = compra.getNumeroComprobante() == null ? "" : compra.getNumeroComprobante().trim();
        if (numero.isEmpty()) throw new IllegalArgumentException("El número de comprobante es obligatorio");
        if (compraRepository.existsByProveedorIdAndNumeroComprobanteIgnoreCase(compra.getProveedor().getId(), numero))
            throw new IllegalArgumentException("El proveedor ya tiene registrado ese comprobante");
        compra.setNumeroComprobante(numero);
        compra.setTipoComprobante(compra.getTipoComprobante() == null || compra.getTipoComprobante().isBlank()
                ? "OTRO" : compra.getTipoComprobante().trim().toUpperCase());
        compra.setEstadoCompra(EstadoCompra.REGISTRADA);
        BigDecimal totalCalculado = BigDecimal.ZERO;
        compra.setFechaCompra(LocalDateTime.now());

        // Primero se valida y calcula todo; recién al final se registran los
        // movimientos de stock, para no dejar movimientos "sueltos" si algún
        // detalle posterior falla la validación.
        for (DetalleCompra detalle : compra.getDetalles()) {
            if (detalle.getProducto() == null || detalle.getProducto().getId() == null ||
                    detalle.getPresentacion() == null || detalle.getPresentacion().getId() == null)
                throw new IllegalArgumentException("Producto y presentación son obligatorios");
            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado ID: " + detalle.getProducto().getId()));
            PresentacionProducto presentacion = presentacionRepository.findById(detalle.getPresentacion().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Presentación no encontrada"));
            if (!presentacion.getProducto().getId().equals(producto.getId()) || !Boolean.TRUE.equals(presentacion.getEstado()))
                throw new IllegalArgumentException("La presentación no pertenece al producto o está inactiva");
            if (!Boolean.TRUE.equals(producto.getEstado()))
                throw new IllegalArgumentException("Producto inactivo: " + producto.getNombre());
            Integer cantidad = detalle.getCantidadPresentaciones();
            Integer recibidas = detalle.getUnidadesRecibidas();
            Integer rechazadas = detalle.getUnidadesRechazadas();
            BigDecimal precio = detalle.getPrecioPresentacion();
            if (cantidad == null || cantidad <= 0 || presentacion.getFactorConversion() == null || presentacion.getFactorConversion() <= 0 ||
                    recibidas == null || recibidas < 0 || rechazadas == null || rechazadas < 0 || rechazadas > recibidas ||
                    precio == null || precio.signum() < 0 || precio.scale() > 2)
                throw new IllegalArgumentException("Cantidad, recepción, rechazo o precio inválido para " + producto.getNombre());
            int esperadas = Math.multiplyExact(cantidad, presentacion.getFactorConversion());
            MotivoDiferencia motivo = detalle.getMotivoDiferencia() == null ? MotivoDiferencia.SIN_DIFERENCIA : detalle.getMotivoDiferencia();
            if ((esperadas != recibidas || rechazadas != 0) && motivo == MotivoDiferencia.SIN_DIFERENCIA)
                throw new IllegalArgumentException("Indique el motivo de la diferencia para " + producto.getNombre());
            detalle.setProducto(producto);
            detalle.setPresentacion(presentacion);
            detalle.setNombrePresentacion(presentacion.getNombrePresentacion());
            detalle.setFactorConversion(presentacion.getFactorConversion());
            detalle.setUnidadesEsperadas(esperadas);
            detalle.setUnidadesIngresadas(recibidas - rechazadas);
            detalle.setMotivoDiferencia(motivo);
            detalle.setCantidad(cantidad); // columnas heredadas
            detalle.setPrecioUnitario(precio);
            detalle.setCostoUnitario(precio.divide(BigDecimal.valueOf(presentacion.getFactorConversion()), 6, RoundingMode.HALF_UP));
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cantidad));
            detalle.setSubtotal(subtotal);
            detalle.setCompra(compra);
            validador.validar(detalle);
            totalCalculado = totalCalculado.add(subtotal);
        }

        BigDecimal subtotalSinIgv = totalCalculado.divide(BigDecimal.valueOf(1.18), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal igvCalculado = totalCalculado.subtract(subtotalSinIgv);

        compra.setSubtotal(subtotalSinIgv);
        compra.setIgv(igvCalculado);
        compra.setTotal(totalCalculado);
        validador.validar(compra);
        Compra compraGuardada = compraRepository.saveAndFlush(compra);

        for (DetalleCompra detalle : compraGuardada.getDetalles()) {
            movimientoService.registrarMovimiento(
                    detalle.getProducto().getId(),
                    compraGuardada.getUsuario().getId(),
                    TipoMovimiento.ENTRADA_COMPRA,
                    detalle.getUnidadesIngresadas(),
                    "Compra de proveedor con comprobante: " + compraGuardada.getNumeroComprobante(),
                    compraGuardada,
                    null
            );
        }

        return compraGuardada;
    }

    @Override
    @Transactional
    public Compra anularCompra(Long id, Long usuarioId, String motivo) {
        Compra compra = compraRepository.findLockedById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada: " + id));
        if (compra.getEstadoCompra() == EstadoCompra.ANULADA)
            throw new IllegalArgumentException("La compra ya está anulada");
        for (DetalleCompra detalle : compra.getDetalles()) {
            movimientoService.registrarMovimiento(detalle.getProducto().getId(), usuarioId,
                    TipoMovimiento.ANULACION_COMPRA, detalle.getUnidadesIngresadas(),
                    "Anulación compra " + compra.getNumeroComprobante() +
                            (motivo == null || motivo.isBlank() ? "" : ": " + motivo.trim()), compra, null);
        }
        compra.setEstadoCompra(EstadoCompra.ANULADA);
        return compraRepository.saveAndFlush(compra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarCompras() {
        return compraRepository.findAllConProveedorYUsuario();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return compraRepository.findByFechaCompraBetweenOrderByFechaCompraDesc(inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public Compra buscarPorId(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));
    }
}
