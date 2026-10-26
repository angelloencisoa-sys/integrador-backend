package pe.edu.utp.Grupo06.service;

import pe.edu.utp.Grupo06.model.Venta;
import java.time.LocalDateTime;
import java.util.List;

public interface IVentaService {
    Venta registrarVenta(Venta venta);
    List<Venta> listarVentas();
    List<Venta> listarPorFechas(LocalDateTime inicio, LocalDateTime fin);
    Venta buscarPorId(Long id);
    Venta buscarPorTicket(String ticket);
    List<Object[]> reporteMayorRotacion();

    /**
     * Anula una venta EMITIDA: cambia su estado a ANULADA y devuelve al
     * inventario la cantidad vendida de cada detalle (movimiento de tipo
     * ENTRADA con el motivo correspondiente).
     *
     * @param ventaId  id de la venta a anular
     * @param usuarioId id del usuario que realiza la anulación (para el
     *                  registro de movimiento de inventario)
     * @param motivo    motivo de la anulación (puede ser null)
     */
    Venta anularVenta(Long ventaId, Long usuarioId, String motivo);

    /**
     * Genera el siguiente número correlativo de ticket comercial (ej. B001-00000001).
     */
    String generarSiguienteNumeroTicket();
}
