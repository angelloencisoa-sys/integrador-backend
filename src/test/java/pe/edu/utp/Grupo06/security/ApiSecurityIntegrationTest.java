package pe.edu.utp.Grupo06.security;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import pe.edu.utp.Grupo06.config.ApiSecurityConfig;
import pe.edu.utp.Grupo06.model.Usuario;
import pe.edu.utp.Grupo06.model.enums.RolNombre;
import pe.edu.utp.Grupo06.repository.*;
import pe.edu.utp.Grupo06.service.*;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Pruebas HTTP reales: filtros, JWT firmado, roles y persistencia PostgreSQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiSecurityIntegrationTest {
    @Autowired Environment env;
    @Autowired UsuarioRepository usuarios;
    @Autowired RolRepository roles;
    @Autowired IUsuarioService servicio;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtEncoder encoder;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    @Autowired ProductoRepository productos;
    @Autowired CategoriaRepository categorias;
    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonMapper json = JsonMapper.builder().build();
    private final List<String> creados = new ArrayList<>();
    private static final String PASSWORD = "PruebaSegura2026!";

    @AfterEach
    void limpiar() {
        creados.forEach(nombre -> usuarios.findByUsername(nombre).ifPresent(usuarios::delete));
    }

    private String nombre() {
        String nombre = "test_" + UUID.randomUUID().toString().replace("-", "");
        creados.add(nombre);
        return nombre;
    }

    private Usuario cuenta(RolNombre rol) {
        Usuario u = new Usuario();
        u.setUsername(nombre()); u.setPassword(PASSWORD); u.setNombreCompleto("Prueba de seguridad");
        u.setEmail(u.getUsername()+"@example.com"); u.setTelefono("999123456");
        u.setRol(roles.findByNombre(rol).orElseThrow());
        return servicio.registrar(u);
    }

    private HttpResponse<String> request(String method, String path, String body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + env.getProperty("local.server.port") + path))
                .header("Content-Type", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return http.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private String token(Usuario u) throws Exception {
        var response = request("POST", "/api/auth/login",
                json.writeValueAsString(Map.of("username",u.getUsername(),"password",PASSWORD)), null);
        assertEquals(200, response.statusCode(), response.body());
        return json.readTree(response.body()).path("accessToken").asText();
    }

    @Test void endpointsInternosExigenAutenticacion() throws Exception {
        for (String path : List.of("/api/usuarios", "/api/productos", "/api/ventas", "/api/reportes/inventario"))
            assertEquals(401, request("GET", path, null, null).statusCode(), path);
    }

    @Test void catalogoPublicoNoExponeInformacionInterna() throws Exception {
        var response=request("GET","/api/catalogo/productos",null,null);
        assertEquals(200,response.statusCode(),response.body());
        var lista=json.readTree(response.body());
        assertTrue(lista.isArray());
        assertTrue(lista.size()>0);
        assertFalse(response.body().contains("precioCompra"));
        assertFalse(response.body().contains("proveedor"));
        assertFalse(response.body().contains("stockMinimo"));
        assertEquals(200,request("GET","/api/catalogo/categorias",null,null).statusCode());
        long id=lista.path(0).path("id").asLong();
        var detail=request("GET","/api/catalogo/productos/"+id,null,null);
        assertEquals(200,detail.statusCode());
        assertEquals(id,json.readTree(detail.body()).path("id").asLong());
        assertEquals(404,request("GET","/api/catalogo/productos/9223372036854775807",null,null).statusCode());
        assertEquals(401,request("POST","/api/catalogo/productos","{}",null).statusCode());
    }

    @Test void productoInactivoNoSePublica() throws Exception {
        var p=new pe.edu.utp.Grupo06.model.Producto();
        p.setCodigo(nombre()); p.setNombre("Producto inactivo de prueba");
        p.setPrecioCompra(new java.math.BigDecimal("1.00")); p.setPrecioVenta(new java.math.BigDecimal("2.00"));
        p.setStockActual(10); p.setStockMinimo(0); p.setEstado(false);
        p.setCategoria(categorias.findAll().get(0));
        p.setUnidadMedida(pe.edu.utp.Grupo06.model.enums.UnidadMedida.UNIDAD);
        Long id=productos.saveAndFlush(p).getId();
        try {
            assertEquals(404,request("GET","/api/catalogo/productos/"+id,null,null).statusCode());
            var lista=json.readTree(request("GET","/api/catalogo/productos",null,null).body());
            for(var producto:lista) assertNotEquals(id.longValue(),producto.path("id").asLong());
        } finally { productos.deleteById(id); }
    }

    @Test void clienteNoAccedeAInformacionInterna() throws Exception {
        String token = token(cuenta(RolNombre.CLIENTE));
        for (String path : List.of("/api/usuarios", "/api/productos", "/api/ventas", "/api/reportes/inventario"))
            assertEquals(403, request("GET", path, null, token).statusCode(), path);
        assertEquals(403, request("POST", "/api/usuarios", "{}", token).statusCode());
    }

    @Test void vendedorAccedeAPosPeroNoAdministracion() throws Exception {
        String token = token(cuenta(RolNombre.VENDEDOR));
        assertEquals(200, request("GET", "/api/productos", null, token).statusCode());
        assertEquals(403, request("GET", "/api/usuarios", null, token).statusCode());
        assertEquals(403, request("GET", "/api/compras", null, token).statusCode());
        assertEquals(403, request("POST", "/api/productos", "{}", token).statusCode());
    }

    @Test void administradorAccedeSinExponerHashes() throws Exception {
        String token = token(cuenta(RolNombre.ADMINISTRADOR));
        var response = request("GET", "/api/usuarios", null, token);
        assertEquals(200, response.statusCode(), response.body());
        assertFalse(response.body().contains("password"));
        assertFalse(response.body().contains("$2a$"));
    }

    @Test void registroIgnoraIntentosDeAsignarRolOId() throws Exception {
        String nombre = nombre();
        String datos = json.writeValueAsString(Map.of("username",nombre,"password",PASSWORD,
                "nombreCompleto","Cliente prueba","email",nombre+"@example.com","telefono","999123456",
                "rol","ADMINISTRADOR","activo",false,"id",1));
        var response = request("POST", "/api/auth/registro", datos, null);
        assertEquals(201, response.statusCode(), response.body());
        Usuario u = usuarios.findByUsername(nombre).orElseThrow();
        assertEquals(RolNombre.CLIENTE, u.getRol().getNombre());
        assertTrue(u.getActivo());
        assertTrue(passwords.matches(PASSWORD, u.getPassword()));
        assertNotEquals(PASSWORD, u.getPassword());
        assertFalse(response.body().contains("password"));
        String token = json.readTree(response.body()).path("accessToken").asText();
        assertEquals(200, request("GET", "/api/auth/me", null, token).statusCode());
        assertEquals(403, request("GET", "/api/usuarios", null, token).statusCode());
    }

    @Test void rechazaDatosDeRegistroInvalidos() throws Exception {
        assertEquals(400, request("POST", "/api/auth/registro", "{}", null).statusCode());
        String nombre=nombre();
        var datos=Map.of("username",nombre,"password","corta","nombreCompleto","Prueba",
                "email","correo-invalido","telefono","999123456");
        assertEquals(400,request("POST","/api/auth/registro",json.writeValueAsString(datos),null).statusCode());
        assertTrue(usuarios.findByUsername(nombre).isEmpty());
    }

    @Test void noPermiteRegistrarUsernameExistente() throws Exception {
        Usuario u=cuenta(RolNombre.VENDEDOR);
        var datos=Map.of("username",u.getUsername(),"password",PASSWORD,"nombreCompleto","Otro usuario",
                "email","otro@example.com","telefono","999123456");
        var response=request("POST","/api/auth/registro",json.writeValueAsString(datos),null);
        assertEquals(400,response.statusCode());
        assertEquals(RolNombre.VENDEDOR,usuarios.findById(u.getId()).orElseThrow().getRol().getNombre());
    }

    @Test void perfilSiempreCorrespondeAlToken() throws Exception {
        Usuario cliente=cuenta(RolNombre.CLIENTE);
        Usuario otro=cuenta(RolNombre.ADMINISTRADOR);
        var response=request("GET","/api/auth/me?id="+otro.getId(),null,token(cliente));
        assertEquals(200,response.statusCode());
        assertEquals(cliente.getId().longValue(),json.readTree(response.body()).path("id").asLong());
    }

    @Test void auditoriaUsaUsuarioAutenticadoAunqueEnvienOtroId() throws Exception {
        Usuario vendedor=cuenta(RolNombre.VENDEDOR);
        Usuario otro=cuenta(RolNombre.ADMINISTRADOR);
        var p=new pe.edu.utp.Grupo06.model.Producto();
        p.setCodigo(nombre()); p.setNombre("Producto prueba auditoria");
        p.setPrecioCompra(new java.math.BigDecimal("1.00")); p.setPrecioVenta(new java.math.BigDecimal("2.00"));
        p.setStockActual(10); p.setStockMinimo(0); p.setCategoria(categorias.findAll().get(0));
        p.setUnidadMedida(pe.edu.utp.Grupo06.model.enums.UnidadMedida.UNIDAD);
        Long productoId=productos.saveAndFlush(p).getId();
        try {
            var datos=Map.of("productoId",productoId,"usuarioId",otro.getId(),"cantidad",1,
                    "motivo",pe.edu.utp.Grupo06.model.enums.MotivoMerma.ROTURA_DANO.toString(),
                    "observacion","Prueba de identidad");
            var response=request("POST","/api/mermas",json.writeValueAsString(datos),token(vendedor));
            assertEquals(201,response.statusCode(),response.body());
            assertEquals(vendedor.getId().longValue(),json.readTree(response.body()).path("usuarioId").asLong());
            assertEquals(vendedor.getId(),jdbc.queryForObject(
                    "SELECT usuario_id FROM movimientos_inventario WHERE producto_id=?",Long.class,productoId));
        } finally {
            jdbc.update("DELETE FROM movimientos_inventario WHERE producto_id=?",productoId);
            jdbc.update("DELETE FROM mermas WHERE producto_id=?",productoId);
            jdbc.update("DELETE FROM alertas_reposicion WHERE producto_id=?",productoId);
            productos.deleteById(productoId);
        }
    }

    @Test void loginIncorrectoDevuelve401() throws Exception {
        Usuario u = cuenta(RolNombre.CLIENTE);
        assertEquals(401,request("POST","/api/auth/login",json.writeValueAsString(
                Map.of("username",u.getUsername(),"password","incorrecta")),null).statusCode());
        assertEquals(401,request("POST","/api/auth/login",json.writeValueAsString(
                Map.of("username",nombre(),"password",PASSWORD)),null).statusCode());
    }

    @Test void rechazaTokensManipuladosYVencidos() throws Exception {
        Usuario u=cuenta(RolNombre.CLIENTE);
        String valid=token(u);
        String[] partes=valid.split("\\.");
        char primero=partes[2].charAt(0);
        String falso=partes[0]+"."+partes[1]+"."+(primero=='A'?'B':'A')+partes[2].substring(1);
        assertEquals(401,request("GET","/api/auth/me",null,falso).statusCode());
        Instant antes=Instant.now().minusSeconds(7200);
        var claims=JwtClaimsSet.builder().issuer(ApiSecurityConfig.ISSUER).subject(u.getId().toString())
                .issuedAt(antes).expiresAt(antes.plusSeconds(60)).build();
        String vencido=encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        assertEquals(401,request("GET","/api/auth/me",null,vencido).statusCode());
    }

    @Test void desactivarCuentaInvalidaTokenExistente() throws Exception {
        Usuario u=cuenta(RolNombre.CLIENTE); String token=token(u);
        servicio.cambiarEstado(u.getId(),false);
        assertEquals(401,request("GET","/api/auth/me",null,token).statusCode());
        assertEquals(401,request("POST","/api/auth/login",json.writeValueAsString(
                Map.of("username",u.getUsername(),"password",PASSWORD)),null).statusCode());
    }

    @Test void cambiarRolActualizaPermisosDelToken() throws Exception {
        Usuario u=cuenta(RolNombre.ADMINISTRADOR); String token=token(u);
        u.setRol(roles.findByNombre(RolNombre.CLIENTE).orElseThrow()); usuarios.saveAndFlush(u);
        assertEquals(403,request("GET","/api/usuarios",null,token).statusCode());
    }

    @Test void clienteNoPuedeIniciarSesionEnEscritorio() {
        Usuario u=cuenta(RolNombre.CLIENTE);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> servicio.validarCredencialesPersonal(u.getUsername(),PASSWORD));
        Usuario vendedor=cuenta(RolNombre.VENDEDOR);
        assertEquals(vendedor.getId(),servicio.validarCredencialesPersonal(vendedor.getUsername(),PASSWORD).getId());
    }

    @Test void corsPermiteSoloFrontendConfigurado() throws Exception {
        String base="http://127.0.0.1:"+env.getProperty("local.server.port")+"/api/auth/login";
        for (String origin : List.of("http://localhost:4200","https://origen-no-permitido.example")) {
            var request=HttpRequest.newBuilder(URI.create(base)).header("Origin",origin)
                    .header("Access-Control-Request-Method","POST")
                    .header("Access-Control-Request-Headers","content-type").method("OPTIONS",HttpRequest.BodyPublishers.noBody()).build();
            var response=http.send(request,HttpResponse.BodyHandlers.ofString());
            if(origin.contains("localhost")) {
                assertEquals(200,response.statusCode());
                assertEquals(origin,response.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
            } else assertEquals(403,response.statusCode());
        }
    }
}
