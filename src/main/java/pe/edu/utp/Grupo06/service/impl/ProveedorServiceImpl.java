package pe.edu.utp.Grupo06.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.Grupo06.model.Proveedor;
import pe.edu.utp.Grupo06.repository.ProveedorRepository;
import pe.edu.utp.Grupo06.service.IProveedorService;
import pe.edu.utp.Grupo06.util.Validador;

import java.util.List;

@Service
public class ProveedorServiceImpl implements IProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private Validador validador;

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarTodos() {
        return proveedorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return proveedorRepository.findByEstadoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor buscarPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Proveedor buscarPorRuc(String ruc) {
        return proveedorRepository.findByRuc(ruc)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con RUC: " + ruc));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Proveedor> buscarPorRazonSocial(String razonSocial) {
        return proveedorRepository.findByRazonSocialContainingIgnoreCase(razonSocial);
    }

    @Override
    @Transactional
    public Proveedor registrar(Proveedor proveedor) {
        validador.validar(proveedor);
        return proveedorRepository.save(proveedor);
    }

    @Autowired
    private pe.edu.utp.Grupo06.repository.ProductoRepository productoRepository;

    @Override
    @Transactional
    public Proveedor actualizar(Long id, Proveedor proveedor) {
        Proveedor existente = buscarPorId(id);
        if (Boolean.FALSE.equals(proveedor.getEstado()) && Boolean.TRUE.equals(existente.getEstado())) {
            long activos = productoRepository.countByProveedorIdAndEstadoTrue(id);
            if (activos > 0) {
                throw new RuntimeException("No se puede desactivar el proveedor porque tiene " + activos + " producto(s) activo(s) vinculado(s).");
            }
        }
        existente.setRazonSocial(proveedor.getRazonSocial());
        existente.setRuc(proveedor.getRuc());
        existente.setTelefono(proveedor.getTelefono());
        existente.setCorreo(proveedor.getCorreo());
        existente.setDireccion(proveedor.getDireccion());
        existente.setEstado(proveedor.getEstado());
        validador.validar(existente);
        return proveedorRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        long activos = productoRepository.countByProveedorIdAndEstadoTrue(id);
        if (activos > 0) {
            throw new RuntimeException("No se puede dar de baja el proveedor porque tiene " + activos + " producto(s) activo(s) vinculado(s).");
        }
        Proveedor proveedor = buscarPorId(id);
        proveedor.setEstado(false);
        proveedorRepository.save(proveedor);
    }
}
