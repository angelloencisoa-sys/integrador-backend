-- Normalización autorizada del catálogo académico. Consulta: 2026-10-25.
-- Fuentes y decisiones: docs/revision-precios/plan-normalizacion.json.
-- Aplicar con psql -v ON_ERROR_STOP=1. Respaldar y verificar primero el destino.
-- No cambia IDs, códigos, marcas, costos, unidad base, existencias ni documentos históricos.
BEGIN;
SET LOCAL lock_timeout = '5s';
CREATE TEMP TABLE normalizacion_catalogo (
 codigo varchar(50) PRIMARY KEY,marca varchar(80),nombre_anterior varchar(150),nombre_nuevo varchar(150),
 precio_anterior numeric(10,2),precio_nuevo numeric(10,2),costo_anterior numeric(10,2)
) ON COMMIT DROP;
INSERT INTO normalizacion_catalogo VALUES
('PROD-0006','Nestlé','Beso de Mosa','Beso de Moza','2.00','2.00','1.42'),
('PROD-0009','Costa','Gretel 38g','Galletas Gretel 32 g','1.50','1.50','1.00'),
('PROD-0010','Lays','Papitas Lays 40g','Papas Lays Clásicas 36 g','1.50','2.50','1.00'),
('PROD-0012','Costeño','Arroz Extra 1 kg','Arroz Extra 750 g','5.50','4.50','4.20'),
('PROD-0013','Cartavio','Azúcar Rubia 1 kg','Azúcar Rubia 1 kg','4.50','4.80','3.50'),
('PROD-0014','Don Vittorio','Fideos Spaghetti 500 g','Fideos Spaghetti 500 g','3.30','3.80','2.40'),
('PROD-0015','Nicolini','Fideos Tallarín 500 g','Fideos Tallarín 500 g','3.10','3.30','2.20'),
('PROD-0016','Primor','Aceite Vegetal 1 L','Aceite Vegetal Clásico 900 ml','11.50','10.00','8.50'),
('PROD-0017','Cocinero','Aceite Vegetal 900 ml','Aceite Vegetal 900 ml','9.90','8.90','7.20'),
('PROD-0018','Emsal','Sal Yodada 1 kg','Sal Yodada 1 kg','2.30','2.60','1.60'),
('PROD-0019','Blanca Flor','Harina Sin Preparar 1 kg','Harina Sin Preparar 1 kg','4.20','8.20','3.00'),
('PROD-0020','Quaker','Avena Tradicional 400 g','Avena Tradicional 500 g','5.20','8.30','3.90'),
('PROD-0022','Gloria','Yogur Fresa 1 L','Yogur Fresa 946 g','7.00','6.30','5.40'),
('PROD-0024','Laive','Mantequilla 200 g','Mantequilla con Sal 180 g','9.30','13.90','7.00'),
('PROD-0030','Alacena','Mayonesa 200 g','Mayonesa Clásica 235 g','5.30','8.60','4.00'),
('PROD-0031','San Luis','Agua Sin Gas 625 ml','Agua Sin Gas 750 ml','2.00','1.80','1.20'),
('PROD-0032','Cielo','Agua Sin Gas 1 L','Agua Sin Gas 1 L','2.50','2.40','1.50'),
('PROD-0034','Sporade','Bebida Rehidratante 500 ml','Bebida Rehidratante 500 ml','4.00','2.50','2.70'),
('PROD-0042','Nestlé','Chocolate Sublime 30 g','Chocolate Sublime Clásico 26 g','2.50','2.80','1.50'),
('PROD-0047','Sapolio','Lavavajilla Líquido 750 ml','Lavavajilla Líquido 750 ml','7.20','9.50','5.50'),
('PROD-0049','Protex','Jabón de Tocador 125 g','Jabón de Tocador 110 g','3.70','3.60','2.50'),
('PROD-0050','Head & Shoulders','Shampoo Sachet 15 ml','Shampoo Limpieza Renovadora Sachet 18 ml','1.20','0.90','0.70');
DO $$
BEGIN
 IF EXISTS (SELECT 1 FROM normalizacion_catalogo n LEFT JOIN productos p ON p.codigo=n.codigo
  GROUP BY n.codigo HAVING count(p.id)<>1) THEN RAISE EXCEPTION 'Falta un código o no es único. Revisar el catálogo del destino.'; END IF;
 -- Orden estable para no bloquear productos en órdenes distintas a caja/checkout.
 PERFORM p.id FROM productos p JOIN normalizacion_catalogo n ON n.codigo=p.codigo ORDER BY p.id FOR UPDATE OF p;
 IF EXISTS (SELECT 1 FROM productos p JOIN normalizacion_catalogo n ON n.codigo=p.codigo
  WHERE p.marca IS DISTINCT FROM n.marca OR p.unidad_medida<>'UNIDAD' OR NOT p.estado OR NOT (
   (p.nombre=n.nombre_anterior AND p.precio_venta=n.precio_anterior AND p.precio_compra=n.costo_anterior)
   OR (p.nombre=n.nombre_nuevo AND p.precio_venta=n.precio_nuevo)))
 THEN RAISE EXCEPTION 'Los datos difieren de la versión revisada. No se aplicó ningún ajuste.'; END IF;
 IF EXISTS (SELECT 1 FROM productos p JOIN normalizacion_catalogo n ON n.codigo=p.codigo
  WHERE (p.nombre,p.precio_venta) IS DISTINCT FROM (n.nombre_nuevo,n.precio_nuevo)
  AND (p.stock_reservado>0 OR EXISTS (SELECT 1 FROM lineas_pedido_web l JOIN pedidos_web ped ON ped.id=l.pedido_id
   WHERE l.producto_id=p.id AND ped.estado IN ('PENDIENTE_PAGO','PENDIENTE_VERIFICACION','PAGADO','EN_PREPARACION','LISTO_PARA_RECOJO'))))
 THEN RAISE EXCEPTION 'Un producto a modificar tiene un pedido activo. Revisar antes de reintentar.'; END IF;
END $$;
UPDATE productos p SET nombre=n.nombre_nuevo,precio_venta=n.precio_nuevo,version=p.version+1
FROM normalizacion_catalogo n WHERE p.codigo=n.codigo
 AND (p.nombre,p.precio_venta) IS DISTINCT FROM (n.nombre_nuevo,n.precio_nuevo);
SELECT p.codigo,p.nombre,p.precio_venta,p.version FROM productos p JOIN normalizacion_catalogo n ON n.codigo=p.codigo ORDER BY p.codigo;
COMMIT;
