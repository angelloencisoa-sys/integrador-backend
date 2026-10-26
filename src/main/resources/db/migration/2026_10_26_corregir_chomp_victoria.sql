-- Corrección solicitada por el usuario según imagen del sixpack Chomp Victoria.
-- Ejecutar sólo tras confirmar que el stock representa sixpacks completos.
-- 6 paquetes x 42 g = 252 g por sixpack; S/ 6.70 por sixpack.
BEGIN;
SET LOCAL lock_timeout='5s';
DO $$ BEGIN
 PERFORM id FROM productos WHERE codigo='PROD-0038' FOR UPDATE;
 IF NOT EXISTS (SELECT 1 FROM productos WHERE codigo='PROD-0038' AND estado
  AND unidad_medida='UNIDAD' AND precio_compra=1.20 AND (
   (nombre='Galletas Chomp 42 g' AND marca='Costa' AND precio_venta=2.00)
   OR (nombre='Galletas Chomp Naranja Victoria Sixpack 6 x 42 g' AND marca='Victoria' AND precio_venta=6.70)))
 THEN RAISE EXCEPTION 'El producto Chomp difiere del revisado. No se aplicaron cambios.'; END IF;
 IF EXISTS (SELECT 1 FROM productos p WHERE codigo='PROD-0038' AND
  (stock_reservado<>0 OR EXISTS (SELECT 1 FROM lineas_pedido_web l JOIN pedidos_web ped ON ped.id=l.pedido_id
  WHERE l.producto_id=p.id AND ped.estado IN ('PENDIENTE_PAGO','PENDIENTE_VERIFICACION','PAGADO','EN_PREPARACION','LISTO_PARA_RECOJO'))))
 THEN RAISE EXCEPTION 'Chomp tiene reservas o pedidos activos. Revisar antes de corregir.'; END IF;
END $$;
UPDATE productos SET nombre='Galletas Chomp Naranja Victoria Sixpack 6 x 42 g',marca='Victoria',
 precio_venta=6.70,version=version+1 WHERE codigo='PROD-0038'
 AND (nombre,marca,precio_venta) IS DISTINCT FROM ('Galletas Chomp Naranja Victoria Sixpack 6 x 42 g','Victoria',6.70);
COMMIT;
