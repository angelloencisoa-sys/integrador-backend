package pe.edu.utp.Grupo06.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.categoria.CategoriaDTO;
import pe.edu.utp.Grupo06.model.Categoria;
import pe.edu.utp.Grupo06.service.ICategoriaService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private ICategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarActivas() {
        List<CategoriaDTO> categorias = categoriaService.listarActivas()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/todas")
    public ResponseEntity<List<CategoriaDTO>> listarTodas() {
        List<CategoriaDTO> categorias = categoriaService.listarTodas()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscarPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.buscarPorId(id);
        return ResponseEntity.ok(mapearADTO(categoria));
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> registrar(@Valid @RequestBody CategoriaDTO request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setEstado(true);

        Categoria guardada = categoriaService.registrar(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapearADTO(guardada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDTO request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setEstado(request.getEstado() != null ? request.getEstado() : true);

        Categoria actualizada = categoriaService.actualizar(id, categoria);
        return ResponseEntity.ok(mapearADTO(actualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private CategoriaDTO mapearADTO(Categoria c) {
        return new CategoriaDTO(
                c.getId(),
                c.getNombre(),
                c.getDescripcion(),
                c.getEstado()
        );
    }
}
