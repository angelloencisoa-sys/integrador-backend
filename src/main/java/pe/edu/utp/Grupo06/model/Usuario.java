package pe.edu.utp.Grupo06.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // NOTA: aquí siempre se guarda el hash (BCrypt), nunca la contraseña en texto plano.
    // La validación de longitud mínima de la contraseña en texto plano se hace
    // ANTES de encriptarla, en UsuarioServiceImpl, no sobre este campo.
    @NotBlank(message = "La contraseña es obligatoria")
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(nullable = false, length = 150)
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100, message = "El nombre completo no puede superar los 100 caracteres")
    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar los 100 caracteres")
    @Column(length = 100)
    private String email;

    @Size(max = 15, message = "El teléfono no puede superar los 15 caracteres")
    @Column(length = 15)
    private String telefono;

    @Column(nullable = false)
    private Boolean activo = true;

    @NotNull(message = "El rol es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
}
