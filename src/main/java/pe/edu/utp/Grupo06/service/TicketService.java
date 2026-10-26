package pe.edu.utp.Grupo06.service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
@Service
public class TicketService {
 private final JdbcTemplate jdbc;
 public TicketService(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public String siguiente(String serie){return serie+"-"+String.format("%08d",jdbc.queryForObject("select nextval('ticket_venta_seq')",Long.class));}
}
