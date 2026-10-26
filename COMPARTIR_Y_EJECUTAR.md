# Backend independiente y conexión a Supabase

Esta carpeta es la raíz del repositorio backend. El frontend se publica en otro repositorio y se conecta mediante la API HTTP; no necesita estar en una carpeta hermana.

## Requisitos

- JDK 21; comprobar `java -version` y `JAVA_HOME` en el equipo. Los scripts no dependen de una ruta de Windows del autor.
- Internet para descargar Maven/dependencias la primera vez. Se incluye Maven Wrapper.
- PostgreSQL local preparado, o acceso al proyecto compartido de Supabase con esquema actualizado.
- Para el panel: sesión gráfica de escritorio. En Linux se requieren las bibliotecas de escritorio de JavaFX (por ejemplo GTK 3 en Ubuntu/Debian). Ejecutar desde un escritorio normal; una consola sin servidor gráfico no muestra JavaFX.

JavaFX resuelve sus bibliotecas nativas según el sistema mediante Maven. Al cambiar de sistema, recargar el proyecto Maven y compilar desde sus fuentes; no copiar la carpeta `target` generada en otro equipo.

## Configuración de base de datos

Elegir uno de los ejemplos y copiarlo a **`conexion-db.properties` en esta carpeta**:

| Entorno | Plantilla |
|---|---|
| PostgreSQL local | `conexion-local.properties.example` |
| Supabase | `conexion-supabase.properties.example` |

Completar el archivo privado y ejecutar los procesos con esta carpeta como directorio de trabajo. Spring Boot carga este archivo tanto desde los scripts como desde IntelliJ. Sin archivo ni variables, mantiene PostgreSQL local como valor por defecto.

En Supabase, ir a **Connect → Session pooler** y copiar host, puerto, usuario y contraseña de PostgreSQL. La URL JDBC tiene esta forma:

```properties
DB_URL=jdbc:postgresql://HOST:5432/postgres?sslmode=require
DB_USERNAME=postgres.REFERENCIA_DEL_PROYECTO
DB_PASSWORD=CONTRASENA_PRIVADA
```

No es la clave publishable/anon ni la clave secret/service_role de la API Supabase. No poner comillas alrededor de los valores. En archivos Java `.properties` las barras invertidas deben escribirse como `\\`; las contraseñas también pueden definirse como variables de entorno para evitar escapes.

Las variables de entorno `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` tienen prioridad sobre el archivo. Si parecen ignorarse los cambios de archivo, revisar las variables de la terminal o de la configuración de IntelliJ. `.env.example` es una referencia; un archivo `.env` no se carga automáticamente.

Se limita el pool a tres conexiones por proceso en el ejemplo Supabase. La API y cada panel abren su propio pool; considerar la cantidad de integrantes y los límites de Supabase. Para un despliegue, configurar un `JWT_SECRET` Base64 persistente de al menos 32 bytes aleatorios y `ALLOWED_ORIGINS` con los orígenes exactos del frontend. Mantener los pagos en DEMO mientras se usan datos de ejemplo.

## Arranque

| Proceso | Windows | Linux |
|---|---|---|
| API, puerto 8080 | `iniciar-api.cmd` | `bash iniciar-api.sh` |
| Panel JavaFX | `iniciar-panel.cmd` | `bash iniciar-panel.sh` |

Se pueden ejecutar ambos simultáneamente. El panel arranca Spring en modo sin servidor web y accede directamente a PostgreSQL; necesita la misma configuración privada que la API. Su cierre no detiene la API.

En IntelliJ: abrir `pom.xml`, elegir SDK 21 y recargar Maven. Ejecutar `pe.edu.utp.Grupo06.Grupo06Application` para la API y `pe.edu.utp.Grupo06.AppLauncher` para el panel. Directorio de trabajo: raíz de este repositorio backend. No es obligatorio usar los scripts.

Si en Linux aparece un error de permisos del wrapper, los scripts ya invocan `bash ./mvnw`; para ejecución directa usar `chmod +x mvnw`. Si hay errores relacionados con `\r`, conservar finales de línea LF conforme a `.gitattributes`. La corrección de dependencias se validó en Windows; aún se debe comprobar la ventana en el equipo Linux del compañero.

## Base nueva y pruebas

Consultar [database/README.md](database/README.md) para crear una base limpia con el catálogo ficticio. No incluye las cuentas privadas del equipo. `ddl-auto=validate` no crea tablas ni importa datos.

Con `sgcivorp_test` preparada:

```bash
# Linux
bash mvnw test
# Windows
mvnw.cmd test
```

`TEST_DB_URL`, `TEST_DB_USERNAME` y `TEST_DB_PASSWORD` permiten escoger una base aislada diferente. No ejecutar las pruebas sobre la base compartida.

## Publicación en GitHub

Crear el repositorio backend vacío en GitHub y ejecutar desde ESTA carpeta:

```bash
git init
git add .
git status
git commit -m "Preparar backend del integrador"
git branch -M main
git remote add origin URL_DEL_REPOSITORIO_BACKEND
git push -u origin main
```

Omitir la inicialización y el agregado del remoto si ya existen. Comprobar con `git status` que no se incluyan `conexion-db.properties`, `.local/`, `.env`, respaldos, claves, `.idea/` ni `target/`. `.gitignore` no elimina archivos ya versionados; si alguna credencial fue publicada anteriormente, retirarla del seguimiento y cambiarla en el servicio correspondiente.

Compartir las credenciales de la base sólo con los colaboradores autorizados, fuera de GitHub. El frontend nunca necesita estas credenciales.

Referencia: [dependencias JavaFX por plataforma](https://openjfx.io/openjfx-docs/#maven).
