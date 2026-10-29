package cl.matiasmunoz.foro.modelo;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
@Getter
@Setter
@NoArgsConstructor
public class Comentario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String contenido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tema_id", nullable = false)
    private Tema tema;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "autor_nombre", nullable = false, length = 100)
    private String autorNombre;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void registrarFecha() {
        fechaCreacion = LocalDateTime.now(java.time.Clock.systemUTC());
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    void actualizarFecha() {
        fechaActualizacion = LocalDateTime.now(java.time.Clock.systemUTC());
    }
}
