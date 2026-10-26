package pe.edu.utp.Grupo06.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.model.Merma;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.model.enums.MotivoMerma;
import pe.edu.utp.Grupo06.model.enums.TipoMovimiento;
import pe.edu.utp.Grupo06.repository.MermaRepository;
import pe.edu.utp.Grupo06.repository.ProductoRepository;
import pe.edu.utp.Grupo06.repository.UsuarioRepository;
import pe.edu.utp.Grupo06.service.IMermaService;
import pe.edu.utp.Grupo06.service.IMovimientoInventarioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MermaServiceImpl implements IMermaService {

    @Autowired
    private MermaRepository mermaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IMovimientoInventarioService movimientoInventarioService;

    @Override
    @Transactional
    public Merma registrarMerma(Long productoId, Long usuarioId, Integer cantidad, MotivoMerma motivo, LocalDate fechaVencimiento, String observacion) {
        if (cantidad == null || cantidad <= 0) {
            throw new RuntimeException("La cantidad a dar de baja debe ser mayor a 0");
        }

        if (motivo == null) {
            throw new RuntimeException("El motivo de la merma es obligatorio");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        if (producto.getStockActual() < cantidad) {
            throw new RuntimeException("Stock insuficiente para dar de baja. Stock actual: " +
                    producto.getStockActual() + ", cantidad solicitada: " + cantidad);
        }

        BigDecimal costoUnitario = producto.getPrecioCompra() != null ? producto.getPrecioCompra() : BigDecimal.ZERO;
        BigDecimal costoTotal = costoUnitario.multiply(BigDecimal.valueOf(cantidad));

        Merma merma = new Merma();
        merma.setProducto(producto);
        merma.setUsuario(usuario);
        merma.setCantidad(cantidad);
        merma.setMotivo(motivo);
        merma.setCostoUnitario(costoUnitario);
        merma.setCostoTotalPerdida(costoTotal);
        merma.setFechaMerma(LocalDateTime.now());
        merma.setFechaVencimiento(fechaVencimiento);
        merma.setObservacion(observacion);

        Merma mermaGuardada = mermaRepository.save(merma);

        // Registro atómico en Kardex y disparo de alertas automáticas
        String motivoKardex = String.format("MERMA [%s]: %s | Pérdida: S/ %.2f",
                motivo.getDescripcion(),
                (observacion != null && !observacion.isBlank() ? observacion : "Sin observación adicional"),
                costoTotal);

        movimientoInventarioService.registrarMovimiento(
                productoId,
                usuarioId,
                TipoMovimiento.MERMA,
                cantidad,
                motivoKardex,
                null,
                null
        );

        return mermaGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Merma> listarTodas() {
        return mermaRepository.findAllConRelaciones();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Merma> listarPorProducto(Long productoId) {
        return mermaRepository.findByProductoIdOrderByFechaMermaDesc(productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Merma> listarPorFiltros(MotivoMerma motivo, LocalDateTime inicio, LocalDateTime fin) {
        return mermaRepository.buscarPorFiltros(motivo, inicio, fin);
    }
}
