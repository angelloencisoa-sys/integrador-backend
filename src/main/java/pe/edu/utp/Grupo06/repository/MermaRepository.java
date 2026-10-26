package pe.edu.utp.Grupo06.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.utp.Grupo06.model.Merma;
import pe.edu.utp.Grupo06.model.enums.MotivoMerma;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MermaRepository extends JpaRepository<Merma, Long> {

    @Query("SELECT m FROM Merma m JOIN FETCH m.producto p JOIN FETCH m.usuario u ORDER BY m.fechaMerma DESC")
    List<Merma> findAllConRelaciones();

    List<Merma> findByProductoIdOrderByFechaMermaDesc(Long productoId);

    List<Merma> findByFechaMermaBetweenOrderByFechaMermaDesc(LocalDateTime start, LocalDateTime end);

    List<Merma> findByMotivoOrderByFechaMermaDesc(MotivoMerma motivo);

    @Query("SELECT m FROM Merma m JOIN FETCH m.producto p JOIN FETCH m.usuario u WHERE " +
           "(:motivo IS NULL OR m.motivo = :motivo) AND " +
           "(:inicio IS NULL OR m.fechaMerma >= :inicio) AND " +
           "(:fin IS NULL OR m.fechaMerma <= :fin) " +
           "ORDER BY m.fechaMerma DESC")
    List<Merma> buscarPorFiltros(@Param("motivo") MotivoMerma motivo,
                                 @Param("inicio") LocalDateTime inicio,
                                 @Param("fin") LocalDateTime fin);
}
