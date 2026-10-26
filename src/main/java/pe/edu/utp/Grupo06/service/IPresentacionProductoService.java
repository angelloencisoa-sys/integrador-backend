package pe.edu.utp.Grupo06.service;

import pe.edu.utp.Grupo06.model.PresentacionProducto;
import java.util.List;

public interface IPresentacionProductoService {
    List<PresentacionProducto> listar(Long productoId, boolean soloActivas);
    PresentacionProducto buscar(Long id);
    PresentacionProducto guardar(Long productoId, Long id, String nombre, Integer factor);
    PresentacionProducto desactivar(Long id);
    PresentacionProducto activar(Long id);
    boolean esBase(Long productoId, Long id);
    PresentacionProducto crearBase(Long productoId);
}
