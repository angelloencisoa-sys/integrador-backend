package pe.edu.utp.Grupo06.util;

import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;

/**
 * Normaliza referencias y obtiene huellas compatibles con los pedidos existentes.
 * Una huella identifica contenido repetido; no acredita una transferencia bancaria.
 * Esta clase no registra referencias, imagenes ni credenciales en los logs.
 */
public final class DatosPedidoSeguros {
    private DatosPedidoSeguros() { }

    /**
     * Obtiene el SHA-256 hexadecimal del contenido, usando Google Guava.
     * @param contenido bytes cuya identidad se quiere comparar
     * @return cadena hexadecimal minuscula de 64 caracteres
     * @throws IllegalArgumentException si el contenido es nulo
     */
    public static String huellaSha256(byte[] contenido) {
        if (contenido == null) throw new IllegalArgumentException("Falta el contenido");
        return Hashing.sha256().hashBytes(contenido).toString();
    }

    /**
     * Recorta espacios exteriores y valida la longitud del motivo o referencia.
     * Apache Commons evita que una entrada nula cause una excepcion imprevista.
     * @param entrada texto recibido del cliente o del personal
     * @param minimo longitud minima despues de recortar
     * @param maximo longitud maxima despues de recortar
     * @return texto normalizado sin alterar sus caracteres interiores
     * @throws IllegalArgumentException si no se cumple la longitud requerida
     */
    public static String textoRequerido(String entrada, int minimo, int maximo) {
        String normalizado = StringUtils.trimToEmpty(entrada);
        if (normalizado.length() < minimo || normalizado.length() > maximo) {
            throw new IllegalArgumentException("Complete el motivo o referencia (" + minimo + " a " + maximo + " caracteres)");
        }
        return normalizado;
    }
}
