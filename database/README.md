# Preparación de PostgreSQL y Supabase

El proyecto valida el esquema al arrancar; no crea una base automáticamente. Los SQL se ejecutan manualmente con `psql -v ON_ERROR_STOP=1`. No hay Flyway configurado.

## Una base nueva para desarrollar

Requisitos: PostgreSQL con sus herramientas `createdb`, `psql`, `pg_dump` y `pg_restore` en PATH. Utilizar una versión compatible con el servidor de destino. Los ejemplos usan el usuario `postgres`; cada equipo debe proporcionar su propia contraseña mediante el prompt o un archivo `.pgpass` privado.

Desde la raíz del repositorio backend:

```bash
createdb -h 127.0.0.1 -U postgres sgcivorp_local
psql -h 127.0.0.1 -U postgres -d sgcivorp_local -v ON_ERROR_STOP=1 -f database/01-esquema-inicial.sql
psql -h 127.0.0.1 -U postgres -d sgcivorp_local -v ON_ERROR_STOP=1 -f database/02-catalogo-demostracion.sql
```

El primer SQL exige que `public` no tenga tablas. El segundo exige que no existan usuarios ni productos. Ambos usan una transacción. No se deben ejecutar sobre una base compartida existente.

El catálogo incluye 51 productos activos, categorías, roles y un proveedor ficticio con RUC de demostración. Cada producto tiene **10 unidades/envases ficticios y costo 0 pendiente de cotización**. Una unidad de los artículos sixpack representa el paquete completo. No incluye usuarios, contraseñas, correos, teléfonos, ventas, compras ni comprobantes de la base de trabajo.

### Primer administrador en una base nueva

1. Iniciar API y frontend; registrar una cuenta propia en la web. Se crea como CLIENTE.
2. Sólo en la base de desarrollo propia, el responsable de la base puede asignarle el rol ADMINISTRADOR. En pgAdmin, sustituir el nombre de ejemplo por el registrado y ejecutar:

```sql
UPDATE usuarios
SET rol_id=(SELECT id FROM roles WHERE nombre='ADMINISTRADOR')
WHERE username='TU_USUARIO_REGISTRADO';
```

3. Iniciar sesión en JavaFX con esa cuenta. No se entrega una contraseña administrativa pública predeterminada.

Para las pruebas de integración, crear **otra base**, `sgcivorp_test`, usando los mismos dos SQL. Nunca configurar las pruebas contra Supabase compartido. Las pruebas insertan y modifican datos de prueba.

## Supabase existente

La actualización revisada es `03-actualizar-supabase-20261026.sql`. Está diseñada para el catálogo y esquema comparados el 26 de octubre de 2026. No es un script genérico de restauración: si el catálogo cambió, aborta para evitar sobrescribir modificaciones no revisadas.

Incluye el rol CLIENTE, cuatro tablas de pedidos web, reservas de stock, soporte DEMO y las correcciones del catálogo. Conserva usuarios, ventas, compras y cantidades del destino. No copia las operaciones de prueba locales. El producto de sardinas se desactiva y se conserva; el atún Campomar se incorpora como artículo nuevo con stock 0 y costo pendiente. Las tablas nuevas de pedidos tienen RLS habilitado sin políticas públicas: el acceso del proyecto es mediante Spring Boot o JavaFX con PostgreSQL autenticado.

Antes de aplicarlo en otro destino:

1. Comparar estructura y catálogo del destino; coordinar que no se cambien los artículos afectados durante la actualización.
2. Obtener un respaldo completo de `public` con `pg_dump --schema=public --format=custom` y guardarlo fuera de Git.
3. Restaurar ese respaldo en una base aislada y probar el SQL y el arranque de Spring Boot.
4. Aplicar el SQL transaccional al destino correcto y verificar sus datos.

Usar conexión directa o Session pooler de Supabase en puerto 5432 con TLS. No utilizar el Transaction pooler de puerto 6543 para estos comandos. No restaurar `01-esquema-inicial.sql` ni el catálogo ficticio sobre Supabase existente.

Fuentes oficiales: [conexiones PostgreSQL](https://supabase.com/docs/guides/database/connecting-to-postgres), [migración a Supabase](https://supabase.com/docs/guides/platform/migrating-to-supabase/postgres).

Los SQL antiguos de `src/main/resources/db/migration/` se conservan como historial de cambios. No ejecutarlos todos en orden ni automáticamente sobre un catálogo que ya fue corregido.
