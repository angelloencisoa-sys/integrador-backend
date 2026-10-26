package pe.edu.utp.Grupo06.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class TiendaConfig {
 public final boolean demo;
 public final String titular,telefono,direccion,horario;
 public TiendaConfig(@Value("${app.tienda.demo:true}") boolean demo,
 @Value("${app.tienda.titular:Comercial Nova - DEMOSTRACION}") String titular,
 @Value("${app.tienda.yape:DEMO - no transferir}") String telefono,
 @Value("${app.tienda.direccion:Av. Los Olivos 123, Lima (direccion referencial)}") String direccion,
 @Value("${app.tienda.horario:Lunes a sabado, 9:00 a 20:00 (horario referencial)}") String horario) {
  this.demo=demo;this.titular=titular;this.telefono=telefono;this.direccion=direccion;this.horario=horario;
  if(!demo && (!telefono.matches("9[0-9]{8}") || titular.contains("DEMOSTRACION") || direccion.contains("referencial") || horario.contains("referencial")))
   throw new IllegalStateException("Configure los datos reales de la tienda antes de desactivar el modo demo");
 }
 public record Datos(boolean demo,String titular,String telefonoYape,String direccion,String horario) {}
 public Datos datos(){return new Datos(demo,titular,telefono,direccion,horario);}
}
