package pe.edu.utp.Grupo06.service;

import pe.edu.utp.Grupo06.model.Compra;
import pe.edu.utp.Grupo06.model.MovimientoInventario;
import pe.edu.utp.Grupo06.model.Venta;
import pe.edu.utp.Grupo06.model.enums.TipoMovimiento;
import java.time.LocalDateTime;
import java.util.List;

public interface IMovimientoInventarioService {
    MovimientoInventario registrarMovimiento(Long productoId, Long usuarioId, TipoMovimiento tipo, Integer cantidad, String motivo, Compra compra, Venta venta);

    default MovimientoInventario registrarMovimiento(Long productoId, Long usuarioId, TipoMovimiento tipo, Integer cantidad, String motivo) {
        return registrarMovimiento(productoId, usuarioId, tipo, cantidad, motivo, null, null);
    }

    List<MovimientoInventario> listarPorProducto(Long productoId);
    List<MovimientoInventario> listarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin);
    List<MovimientoInventario> listarPorTipo(TipoMovimiento tipo);
}