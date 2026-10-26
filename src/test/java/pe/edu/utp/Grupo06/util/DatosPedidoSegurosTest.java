package pe.edu.utp.Grupo06.util;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class DatosPedidoSegurosTest {
    @Test void huellaCoincideConVectorSha256Conocido() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                DatosPedidoSeguros.huellaSha256("abc".getBytes(StandardCharsets.UTF_8)));
    }

    @Test void contenidoIgualProduceMismaHuellaYCambioLaModifica() {
        byte[] imagen = {1, 3, 5, 7};
        assertEquals(DatosPedidoSeguros.huellaSha256(imagen), DatosPedidoSeguros.huellaSha256(imagen.clone()));
        assertNotEquals(DatosPedidoSeguros.huellaSha256(imagen), DatosPedidoSeguros.huellaSha256(new byte[]{1, 3, 5, 8}));
    }

    @Test void huellaRechazaContenidoNulo() {
        assertThrows(IllegalArgumentException.class, () -> DatosPedidoSeguros.huellaSha256(null));
    }

    @Test void referenciaSeRecortaSinModificarElContenidoInterior() {
        assertEquals("DEMO 001", DatosPedidoSeguros.textoRequerido(" \tDEMO 001\r\n", 3, 100));
    }

    @Test void referenciaVaciaONulaNoPuedeSuperarLaValidacion() {
        for (String entrada : new String[]{null, "", "  \t\r\n", "ab"}) {
            assertThrows(IllegalArgumentException.class, () -> DatosPedidoSeguros.textoRequerido(entrada, 3, 100));
        }
    }

    @Test void limitesSeAplicanDespuesDeRecortar() {
        assertEquals("abc", DatosPedidoSeguros.textoRequerido(" abc ", 3, 3));
        assertThrows(IllegalArgumentException.class, () -> DatosPedidoSeguros.textoRequerido(" abcd ", 3, 3));
    }
}
