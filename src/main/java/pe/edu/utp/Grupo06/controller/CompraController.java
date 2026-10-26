package pe.edu.utp.Grupo06.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.utp.Grupo06.dto.compra.CompraRequestDTO;
import pe.edu.utp.Grupo06.dto.compra.CompraResponseDTO;
import pe.edu.utp.Grupo06.dto.compra.DetalleCompraRequestDTO;
import pe.edu.utp.Grupo06.dto.compra.DetalleCompraResponseDTO;
import pe.edu.utp.Grupo06.model.Compra;
import pe.edu.utp.Grupo06.model.DetalleCompra;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.Proveedor;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.service.ICompraService;
import pe.edu.utp.Grupo06.service.IProductoService;
import pe.edu.utp.Grupo06.service.IProveedorService;
import pe.edu.utp.Grupo06.service.IUsuarioService;
import pe.edu.utp.Grupo06.service.IPresentacionProductoService;
import pe.edu.utp.Grupo06.model.PresentacionProducto;
import pe.edu.utp.Grupo06.repository.DetalleCompraRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    @Autowired
    private ICompraService compraService;

    @Autowired
    private IProveedorService proveedorService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IProductoService productoService;

    @Autowired
    private IPresentacionProductoService presentacionService;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @GetMapping
    public ResponseEntity<List<CompraResponseDTO>> listarCompras() {
        List<CompraResponseDTO> compras = compraService.listarCompras()
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponseDTO> buscarPorId(@PathVariable Long id) {
        Compra compra = compraService.buscarPorId(id);
        return ResponseEntity.ok(mapearADTO(compra));
    }

    @GetMapping("/rango")
    public ResponseEntity<List<CompraResponseDTO>> listarPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<CompraResponseDTO> compras = compraService.listarPorFechas(inicio, fin)
                .stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(compras);
    }

    @PostMapping
    public ResponseEntity<CompraResponseDTO> registrarCompra(@Valid @RequestBody CompraRequestDTO request,
                                                            java.security.Principal principal) {
        Proveedor proveedor = proveedorService.buscarPorId(request.getProveedorId());
        Usuario usuario = usuarioService.buscarPorId(Long.parseLong(principal.getName()));

        Compra compra = new Compra();
        compra.setNumeroComprobante(request.getNumeroComprobante());
        compra.setTipoComprobante(request.getTipoComprobante());
        compra.setProveedor(proveedor);
        compra.setUsuario(usuario);

        List<DetalleCompra> detalles = new ArrayList<>();
        for (DetalleCompraRequestDTO detDto : request.getDetalles()) {
            Producto producto = productoService.buscarPorId(detDto.getProductoId());
            DetalleCompra detalle = new DetalleCompra();
            detalle.setProducto(producto);
            PresentacionProducto presentacion = detDto.getPresentacionId() == null
                    ? presentacionService.listar(producto.getId(), true).stream()
                        .filter(p -> p.getFactorConversion() == 1).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Falta la presentación base del producto"))
                    : presentacionService.buscar(detDto.getPresentacionId());
            detalle.setPresentacion(presentacion);
            Integer cantidad = detDto.getCantidadPresentaciones() != null ? detDto.getCantidadPresentaciones() : detDto.getCantidad();
            detalle.setCantidadPresentaciones(cantidad);
            detalle.setPrecioPresentacion(detDto.getPrecioPresentacion() != null ? detDto.getPrecioPresentacion() : detDto.getPrecioUnitario());
            detalle.setUnidadesRecibidas(detDto.getUnidadesRecibidas() != null ? detDto.getUnidadesRecibidas()
                    : cantidad == null ? null : Math.multiplyExact(cantidad, presentacion.getFactorConversion()));
            detalle.setUnidadesRechazadas(detDto.getUnidadesRechazadas() == null ? 0 : detDto.getUnidadesRechazadas());
            detalle.setMotivoDiferencia(detDto.getMotivoDiferencia());
            detalle.setObservacion(detDto.getObservacion());
            detalles.add(detalle);
        }
        compra.setDetalles(detalles);

        Compra compraGuardada = compraService.registrarCompra(compra);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapearADTO(compraGuardada));
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<CompraResponseDTO> anularCompra(@PathVariable Long id,
                                                           @RequestParam(required = false) Long usuarioId,
                                                           @RequestParam(required = false) String motivo,
                                                           java.security.Principal principal) {
        return ResponseEntity.ok(mapearADTO(compraService.anularCompra(id, Long.parseLong(principal.getName()), motivo)));
    }

    private CompraResponseDTO mapearADTO(Compra c) {
        List<DetalleCompra> origen = c.getId() == null ? c.getDetalles() : detalleCompraRepository.findByCompraId(c.getId());
        List<DetalleCompraResponseDTO> detalles = origen != null ? origen.stream().map(d -> {
            DetalleCompraResponseDTO dto = new DetalleCompraResponseDTO();
            dto.setId(d.getId());
            dto.setProductoId(d.getProducto().getId());
            dto.setProductoNombre(d.getProducto().getNombre());
            dto.setProductoCodigo(d.getProducto().getCodigo());
            dto.setCantidad(d.getCantidad());
            dto.setPrecioUnitario(d.getPrecioUnitario());
            dto.setSubtotal(d.getSubtotal());
            dto.setPresentacionId(d.getPresentacion().getId());
            dto.setNombrePresentacion(d.getNombrePresentacion());
            dto.setCantidadPresentaciones(d.getCantidadPresentaciones());
            dto.setFactorConversion(d.getFactorConversion());
            dto.setUnidadesEsperadas(d.getUnidadesEsperadas());
            dto.setUnidadesRecibidas(d.getUnidadesRecibidas());
            dto.setUnidadesRechazadas(d.getUnidadesRechazadas());
            dto.setUnidadesIngresadas(d.getUnidadesIngresadas());
            dto.setPrecioPresentacion(d.getPrecioPresentacion());
            dto.setCostoUnitario(d.getCostoUnitario());
            dto.setMotivoDiferencia(d.getMotivoDiferencia().name());
            dto.setObservacion(d.getObservacion());
            return dto;
        }).collect(Collectors.toList()) : List.of();

        CompraResponseDTO dto = new CompraResponseDTO();
        dto.setId(c.getId());
        dto.setNumeroComprobante(c.getNumeroComprobante());
        dto.setTipoComprobante(c.getTipoComprobante());
        dto.setEstadoCompra(c.getEstadoCompra().name());
        dto.setFechaCompra(c.getFechaCompra());
        dto.setTotal(c.getTotal());
        dto.setProveedorId(c.getProveedor().getId());
        dto.setProveedorRazonSocial(c.getProveedor().getRazonSocial());
        dto.setUsuarioId(c.getUsuario().getId());
        dto.setUsuarioNombre(c.getUsuario().getNombreCompleto());
        dto.setDetalles(detalles);
        return dto;
    }
}
