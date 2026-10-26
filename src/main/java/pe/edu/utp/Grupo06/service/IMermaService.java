package pe.edu.utp.Grupo06.service;

import pe.edu.utp.Grupo06.model.Merma;
import pe.edu.utp.Grupo06.model.enums.MotivoMerma;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IMermaService {

    Merma registrarMerma(Long productoId, Long usuarioId, Integer cantidad, MotivoMerma motivo, LocalDate fechaVencimiento, String observacion);

    List<Merma> listarTodas();

    List<Merma> listarPorProducto(Long productoId);

    List<Merma> listarPorFiltros(MotivoMerma motivo, LocalDateTime inicio, LocalDateTime fin);
}
