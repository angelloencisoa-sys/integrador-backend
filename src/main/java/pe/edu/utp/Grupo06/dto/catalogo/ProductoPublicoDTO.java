package pe.edu.utp.Grupo06.dto.catalogo;

import java.math.BigDecimal;

/** Vista comercial. No expone costos, proveedores, margenes ni datos internos. */
public record ProductoPublicoDTO(Long id, String codigo, String nombre, String marca,
        String descripcion, BigDecimal precioVenta, int disponible,
        Long categoriaId, String categoriaNombre, String unidadMedida) { }
