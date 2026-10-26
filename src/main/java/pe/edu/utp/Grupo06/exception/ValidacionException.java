package pe.edu.utp.Grupo06.exception;

import java.util.List;

/**
 * Se lanza cuando una entidad no cumple sus restricciones de Bean Validation
 * (@NotBlank, @Positive, @Email, etc.) antes de ser persistida.
 *
 * Se agrupan todos los errores en un solo mensaje para que el front
 * (por ahora, futuro Controller REST) pueda mostrarlos todos de una vez.
 */
public class ValidacionException extends RuntimeException {

    private final List<String> errores;

    public ValidacionException(List<String> errores) {
        super("Errores de validación: " + String.join(" | ", errores));
        this.errores = errores;
    }

    public List<String> getErrores() {
        return errores;
    }
}
