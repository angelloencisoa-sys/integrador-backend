package pe.edu.utp.Grupo06.service;

import pe.edu.utp.Grupo06.model.Categoria;
import java.util.List;

public interface ICategoriaService {
    List<Categoria> listarTodas();
    List<Categoria> listarActivas();
    Categoria buscarPorId(Long id);
    Categoria registrar(Categoria categoria);
    Categoria actualizar(Long id, Categoria categoria);
    void eliminar(Long id);
}
