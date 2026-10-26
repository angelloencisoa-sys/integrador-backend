package pe.edu.utp.Grupo06.dto.producto;

public record PresentacionResponseDTO(Long id, Long productoId, String nombrePresentacion,
                                      Integer factorConversion, Boolean estado) { }
