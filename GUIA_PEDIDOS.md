# Probar pedidos de La Bodega

La ubicación en Google Maps y el horario (lunes a sábado, 8 a. m. a 8 p. m.) están confirmados. Ubicación de recojo: Asociación de Vivienda La Planicie de la Era / Zona de La Era de Pando, Ate, Lima 15487. Referencia: Pista nueva-Santa Clara, al costado de Ceviche69. Los datos de pago siguen en **DEMO**.
No transfieras dinero. Las cuentas y productos existentes se conservaron.

## Arranque

1. Cierra y vuelve a abrir `iniciar-api.cmd` e `iniciar-panel.cmd` si estaban ejecutandose.
2. Ejecuta `INTEGRADOR_FRONTEND/iniciar-web.cmd` y entra a http://127.0.0.1:4200.
3. Usa o registra una cuenta CLIENTE en la web. El panel requiere un administrador o vendedor existente.

## Recorrido de demostracion

1. Elige productos y cantidades. El carrito funciona como invitado.
2. Al continuar al checkout inicia sesion. Revisa los datos y pulsa **Crear pedido de prueba**.
3. El pedido queda guardado y reserva stock durante diez minutos. Veras el total y la fecha limite.
4. Sube una imagen PNG/JPEG de prueba (maximo 3 MB) y una referencia como DEMO-001.
   La imagen debe ser distinta de comprobantes usados antes. No se realiza ningun cobro.
5. Abre **Pedidos web** en JavaFX y pulsa **Actualizar pedidos**. Selecciona el pedido y **Ver comprobante**.
6. Marca la casilla de revision DEMO y pulsa **Aprobar pago**. Se registra una venta D001 simulada y una salida de stock.
7. Pulsa **Preparar pedido** y luego **Listo para recoger**.
8. En la web, **Mis pedidos** muestra el seguimiento automaticamente cada cinco segundos.
   El cliente vera un QR y su codigo NV-... despues de aprobar el pago.
9. Copia el codigo en JavaFX o usa un lector QR USB que escriba el contenido del QR, y pulsa **Registrar entrega**.
   Debe estar listo para recoger. El mismo codigo no permite una segunda entrega. No incluye lectura por camara.

## Cancelaciones y excepciones

- El cliente puede cancelar antes de enviar comprobante; se libera la reserva.
- Sin comprobante, la reserva vence y un proceso la libera cada treinta segundos.
- Con comprobante, permanece en revision y no libera stock automaticamente: el personal debe verificarlo.
- Rechazar requiere motivo. Se cierra el pedido y se libera la reserva. Si hubo un abono real, coordinar su devolucion; no efectuar otro pago.
- Para un pedido aprobado aun no entregado, solo un administrador puede confirmar un reembolso total.
  Primero se realiza la devolucion externamente (o se simula en DEMO); luego se registra su referencia.
  Marcar reposicion unicamente si la mercaderia fue revisada y es apta. La operacion no transfiere dinero.
- Un pedido web se anula desde este modulo, evitando una anulacion separada de su venta que desincronice el estado.
- Las pruebas DEMO consumen stock del inventario local de demostracion. Para devolverlo antes de entregar,
  usa el reembolso simulado con reposicion. No borres ventas ni modifiques reservados manualmente.

## Cambiar los datos despues

Edita `INTEGRADOR_BACKEND/configuracion-tienda.properties`. Contiene titular,
numero Yape, direccion, horario y modo DEMO. Se pueden reemplazar sin cambiar codigo.
Los cambios requieren reiniciar API y panel; los pedidos anteriores conservan sus datos originales.

Para cobrar realmente, reemplaza TODOS los datos referenciales por datos de la bodega y
establece `app.tienda.demo=false`. La aplicacion impide habilitarlo con el numero ficticio o
los textos referenciales predeterminados. Prueba antes el destinatario. Las variables TIENDA_*
pueden sobrescribir los valores si estan definidas.

El personal debe comprobar el abono, destinatario e importe directamente en su cuenta Yape.
Una captura, numero de operacion o codigo de seguridad no confirma el pago por si solo.
No hay conexion con la cuenta bancaria, pasarela ni devoluciones automaticas.

## Archivos y respaldo

- Backend: `src/main/resources/db/migration/2026_10_25_pedidos_web.sql`.
- Respaldo previo: `.local/backups/sgcivorp_local_antes_pedidos_20261025_195148.dump`.
- Las cuentas, productos y pedidos temporales usados para verificar se eliminaron de `sgcivorp_test`.
- No se crearon pedidos de prueba en `sgcivorp_local`.
- Las 51 fotos de productos ya están integradas al frontend en formato WebP.
