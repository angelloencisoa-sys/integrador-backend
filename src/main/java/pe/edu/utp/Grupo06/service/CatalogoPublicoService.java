package pe.edu.utp.Grupo06.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.dto.catalogo.*;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.repository.*;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogoPublicoService {
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;

    public CatalogoPublicoService(ProductoRepository productos, CategoriaRepository categorias) {
        this.productos = productos;
        this.categorias = categorias;
    }

    public List<ProductoPublicoDTO> productos() {
        return productos.findByEstadoTrue().stream().filter(this::visible)
                .sorted(Comparator.comparing(Producto::getId)).map(this::mapear).toList();
    }

    public ProductoPublicoDTO producto(Long id) {
        return productos.findById(id).filter(this::visible).map(this::mapear)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    public List<CategoriaPublicaDTO> categorias() {
        return categorias.findByEstadoTrue().stream()
                .sorted(Comparator.comparing(c -> c.getId()))
                .map(c -> new CategoriaPublicaDTO(c.getId(), c.getNombre())).toList();
    }

    private boolean visible(Producto p) {
        return Boolean.TRUE.equals(p.getEstado()) && p.getCategoria() != null
                && Boolean.TRUE.equals(p.getCategoria().getEstado());
    }

    private ProductoPublicoDTO mapear(Producto p) {
        return new ProductoPublicoDTO(p.getId(), p.getCodigo(), p.getNombre(), p.getMarca(),
                p.getDescripcion(), p.getPrecioVenta(), p.getStockDisponible(),
                p.getCategoria().getId(), p.getCategoria().getNombre(), p.getUnidadMedida().name());
    }
}
