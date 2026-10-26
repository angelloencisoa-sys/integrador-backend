package pe.edu.utp.Grupo06.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.venta.*;
import pe.edu.utp.Grupo06.model.DetalleVenta;
import pe.edu.utp.Grupo06.model.Pago;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.model.Venta;
import pe.edu.utp.Grupo06.service.IProductoService;
import pe.edu.utp.Grupo06.service.IUsuarioService;
import pe.edu.utp.Grupo06.service.IVentaService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private IVentaService ventaService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IProductoService productoService;

    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> listarVentas() {
        List<VentaResponseDTO> ventas = ventaService.listarVentas()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> buscarPorId(@PathVariable Long id) {
        Venta venta = ventaService.buscarPorId(id);
        return ResponseEntity.ok(mapearADTO(venta));
    }

    @GetMapping("/ticket/{ticket}")
    public ResponseEntity<VentaResponseDTO> buscarPorTicket(@PathVariable String ticket) {
        Venta venta = ventaService.buscarPorTicket(ticket);
        return ResponseEntity.ok(mapearADTO(venta));
    }

    @GetMapping("/rango")
    public ResponseEntity<List<VentaResponseDTO>> listarPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<VentaResponseDTO> ventas = ventaService.listarPorFechas(inicio, fin)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/reporte/mayor-rotacion")
    public ResponseEntity<List<ProductoRotacionDTO>> reporteMayorRotacion() {
        List<Object[]> resultados = ventaService.reporteMayorRotacion();

        List<ProductoRotacionDTO> reporte = resultados.stream().map(fila ->
                new ProductoRotacionDTO(
                        ((Number) fila[0]).longValue(), // ID del producto
                        (String) fila[2],               // Nombre
                        (String) fila[1],               // Código
                        ((Number) fila[3]).longValue(), // Cantidad vendida
                        (BigDecimal) fila[4]            // Total recaudado
                )
        ).collect(Collectors.toList());

        return ResponseEntity.ok(reporte);
    }

    @PostMapping
    public ResponseEntity<VentaResponseDTO> registrarVenta(@Valid @RequestBody VentaRequestDTO request,
                                                         java.security.Principal principal) {
        Usuario usuario = usuarioService.buscarPorId(Long.parseLong(principal.getName()));

        Venta venta = new Venta();
        venta.setNumeroTicket(request.getNumeroTicket());
        venta.setUsuario(usuario);

        List<DetalleVenta> detalles = new ArrayList<>();
        for (DetalleVentaRequestDTO detDto : request.getDetalles()) {
            Producto producto = productoService.buscarPorId(detDto.getProductoId());
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(detDto.getCantidad());
            detalles.add(detalle);
        }
        venta.setDetalles(detalles);

        List<Pago> pagos = new ArrayList<>();
        for (PagoRequestDTO pagoDto : request.getPagos()) {
            Pago pago = new Pago();
            pago.setMetodoPago(pagoDto.getMetodoPago());
            pago.setMonto(pagoDto.getMonto());
            pagos.add(pago);
        }
        venta.setPagos(pagos);

        Venta ventaGuardada = ventaService.registrarVenta(venta);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapearADTO(ventaGuardada));
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<VentaResponseDTO> anularVenta(
            @PathVariable Long id,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String motivo, java.security.Principal principal) {
        Venta ventaAnulada = ventaService.anularVenta(id, Long.parseLong(principal.getName()), motivo);
        return ResponseEntity.ok(mapearADTO(ventaAnulada));
    }

    private VentaResponseDTO mapearADTO(Venta v) {
        List<DetalleVentaResponseDTO> detalles = v.getDetalles() != null ? v.getDetalles().stream().map(d ->
                new DetalleVentaResponseDTO(
                        d.getId(),
                        d.getProducto() != null ? d.getProducto().getId() : null,
                        d.getProducto() != null ? d.getProducto().getNombre() : null,
                        d.getProducto() != null ? d.getProducto().getCodigo() : null,
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()
                )
        ).collect(Collectors.toList()) : List.of();

        List<PagoResponseDTO> pagos = v.getPagos() != null ? v.getPagos().stream().map(p ->
                new PagoResponseDTO(
                        p.getId(),
                        p.getMetodoPago(),
                        p.getMonto()
                )
        ).collect(Collectors.toList()) : List.of();

        return new VentaResponseDTO(
                v.getId(),
                v.getNumeroTicket(),
                v.getFechaVenta(),
                v.getTotal(),
                v.getEstado(),
                v.getUsuario() != null ? v.getUsuario().getId() : null,
                v.getUsuario() != null ? v.getUsuario().getNombreCompleto() : null,
                detalles,
                pagos
        );
    }
}
