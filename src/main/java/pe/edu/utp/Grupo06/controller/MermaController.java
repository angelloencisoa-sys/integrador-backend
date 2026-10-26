package pe.edu.utp.Grupo06.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.merma.MermaRequestDTO;
import pe.edu.utp.Grupo06.dto.merma.MermaResponseDTO;
import pe.edu.utp.Grupo06.model.Merma;
import pe.edu.utp.Grupo06.model.enums.MotivoMerma;
import pe.edu.utp.Grupo06.service.IMermaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mermas")
public class MermaController {

    @Autowired
    private IMermaService mermaService;

    @PostMapping
    public ResponseEntity<MermaResponseDTO> registrarMerma(@Valid @RequestBody MermaRequestDTO request,
                                                         java.security.Principal principal) {
        Merma merma = mermaService.registrarMerma(
                request.getProductoId(),
                Long.parseLong(principal.getName()),
                request.getCantidad(),
                request.getMotivo(),
                request.getFechaVencimiento(),
                request.getObservacion()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapearADTO(merma));
    }

    @GetMapping
    public ResponseEntity<List<MermaResponseDTO>> listarMermas(
            @RequestParam(required = false) MotivoMerma motivo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        List<Merma> lista;
        if (motivo != null || inicio != null || fin != null) {
            lista = mermaService.listarPorFiltros(motivo, inicio, fin);
        } else {
            lista = mermaService.listarTodas();
        }

        List<MermaResponseDTO> dtos = lista.stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<MermaResponseDTO>> listarPorProducto(@PathVariable Long productoId) {
        List<MermaResponseDTO> dtos = mermaService.listarPorProducto(productoId)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private MermaResponseDTO mapearADTO(Merma m) {
        MermaResponseDTO dto = new MermaResponseDTO();
        dto.setId(m.getId());
        if (m.getProducto() != null) {
            dto.setProductoId(m.getProducto().getId());
            dto.setProductoNombre(m.getProducto().getNombre());
            dto.setProductoCodigo(m.getProducto().getCodigo());
            dto.setProductoMarca(m.getProducto().getMarca() != null ? m.getProducto().getMarca() : "Genérico");
        }
        if (m.getUsuario() != null) {
            dto.setUsuarioId(m.getUsuario().getId());
            dto.setUsuarioNombre(m.getUsuario().getNombreCompleto());
        }
        dto.setCantidad(m.getCantidad());
        dto.setMotivo(m.getMotivo());
        dto.setMotivoDescripcion(m.getMotivo() != null ? m.getMotivo().getDescripcion() : "");
        dto.setCostoUnitario(m.getCostoUnitario());
        dto.setCostoTotalPerdida(m.getCostoTotalPerdida());
        dto.setFechaMerma(m.getFechaMerma());
        dto.setFechaVencimiento(m.getFechaVencimiento());
        dto.setObservacion(m.getObservacion());
        return dto;
    }
}
