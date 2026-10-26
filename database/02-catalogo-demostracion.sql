-- Catálogo académico sin usuarios, comprobantes, contactos ni operaciones reales.
-- Cada producto inicia con stock FICTICIO de 10 unidades/envases y costo pendiente 0.
BEGIN;
DO $$ BEGIN IF EXISTS(SELECT 1 FROM productos) OR EXISTS(SELECT 1 FROM usuarios) THEN RAISE EXCEPTION 'Use este catálogo únicamente en una base nueva sin productos ni usuarios.'; END IF; END $$;
INSERT INTO categorias(id,nombre,descripcion,estado) VALUES (1,'Lacteos','Leches, yogures, quesos, mantequillas y cremas',true),(2,'Embutidos','Jamonadas, salchichas, chorizos y salames',true),(3,'Chocolates y Golosinas','Chocolates, caramelos, chupetines y gomas de mascar.',true),(4,'Abarrotes Secos','Arroz, azúcar, fideos, menestras, harinas y avena',true),(5,'Limpieza del Hogar','Detergentes, lavavajillas, desinfectantes de piso y bolsas de basura',true),(6,'Helados y Hielos','Helados de paleta, potes familiares, hielo en bolsa y marcianos',true),(7,'Cuidado Personal','Jabones de tocador, Shampoos, desodorantes y pasta dental',true),(8,'Bebidas y Refrescos','Gaseosas, aguas de mesa, jugos en caja, rehidratantes y cervezas',true),(9,'Conservas y Salsas','Latas de atún, trozos de pollo, aceite, mayonesa, kétchup y ajíes',true),(10,'Snacks y Piqueos','Papitas, camotes, chifles, galletas saladas y frutos secos',true),(11,'Galletas','Creada desde catálogo',true);
INSERT INTO proveedores(id,razon_social,ruc,estado) VALUES (1,'Proveedor de demostración','00000000000',true);
INSERT INTO roles(nombre,descripcion) VALUES ('ADMINISTRADOR','Administrador del sistema'),('VENDEDOR','Vendedor de la bodega'),('CLIENTE','Cliente de la tienda web');
INSERT INTO productos(id,codigo,nombre,marca,descripcion,precio_compra,precio_venta,stock_actual,stock_minimo,stock_reservado,unidad_medida,estado,version,categoria_id,proveedor_id) VALUES
(1,'PROD-0001','Leche Evaporada Entera Gloria Lata 400 g','Gloria','Producto del catálogo de demostración académica.',0,4.2,10,5,0,'UNIDAD',true,0,1,1),
(2,'PROD-0002','Leche Evaporada Light Gloria Lata 400 g','Gloria','Producto del catálogo de demostración académica.',0,4.5,10,5,0,'UNIDAD',true,0,1,1),
(4,'PROD-0003','Chorizo Braedt Parrillero Precocido Paquete 500 g','Braedt','Producto del catálogo de demostración académica.',0,15.1,10,5,0,'UNIDAD',true,0,2,1),
(5,'PROD-0004','Chorizo San Fernando Unidad 40 g','San Fernando','Producto del catálogo de demostración académica.',0,2.5,10,5,0,'UNIDAD',true,0,2,1),
(6,'PROD-0005','Tuyo - Costa','Costa','Producto del catálogo de demostración académica.',0,1.5,10,5,0,'UNIDAD',true,0,3,1),
(7,'PROD-0006','Beso de Moza','Nestlé','Producto del catálogo de demostración académica.',0,2.0,10,5,0,'UNIDAD',true,0,3,1),
(18,'PROD-0007','Gaseosa Inca Kola Botella 1.5L','Inca Kola','Producto del catálogo de demostración académica.',0,7.0,10,5,0,'LITRO',true,0,8,1),
(19,'PROD-0008','Gaseosa Coca Cola Sin Azúcar 500ml','Coca Cola','Producto del catálogo de demostración académica.',0,3.0,10,5,0,'MILILITRO',true,0,8,1),
(20,'PROD-0009','Galletas Gretel 32 g','Costa','Producto del catálogo de demostración académica.',0,1.5,10,5,0,'UNIDAD',true,0,11,1),
(21,'PROD-0010','Papas Lays Clásicas 36 g','Lays','Producto del catálogo de demostración académica.',0,2.5,10,5,0,'UNIDAD',true,0,10,1),
(22,'PROD-0011','Duraznos Dos Caballos Lata 820 g','Dos Caballos','Producto del catálogo de demostración académica.',0,12.9,10,5,0,'UNIDAD',true,0,9,1),
(23,'PROD-0012','Arroz Extra 750 g','Costeño','Producto del catálogo de demostración académica.',0,4.5,10,5,0,'UNIDAD',true,0,4,1),
(24,'PROD-0013','Azúcar Rubia 1 kg','Cartavio','Producto del catálogo de demostración académica.',0,4.8,10,5,0,'UNIDAD',true,0,4,1),
(25,'PROD-0014','Fideos Spaghetti 500 g','Don Vittorio','Producto del catálogo de demostración académica.',0,3.8,10,5,0,'UNIDAD',true,0,4,1),
(26,'PROD-0015','Fideos Tallarín 500 g','Nicolini','Producto del catálogo de demostración académica.',0,3.3,10,5,0,'UNIDAD',true,0,4,1),
(27,'PROD-0016','Aceite Vegetal Clásico 900 ml','Primor','Producto del catálogo de demostración académica.',0,10.0,10,5,0,'UNIDAD',true,0,4,1),
(28,'PROD-0017','Aceite Vegetal 900 ml','Cocinero','Producto del catálogo de demostración académica.',0,8.9,10,5,0,'UNIDAD',true,0,4,1),
(29,'PROD-0018','Sal Yodada 1 kg','Emsal','Producto del catálogo de demostración académica.',0,2.6,10,5,0,'UNIDAD',true,0,4,1),
(30,'PROD-0019','Harina Sin Preparar 1 kg','Blanca Flor','Producto del catálogo de demostración académica.',0,8.2,10,5,0,'UNIDAD',true,0,4,1),
(31,'PROD-0020','Avena Tradicional 500 g','Quaker','Producto del catálogo de demostración académica.',0,8.3,10,5,0,'UNIDAD',true,0,4,1),
(32,'PROD-0021','Mezcla Láctea Ideal Cremosita Lata 390 g','Ideal','Producto del catálogo de demostración académica.',0,4.0,10,5,0,'UNIDAD',true,0,1,1),
(33,'PROD-0022','Yogur Fresa 946 g','Gloria','Producto del catálogo de demostración académica.',0,6.3,10,5,0,'UNIDAD',true,0,1,1),
(34,'PROD-0023','Queso Fresco 250 g','Laive','Producto del catálogo de demostración académica.',0,9.9,10,5,0,'UNIDAD',true,0,1,1),
(35,'PROD-0024','Mantequilla con Sal 180 g','Laive','Producto del catálogo de demostración académica.',0,13.9,10,5,0,'UNIDAD',true,0,1,1),
(36,'PROD-0025','Jamonada de Pollo San Fernando Paquete 200 g','San Fernando','Producto del catálogo de demostración académica.',0,5.5,10,5,0,'UNIDAD',true,0,2,1),
(37,'PROD-0026','Hot Dog de Pollo San Fernando Paquete 250 g','San Fernando','Producto del catálogo de demostración académica.',0,7.3,10,5,0,'UNIDAD',true,0,2,1),
(38,'PROD-0027','Trozos de Atún Florida en Aceite Vegetal Lata 140 g','Florida','Producto del catálogo de demostración académica.',0,6.6,10,5,0,'UNIDAD',true,0,9,1),
(40,'PROD-0029','Salsa de Tomate 200 g','Alacena','Producto del catálogo de demostración académica.',0,3.2,10,5,0,'UNIDAD',true,0,9,1),
(41,'PROD-0030','Mayonesa Clásica 235 g','Alacena','Producto del catálogo de demostración académica.',0,8.6,10,5,0,'UNIDAD',true,0,9,1),
(42,'PROD-0031','Agua Sin Gas 750 ml','San Luis','Producto del catálogo de demostración académica.',0,1.8,10,5,0,'UNIDAD',true,0,8,1),
(43,'PROD-0032','Agua Sin Gas 1 L','Cielo','Producto del catálogo de demostración académica.',0,2.4,10,5,0,'UNIDAD',true,0,8,1),
(44,'PROD-0033','Néctar de Durazno 1 L','Frugos','Producto del catálogo de demostración académica.',0,5.8,10,5,0,'UNIDAD',true,0,8,1),
(45,'PROD-0034','Bebida Rehidratante 500 ml','Sporade','Producto del catálogo de demostración académica.',0,3.0,10,5,0,'UNIDAD',true,0,8,1),
(46,'PROD-0035','Gaseosa Pepsi 500 ml','Pepsi','Producto del catálogo de demostración académica.',0,3.0,10,5,0,'UNIDAD',true,0,8,1),
(47,'PROD-0036','Galletas Soda Field Sixpack 6 Paquetes','Field','Producto del catálogo de demostración académica.',0,4.0,10,5,0,'UNIDAD',true,0,11,1),
(48,'PROD-0037','Galletas Oreo 36 g','Oreo','Producto del catálogo de demostración académica.',0,2.0,10,5,0,'UNIDAD',true,0,11,1),
(49,'PROD-0038','Galletas Chomp Naranja Victoria Sixpack 6 x 42 g','Victoria','Producto del catálogo de demostración académica.',0,6.7,10,5,0,'UNIDAD',true,0,11,1),
(50,'PROD-0039','Galletas Vainilla Field Sixpack 6 Paquetes','Field','Producto del catálogo de demostración académica.',0,5.5,10,5,0,'UNIDAD',true,0,11,1),
(51,'PROD-0040','Chizitos 35 g','Lays','Producto del catálogo de demostración académica.',0,2.0,10,5,0,'UNIDAD',true,0,10,1),
(52,'PROD-0041','Cheetos 50 g','Cheetos','Producto del catálogo de demostración académica.',0,2.5,10,5,0,'UNIDAD',true,0,10,1),
(53,'PROD-0042','Chocolate Sublime Clásico 26 g','Nestlé','Producto del catálogo de demostración académica.',0,2.8,10,5,0,'UNIDAD',true,0,3,1),
(54,'PROD-0043','Gomitas Dulces Ambrosoli Bolsa 430 g','Ambrosoli','Producto del catálogo de demostración académica.',0,14.5,10,5,0,'UNIDAD',true,0,3,1),
(55,'PROD-0044','Chupetines Globo Pop Paquete 24 Unidades','Globo Pop','Producto del catálogo de demostración académica.',0,8.2,10,5,0,'UNIDAD',true,0,3,1),
(56,'PROD-0045','Detergente en Polvo Ariel con Downy Bolsa 800 g','Ariel','Producto del catálogo de demostración académica.',0,10.9,10,5,0,'UNIDAD',true,0,5,1),
(57,'PROD-0046','Lejía Clorox Tradicional Botella 1 L','Clorox','Producto del catálogo de demostración académica.',0,4.6,10,5,0,'UNIDAD',true,0,5,1),
(58,'PROD-0047','Lavavajilla Líquido 750 ml','Sapolio','Producto del catálogo de demostración académica.',0,9.5,10,5,0,'UNIDAD',true,0,5,1),
(59,'PROD-0048','Papel Higiénico Elite Aloe Vera Doble Hoja Paquete 4 Rollos','Elite','Producto del catálogo de demostración académica.',0,8.5,10,5,0,'UNIDAD',true,0,7,1),
(60,'PROD-0049','Jabón de Tocador 110 g','Protex','Producto del catálogo de demostración académica.',0,3.6,10,5,0,'UNIDAD',true,0,7,1),
(61,'PROD-0050','Shampoo Limpieza Renovadora Sachet 18 ml','Head & Shoulders','Producto del catálogo de demostración académica.',0,0.9,10,5,0,'UNIDAD',true,0,7,1),
(62,'PROD-0051','Crema Dental Colgate Máxima Protección Anticaries 90 g','Colgate','Producto del catálogo de demostración académica.',0,5.5,10,5,0,'UNIDAD',true,0,7,1),
(63,'PROD-0052','Trozos de Atún Campomar en Aceite Vegetal Lata 150 g','Campomar','Producto del catálogo de demostración académica.',0,6.1,10,5,0,'UNIDAD',true,0,9,1);
INSERT INTO presentacion_producto(producto_id,nombre_presentacion,factor_conversion,estado) SELECT id,'Unidad',1,true FROM productos;
SELECT setval(pg_get_serial_sequence('categorias','id'),(SELECT max(id) FROM categorias),true);
SELECT setval(pg_get_serial_sequence('proveedores','id'),(SELECT max(id) FROM proveedores),true);
SELECT setval(pg_get_serial_sequence('productos','id'),(SELECT max(id) FROM productos),true);
COMMIT;
