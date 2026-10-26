package pe.edu.utp.Grupo06.model.enums;

public enum TipoMovimiento {
    ENTRADA,      // Ingreso por compra a proveedor o ajuste inicial
    SALIDA,       // Salida por venta realizada
    AJUSTE,       // Corrección manual de inventario
    REPOSICION,   // Ingreso por atención de reposición
    MERMA,        // Pérdida por vencimiento, rotura o deterioro
    ENTRADA_COMPRA,
    ANULACION_COMPRA
}
