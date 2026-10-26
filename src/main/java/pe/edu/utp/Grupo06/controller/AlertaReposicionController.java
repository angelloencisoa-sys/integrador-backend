package pe.edu.utp.Grupo06.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.alerta.AlertaResponseDTO;
import pe.edu.utp.Grupo06.dto.alerta.AtenderAlertaDTO;
import pe.edu.utp.Grupo06.model.AlertaReposicion;
import pe.edu.utp.Grupo06.model.enums.EstadoAlerta;
import pe.edu.utp.Grupo06.service.IAlertaReposicionService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alertas")
public class AlertaReposicionController {

    @Autowired
    private IAlertaReposicionService alertaService;

    @GetMapping("/pendientes")
    public ResponseEntity<List<AlertaResponseDTO>> listarPendientes() {
        List<AlertaResponseDTO> alertas = alertaService.listarPendientes()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<AlertaResponseDTO>> listarPorEstado(@PathVariable EstadoAlerta estado) {
        List<AlertaResponseDTO> alertas = alertaService.listarPorEstado(estado)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(alertas);
    }

    @PatchMapping("/{id}/atender")
    public ResponseEntity<AlertaResponseDTO> atenderAlerta(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) AtenderAlertaDTO request) {
        String observacion = (request != null && request.getObservacion() != null) ? request.getObservacion() : "Atendida desde API";
        AlertaReposicion alerta = alertaService.atenderAlerta(id, observacion);
        return ResponseEntity.ok(mapearADTO(alerta));
    }

    @PatchMapping("/{id}/descartar")
    public ResponseEntity<AlertaResponseDTO> descartarAlerta(@PathVariable Long id) {
        AlertaReposicion alerta = alertaService.descartarAlerta(id);
        return ResponseEntity.ok(mapearADTO(alerta));
    }

    private AlertaResponseDTO mapearADTO(AlertaReposicion a) {
        return new AlertaResponseDTO(
                a.getId(),
                a.getProducto() != null ? a.getProducto().getId() : null,
                a.getProducto() != null ? a.getProducto().getNombre() : null,
                a.getProducto() != null ? a.getProducto().getCodigo() : null,
                a.getStockRegistrado(),
                a.getStockMinimo(),
                a.getCantidadSugerida(),
                a.getEstado(),
                a.getFechaGeneracion(),
                a.getObservacion()
        );
    }
}
