package pe.edu.utp.Grupo06.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "proveedores")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(max = 20, message = "El RUC no puede superar los 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 150, message = "La razón social no puede superar los 150 caracteres")
    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    @Column(length = 20)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    @Column(length = 100)
    private String correo;

    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    @Column(length = 200)
    private String direccion;

    @Column(nullable = false)
    private Boolean estado = true;
}
