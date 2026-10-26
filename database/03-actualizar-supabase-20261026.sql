-- Actualización revisada de Supabase: 26-10-2026. Respaldar antes de ejecutar.
-- Conserva usuarios, ventas, compras, mermas, cantidades y datos del compañero.
BEGIN;
SET LOCAL lock_timeout='5s';
LOCK TABLE productos IN SHARE ROW EXCLUSIVE MODE;
CREATE TEMP TABLE catalogo_actualizacion(codigo text PRIMARY KEY,nombre_anterior text,marca_anterior text,precio_anterior numeric,estado_anterior boolean,nombre_nuevo text,marca_nueva text,precio_nuevo numeric,estado_nuevo boolean) ON COMMIT DROP;
INSERT INTO catalogo_actualizacion VALUES ('PROD-0001','Leche Gloria Azul 400g','Gloria',4.2,true,'Leche Evaporada Entera Gloria Lata 400 g','Gloria',4.2,true),
('PROD-0002','Leche Gloria Roja 400g','Gloria',4.5,true,'Leche Evaporada Light Gloria Lata 400 g','Gloria',4.5,true),
('PROD-0003','Chorizo - Braedt','Braedt',2.0,true,'Chorizo Braedt Parrillero Precocido Paquete 500 g','Braedt',15.1,true),
('PROD-0004','Chorizo - San Fernando','San Fernando',2.0,true,'Chorizo San Fernando Unidad 40 g','San Fernando',2.5,true),
('PROD-0006','Beso de Mosa','Nestlé',2.0,true,'Beso de Moza','Nestlé',2.0,true),
('PROD-0009','Gretel 38g','Costa',1.5,true,'Galletas Gretel 32 g','Costa',1.5,true),
('PROD-0010','Papitas Lays 40g','Lays',1.5,true,'Papas Lays Clásicas 36 g','Lays',2.5,true),
('PROD-0011','Durazno 480g','Dos Caballos',12.9,true,'Duraznos Dos Caballos Lata 820 g','Dos Caballos',12.9,true),
('PROD-0012','Arroz Extra 1 kg','Costeño',5.5,true,'Arroz Extra 750 g','Costeño',4.5,true),
('PROD-0013','Azúcar Rubia 1 kg','Cartavio',4.5,true,'Azúcar Rubia 1 kg','Cartavio',4.8,true),
('PROD-0014','Fideos Spaghetti 500 g','Don Vittorio',3.3,true,'Fideos Spaghetti 500 g','Don Vittorio',3.8,true),
('PROD-0015','Fideos Tallarín 500 g','Nicolini',3.1,true,'Fideos Tallarín 500 g','Nicolini',3.3,true),
('PROD-0016','Aceite Vegetal 1 L','Primor',11.5,true,'Aceite Vegetal Clásico 900 ml','Primor',10.0,true),
('PROD-0017','Aceite Vegetal 900 ml','Cocinero',9.9,true,'Aceite Vegetal 900 ml','Cocinero',8.9,true),
('PROD-0018','Sal Yodada 1 kg','Emsal',2.3,true,'Sal Yodada 1 kg','Emsal',2.6,true),
('PROD-0019','Harina Sin Preparar 1 kg','Blanca Flor',4.2,true,'Harina Sin Preparar 1 kg','Blanca Flor',8.2,true),
('PROD-0020','Avena Tradicional 400 g','Quaker',5.2,true,'Avena Tradicional 500 g','Quaker',8.3,true),
('PROD-0021','Leche Evaporada 400 g','Ideal',4.3,true,'Mezcla Láctea Ideal Cremosita Lata 390 g','Ideal',4.0,true),
('PROD-0022','Yogur Fresa 1 L','Gloria',7.0,true,'Yogur Fresa 946 g','Gloria',6.3,true),
('PROD-0024','Mantequilla 200 g','Laive',9.3,true,'Mantequilla con Sal 180 g','Laive',13.9,true),
('PROD-0025','Jamonada 200 g','San Fernando',6.8,true,'Jamonada de Pollo San Fernando Paquete 200 g','San Fernando',5.5,true),
('PROD-0026','Salchicha Hot Dog 250 g','San Fernando',7.3,true,'Hot Dog de Pollo San Fernando Paquete 250 g','San Fernando',7.3,true),
('PROD-0027','Atún en Aceite 170 g','Florida',7.2,true,'Trozos de Atún Florida en Aceite Vegetal Lata 140 g','Florida',6.6,true),
('PROD-0028','Sardinas en Salsa de Tomate 425 g','Campomar',6.8,true,'Sardinas en Salsa de Tomate 425 g','Campomar',6.8,false),
('PROD-0030','Mayonesa 200 g','Alacena',5.3,true,'Mayonesa Clásica 235 g','Alacena',8.6,true),
('PROD-0031','Agua Sin Gas 625 ml','San Luis',2.0,true,'Agua Sin Gas 750 ml','San Luis',1.8,true),
('PROD-0032','Agua Sin Gas 1 L','Cielo',2.5,true,'Agua Sin Gas 1 L','Cielo',2.4,true),
('PROD-0034','Bebida Rehidratante 500 ml','Sporade',4.0,true,'Bebida Rehidratante 500 ml','Sporade',3.0,true),
('PROD-0036','Galletas Soda 6 unidades','Field',2.5,true,'Galletas Soda Field Sixpack 6 Paquetes','Field',4.0,true),
('PROD-0038','Galletas Chomp 42 g','Costa',2.0,true,'Galletas Chomp Naranja Victoria Sixpack 6 x 42 g','Victoria',6.7,true),
('PROD-0039','Galletas Vainilla 6 unidades','Field',2.3,true,'Galletas Vainilla Field Sixpack 6 Paquetes','Field',5.5,true),
('PROD-0042','Chocolate Sublime 30 g','Nestlé',2.5,true,'Chocolate Sublime Clásico 26 g','Nestlé',2.8,true),
('PROD-0043','Caramelos Frutales Bolsa 100 g','Ambrosoli',3.0,true,'Gomitas Dulces Ambrosoli Bolsa 430 g','Ambrosoli',14.5,true),
('PROD-0044','Chupetines Globo Pop Bolsa 10 unidades','Globo Pop',4.5,true,'Chupetines Globo Pop Paquete 24 Unidades','Globo Pop',8.2,true),
('PROD-0045','Detergente en Polvo 800 g','Ariel',10.5,true,'Detergente en Polvo Ariel con Downy Bolsa 800 g','Ariel',10.9,true),
('PROD-0046','Lejía 1 L','Clorox',5.8,true,'Lejía Clorox Tradicional Botella 1 L','Clorox',4.6,true),
('PROD-0047','Lavavajilla Líquido 750 ml','Sapolio',7.2,true,'Lavavajilla Líquido 750 ml','Sapolio',9.5,true),
('PROD-0048','Papel Higiénico Pack 4 rollos','Elite',8.5,true,'Papel Higiénico Elite Aloe Vera Doble Hoja Paquete 4 Rollos','Elite',8.5,true),
('PROD-0049','Jabón de Tocador 125 g','Protex',3.7,true,'Jabón de Tocador 110 g','Protex',3.6,true),
('PROD-0050','Shampoo Sachet 15 ml','Head & Shoulders',1.2,true,'Shampoo Limpieza Renovadora Sachet 18 ml','Head & Shoulders',0.9,true),
('PROD-0051','Pasta Dental 90 g','Colgate',6.5,true,'Crema Dental Colgate Máxima Protección Anticaries 90 g','Colgate',5.5,true);
DO $$ BEGIN IF EXISTS(SELECT 1 FROM catalogo_actualizacion a LEFT JOIN productos p ON p.codigo=a.codigo WHERE p.id IS NULL OR NOT ((p.nombre,p.marca,p.precio_venta,p.estado)=(a.nombre_anterior,a.marca_anterior,a.precio_anterior,a.estado_anterior) OR (p.nombre,p.marca,p.precio_venta,p.estado)=(a.nombre_nuevo,a.marca_nueva,a.precio_nuevo,a.estado_nuevo))) THEN RAISE EXCEPTION 'Hay cambios de catálogo no revisados en el destino; se canceló la actualización.'; END IF; IF EXISTS(SELECT 1 FROM productos WHERE codigo='PROD-0038' AND stock_actual<>88) THEN RAISE EXCEPTION 'Revisar la cantidad de sixpacks Chomp antes de reintentar.'; END IF; END $$;
-- Aplicar solo en la base de destino deseada. No modifica usuarios existentes.
SET LOCAL lock_timeout = '5s';
ALTER TABLE public.roles DROP CONSTRAINT IF EXISTS roles_nombre_check;
ALTER TABLE public.roles ADD CONSTRAINT roles_nombre_check
    CHECK (nombre IN ('ADMINISTRADOR', 'VENDEDOR', 'CLIENTE'));
