package pe.edu.utp.Grupo06.service;

import pe.edu.utp.Grupo06.model.Proveedor;
import java.util.List;

public interface IProveedorService {
    List<Proveedor> listarTodos();
    List<Proveedor> listarActivos();
    Proveedor buscarPorId(Long id);
    Proveedor buscarPorRuc(String ruc);
    List<Proveedor> buscarPorRazonSocial(String razonSocial);
    Proveedor registrar(Proveedor proveedor);
    Proveedor actualizar(Long id, Proveedor proveedor);
    void eliminar(Long id);
}