-- Reemplazo autorizado: conservar íntegro el historial del producto anterior.
-- Atún Campomar en trozos, aceite vegetal, lata 150 g: Tottus S/ 6.10.
-- Fuente: https://www.tottus.com.pe/tottus-pe/marca/CAMPOMAR
-- Respaldar y comparar el destino antes de aplicar. No se transfiere stock.
BEGIN;
SET LOCAL lock_timeout='5s';
LOCK TABLE productos IN SHARE ROW EXCLUSIVE MODE;
DO $$ BEGIN
 IF NOT EXISTS (SELECT 1 FROM productos WHERE codigo='PROD-0028'
   AND nombre='Sardinas en Salsa de Tomate 425 g' AND marca='Campomar'
   AND precio_compra=5.00 AND precio_venta=6.80 AND unidad_medida='UNIDAD')
 THEN RAISE EXCEPTION 'El producto anterior no coincide con el revisado.'; END IF;
 IF EXISTS (SELECT 1 FROM productos p WHERE p.codigo='PROD-0028'
   AND (p.stock_reservado<>0 OR EXISTS (SELECT 1 FROM lineas_pedido_web l
   JOIN pedidos_web ped ON ped.id=l.pedido_id WHERE l.producto_id=p.id
   AND ped.estado IN ('PENDIENTE_PAGO','PENDIENTE_VERIFICACION','PAGADO','EN_PREPARACION','LISTO_PARA_RECOJO'))))
 THEN RAISE EXCEPTION 'El producto anterior tiene reservas o pedidos activos.'; END IF;
 IF EXISTS (SELECT 1 FROM productos WHERE codigo='PROD-0052'
   AND (nombre IS DISTINCT FROM 'Trozos de Atún Campomar en Aceite Vegetal Lata 150 g'
     OR marca IS DISTINCT FROM 'Campomar' OR precio_venta<>6.10 OR unidad_medida<>'UNIDAD' OR NOT estado))
 THEN RAISE EXCEPTION 'El código nuevo ya corresponde a otro producto o fue modificado.'; END IF;
 IF EXISTS (SELECT 1 FROM productos WHERE codigo<>'PROD-0052'
   AND nombre='Trozos de Atún Campomar en Aceite Vegetal Lata 150 g' AND estado)
 THEN RAISE EXCEPTION 'Ya existe otro registro activo para el atún elegido.'; END IF;
END $$;
UPDATE productos SET estado=false,version=version+1
 WHERE codigo='PROD-0028' AND estado;
INSERT INTO productos (codigo,nombre,marca,descripcion,precio_compra,precio_venta,
 stock_actual,stock_minimo,stock_reservado,unidad_medida,estado,version,categoria_id,proveedor_id)
 SELECT 'PROD-0052','Trozos de Atún Campomar en Aceite Vegetal Lata 150 g','Campomar',
 'Conserva de trozos de atún en aceite vegetal. Lata individual de 150 g.',
 0.00,6.10,0,5,0,'UNIDAD',true,0,categoria_id,proveedor_id
 FROM productos WHERE codigo='PROD-0028'
 AND NOT EXISTS (SELECT 1 FROM productos WHERE codigo='PROD-0052');
INSERT INTO presentacion_producto (producto_id,nombre_presentacion,factor_conversion,estado)
 SELECT p.id,'Unidad',1,true FROM productos p WHERE p.codigo='PROD-0052'
 AND NOT EXISTS (SELECT 1 FROM presentacion_producto pp WHERE pp.producto_id=p.id AND pp.nombre_presentacion='Unidad');
COMMIT;
