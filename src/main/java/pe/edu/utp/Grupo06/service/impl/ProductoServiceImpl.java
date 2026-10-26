package pe.edu.utp.Grupo06.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.repository.ProductoRepository;
import pe.edu.utp.Grupo06.service.IAlertaReposicionService;
import pe.edu.utp.Grupo06.service.IProductoService;
import pe.edu.utp.Grupo06.service.IPresentacionProductoService;
import pe.edu.utp.Grupo06.model.enums.UnidadMedida;
import pe.edu.utp.Grupo06.util.Validador;

import java.util.List;

@Service
public class ProductoServiceImpl implements IProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private IAlertaReposicionService alertaReposicionService;

    @Autowired
    private Validador validador;

    @Autowired
    private IPresentacionProductoService presentacionService;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository.findByEstadoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaIdAndEstadoTrue(categoriaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarConBajoStock() {
        return productoRepository.findProductosConBajoStock();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Producto buscarPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con código: " + codigo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndEstadoTrue(nombre);
    }

    @Override
    @Transactional
    public Producto registrar(Producto producto) {
        producto.setStockReservado(0);
        validarUnidadNueva(producto.getUnidadMedida());
        validador.validar(producto);
        Producto guardado = productoRepository.save(producto);
        presentacionService.crearBase(guardado.getId());
        alertaReposicionService.verificarYGenerarAlerta(guardado.getId());
        return guardado;
    }

    @Override
    @Transactional
    public Producto actualizar(Long id, Producto producto) {
        Producto existente = buscarPorId(id);
        if (producto.getUnidadMedida() != existente.getUnidadMedida())
            throw new IllegalArgumentException("La unidad base de un producto existente requiere migración manual de stock e historial");
        existente.setNombre(producto.getNombre());
        existente.setMarca(producto.getMarca());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPrecioCompra(producto.getPrecioCompra());
        existente.setPrecioVenta(producto.getPrecioVenta());
        existente.setStockMinimo(producto.getStockMinimo());
        existente.setCategoria(producto.getCategoria());
        existente.setProveedor(producto.getProveedor());
        existente.setUnidadMedida(producto.getUnidadMedida());
        existente.setEstado(producto.getEstado());
        // codigo y stockActual NO se tocan aquí a propósito:
        // el código es el identificador comercial del producto (no debería
        // cambiar tras su creación) y el stock solo se modifica a través de
        // MovimientoInventarioService, para mantener la trazabilidad (RF09).

        validador.validar(existente);

        Producto actualizado = productoRepository.save(existente);
        alertaReposicionService.verificarYGenerarAlerta(actualizado.getId());
        return actualizado;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        if (producto.getStockActual() != null && producto.getStockActual() > 0) {
            throw new RuntimeException("No se puede dar de baja el producto '" + producto.getNombre()
                    + "' porque cuenta con " + producto.getStockActual()
                    + " unidades en stock. Primero debe liquidar o ajustar el stock a 0.");
        }
        producto.setEstado(false);
        productoRepository.save(producto);
    }

    private void validarUnidadNueva(UnidadMedida unidad) {
        if (unidad == null || unidad == UnidadMedida.PAQUETE || unidad == UnidadMedida.CAJA || unidad == UnidadMedida.DOCENA)
            throw new IllegalArgumentException("Seleccione una unidad base: UNIDAD, KILOGRAMO, GRAMO, LITRO o MILILITRO");
    }
}
