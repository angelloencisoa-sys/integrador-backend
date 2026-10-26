package pe.edu.utp.Grupo06.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.producto.PresentacionRequestDTO;
import pe.edu.utp.Grupo06.dto.producto.PresentacionResponseDTO;
import pe.edu.utp.Grupo06.model.PresentacionProducto;
import pe.edu.utp.Grupo06.service.IPresentacionProductoService;
import java.util.List;

@RestController
@RequestMapping("/api/productos/{productoId}/presentaciones")
public class PresentacionProductoController {
    private final IPresentacionProductoService service;

    public PresentacionProductoController(IPresentacionProductoService service) { this.service = service; }

    @GetMapping
    public List<PresentacionResponseDTO> listar(@PathVariable Long productoId,
                                                 @RequestParam(defaultValue = "true") boolean soloActivas) {
        return service.listar(productoId, soloActivas).stream().map(this::mapear).toList();
    }

    @PostMapping
    public ResponseEntity<PresentacionResponseDTO> crear(@PathVariable Long productoId,
                                                          @Valid @RequestBody PresentacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapear(service.guardar(productoId, null, dto.nombrePresentacion(), dto.factorConversion())));
    }

    @PutMapping("/{id}")
    public PresentacionResponseDTO editar(@PathVariable Long productoId, @PathVariable Long id,
                                           @Valid @RequestBody PresentacionRequestDTO dto) {
        return mapear(service.guardar(productoId, id, dto.nombrePresentacion(), dto.factorConversion()));
    }

    @DeleteMapping("/{id}")
    public PresentacionResponseDTO desactivar(@PathVariable Long productoId, @PathVariable Long id) {
        PresentacionProducto presentacion = service.buscar(id);
        if (!presentacion.getProducto().getId().equals(productoId))
            throw new IllegalArgumentException("La presentación no pertenece al producto");
        return mapear(service.desactivar(id));
    }

    @PostMapping("/{id}/activar")
    public PresentacionResponseDTO activar(@PathVariable Long productoId, @PathVariable Long id) {
        PresentacionProducto presentacion = service.buscar(id);
        if (!presentacion.getProducto().getId().equals(productoId))
            throw new IllegalArgumentException("La presentación no pertenece al producto");
        return mapear(service.activar(id));
    }

    private PresentacionResponseDTO mapear(PresentacionProducto p) {
        return new PresentacionResponseDTO(p.getId(), p.getProducto().getId(), p.getNombrePresentacion(), p.getFactorConversion(), p.getEstado());
    }
}
