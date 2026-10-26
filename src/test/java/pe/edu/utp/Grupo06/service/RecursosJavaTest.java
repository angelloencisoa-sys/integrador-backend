package pe.edu.utp.Grupo06.service;

import ch.qos.logback.classic.LoggerContext;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.service.impl.ExportacionServiceImpl;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RecursosJavaTest {
    @TempDir Path temporal;

    @Test void exportacionVaciaGeneraExcelValidoConCabeceraYTotales() throws Exception {
        var destino = temporal.resolve("inventario-vacio.xlsx").toFile();
        new ExportacionServiceImpl().exportarInventarioExcel(destino, List.of());
        try (var libro = new XSSFWorkbook(destino)) {
            var hoja = libro.getSheet("Inventario y Stock");
            assertNotNull(hoja);
            assertEquals("Código", hoja.getRow(4).getCell(0).getStringCellValue());
            assertEquals(0, hoja.getRow(5).getCell(5).getNumericCellValue());
        }
    }

    @Test void nombreConFormulaSeEscribeComoTextoYStockComoNumero() throws Exception {
        var producto = new Producto();
        producto.setCodigo("DEMO-POI");
        producto.setNombre("=HYPERLINK(\"https://example.com\",\"prueba\")");
        producto.setStockActual(7);
        producto.setStockMinimo(2);
        producto.setPrecioCompra(new BigDecimal("2.50"));
        producto.setPrecioVenta(new BigDecimal("4.00"));
        var destino = temporal.resolve("inventario-seguro.xlsx").toFile();
        new ExportacionServiceImpl().exportarInventarioExcel(destino, List.of(producto));
        try (var libro = new XSSFWorkbook(destino)) {
            var fila = libro.getSheetAt(0).getRow(5);
            assertEquals(CellType.STRING, fila.getCell(1).getCellType());
            assertEquals(producto.getNombre(), fila.getCell(1).getStringCellValue());
            assertEquals(CellType.NUMERIC, fila.getCell(5).getCellType());
            assertEquals(7, fila.getCell(5).getNumericCellValue());
            assertEquals(17.50, fila.getCell(10).getNumericCellValue());
        }
    }

    @Test void logbackEsElMotorActivoDetrasDeSlf4j() {
        assertInstanceOf(LoggerContext.class, LoggerFactory.getILoggerFactory());
    }
}
