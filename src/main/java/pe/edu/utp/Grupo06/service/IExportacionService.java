package pe.edu.utp.Grupo06.service;

import pe.edu.utp.Grupo06.dto.venta.ProductoRotacionDTO;
import pe.edu.utp.Grupo06.model.Compra;
import pe.edu.utp.Grupo06.model.Merma;
import pe.edu.utp.Grupo06.model.Producto;
import pe.edu.utp.Grupo06.model.Venta;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public interface IExportacionService {

    void exportarVentasExcel(File destino, List<Venta> ventas, LocalDate inicio, LocalDate fin) throws Exception;

    void exportarComprasExcel(File destino, List<Compra> compras, LocalDate inicio, LocalDate fin) throws Exception;

    void exportarMermasExcel(File destino, List<Merma> mermas, LocalDate inicio, LocalDate fin) throws Exception;

    void exportarInventarioExcel(File destino, List<Producto> productos) throws Exception;

    void exportarRotacionExcel(File destino, List<ProductoRotacionDTO> rotacion, String periodo, String categoria) throws Exception;

    default void exportarVentasCsv(File destino, List<Venta> ventas, LocalDate inicio, LocalDate fin) throws Exception {
        exportarVentasExcel(destino, ventas, inicio, fin);
    }

    default void exportarComprasCsv(File destino, List<Compra> compras, LocalDate inicio, LocalDate fin) throws Exception {
        exportarComprasExcel(destino, compras, inicio, fin);
    }

    default void exportarMermasCsv(File destino, List<Merma> mermas, LocalDate inicio, LocalDate fin) throws Exception {
        exportarMermasExcel(destino, mermas, inicio, fin);
    }

    default void exportarInventarioCsv(File destino, List<Producto> productos) throws Exception {
        exportarInventarioExcel(destino, productos);
    }

    default void exportarRotacionCsv(File destino, List<ProductoRotacionDTO> rotacion, String periodo, String categoria) throws Exception {
        exportarRotacionExcel(destino, rotacion, periodo, categoria);
    }
}
