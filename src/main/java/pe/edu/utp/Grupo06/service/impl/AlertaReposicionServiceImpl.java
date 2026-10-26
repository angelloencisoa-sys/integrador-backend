package pe.edu.utp.Grupo06.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.model.AlertaReposicion;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.enums.EstadoAlerta;
import pe.edu.utp.Grupo06.repository.AlertaReposicionRepository;
import pe.edu.utp.Grupo06.repository.ProductoRepository;
import pe.edu.utp.Grupo06.service.IAlertaReposicionService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertaReposicionServiceImpl implements IAlertaReposicionService {

    @Autowired
    private AlertaReposicionRepository alertaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    @Transactional
    public void verificarYGenerarAlerta(Long productoId) {
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null || !producto.getEstado()) return;

        if (producto.getStockActual() <= producto.getStockMinimo()) {
            // Stock bajo o en quiebre (RF05): generar alerta si no hay una PENDIENTE ya.
            boolean yaExistePendiente = alertaRepository.existsByProductoIdAndEstado(productoId, EstadoAlerta.PENDIENTE);
            if (!yaExistePendiente) {
                AlertaReposicion alerta = new AlertaReposicion();
                alerta.setProducto(producto);
                alerta.setStockRegistrado(producto.getStockActual());
                alerta.setStockMinimo(producto.getStockMinimo());
                // Sugerencia base: reponer al menos el doble del stock mínimo
                alerta.setCantidadSugerida(Math.max((producto.getStockMinimo() * 2) - producto.getStockActual(), producto.getStockMinimo()));
                alerta.setEstado(EstadoAlerta.PENDIENTE);
                alerta.setFechaGeneracion(LocalDateTime.now());
                alerta.setObservacion("Generada automáticamente por quiebre/bajo stock");
                alertaRepository.save(alerta);
            }
        } else {
            // El stock volvió a estar por encima del mínimo (por una compra,
            // una reposición o un ajuste): si había una alerta PENDIENTE para
            // este producto, ya no refleja la realidad — se cierra sola.
            cerrarAlertasPendientesPorStockRecuperado(productoId, producto.getStockActual());
        }
    }

    private void cerrarAlertasPendientesPorStockRecuperado(Long productoId, Integer stockActual) {
        List<AlertaReposicion> pendientes = alertaRepository.findByProductoIdOrderByFechaGeneracionDesc(productoId)
                .stream()
                .filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE)
                .toList();

        for (AlertaReposicion alerta : pendientes) {
            alerta.setEstado(EstadoAlerta.ATENDIDA);
            alerta.setObservacion("Cerrada automáticamente: el stock (" + stockActual + ") volvió a superar el mínimo establecido");
            alertaRepository.save(alerta);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaReposicion> listarPendientes() {
        return alertaRepository.findByEstadoOrderByFechaGeneracionDesc(EstadoAlerta.PENDIENTE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaReposicion> listarPorEstado(EstadoAlerta estado) {
        return alertaRepository.findByEstadoOrderByFechaGeneracionDesc(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaReposicion> listarTodas() {
        return alertaRepository.findAllOrderByFechaGeneracionDesc();
    }

    @Override
    @Transactional
    public AlertaReposicion atenderAlerta(Long alertaId, String observacion) {
        AlertaReposicion alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada con ID: " + alertaId));
        alerta.setEstado(EstadoAlerta.ATENDIDA);
        alerta.setObservacion(observacion != null ? observacion : "Alerta atendida correctamente");
        return alertaRepository.save(alerta);
    }

    @Override
    @Transactional
    public AlertaReposicion descartarAlerta(Long alertaId) {
        AlertaReposicion alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada con ID: " + alertaId));
        alerta.setEstado(EstadoAlerta.DESCARTADA);
        return alertaRepository.save(alerta);
    }
}
