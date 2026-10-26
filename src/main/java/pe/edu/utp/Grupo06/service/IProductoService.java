package pe.edu.utp.Grupo06.service;

import pe.edu.utp.Grupo06.model.Producto;
import java.util.List;

public interface IProductoService {
    List<Producto> listarTodos();
    List<Producto> listarActivos();
    List<Producto> listarPorCategoria(Long categoriaId);
    List<Producto> listarConBajoStock();
    Producto buscarPorId(Long id);
    Producto buscarPorCodigo(String codigo);
    List<Producto> buscarPorNombre(String nombre);
    Producto registrar(Producto producto);
    Producto actualizar(Long id, Producto producto);
    void eliminar(Long id);
}