# SGCIVORP Backend y panel JavaFX

**Este es el repositorio backend independiente.** Inicio en Windows/Linux, IntelliJ y conexión privada a Supabase: [COMPARTIR_Y_EJECUTAR.md](COMPARTIR_Y_EJECUTAR.md). Preparación de una base nueva y migración revisada: [database/README.md](database/README.md).

El proyecto utiliza Java 21, Spring Boot y PostgreSQL. La API y el panel
JavaFX tienen entradas independientes y pueden funcionar al mismo tiempo.

## Inicio

- API HTTP sin ventana: ejecutar `iniciar-api.cmd`, o `mvnw.cmd spring-boot:run`.
- Panel JavaFX: ejecutar `iniciar-panel.cmd`, o `mvnw.cmd -Pdesktop spring-boot:run`.

La API utiliza el puerto 8080. El panel inicializa los servicios Spring en
modo sin servidor web, por lo que no ocupa ese puerto ni cierra la API al
salir. En el IDE, usar `Grupo06Application` para la API y `AppLauncher` para
JavaFX. Reiniciar cualquier instancia anterior para cargar los cambios.

El escritorio conserva llamadas locales a los servicios y a PostgreSQL;
todavia no se ha convertido en un cliente HTTP remoto de la API.

## Base de datos local

La configuracion por defecto apunta a `sgcivorp_local` en `127.0.0.1:5432`,
con usuario `postgres` y contrasena vacia. La base debe existir y contener el
esquema y los datos restaurados antes de iniciar la aplicacion.

Hibernate usa `ddl-auto=validate`: comprueba la estructura, sin crearla ni
modificarla automaticamente.

Para otro entorno se pueden definir estas variables antes del arranque:

```powershell
$env:DB_URL='jdbc:postgresql://127.0.0.1:5432/sgcivorp_local'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD=''
```

Los respaldos y las configuraciones privadas se guardan en `.local/`, que
esta excluida de Git. No subir respaldos, contrasenas ni archivos `.env`.

## Copia inicial del 25 de octubre de 2026

Se restauro el esquema `public` del Integrador desde Supabase en la base
local. Se conservaron sus 14 tablas, datos, relaciones, restricciones y
secuencias. Los esquemas internos de los servicios de Supabase no son
necesarios para esta aplicacion y no se trasladaron.

Se compararon las huellas de todas las filas y los contadores de secuencias
entre origen y destino. La compilacion y las tres pruebas existentes en
esta copia del proyecto pasaron contra una base local temporal aislada.

En pgAdmin, actualizar la lista de bases del servidor local y abrir
`sgcivorp_local` > `Schemas` > `public` > `Tables`.

## Autenticacion y permisos de la API

Aplicar `src/main/resources/db/migration/2026_10_25_rol_cliente.sql` en cada
base antes del primer arranque con este codigo. Es idempotente. La migracion
agrega CLIENTE a los roles admitidos y no altera cuentas ni contrasenas.

| Ruta o modulo | Acceso |
|---|---|
| POST /api/auth/registro | Publico; siempre crea CLIENTE activo |
| POST /api/auth/login | Publico; devuelve accessToken, tokenType y expiresIn |
| GET /api/auth/me | Cuenta autenticada; devuelve solo su perfil |
| GET /api/catalogo/productos y /api/catalogo/productos/{id} | Publico; solo datos comerciales de productos y categorias activos |
| GET /api/catalogo/categorias | Publico; categorias activas |
| Usuarios, compras, proveedores, reportes | ADMINISTRADOR |
| Consulta interna de productos y categorias | ADMINISTRADOR o VENDEDOR |
| Modificar productos, presentaciones y categorias | ADMINISTRADOR |
| Ventas POS, alertas, mermas y movimientos | ADMINISTRADOR o VENDEDOR |
| Otras rutas | Denegadas |

Registro: enviar JSON con `username`, `password` (8 caracteres como minimo,
maximo 72 bytes UTF-8), `nombreCompleto`, `email` y `telefono`. El servidor
ignora intentos de asignar `rol`, `id` o `activo` desde el registro publico.
El login recibe `username` y `password`. Para usar la API, enviar
`Authorization: Bearer <accessToken>` en cada solicitud.

Los JWT duran una hora. Su firma y vencimiento se comprueban y el estado y
rol de la cuenta se consultan en cada peticion. Desactivar o cambiar el rol
tiene efecto en el siguiente acceso. No hay renovacion automatica ni logout
con revocacion individual: el frontend debe descartar el token al salir.

`JWT_SECRET` admite una clave Base64 de al menos 32 bytes. Sin esta variable,
el servidor genera una clave aleatoria temporal: reiniciarlo invalida los
tokens anteriores. Al desplegar, configurar una clave persistente privada,
HTTPS y `ALLOWED_ORIGINS` con el dominio real. Las cookies y HTTP Basic no
autentican la API. Los origenes locales permitidos son localhost:4200 y
127.0.0.1:4200.

