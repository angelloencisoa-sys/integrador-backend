package pe.edu.utp.Grupo06;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import pe.edu.utp.Grupo06.controller.AuthController;
import pe.edu.utp.Grupo06.service.IUsuarioService;
import static org.junit.jupiter.api.Assertions.*;

/** Infraestructura JavaFX sin abrir ventanas en las pruebas. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DesktopContextTest {
    @Autowired ApplicationContext context;
    @Test void escritorioConservaServiciosSinCrearServidorNiFiltrosHttp() {
        assertNotNull(context.getBean(IUsuarioService.class));
        assertTrue(context.getBeansOfType(SecurityFilterChain.class).isEmpty());
        assertTrue(context.getBeansOfType(AuthController.class).isEmpty());
        assertFalse(context.containsBean("servletWebServerFactory"));
    }
}
