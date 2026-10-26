package pe.edu.utp.Grupo06.controller;

import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.catalogo.*;
import pe.edu.utp.Grupo06.service.CatalogoPublicoService;
import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoPublicoController {
    private final CatalogoPublicoService catalogo;
    public CatalogoPublicoController(CatalogoPublicoService catalogo) { this.catalogo = catalogo; }
    @GetMapping("/productos") public List<ProductoPublicoDTO> productos() { return catalogo.productos(); }
    @GetMapping("/productos/{id}") public ProductoPublicoDTO producto(@PathVariable Long id) { return catalogo.producto(id); }
    @GetMapping("/categorias") public List<CategoriaPublicaDTO> categorias() { return catalogo.categorias(); }
}
