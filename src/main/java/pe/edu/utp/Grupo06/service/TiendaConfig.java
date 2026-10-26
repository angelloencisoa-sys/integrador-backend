package pe.edu.utp.Grupo06.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class TiendaConfig {
 public final boolean demo;
 public final String titular,telefono,direccion,horario,mapaUrl;
 public TiendaConfig(@Value("${app.tienda.demo:true}") boolean demo,
 @Value("${app.tienda.titular:Comercial Nova - DEMOSTRACION}") String titular,
 @Value("${app.tienda.yape:DEMO - no transferir}") String telefono,
 @Value("${app.tienda.direccion:Asociación de Vivienda La Planicie de la Era / Zona de La Era de Pando, Ate, Lima 15487. Referencia: Pista nueva-Santa Clara, al costado de Ceviche69.}") String direccion,
 @Value("${app.tienda.horario:Lunes a sabado, de 8:00 a. m. a 8:00 p. m.}") String horario,
 @Value("${app.tienda.mapa-url:https://maps.app.goo.gl/fYfcULhRuRgEEYhZ7}") String mapaUrl) {
  this.demo=demo;this.titular=titular;this.telefono=telefono;this.direccion=direccion;this.horario=horario;this.mapaUrl=mapaUrl;
  if(!demo && (!telefono.matches("9[0-9]{8}") || titular.contains("DEMOSTRACION") || direccion.contains("referencial") || horario.contains("referencial")))
   throw new IllegalStateException("Configure los datos reales de la tienda antes de desactivar el modo demo");
 }
 public record Datos(boolean demo,String titular,String telefonoYape,String direccion,String horario,String mapaUrl) {}
 public Datos datos(){return new Datos(demo,titular,telefono,direccion,horario,mapaUrl);}
}