INSERT INTO public.roles (nombre, descripcion)
SELECT 'CLIENTE', 'Cliente de la tienda web'
WHERE NOT EXISTS (SELECT 1 FROM public.roles WHERE nombre = 'CLIENTE')
ON CONFLICT (nombre) DO NOTHING;
SET LOCAL lock_timeout = '5s';
ALTER TABLE productos ADD COLUMN IF NOT EXISTS stock_reservado integer NOT NULL DEFAULT 0;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='productos_reserva_check') THEN ALTER TABLE productos ADD CONSTRAINT productos_reserva_check CHECK(stock_reservado>=0 AND stock_reservado<=stock_actual); END IF; END $$;
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS demo boolean NOT NULL DEFAULT false;
CREATE SEQUENCE IF NOT EXISTS ticket_venta_seq;
CREATE TABLE IF NOT EXISTS pedidos_web (
 id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, cliente_id bigint NOT NULL REFERENCES usuarios(id), clave varchar(36) NOT NULL,
 huella varchar(64) NOT NULL, numero varchar(30) NOT NULL UNIQUE, estado varchar(32) NOT NULL,
 demo boolean NOT NULL, creado timestamp NOT NULL, vence timestamp NOT NULL, total numeric(10,2) NOT NULL CHECK(total>0),
 codigo_recojo varchar(40) UNIQUE, direccion varchar(255) NOT NULL, horario varchar(150) NOT NULL,
 titular varchar(100) NOT NULL, telefono_yape varchar(30) NOT NULL, observacion varchar(500), venta_id bigint UNIQUE REFERENCES ventas(id), version bigint NOT NULL DEFAULT 0,
 UNIQUE(cliente_id,clave), CHECK(estado IN ('PENDIENTE_PAGO','PENDIENTE_VERIFICACION','PAGADO','EN_PREPARACION','LISTO_PARA_RECOJO','ENTREGADO','CANCELADO','EXPIRADO','RECHAZADO','REEMBOLSADO'))
);
CREATE INDEX IF NOT EXISTS pedidos_web_cliente_idx ON pedidos_web(cliente_id,creado DESC);
CREATE INDEX IF NOT EXISTS pedidos_web_vencimiento_idx ON pedidos_web(estado,vence);
CREATE UNIQUE INDEX IF NOT EXISTS pedidos_web_un_pendiente_idx ON pedidos_web(cliente_id) WHERE estado IN ('PENDIENTE_PAGO','PENDIENTE_VERIFICACION');
CREATE TABLE IF NOT EXISTS lineas_pedido_web (
 id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, pedido_id bigint NOT NULL REFERENCES pedidos_web(id), producto_id bigint NOT NULL REFERENCES productos(id),
 nombre varchar(150) NOT NULL,cantidad integer NOT NULL CHECK(cantidad>0),precio numeric(10,2) NOT NULL CHECK(precio>0),UNIQUE(pedido_id,producto_id)
);
CREATE TABLE IF NOT EXISTS comprobantes_pedido_web (
 pedido_id bigint PRIMARY KEY REFERENCES pedidos_web(id),hash varchar(64) NOT NULL UNIQUE,tipo varchar(20) NOT NULL CHECK(tipo IN ('image/png','image/jpeg')),
 operacion varchar(100) NOT NULL,contenido bytea NOT NULL CHECK(octet_length(contenido)<=3145728)
);
CREATE TABLE IF NOT EXISTS eventos_pedido_web (
 id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,pedido_id bigint NOT NULL REFERENCES pedidos_web(id),actor_id bigint NOT NULL REFERENCES usuarios(id),
 fecha timestamp NOT NULL,estado varchar(32) NOT NULL,mensaje varchar(500) NOT NULL
);
CREATE INDEX IF NOT EXISTS eventos_pedido_web_pedido_idx ON eventos_pedido_web(pedido_id,id);
DO $$ DECLARE mx bigint; lv bigint; BEGIN SELECT COALESCE(max(substring(numero_ticket from '^[BWD]001-([0-9]+)$')::bigint),0) INTO mx FROM ventas WHERE numero_ticket ~ '^[BWD]001-[0-9]+$'; SELECT last_value INTO lv FROM ticket_venta_seq; IF mx>=lv THEN PERFORM setval('ticket_venta_seq',mx,true); END IF; END $$;
DO $$ BEGIN IF EXISTS(SELECT 1 FROM productos p JOIN catalogo_actualizacion a ON a.codigo=p.codigo WHERE (p.nombre,p.marca,p.precio_venta,p.estado) IS DISTINCT FROM (a.nombre_nuevo,a.marca_nueva,a.precio_nuevo,a.estado_nuevo) AND (p.stock_reservado>0 OR EXISTS(SELECT 1 FROM lineas_pedido_web l JOIN pedidos_web ped ON ped.id=l.pedido_id WHERE l.producto_id=p.id AND ped.estado IN ('PENDIENTE_PAGO','PENDIENTE_VERIFICACION','PAGADO','EN_PREPARACION','LISTO_PARA_RECOJO')))) THEN RAISE EXCEPTION 'Un producto afectado tiene pedidos activos. Revisar antes de reintentar.'; END IF; END $$;
UPDATE productos p SET nombre=a.nombre_nuevo,marca=a.marca_nueva,precio_venta=a.precio_nuevo,estado=a.estado_nuevo,version=p.version+1 FROM catalogo_actualizacion a WHERE p.codigo=a.codigo AND (p.nombre,p.marca,p.precio_venta,p.estado) IS DISTINCT FROM (a.nombre_nuevo,a.marca_nueva,a.precio_nuevo,a.estado_nuevo);
DO $$ BEGIN IF EXISTS(SELECT 1 FROM productos WHERE codigo='PROD-0052' AND (nombre IS DISTINCT FROM 'Trozos de Atún Campomar en Aceite Vegetal Lata 150 g' OR marca IS DISTINCT FROM 'Campomar' OR precio_venta<>6.10)) THEN RAISE EXCEPTION 'El código PROD-0052 ya fue utilizado para otro artículo.'; END IF; END $$;
INSERT INTO productos(codigo,nombre,marca,descripcion,precio_compra,precio_venta,stock_actual,stock_minimo,stock_reservado,unidad_medida,estado,version,categoria_id,proveedor_id) SELECT 'PROD-0052','Trozos de Atún Campomar en Aceite Vegetal Lata 150 g','Campomar','Conserva de trozos de atún en aceite vegetal. Lata individual de 150 g.',0,6.10,0,5,0,'UNIDAD',true,0,categoria_id,proveedor_id FROM productos WHERE codigo='PROD-0028' AND NOT EXISTS(SELECT 1 FROM productos WHERE codigo='PROD-0052');
INSERT INTO presentacion_producto(producto_id,nombre_presentacion,factor_conversion,estado) SELECT id,'Unidad',1,true FROM productos p WHERE codigo='PROD-0052' AND NOT EXISTS(SELECT 1 FROM presentacion_producto pp WHERE pp.producto_id=p.id);
-- Spring Boot/JavaFX usan PostgreSQL autenticado. Las nuevas tablas no se exponen a clientes anon.
ALTER TABLE pedidos_web ENABLE ROW LEVEL SECURITY;
ALTER TABLE lineas_pedido_web ENABLE ROW LEVEL SECURITY;
ALTER TABLE comprobantes_pedido_web ENABLE ROW LEVEL SECURITY;
ALTER TABLE eventos_pedido_web ENABLE ROW LEVEL SECURITY;
COMMIT;
