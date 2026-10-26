package pe.edu.utp.Grupo06.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.utp.Grupo06.model.Rol;
import pe.edu.utp.Grupo06.model.enums.RolNombre;
import pe.edu.utp.Grupo06.repository.RolRepository;

@Configuration
public class DataInitializerConfig {

    @Bean
    CommandLineRunner inicializarRoles(RolRepository rolRepository) {
        return args -> {
            for (RolNombre rolNombre : RolNombre.values()) {
                if (rolRepository.findByNombre(rolNombre).isEmpty()) {
                    Rol rol = new Rol();
                    rol.setNombre(rolNombre);
                    rolRepository.save(rol);
                    System.out.println(" Rol inicializado en BD: " + rolNombre);
                }
            }
        };
    }

}
