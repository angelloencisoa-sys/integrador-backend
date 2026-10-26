package pe.edu.utp.Grupo06.model;

import jakarta.persistence.*;
import lombok.*;
import pe.edu.utp.Grupo06.model.enums.RolNombre;

@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private RolNombre nombre;

    @Column(length = 150)
    private String descripcion;
}
