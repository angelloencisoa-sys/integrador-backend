-- Aplicar solo en la base de destino deseada. No modifica usuarios existentes.
BEGIN;
SET LOCAL lock_timeout = '5s';
ALTER TABLE public.roles DROP CONSTRAINT IF EXISTS roles_nombre_check;
ALTER TABLE public.roles ADD CONSTRAINT roles_nombre_check
    CHECK (nombre IN ('ADMINISTRADOR', 'VENDEDOR', 'CLIENTE'));
INSERT INTO public.roles (nombre, descripcion)
SELECT 'CLIENTE', 'Cliente de la tienda web'
WHERE NOT EXISTS (SELECT 1 FROM public.roles WHERE nombre = 'CLIENTE')
ON CONFLICT (nombre) DO NOTHING;
COMMIT;
