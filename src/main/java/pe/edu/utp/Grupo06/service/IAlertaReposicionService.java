package pe.edu.utp.Grupo06.service;

import pe.edu.utp.Grupo06.model.AlertaReposicion;
import pe.edu.utp.Grupo06.model.enums.EstadoAlerta;
import java.util.List;

public interface IAlertaReposicionService {
    void verificarYGenerarAlerta(Long productoId);
    List<AlertaReposicion> listarPendientes();
    List<AlertaReposicion> listarPorEstado(EstadoAlerta estado);
    List<AlertaReposicion> listarTodas();
    AlertaReposicion atenderAlerta(Long alertaId, String observacion);
    AlertaReposicion descartarAlerta(Long alertaId);
}