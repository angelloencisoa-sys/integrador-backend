package pe.edu.utp.Grupo06.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import pe.edu.utp.Grupo06.dto.venta.ProductoRotacionDTO;
import pe.edu.utp.Grupo06.model.Compra;
import pe.edu.utp.Grupo06.model.DetalleCompra;
import pe.edu.utp.Grupo06.repository.DetalleCompraRepository;
import pe.edu.utp.Grupo06.model.Merma;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.Venta;
import pe.edu.utp.Grupo06.service.IExportacionService;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportacionServiceImpl implements IExportacionService {

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void exportarVentasExcel(File destino, List<Venta> ventas, LocalDate inicio, LocalDate fin) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Reporte de Ventas");
            sheet.setDisplayGridlines(true);

            EstilosExcel estilos = new EstilosExcel(wb);

            // 1. Título y Metadatos
            int rowIdx = 0;
            Row r0 = sheet.createRow(rowIdx++);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("BODEGA & MINIMARKET SGCIVORP - REPORTE DE VENTAS");
            c0.setCellStyle(estilos.titulo);

            Row r1 = sheet.createRow(rowIdx++);
            Cell c1 = r1.createCell(0);
            c1.setCellValue("RUC: 20601234567 | Dirección: Av. Principal 123, Lima | Sistema de Gestión Comercial");
            c1.setCellStyle(estilos.subtitulo);

            String rangoTxt = (inicio != null && fin != null) ? "Del " + inicio.format(df) + " al " + fin.format(df) : "Historial Completo";
            Row r2 = sheet.createRow(rowIdx++);
            Cell c2 = r2.createCell(0);
            c2.setCellValue("Período evaluado: " + rangoTxt + "  |  Fecha de emisión: " + LocalDateTime.now().format(dtf) + "  |  Registros: " + ventas.size());
            c2.setCellStyle(estilos.subtitulo);

            rowIdx++; // Fila en blanco

            // 2. Encabezados de tabla
            String[] headers = {
                    "N° Ticket", "Fecha y Hora", "Vendedor / Cajero",
                    "Op. Gravada (S/)", "IGV 18% (S/)", "Total Venta (S/)",
                    "Estado", "Métodos de Pago"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(estilos.cabecera);
            }

            // 3. Filas de Datos
            BigDecimal sumSubtotal = BigDecimal.ZERO;
            BigDecimal sumIgv = BigDecimal.ZERO;
            BigDecimal sumTotal = BigDecimal.ZERO;

            for (Venta v : ventas) {
                BigDecimal tot = v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO;
                BigDecimal sub = v.getSubtotal() != null && v.getSubtotal().compareTo(BigDecimal.ZERO) > 0 ?
                        v.getSubtotal() : tot.divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
                BigDecimal igv = v.getIgv() != null && v.getIgv().compareTo(BigDecimal.ZERO) > 0 ?
                        v.getIgv() : tot.subtract(sub);

                sumSubtotal = sumSubtotal.add(sub);
                sumIgv = sumIgv.add(igv);
                sumTotal = sumTotal.add(tot);

                String pagosStr = "";
                if (v.getPagos() != null && org.hibernate.Hibernate.isInitialized(v.getPagos()) && !v.getPagos().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < v.getPagos().size(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(v.getPagos().get(i).getMetodoPago()).append(" (S/ ").append(v.getPagos().get(i).getMonto()).append(")");
                    }
                    pagosStr = sb.toString();
                }

                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(19);

                Cell cTkt = row.createCell(0);
                cTkt.setCellValue(v.getNumeroTicket() != null ? v.getNumeroTicket() : "");
                cTkt.setCellStyle(estilos.centrado);

                Cell cFec = row.createCell(1);
                cFec.setCellValue(v.getFechaVenta() != null ? v.getFechaVenta().format(dtf) : "");
                cFec.setCellStyle(estilos.centrado);

                Cell cCaj = row.createCell(2);
                cCaj.setCellValue(v.getUsuario() != null ? v.getUsuario().getNombreCompleto() : "N/A");
                cCaj.setCellStyle(estilos.texto);

                Cell cSub = row.createCell(3);
                cSub.setCellValue(sub.doubleValue());
                cSub.setCellStyle(estilos.moneda);

                Cell cIgv = row.createCell(4);
                cIgv.setCellValue(igv.doubleValue());
                cIgv.setCellStyle(estilos.moneda);

                Cell cTot = row.createCell(5);
                cTot.setCellValue(tot.doubleValue());
                cTot.setCellStyle(estilos.moneda);

                Cell cEst = row.createCell(6);
                cEst.setCellValue(v.getEstado() != null ? v.getEstado().name() : "COMPLETADA");
                cEst.setCellStyle(estilos.centrado);

                Cell cPag = row.createCell(7);
                cPag.setCellValue(pagosStr);
                cPag.setCellStyle(estilos.texto);
            }

            // 4. Fila de Totales
            Row totRow = sheet.createRow(rowIdx++);
            totRow.setHeightInPoints(22);
            Cell cTotLbl = totRow.createCell(0);
            cTotLbl.setCellValue("TOTALES (" + ventas.size() + " Ventas)");
            cTotLbl.setCellStyle(estilos.totalTexto);

            totRow.createCell(1).setCellStyle(estilos.totalTexto);
            totRow.createCell(2).setCellStyle(estilos.totalTexto);

            Cell cTotSub = totRow.createCell(3);
            cTotSub.setCellValue(sumSubtotal.doubleValue());
            cTotSub.setCellStyle(estilos.totalMoneda);

            Cell cTotIgv = totRow.createCell(4);
            cTotIgv.setCellValue(sumIgv.doubleValue());
            cTotIgv.setCellStyle(estilos.totalMoneda);

            Cell cTotTot = totRow.createCell(5);
            cTotTot.setCellValue(sumTotal.doubleValue());
            cTotTot.setCellStyle(estilos.totalMoneda);

            totRow.createCell(6).setCellStyle(estilos.totalTexto);
            totRow.createCell(7).setCellStyle(estilos.totalTexto);

            // Autoajustar columnas con holgura
            ajustarColumnas(sheet, headers.length);

            // Escribir archivo
            try (FileOutputStream fos = new FileOutputStream(destino)) {
                wb.write(fos);
            }
        }
    }

    @Override
    public void exportarComprasExcel(File destino, List<Compra> compras, LocalDate inicio, LocalDate fin) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Reporte de Compras");
            sheet.setDisplayGridlines(true);

            EstilosExcel estilos = new EstilosExcel(wb);

            int rowIdx = 0;
            Row r0 = sheet.createRow(rowIdx++);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("BODEGA & MINIMARKET SGCIVORP - REPORTE DE COMPRAS Y ABASTECIMIENTO");
            c0.setCellStyle(estilos.titulo);

            Row r1 = sheet.createRow(rowIdx++);
            Cell c1 = r1.createCell(0);
            c1.setCellValue("RUC: 20601234567 | Dirección: Av. Principal 123, Lima | Auditoría de Proveedores");
            c1.setCellStyle(estilos.subtitulo);

            String rangoTxt = (inicio != null && fin != null) ? "Del " + inicio.format(df) + " al " + fin.format(df) : "Historial Completo";
            Row r2 = sheet.createRow(rowIdx++);
            Cell c2 = r2.createCell(0);
            c2.setCellValue("Período evaluado: " + rangoTxt + "  |  Fecha de emisión: " + LocalDateTime.now().format(dtf) + "  |  Facturas: " + compras.size());
            c2.setCellStyle(estilos.subtitulo);

            rowIdx++;

            String[] headers = {
                    "N° Factura / Comprobante", "Fecha Compra", "Proveedor", "RUC Proveedor",
                    "Op. Gravada (S/)", "IGV 18% (S/)", "Total Factura (S/)", "Estado", "Registrado Por",
                    "Unidades esperadas", "Unidades recibidas", "Unidades rechazadas", "Unidades ingresadas"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(estilos.cabecera);
            }

            BigDecimal sumSubtotal = BigDecimal.ZERO;
            BigDecimal sumIgv = BigDecimal.ZERO;
            BigDecimal sumTotal = BigDecimal.ZERO;
            long sumIngresadas = 0;

            for (Compra c : compras) {
                BigDecimal tot = c.getTotal() != null ? c.getTotal() : BigDecimal.ZERO;
                BigDecimal sub = c.getSubtotal() != null && c.getSubtotal().compareTo(BigDecimal.ZERO) > 0 ?
                        c.getSubtotal() : tot.divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);
                BigDecimal igv = c.getIgv() != null && c.getIgv().compareTo(BigDecimal.ZERO) > 0 ?
                        c.getIgv() : tot.subtract(sub);

                if (c.getEstadoCompra() == pe.edu.utp.Grupo06.model.enums.EstadoCompra.REGISTRADA) {
                    sumSubtotal = sumSubtotal.add(sub);
                    sumIgv = sumIgv.add(igv);
                    sumTotal = sumTotal.add(tot);
                }

                String provNombre = c.getProveedor() != null ? c.getProveedor().getRazonSocial() : "N/A";
                String provRuc = c.getProveedor() != null ? c.getProveedor().getRuc() : "-";
                String usuNombre = c.getUsuario() != null ? c.getUsuario().getNombreCompleto() : "N/A";

                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(19);

                Cell cComp = row.createCell(0);
                cComp.setCellValue(c.getNumeroComprobante() != null ? c.getNumeroComprobante() : "");
                cComp.setCellStyle(estilos.centrado);

                Cell cFec = row.createCell(1);
                cFec.setCellValue(c.getFechaCompra() != null ? c.getFechaCompra().format(dtf) : "");
                cFec.setCellStyle(estilos.centrado);

                Cell cProv = row.createCell(2);
                cProv.setCellValue(provNombre);
                cProv.setCellStyle(estilos.texto);

                Cell cRuc = row.createCell(3);
                cRuc.setCellValue(provRuc);
                cRuc.setCellStyle(estilos.centrado);

                Cell cSub = row.createCell(4);
                cSub.setCellValue(sub.doubleValue());
                cSub.setCellStyle(estilos.moneda);

                Cell cIgv = row.createCell(5);
                cIgv.setCellValue(igv.doubleValue());
                cIgv.setCellStyle(estilos.moneda);

                Cell cTot = row.createCell(6);
                cTot.setCellValue(tot.doubleValue());
                cTot.setCellStyle(estilos.moneda);

                Cell cEst = row.createCell(7);
                cEst.setCellValue(c.getEstadoCompra().name());
                cEst.setCellStyle(estilos.centrado);

                Cell cUsu = row.createCell(8);
                cUsu.setCellValue(usuNombre);
                cUsu.setCellStyle(estilos.texto);
                List<DetalleCompra> detalles = detalleCompraRepository.findByCompraId(c.getId());
                long esperadas = detalles.stream().mapToLong(d -> d.getUnidadesEsperadas()).sum();
                long recibidas = detalles.stream().mapToLong(d -> d.getUnidadesRecibidas()).sum();
                long rechazadas = detalles.stream().mapToLong(d -> d.getUnidadesRechazadas()).sum();
                long ingresadas = detalles.stream().mapToLong(d -> d.getUnidadesIngresadas()).sum();
                long[] cantidades = {esperadas, recibidas, rechazadas, ingresadas};
                for (int i = 0; i < cantidades.length; i++) {
                    Cell celda = row.createCell(9 + i);
                    celda.setCellValue(cantidades[i]);
                    celda.setCellStyle(estilos.centrado);
                }
                if (c.getEstadoCompra() == pe.edu.utp.Grupo06.model.enums.EstadoCompra.REGISTRADA)
                    sumIngresadas += ingresadas;
            }

            Row totRow = sheet.createRow(rowIdx++);
            totRow.setHeightInPoints(22);
            Cell cTotLbl = totRow.createCell(0);
            long vigentes = compras.stream().filter(c -> c.getEstadoCompra() == pe.edu.utp.Grupo06.model.enums.EstadoCompra.REGISTRADA).count();
            cTotLbl.setCellValue("TOTALES (" + vigentes + " Compras vigentes)");
            cTotLbl.setCellStyle(estilos.totalTexto);

            totRow.createCell(1).setCellStyle(estilos.totalTexto);
            totRow.createCell(2).setCellStyle(estilos.totalTexto);
            totRow.createCell(3).setCellStyle(estilos.totalTexto);

            Cell cTotSub = totRow.createCell(4);
            cTotSub.setCellValue(sumSubtotal.doubleValue());
            cTotSub.setCellStyle(estilos.totalMoneda);

            Cell cTotIgv = totRow.createCell(5);
            cTotIgv.setCellValue(sumIgv.doubleValue());
            cTotIgv.setCellStyle(estilos.totalMoneda);

            Cell cTotTot = totRow.createCell(6);
            cTotTot.setCellValue(sumTotal.doubleValue());
            cTotTot.setCellStyle(estilos.totalMoneda);

            totRow.createCell(7).setCellStyle(estilos.totalTexto);
            totRow.createCell(8).setCellStyle(estilos.totalTexto);
            for (int i = 9; i <= 11; i++) totRow.createCell(i).setCellStyle(estilos.totalTexto);
            Cell cIngresadas = totRow.createCell(12);
            cIngresadas.setCellValue(sumIngresadas);
            cIngresadas.setCellStyle(estilos.totalTexto);

            ajustarColumnas(sheet, headers.length);

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                wb.write(fos);
            }
        }
    }

    @Override
    public void exportarMermasExcel(File destino, List<Merma> mermas, LocalDate inicio, LocalDate fin) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Reporte de Mermas y Bajas");
            sheet.setDisplayGridlines(true);

            EstilosExcel estilos = new EstilosExcel(wb);

            int rowIdx = 0;
            Row r0 = sheet.createRow(rowIdx++);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("BODEGA & MINIMARKET SGCIVORP - CONTROL DE MERMAS Y BAJAS DE INVENTARIO");
            c0.setCellStyle(estilos.titulo);

            Row r1 = sheet.createRow(rowIdx++);
            Cell c1 = r1.createCell(0);
            c1.setCellValue("Auditoría de Pérdidas, Descomposición, Vencimientos y Bajas Operativas");
            c1.setCellStyle(estilos.subtitulo);

            String rangoTxt = (inicio != null && fin != null) ? "Del " + inicio.format(df) + " al " + fin.format(df) : "Historial Completo";
            Row r2 = sheet.createRow(rowIdx++);
            Cell c2 = r2.createCell(0);
            c2.setCellValue("Período: " + rangoTxt + "  |  Fecha de emisión: " + LocalDateTime.now().format(dtf) + "  |  Total registros: " + mermas.size());
            c2.setCellStyle(estilos.subtitulo);

            rowIdx++;

            String[] headers = {
                    "Fecha y Hora", "Código", "Producto", "Marca", "Motivo de Merma",
                    "F. Vencimiento", "Unidades Dadas de Baja", "Costo Unitario (S/)",
                    "Pérdida Total (S/)", "Registrado Por", "Observaciones"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(estilos.cabecera);
            }

            long sumUnidades = 0;
            BigDecimal sumPerdida = BigDecimal.ZERO;

            for (Merma m : mermas) {
                Producto p = m.getProducto();
                String cod = p != null ? p.getCodigo() : "-";
                String nom = p != null ? p.getNombre() : "-";
                String marca = (p != null && p.getMarca() != null && !p.getMarca().isBlank()) ? p.getMarca() : "Genérico";
                String motivo = m.getMotivo() != null ? m.getMotivo().getDescripcion() : "Otro";
                int cant = m.getCantidad() != null ? m.getCantidad() : 0;
                BigDecimal cu = m.getCostoUnitario() != null ? m.getCostoUnitario() : BigDecimal.ZERO;
                BigDecimal perd = m.getCostoTotalPerdida() != null ? m.getCostoTotalPerdida() : BigDecimal.ZERO;
                String usu = m.getUsuario() != null ? m.getUsuario().getNombreCompleto() : "N/A";
                String fVenc = m.getFechaVencimiento() != null ? m.getFechaVencimiento().format(df) : "-";
                String obs = m.getObservacion() != null ? m.getObservacion() : "";

                sumUnidades += cant;
                sumPerdida = sumPerdida.add(perd);

                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(19);

                Cell cFec = row.createCell(0);
                cFec.setCellValue(m.getFechaMerma() != null ? m.getFechaMerma().format(dtf) : "");
                cFec.setCellStyle(estilos.centrado);

                Cell cCod = row.createCell(1);
                cCod.setCellValue(cod);
                cCod.setCellStyle(estilos.centrado);

                Cell cNom = row.createCell(2);
                cNom.setCellValue(nom);
                cNom.setCellStyle(estilos.texto);

                Cell cMar = row.createCell(3);
                cMar.setCellValue(marca);
                cMar.setCellStyle(estilos.centrado);

                Cell cMot = row.createCell(4);
                cMot.setCellValue(motivo);
                cMot.setCellStyle(estilos.centrado);

                Cell cVenc = row.createCell(5);
                cVenc.setCellValue(fVenc);
                cVenc.setCellStyle(estilos.centrado);

                Cell cCant = row.createCell(6);
                cCant.setCellValue(cant);
                cCant.setCellStyle(estilos.entero);

                Cell cCu = row.createCell(7);
                cCu.setCellValue(cu.doubleValue());
                cCu.setCellStyle(estilos.moneda);

                Cell cPerd = row.createCell(8);
                cPerd.setCellValue(perd.doubleValue());
                cPerd.setCellStyle(estilos.moneda);

                Cell cUsu = row.createCell(9);
                cUsu.setCellValue(usu);
                cUsu.setCellStyle(estilos.texto);

                Cell cObs = row.createCell(10);
                cObs.setCellValue(obs);
                cObs.setCellStyle(estilos.texto);
            }

            Row totRow = sheet.createRow(rowIdx++);
            totRow.setHeightInPoints(22);
            Cell cTotLbl = totRow.createCell(0);
            cTotLbl.setCellValue("TOTALES (" + mermas.size() + " Registros)");
            cTotLbl.setCellStyle(estilos.totalTexto);

            for (int k = 1; k <= 5; k++) totRow.createCell(k).setCellStyle(estilos.totalTexto);

            Cell cTotCant = totRow.createCell(6);
            cTotCant.setCellValue(sumUnidades);
            cTotCant.setCellStyle(estilos.totalEntero);

            totRow.createCell(7).setCellStyle(estilos.totalTexto);

            Cell cTotPerd = totRow.createCell(8);
            cTotPerd.setCellValue(sumPerdida.doubleValue());
            cTotPerd.setCellStyle(estilos.totalMoneda);

            totRow.createCell(9).setCellStyle(estilos.totalTexto);
            totRow.createCell(10).setCellStyle(estilos.totalTexto);

            ajustarColumnas(sheet, headers.length);

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                wb.write(fos);
            }
        }
    }

    @Override
    public void exportarInventarioExcel(File destino, List<Producto> productos) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Inventario y Stock");
            sheet.setDisplayGridlines(true);

            EstilosExcel estilos = new EstilosExcel(wb);

            int rowIdx = 0;
            Row r0 = sheet.createRow(rowIdx++);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("BODEGA & MINIMARKET SGCIVORP - CATÁLOGO DE INVENTARIO Y STOCK");
            c0.setCellStyle(estilos.titulo);

            Row r1 = sheet.createRow(rowIdx++);
            Cell c1 = r1.createCell(0);
            c1.setCellValue("Control de Existencias Físicas y Valorización Total de Almacén");
            c1.setCellStyle(estilos.subtitulo);

            Row r2 = sheet.createRow(rowIdx++);
            Cell c2 = r2.createCell(0);
            c2.setCellValue("Fecha de emisión: " + LocalDateTime.now().format(dtf) + "  |  Total productos activos: " + productos.size());
            c2.setCellStyle(estilos.subtitulo);

            rowIdx++;

            String[] headers = {
                    "Código", "Producto", "Marca", "Categoría", "Unidad Medida",
                    "Stock Actual", "Stock Mínimo", "Estado Stock",
                    "P. Compra (S/)", "P. Venta (S/)", "Valorización Almacén (S/)", "Estado"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(estilos.cabecera);
            }

            long sumStock = 0;
            BigDecimal sumValorizacion = BigDecimal.ZERO;

            for (Producto p : productos) {
                String marca = (p.getMarca() != null && !p.getMarca().isBlank()) ? p.getMarca() : "Genérico";
                String cat = p.getCategoria() != null ? p.getCategoria().getNombre() : "-";
                String um = p.getUnidadMedida() != null ? p.getUnidadMedida().name() : "UNIDAD";

                int stock = p.getStockActual() != null ? p.getStockActual() : 0;
                int min = p.getStockMinimo() != null ? p.getStockMinimo() : 0;
                String estadoStock = stock <= min ? "REPONER (BAJO STOCK)" : "ÓPTIMO";

                BigDecimal pc = p.getPrecioCompra() != null ? p.getPrecioCompra() : BigDecimal.ZERO;
                BigDecimal pv = p.getPrecioVenta() != null ? p.getPrecioVenta() : BigDecimal.ZERO;
                BigDecimal valorizacion = pc.multiply(BigDecimal.valueOf(stock));

                sumStock += stock;
                sumValorizacion = sumValorizacion.add(valorizacion);

                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(19);

                Cell cCod = row.createCell(0);
                cCod.setCellValue(p.getCodigo() != null ? p.getCodigo() : "");
                cCod.setCellStyle(estilos.centrado);

                Cell cNom = row.createCell(1);
                cNom.setCellValue(p.getNombre() != null ? p.getNombre() : "");
                cNom.setCellStyle(estilos.texto);

                Cell cMar = row.createCell(2);
                cMar.setCellValue(marca);
                cMar.setCellStyle(estilos.centrado);

                Cell cCat = row.createCell(3);
                cCat.setCellValue(cat);
                cCat.setCellStyle(estilos.centrado);

                Cell cUm = row.createCell(4);
                cUm.setCellValue(um);
                cUm.setCellStyle(estilos.centrado);

                Cell cStk = row.createCell(5);
                cStk.setCellValue(stock);
                cStk.setCellStyle(estilos.entero);

                Cell cMin = row.createCell(6);
                cMin.setCellValue(min);
                cMin.setCellStyle(estilos.entero);

                Cell cEstStk = row.createCell(7);
                cEstStk.setCellValue(estadoStock);
                cEstStk.setCellStyle(stock <= min ? estilos.alertaBajoStock : estilos.centrado);

                Cell cPc = row.createCell(8);
                cPc.setCellValue(pc.doubleValue());
                cPc.setCellStyle(estilos.moneda);

                Cell cPv = row.createCell(9);
                cPv.setCellValue(pv.doubleValue());
                cPv.setCellStyle(estilos.moneda);

                Cell cVal = row.createCell(10);
                cVal.setCellValue(valorizacion.doubleValue());
                cVal.setCellStyle(estilos.moneda);

                Cell cEst = row.createCell(11);
                cEst.setCellValue(Boolean.TRUE.equals(p.getEstado()) ? "ACTIVO" : "INACTIVO");
                cEst.setCellStyle(estilos.centrado);
            }

            Row totRow = sheet.createRow(rowIdx++);
            totRow.setHeightInPoints(22);
            Cell cTotLbl = totRow.createCell(0);
            cTotLbl.setCellValue("TOTALES (" + productos.size() + " Productos)");
            cTotLbl.setCellStyle(estilos.totalTexto);

            for (int k = 1; k <= 4; k++) totRow.createCell(k).setCellStyle(estilos.totalTexto);

            Cell cTotStk = totRow.createCell(5);
            cTotStk.setCellValue(sumStock);
            cTotStk.setCellStyle(estilos.totalEntero);

            for (int k = 6; k <= 9; k++) totRow.createCell(k).setCellStyle(estilos.totalTexto);

            Cell cTotVal = totRow.createCell(10);
            cTotVal.setCellValue(sumValorizacion.doubleValue());
            cTotVal.setCellStyle(estilos.totalMoneda);

            totRow.createCell(11).setCellStyle(estilos.totalTexto);

            ajustarColumnas(sheet, headers.length);

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                wb.write(fos);
            }
        }
    }

    @Override
    public void exportarRotacionExcel(File destino, List<ProductoRotacionDTO> rotacion, String periodo, String categoria) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Ranking de Rotación");
            sheet.setDisplayGridlines(true);

            EstilosExcel estilos = new EstilosExcel(wb);

            int rowIdx = 0;
            Row r0 = sheet.createRow(rowIdx++);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("BODEGA & MINIMARKET SGCIVORP - RANKING DE ROTACIÓN DE PRODUCTOS");
            c0.setCellStyle(estilos.titulo);

            Row r1 = sheet.createRow(rowIdx++);
            Cell c1 = r1.createCell(0);
            c1.setCellValue("Métricas de Demanda Comercial y Productos Más Vendidos");
            c1.setCellStyle(estilos.subtitulo);

            Row r2 = sheet.createRow(rowIdx++);
            Cell c2 = r2.createCell(0);
            c2.setCellValue("Período evaluado: " + (periodo != null ? periodo : "Historial") +
                    "  |  Categoría: " + (categoria != null ? categoria : "Todas") +
                    "  |  Fecha de emisión: " + LocalDateTime.now().format(dtf) +
                    "  |  Total productos en ranking: " + rotacion.size());
            c2.setCellStyle(estilos.subtitulo);

            rowIdx++;

            String[] headers = {
                    "Puesto / Ranking", "Código", "Nombre del Producto",
                    "Unidades Vendidas", "Total Recaudado (S/)"
            };

            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeightInPoints(24);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(estilos.cabecera);
            }

            long sumUnidades = 0;
            BigDecimal sumRecaudado = BigDecimal.ZERO;
            int rank = 1;

            for (ProductoRotacionDTO r : rotacion) {
                long cant = r.getCantidadTotalVendida() != null ? r.getCantidadTotalVendida() : 0L;
                BigDecimal rec = r.getTotalRecaudado() != null ? r.getTotalRecaudado() : BigDecimal.ZERO;

                sumUnidades += cant;
                sumRecaudado = sumRecaudado.add(rec);

                Row row = sheet.createRow(rowIdx++);
                row.setHeightInPoints(19);

                Cell cRank = row.createCell(0);
                cRank.setCellValue(rank++);
                cRank.setCellStyle(estilos.centrado);

                Cell cCod = row.createCell(1);
                cCod.setCellValue(r.getCodigo() != null ? r.getCodigo() : "");
                cCod.setCellStyle(estilos.centrado);

                Cell cNom = row.createCell(2);
                cNom.setCellValue(r.getNombre() != null ? r.getNombre() : "");
                cNom.setCellStyle(estilos.texto);

                Cell cCant = row.createCell(3);
                cCant.setCellValue(cant);
                cCant.setCellStyle(estilos.entero);

                Cell cRec = row.createCell(4);
                cRec.setCellValue(rec.doubleValue());
                cRec.setCellStyle(estilos.moneda);
            }

            Row totRow = sheet.createRow(rowIdx++);
            totRow.setHeightInPoints(22);
            Cell cTotLbl = totRow.createCell(0);
            cTotLbl.setCellValue("TOTALES (" + rotacion.size() + " Productos)");
            cTotLbl.setCellStyle(estilos.totalTexto);

            totRow.createCell(1).setCellStyle(estilos.totalTexto);
            totRow.createCell(2).setCellStyle(estilos.totalTexto);

            Cell cTotCant = totRow.createCell(3);
            cTotCant.setCellValue(sumUnidades);
            cTotCant.setCellStyle(estilos.totalEntero);

            Cell cTotRec = totRow.createCell(4);
            cTotRec.setCellValue(sumRecaudado.doubleValue());
            cTotRec.setCellStyle(estilos.totalMoneda);

            ajustarColumnas(sheet, headers.length);

            try (FileOutputStream fos = new FileOutputStream(destino)) {
                wb.write(fos);
            }
        }
    }

    private void ajustarColumnas(Sheet sheet, int totalColumnas) {
        for (int i = 0; i < totalColumnas; i++) {
            sheet.autoSizeColumn(i);
            int curWidth = sheet.getColumnWidth(i);
            // Agregar holgura adicional para que ningún texto toque los bordes de la celda
            sheet.setColumnWidth(i, Math.max(curWidth + 1400, 3200));
        }
    }

    // Helper interno para paleta de estilos y formatos nativos de Excel
    private static class EstilosExcel {
        final CellStyle titulo;
        final CellStyle subtitulo;
        final CellStyle cabecera;
        final CellStyle texto;
        final CellStyle centrado;
        final CellStyle entero;
        final CellStyle moneda;
        final CellStyle alertaBajoStock;
        final CellStyle totalTexto;
        final CellStyle totalEntero;
        final CellStyle totalMoneda;

        EstilosExcel(XSSFWorkbook wb) {
            DataFormat df = wb.createDataFormat();
            short formatoMoneda = df.getFormat("\"S/ \"#,##0.00");
            short formatoEntero = df.getFormat("#,##0");

            // Colores corporativos
            byte[] azulRGB = new byte[]{(byte) 2, (byte) 132, (byte) 199};      // #0284C7
            byte[] grisFilaRGB = new byte[]{(byte) 241, (byte) 245, (byte) 249}; // #F1F5F9
            byte[] rojoAlertaRGB = new byte[]{(byte) 254, (byte) 226, (byte) 226}; // #FEE2E2
            byte[] bordeRGB = new byte[]{(byte) 203, (byte) 213, (byte) 225};   // #CBD5E1

            XSSFColor colorAzul = new XSSFColor(azulRGB, null);
            XSSFColor colorGris = new XSSFColor(grisFilaRGB, null);
            XSSFColor colorRojoAlerta = new XSSFColor(rojoAlertaRGB, null);
            XSSFColor colorBorde = new XSSFColor(bordeRGB, null);

            // Fuentes
            XSSFFont fTitulo = wb.createFont();
            fTitulo.setFontName("Segoe UI");
            fTitulo.setFontHeightInPoints((short) 14);
            fTitulo.setBold(true);

            XSSFFont fSubtitulo = wb.createFont();
            fSubtitulo.setFontName("Segoe UI");
            fSubtitulo.setFontHeightInPoints((short) 9.5);
            fSubtitulo.setItalic(true);

            XSSFFont fCabecera = wb.createFont();
            fCabecera.setFontName("Segoe UI");
            fCabecera.setFontHeightInPoints((short) 10);
            fCabecera.setBold(true);
            fCabecera.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));

            XSSFFont fDatos = wb.createFont();
            fDatos.setFontName("Segoe UI");
            fDatos.setFontHeightInPoints((short) 10);

            XSSFFont fAlerta = wb.createFont();
            fAlerta.setFontName("Segoe UI");
            fAlerta.setFontHeightInPoints((short) 9.5);
            fAlerta.setBold(true);
            fAlerta.setColor(new XSSFColor(new byte[]{(byte) 220, (byte) 38, (byte) 38}, null)); // #DC2626

            XSSFFont fTotal = wb.createFont();
            fTotal.setFontName("Segoe UI");
            fTotal.setFontHeightInPoints((short) 10);
            fTotal.setBold(true);

            // Estilos
            titulo = wb.createCellStyle();
            titulo.setFont(fTitulo);

            subtitulo = wb.createCellStyle();
            subtitulo.setFont(fSubtitulo);

            cabecera = wb.createCellStyle();
            ((XSSFCellStyle) cabecera).setFillForegroundColor(colorAzul);
            cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cabecera.setFont(fCabecera);
            cabecera.setAlignment(HorizontalAlignment.CENTER);
            cabecera.setVerticalAlignment(VerticalAlignment.CENTER);
            aplicarBordes(cabecera, colorBorde);

            texto = wb.createCellStyle();
            texto.setFont(fDatos);
            texto.setVerticalAlignment(VerticalAlignment.CENTER);
            aplicarBordes(texto, colorBorde);

            centrado = wb.createCellStyle();
            centrado.setFont(fDatos);
            centrado.setAlignment(HorizontalAlignment.CENTER);
            centrado.setVerticalAlignment(VerticalAlignment.CENTER);
            aplicarBordes(centrado, colorBorde);

            entero = wb.createCellStyle();
            entero.setFont(fDatos);
            entero.setDataFormat(formatoEntero);
            entero.setAlignment(HorizontalAlignment.RIGHT);
            entero.setVerticalAlignment(VerticalAlignment.CENTER);
            aplicarBordes(entero, colorBorde);

            moneda = wb.createCellStyle();
            moneda.setFont(fDatos);
            moneda.setDataFormat(formatoMoneda);
            moneda.setAlignment(HorizontalAlignment.RIGHT);
            moneda.setVerticalAlignment(VerticalAlignment.CENTER);
            aplicarBordes(moneda, colorBorde);

            alertaBajoStock = wb.createCellStyle();
            ((XSSFCellStyle) alertaBajoStock).setFillForegroundColor(colorRojoAlerta);
            alertaBajoStock.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            alertaBajoStock.setFont(fAlerta);
            alertaBajoStock.setAlignment(HorizontalAlignment.CENTER);
            alertaBajoStock.setVerticalAlignment(VerticalAlignment.CENTER);
            aplicarBordes(alertaBajoStock, colorBorde);

            totalTexto = wb.createCellStyle();
            ((XSSFCellStyle) totalTexto).setFillForegroundColor(colorGris);
            totalTexto.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalTexto.setFont(fTotal);
            totalTexto.setVerticalAlignment(VerticalAlignment.CENTER);
            totalTexto.setBorderTop(BorderStyle.THIN);
            totalTexto.setBorderBottom(BorderStyle.DOUBLE);

            totalEntero = wb.createCellStyle();
            ((XSSFCellStyle) totalEntero).setFillForegroundColor(colorGris);
            totalEntero.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalEntero.setFont(fTotal);
            totalEntero.setDataFormat(formatoEntero);
            totalEntero.setAlignment(HorizontalAlignment.RIGHT);
            totalEntero.setVerticalAlignment(VerticalAlignment.CENTER);
            totalEntero.setBorderTop(BorderStyle.THIN);
            totalEntero.setBorderBottom(BorderStyle.DOUBLE);

            totalMoneda = wb.createCellStyle();
            ((XSSFCellStyle) totalMoneda).setFillForegroundColor(colorGris);
            totalMoneda.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalMoneda.setFont(fTotal);
            totalMoneda.setDataFormat(formatoMoneda);
            totalMoneda.setAlignment(HorizontalAlignment.RIGHT);
            totalMoneda.setVerticalAlignment(VerticalAlignment.CENTER);
            totalMoneda.setBorderTop(BorderStyle.THIN);
            totalMoneda.setBorderBottom(BorderStyle.DOUBLE);
        }

        private void aplicarBordes(CellStyle s, XSSFColor c) {
            s.setBorderTop(BorderStyle.THIN);
            s.setBorderBottom(BorderStyle.THIN);
            s.setBorderLeft(BorderStyle.THIN);
            s.setBorderRight(BorderStyle.THIN);
            if (s instanceof XSSFCellStyle xs) {
                xs.setTopBorderColor(c);
                xs.setBottomBorderColor(c);
                xs.setLeftBorderColor(c);
                xs.setRightBorderColor(c);
            }
        }
    }
}
