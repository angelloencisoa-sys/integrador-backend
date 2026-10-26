-- Cierre autorizado del catálogo: 2026-10-25.
-- Precios propios provisionales identificados en docs/revision-precios/CIERRE_CATALOGO.md.
-- Respaldar y comparar el destino antes de aplicar; no cambia costos ni stock.
BEGIN;
SET LOCAL lock_timeout='5s';
CREATE TEMP TABLE cierre_catalogo (
 codigo varchar(50) PRIMARY KEY,marca varchar(80),nombre_anterior varchar(150),nombre_nuevo varchar(150),
 precio_anterior numeric(10,2),precio_nuevo numeric(10,2),costo_conservado numeric(10,2)
) ON COMMIT DROP;
INSERT INTO cierre_catalogo VALUES
('PROD-0003','Braedt','Chorizos Braedt Paquete 500 g','Chorizo Braedt Parrillero Precocido Paquete 500 g','15.10','15.10','1.10'),
('PROD-0004','San Fernando','Chorizo San Fernando Unidad 40 g','Chorizo San Fernando Unidad 40 g','2.00','2.50','1.10'),
('PROD-0025','San Fernando','Jamonada San Fernando Paquete 200 g','Jamonada de Pollo San Fernando Paquete 200 g','6.80','5.50','5.00'),
('PROD-0026','San Fernando','Hot Dog San Fernando Paquete 250 g','Hot Dog de Pollo San Fernando Paquete 250 g','7.30','7.30','5.50'),
('PROD-0034','Sporade','Bebida Rehidratante 500 ml','Bebida Rehidratante 500 ml','2.50','3.00','2.70'),
('PROD-0046','Clorox','Lejía Clorox Tradicional Botella 1 L','Lejía Clorox Tradicional Botella 1 L','5.80','4.60','4.10'),
('PROD-0048','Elite','Papel Higiénico Elite Doble Hoja Paquete 4 Rollos','Papel Higiénico Elite Aloe Vera Doble Hoja Paquete 4 Rollos','8.50','8.50','6.20'),
('PROD-0051','Colgate','Crema Dental Colgate Máxima Protección Anticaries 90 g','Crema Dental Colgate Máxima Protección Anticaries 90 g','3.60','5.50','4.80');
DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM cierre_catalogo a LEFT JOIN productos p ON p.codigo=a.codigo
 GROUP BY a.codigo HAVING count(p.id)<>1) THEN RAISE EXCEPTION 'Falta un producto o su código no es único.'; END IF;
 PERFORM p.id FROM productos p JOIN cierre_catalogo a ON a.codigo=p.codigo ORDER BY p.id FOR UPDATE OF p;
 IF EXISTS (SELECT 1 FROM productos p JOIN cierre_catalogo a ON a.codigo=p.codigo
 WHERE p.marca IS DISTINCT FROM a.marca OR p.unidad_medida<>'UNIDAD' OR NOT p.estado
 OR p.precio_compra IS DISTINCT FROM a.costo_conservado
 OR NOT ((p.nombre,p.precio_venta)=(a.nombre_anterior,a.precio_anterior)
 OR (p.nombre,p.precio_venta)=(a.nombre_nuevo,a.precio_nuevo)))
 THEN RAISE EXCEPTION 'El catálogo difiere del revisado. No se modificó ningún dato.'; END IF;
 IF EXISTS (SELECT 1 FROM productos p JOIN cierre_catalogo a ON a.codigo=p.codigo
 WHERE (p.nombre,p.precio_venta) IS DISTINCT FROM (a.nombre_nuevo,a.precio_nuevo)
 AND (p.stock_reservado>0 OR EXISTS (SELECT 1 FROM lineas_pedido_web l JOIN pedidos_web ped ON ped.id=l.pedido_id
 WHERE l.producto_id=p.id AND ped.estado IN ('PENDIENTE_PAGO','PENDIENTE_VERIFICACION','PAGADO','EN_PREPARACION','LISTO_PARA_RECOJO'))))
 THEN RAISE EXCEPTION 'Hay un pedido activo afectado por este cambio.'; END IF;
END $$;
UPDATE productos p SET nombre=a.nombre_nuevo,precio_venta=a.precio_nuevo,version=p.version+1
FROM cierre_catalogo a WHERE p.codigo=a.codigo
AND (p.nombre,p.precio_venta) IS DISTINCT FROM (a.nombre_nuevo,a.precio_nuevo);
COMMIT;