En ventas, compras, mermas y movimientos por HTTP, el responsable se obtiene
del token. El campo `usuarioId` anterior es opcional y no cambia esa identidad.
Las cuentas CLIENTE no pueden entrar al panel JavaFX.

El catalogo publico ya esta conectado al frontend HTML, CSS y JavaScript nativo. `/api/productos`
sigue siendo un endpoint interno protegido. El carrito se conserva en el
navegador. El checkout autenticado crea pedidos, reserva existencias y recibe
comprobantes para revision manual. Consulte GUIA_PEDIDOS.md para probar el flujo.

## Pruebas

Las pruebas apuntan por defecto a `sgcivorp_test`, nunca a la base de trabajo.
Crear esa base con una copia del esquema y datos, aplicar la migracion de
CLIENTE y pedidos web y ejecutar `mvnw.cmd test` usando Java 21. Para otro destino de pruebas
usar `TEST_DB_URL`, `TEST_DB_USERNAME` y `TEST_DB_PASSWORD`.

Las pruebas HTTP usan un puerto aleatorio y cubren autenticacion, permisos,
registro, tokens alterados o vencidos, cuentas desactivadas, cambios de rol,
perfil propio, CORS y auditoria. El contexto de escritorio se valida sin
abrir una ventana. Las pruebas de mermas existentes siguen incluidas.

Referencia tecnica: [JWT en Spring Security](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html).

## Pedidos web (25/10/2026)

Flujo completo documentado en [GUIA_PEDIDOS.md](GUIA_PEDIDOS.md). La tienda usa
`configuracion-tienda.properties`, compartida por API y JavaFX; por defecto
esta en DEMO. Los pedidos conservan una copia de precios, modo y datos de tienda.

- GET `/api/tienda`: datos publicos del comercio, sin credenciales.
- `/api/pedidos/**`: exclusivamente CLIENTE; solo sus propios pedidos,
  comprobantes y QR. POST crea con clave idempotente, productos, cantidades
  y precio esperado. El servidor calcula el total y valida el stock.
- `/api/personal/pedidos/**`: ADMINISTRADOR o VENDEDOR. Aprobacion, rechazo,
  preparacion, listo y entrega; reembolso confirmado solo ADMINISTRADOR.
- El panel JavaFX llama al mismo servicio transaccional y revalida el rol.
- Reservas impagas: 10 minutos, liberacion automatica cada 30 segundos.
  Una captura enviada conserva la reserva hasta revision manual; no expira.
- PNG/JPEG real hasta 3 MB, dimensiones acotadas, hash para evitar reutilizacion,
  almacenamiento privado en PostgreSQL. La imagen no demuestra el abono.
- Aprobacion atomica: precio reservado, una venta, una salida de inventario.
  La entrega exige estado listo y codigo de un solo uso (manual o lector USB).
- DEMO crea tickets D001 y mueve el inventario local para demostrar el flujo;
  se excluye de ingresos, metodos de pago y rotacion de ventas reales.
- Yape es una verificacion humana de transferencia externa, sin pasarela,
  webhook ni consulta automatica a Yape. Reembolsos igualmente manuales.

Migracion: `src/main/resources/db/migration/2026_10_25_pedidos_web.sql`.
Aplicada primero en `sgcivorp_test` y luego en `sgcivorp_local`, con respaldo
previo en `.local/backups/`. Conserva productos, usuarios, ventas y stock.
Para una instalacion nueva/restaurada aplicar las migraciones con `psql -v ON_ERROR_STOP=1`;
Hibernate valida el esquema y no lo crea automaticamente.

Validacion actualizada: 46 pruebas backend (incluyen 15 de pedidos, concurrencia real,
autorizacion HTTP, expiracion, imagen falsa, idempotencia, precio reservado,
entrega repetida, reembolso y exclusion de ingresos), carga FXML sin abrir
ventana y prueba integral Chrome contra la base aislada a 1440, 390 y 320 px.
Generacion de QR: [ZXing](https://github.com/zxing/zxing), version 3.5.4.

## Documentacion del avance 3

Guia reproducible, decisiones de arquitectura y evidencias:
`../docs/avance3-final/GUIA_TECNICA.md`.
Guava obtiene huellas SHA-256 compatibles con los comprobantes existentes;
Apache Commons normaliza referencias y motivos. Logback registra identificador
y estado del pedido, sin incluir tokens, imagenes ni referencias de pago.
Las nueve pruebas nuevas comprueban esas utilidades, la exportacion POI y
el motor de logging. La documentacion navegable esta en
`../docs/avance3-final/javadoc/index.html`.
