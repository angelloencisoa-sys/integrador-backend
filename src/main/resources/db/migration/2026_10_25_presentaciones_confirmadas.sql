-- Presentaciones confirmadas por el usuario el 2026-10-25.
-- Revisar plan-presentaciones-confirmadas.json e informe antes de usar en otro entorno.
-- UNIDAD equivale a un envase/paquete completo en los artículos empaquetados.
-- Factor de conversión 1 conservado; no es un gramaje ni número de piezas interiores.
-- Requiere las tablas del proyecto. No es una migración automática a Supabase.
BEGIN;
SET LOCAL lock_timeout='5s';
CREATE TEMP TABLE ajuste_presentaciones (
 codigo varchar(50) PRIMARY KEY,marca varchar(80),nombre_anterior varchar(150),nombre_nuevo varchar(150),
 precio_anterior numeric(10,2),precio_nuevo numeric(10,2),costo_conservado numeric(10,2)
) ON COMMIT DROP;
INSERT INTO ajuste_presentaciones VALUES
('PROD-0001','Gloria','Leche Gloria Azul 400g','Leche Evaporada Entera Gloria Lata 400 g','4.20','4.20','3.20'),
('PROD-0002','Gloria','Leche Gloria Roja 400g','Leche Evaporada Light Gloria Lata 400 g','4.50','4.50','3.50'),
('PROD-0003','Braedt','Chorizo - Braedt','Chorizos Braedt Paquete 500 g','2.00','15.10','1.10'),
('PROD-0004','San Fernando','Chorizo - San Fernando','Chorizo San Fernando Unidad 40 g','2.00','2.00','1.10'),
('PROD-0011','Dos Caballos','Durazno 480g','Duraznos Dos Caballos Lata 820 g','12.90','12.90','8.00'),
('PROD-0021','Ideal','Leche Evaporada 400 g','Mezcla Láctea Ideal Cremosita Lata 390 g','4.30','4.00','3.20'),
('PROD-0025','San Fernando','Jamonada 200 g','Jamonada San Fernando Paquete 200 g','6.80','6.80','5.00'),
('PROD-0026','San Fernando','Salchicha Hot Dog 250 g','Hot Dog San Fernando Paquete 250 g','7.30','7.30','5.50'),
('PROD-0027','Florida','Atún en Aceite 170 g','Trozos de Atún Florida en Aceite Vegetal Lata 140 g','7.20','6.60','5.40'),
('PROD-0036','Field','Galletas Soda 6 unidades','Galletas Soda Field Sixpack 6 Paquetes','2.50','4.00','1.60'),
('PROD-0039','Field','Galletas Vainilla 6 unidades','Galletas Vainilla Field Sixpack 6 Paquetes','2.30','5.50','1.50'),
('PROD-0043','Ambrosoli','Caramelos Frutales Bolsa 100 g','Gomitas Dulces Ambrosoli Bolsa 430 g','3.00','14.50','2.00'),
('PROD-0044','Globo Pop','Chupetines Globo Pop Bolsa 10 unidades','Chupetines Globo Pop Paquete 24 Unidades','4.50','8.20','3.00'),
('PROD-0045','Ariel','Detergente en Polvo 800 g','Detergente en Polvo Ariel con Downy Bolsa 800 g','10.50','10.90','8.00'),
('PROD-0046','Clorox','Lejía 1 L','Lejía Clorox Tradicional Botella 1 L','5.80','5.80','4.10'),
('PROD-0048','Elite','Papel Higiénico Pack 4 rollos','Papel Higiénico Elite Doble Hoja Paquete 4 Rollos','8.50','8.50','6.20'),
('PROD-0051','Colgate','Pasta Dental 90 g','Crema Dental Colgate Máxima Protección Anticaries 90 g','6.50','3.60','4.80');
DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM ajuste_presentaciones a LEFT JOIN productos p ON p.codigo=a.codigo
 GROUP BY a.codigo HAVING count(p.id)<>1) THEN RAISE EXCEPTION 'Falta un producto o el código no es único.'; END IF;
 PERFORM p.id FROM productos p JOIN ajuste_presentaciones a ON a.codigo=p.codigo ORDER BY p.id FOR UPDATE OF p;
 IF EXISTS (SELECT 1 FROM productos p JOIN ajuste_presentaciones a ON a.codigo=p.codigo
 WHERE p.marca IS DISTINCT FROM a.marca OR p.unidad_medida<>'UNIDAD' OR NOT p.estado
 OR p.precio_compra IS DISTINCT FROM a.costo_conservado
 OR NOT ((p.nombre,p.precio_venta)=(a.nombre_anterior,a.precio_anterior)
 OR (p.nombre,p.precio_venta)=(a.nombre_nuevo,a.precio_nuevo)))
 THEN RAISE EXCEPTION 'El catálogo difiere del revisado: no se modificó ningún dato.'; END IF;
 -- Sólo las leches Gloria admiten cambio de nombre con reserva: mismo envase 400 g y mismo precio.
 IF EXISTS (SELECT 1 FROM productos p JOIN ajuste_presentaciones a ON a.codigo=p.codigo
 WHERE (p.nombre,p.precio_venta) IS DISTINCT FROM (a.nombre_nuevo,a.precio_nuevo)
 AND (p.stock_reservado>0 OR EXISTS (SELECT 1 FROM lineas_pedido_web l JOIN pedidos_web ped ON ped.id=l.pedido_id
 WHERE l.producto_id=p.id AND ped.estado IN ('PENDIENTE_PAGO','PENDIENTE_VERIFICACION','PAGADO','EN_PREPARACION','LISTO_PARA_RECOJO')))
 AND NOT (p.codigo IN ('PROD-0001','PROD-0002') AND a.precio_anterior=a.precio_nuevo
 AND a.nombre_anterior LIKE '%400g' AND a.nombre_nuevo LIKE '%400 g'))
 THEN RAISE EXCEPTION 'Hay un pedido activo afectado por el ajuste; revisar antes de continuar.'; END IF;
END $$;
UPDATE productos p SET nombre=a.nombre_nuevo,precio_venta=a.precio_nuevo,version=p.version+1
FROM ajuste_presentaciones a WHERE p.codigo=a.codigo
AND (p.nombre,p.precio_venta) IS DISTINCT FROM (a.nombre_nuevo,a.precio_nuevo);
COMMIT;
