package pe.edu.utp.Grupo06.service;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.*;
@Component @EnableScheduling @ConditionalOnProperty(name="app.pedidos.expirar",havingValue="true",matchIfMissing=true)
public class PedidoExpiracion {
 private final PedidoService pedidos;
 public PedidoExpiracion(PedidoService pedidos){this.pedidos=pedidos;}
 @Scheduled(fixedDelay=30000) public void revisar(){for(Long id:pedidos.vencidos())try{pedidos.expirar(id);}catch(RuntimeException ex){org.slf4j.LoggerFactory.getLogger(getClass()).warn("No se pudo revisar reserva {}: {}",id,ex.getClass().getSimpleName());}}
}
