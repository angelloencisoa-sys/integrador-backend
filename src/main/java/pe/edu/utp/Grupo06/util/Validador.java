package pe.edu.utp.Grupo06.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.utp.Grupo06.exception.ValidacionException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Punto único para validar entidades con las anotaciones de Bean Validation
 * (@NotBlank, @Positive, @Email, etc.) antes de guardarlas.
 *
 * Se usa manualmente en los ServiceImpl porque todavía no existe una capa
 * @RestController con @Valid que dispare la validación automáticamente.
 * Cuando se agregue esa capa, esta clase se puede seguir usando igual,
 * o delegar en @Valid a nivel de DTO — no se pierde nada.
 */
@Component
public class Validador {

    @Autowired
    private Validator validator;

    public <T> void validar(T objeto) {
        Set<ConstraintViolation<T>> violaciones = validator.validate(objeto);
        if (!violaciones.isEmpty()) {
            List<String> errores = violaciones.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.toList());
            throw new ValidacionException(errores);
        }
    }
}
