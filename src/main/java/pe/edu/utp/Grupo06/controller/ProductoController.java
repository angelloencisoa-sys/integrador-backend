package pe.edu.utp.Grupo06.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.producto.ProductoRequestDTO;
import pe.edu.utp.Grupo06.dto.producto.ProductoResponseDTO;
import pe.edu.utp.Grupo06.model.Categoria;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.Proveedor;
import pe.edu.utp.Grupo06.service.ICategoriaService;
import pe.edu.utp.Grupo06.service.IProductoService;
import pe.edu.utp.Grupo06.service.IProveedorService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private IProductoService productoService;

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarActivos() {
        List<ProductoResponseDTO> productos = productoService.listarActivos()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/todos")
    public ResponseEntity<List<ProductoResponseDTO>> listarTodos() {
        List<ProductoResponseDTO> productos = productoService.listarTodos()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> buscarPorId(@PathVariable Long id) {
        Producto producto = productoService.buscarPorId(id);
        return ResponseEntity.ok(mapearADTO(producto));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ProductoResponseDTO> buscarPorCodigo(@PathVariable String codigo) {
        Producto producto = productoService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(mapearADTO(producto));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        List<ProductoResponseDTO> productos = productoService.buscarPorNombre(nombre)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponseDTO>> listarPorCategoria(@PathVariable Long categoriaId) {
        List<ProductoResponseDTO> productos = productoService.listarPorCategoria(categoriaId)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/bajo-stock")
    public ResponseEntity<List<ProductoResponseDTO>> listarBajoStock() {
        List<ProductoResponseDTO> productos = productoService.listarConBajoStock()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productos);
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> registrar(@Valid @RequestBody ProductoRequestDTO request) {
        Categoria categoria = categoriaService.buscarPorId(request.getCategoriaId());
        Proveedor proveedor = null;
        if (request.getProveedorId() != null) {
            proveedor = proveedorService.buscarPorId(request.getProveedorId());
        }

        Producto producto = new Producto();
        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setMarca(request.getMarca());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioCompra(request.getPrecioCompra());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setStockActual(request.getStockActual() != null ? request.getStockActual() : 0);
        producto.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 5);
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);
        producto.setUnidadMedida(request.getUnidadMedida());
        producto.setEstado(true);

        Producto guardado = productoService.registrar(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapearADTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO request) {
        Categoria categoria = categoriaService.buscarPorId(request.getCategoriaId());
        Proveedor proveedor = null;
        if (request.getProveedorId() != null) {
            proveedor = proveedorService.buscarPorId(request.getProveedorId());
        }

        Producto producto = new Producto();
        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setMarca(request.getMarca());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioCompra(request.getPrecioCompra());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setStockActual(request.getStockActual());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);
        producto.setUnidadMedida(request.getUnidadMedida());

        Producto actualizado = productoService.actualizar(id, producto);
        return ResponseEntity.ok(mapearADTO(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private ProductoResponseDTO mapearADTO(Producto p) {
        boolean bajoStock = p.getStockActual() <= p.getStockMinimo();
        return new ProductoResponseDTO(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getMarca(),
                p.getDescripcion(),
                p.getPrecioCompra(),
                p.getPrecioVenta(),
                p.getStockActual(),
                p.getStockMinimo(),
                p.getEstado(),
                p.getCategoria() != null ? p.getCategoria().getId() : null,
                p.getCategoria() != null ? p.getCategoria().getNombre() : null,
                p.getProveedor() != null ? p.getProveedor().getId() : null,
                p.getProveedor() != null ? p.getProveedor().getRazonSocial() : null,
                p.getUnidadMedida(),
                bajoStock
        );
    }
}
