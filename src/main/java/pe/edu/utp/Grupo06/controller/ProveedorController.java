package pe.edu.utp.Grupo06.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.proveedor.ProveedorDTO;
import pe.edu.utp.Grupo06.model.Proveedor;
import pe.edu.utp.Grupo06.service.IProveedorService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    @Autowired
    private IProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<List<ProveedorDTO>> listarActivos() {
        List<ProveedorDTO> proveedores = proveedorService.listarActivos()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(proveedores);
    }

    @GetMapping("/todos")
    public ResponseEntity<List<ProveedorDTO>> listarTodos() {
        List<ProveedorDTO> proveedores = proveedorService.listarTodos()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(proveedores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorDTO> buscarPorId(@PathVariable Long id) {
        Proveedor proveedor = proveedorService.buscarPorId(id);
        return ResponseEntity.ok(mapearADTO(proveedor));
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<ProveedorDTO> buscarPorRuc(@PathVariable String ruc) {
        Proveedor proveedor = proveedorService.buscarPorRuc(ruc);
        return ResponseEntity.ok(mapearADTO(proveedor));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProveedorDTO>> buscarPorRazonSocial(@RequestParam String q) {
        List<ProveedorDTO> proveedores = proveedorService.buscarPorRazonSocial(q)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(proveedores);
    }

    @PostMapping
    public ResponseEntity<ProveedorDTO> registrar(@Valid @RequestBody ProveedorDTO request) {
        Proveedor proveedor = new Proveedor();
        proveedor.setRuc(request.getRuc());
        proveedor.setRazonSocial(request.getRazonSocial());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setEstado(true);

        Proveedor guardado = proveedorService.registrar(proveedor);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapearADTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorDTO request) {
        Proveedor proveedor = new Proveedor();
        proveedor.setRuc(request.getRuc());
        proveedor.setRazonSocial(request.getRazonSocial());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setEstado(request.getEstado() != null ? request.getEstado() : true);

        Proveedor actualizado = proveedorService.actualizar(id, proveedor);
        return ResponseEntity.ok(mapearADTO(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private ProveedorDTO mapearADTO(Proveedor p) {
        return new ProveedorDTO(
                p.getId(),
                p.getRuc(),
                p.getRazonSocial(),
                p.getTelefono(),
                p.getCorreo(),
                p.getDireccion(),
                p.getEstado()
        );
    }
}
