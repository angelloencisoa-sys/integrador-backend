package pe.edu.utp.Grupo06.dto.alerta;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtenderAlertaDTO {

    @Size(max = 255, message = "La observación no puede superar los 255 caracteres")
    private String observacion;
}
