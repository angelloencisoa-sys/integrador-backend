package pe.edu.utp.Grupo06.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.movimiento.MovimientoRequestDTO;
import pe.edu.utp.Grupo06.dto.movimiento.MovimientoResponseDTO;
import pe.edu.utp.Grupo06.model.MovimientoInventario;
import pe.edu.utp.Grupo06.model.enums.TipoMovimiento;
import pe.edu.utp.Grupo06.service.IMovimientoInventarioService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoInventarioController {

    @Autowired
    private IMovimientoInventarioService movimientoService;

    @PostMapping
    public ResponseEntity<MovimientoResponseDTO> registrarMovimiento(@Valid @RequestBody MovimientoRequestDTO request,
                                                                    java.security.Principal principal) {
        MovimientoInventario movimiento = movimientoService.registrarMovimiento(
                request.getProductoId(),
                Long.parseLong(principal.getName()),
                request.getTipoMovimiento(),
                request.getCantidad(),
                request.getMotivo()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapearADTO(movimiento));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<MovimientoResponseDTO>> listarPorProducto(@PathVariable Long productoId) {
        List<MovimientoResponseDTO> movimientos = movimientoService.listarPorProducto(productoId)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<MovimientoResponseDTO>> listarPorTipo(@PathVariable TipoMovimiento tipo) {
        List<MovimientoResponseDTO> movimientos = movimientoService.listarPorTipo(tipo)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/rango")
    public ResponseEntity<List<MovimientoResponseDTO>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<MovimientoResponseDTO> movimientos = movimientoService.listarPorRangoFechas(inicio, fin)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(movimientos);
    }

    private MovimientoResponseDTO mapearADTO(MovimientoInventario m) {
        return new MovimientoResponseDTO(
                m.getId(),
                m.getProducto() != null ? m.getProducto().getId() : null,
                m.getProducto() != null ? m.getProducto().getNombre() : null,
                m.getUsuario() != null ? m.getUsuario().getId() : null,
                m.getUsuario() != null ? m.getUsuario().getNombreCompleto() : null,
                m.getTipoMovimiento(),
                m.getCantidad(),
                m.getStockAnterior(),
                m.getStockPosterior(),
                m.getFechaMovimiento(),
                m.getMotivo()
        );
    }
}
