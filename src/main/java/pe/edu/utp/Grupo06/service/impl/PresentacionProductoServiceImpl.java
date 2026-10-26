package pe.edu.utp.Grupo06.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.model.PresentacionProducto;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.enums.UnidadMedida;
import pe.edu.utp.Grupo06.repository.PresentacionProductoRepository;
import pe.edu.utp.Grupo06.repository.ProductoRepository;
import pe.edu.utp.Grupo06.service.IPresentacionProductoService;
import java.util.List;
import java.util.Objects;

@Service
public class PresentacionProductoServiceImpl implements IPresentacionProductoService {
    private final PresentacionProductoRepository repository;
    private final ProductoRepository productoRepository;

    public PresentacionProductoServiceImpl(PresentacionProductoRepository repository, ProductoRepository productoRepository) {
        this.repository = repository;
        this.productoRepository = productoRepository;
    }

    @Override @Transactional(readOnly = true)
    public List<PresentacionProducto> listar(Long productoId, boolean soloActivas) {
        return soloActivas ? repository.findByProductoIdAndEstadoTrueOrderById(productoId) : repository.findByProductoIdOrderById(productoId);
    }

    @Override @Transactional(readOnly = true)
    public PresentacionProducto buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Presentación no encontrada: " + id));
    }

    @Override @Transactional
    public PresentacionProducto guardar(Long productoId, Long id, String nombre, Integer factor) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 80 || factor == null || factor <= 0)
            throw new IllegalArgumentException("Nombre y factor de conversión válidos son obligatorios");
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        PresentacionProducto presentacion = id == null ? new PresentacionProducto() : buscar(id);
        if (id != null && !presentacion.getProducto().getId().equals(productoId))
            throw new IllegalArgumentException("La presentación no pertenece al producto");
        String nombreFinal = nombre.trim();
        repository.findByProductoIdAndNombrePresentacionIgnoreCase(productoId, nombreFinal)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> { throw new IllegalArgumentException("Ya existe una presentación con ese nombre"); });
        if (id != null && esBase(productoId, id) && factor != 1)
            throw new IllegalArgumentException("La presentación base debe conservar factor 1");
        presentacion.setProducto(producto);
        presentacion.setNombrePresentacion(nombreFinal);
        presentacion.setFactorConversion(factor);
        if (id == null) presentacion.setEstado(true);
        return repository.save(presentacion);
    }

    @Override @Transactional
    public PresentacionProducto desactivar(Long id) {
        PresentacionProducto presentacion = buscar(id);
        if (esBase(presentacion.getProducto().getId(), id))
            throw new IllegalArgumentException("No se puede desactivar la presentación base");
        presentacion.setEstado(false);
        return repository.save(presentacion);
    }

    @Override @Transactional
    public PresentacionProducto activar(Long id) {
        PresentacionProducto presentacion = buscar(id);
        presentacion.setEstado(true);
        return repository.save(presentacion);
    }

    @Override @Transactional(readOnly = true)
    public boolean esBase(Long productoId, Long id) {
        return repository.findFirstByProductoIdAndFactorConversionOrderByIdAsc(productoId, 1)
                .map(base -> Objects.equals(base.getId(), id))
                .orElse(false);
    }

    @Override @Transactional
    public PresentacionProducto crearBase(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));
        String nombre = nombreBase(producto.getUnidadMedida());
        return repository.findFirstByProductoIdAndFactorConversionOrderByIdAsc(productoId, 1)
                .orElseGet(() -> guardar(productoId, null, nombre, 1));
    }

    public static String nombreBase(UnidadMedida unidad) {
        return switch (unidad) {
            case UNIDAD -> "Unidad";
            case KILOGRAMO -> "Kilogramo";
            case GRAMO -> "Gramo";
            case LITRO -> "Litro";
            case MILILITRO -> "Mililitro";
            case PAQUETE -> "Paquete";
            case CAJA -> "Caja";
            case DOCENA -> "Docena";
        };
    }
}
