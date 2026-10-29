package cl.matiasmunoz.foro.dto;

import cl.matiasmunoz.foro.modelo.*;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public final class DatosForo {
    private DatosForo() {}

    public record DatosCategoria(
            @NotBlank(message = "El nombre es obligatorio")
                    @Size(max = 80, message = "El nombre admite hasta 80 caracteres")
                    String nombre,
            @NotBlank(message = "La descripción es obligatoria")
                    @Size(max = 300, message = "La descripción admite hasta 300 caracteres")
                    String descripcion) {}

    public record DatosTema(
            @NotBlank(message = "El título es obligatorio")
                    @Size(max = 180, message = "El título admite hasta 180 caracteres")
                    String titulo,
            @NotBlank(message = "El contenido es obligatorio")
                    @Size(max = 4000, message = "El contenido admite hasta 4000 caracteres")
                    String contenido,
            @NotNull(message = "La categoría es obligatoria")
                    @Positive(message = "La categoría debe ser positiva")
                    Long categoriaId) {}

    public record DatosComentario(
            @NotBlank(message = "El contenido es obligatorio")
                    @Size(max = 2000, message = "El contenido admite hasta 2000 caracteres")
                    String contenido) {}

    public record RespuestaCategoria(Long id, String nombre, String descripcion) {
        public static RespuestaCategoria desde(Categoria categoria) {
            return new RespuestaCategoria(
                    categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
        }
    }

    public record RespuestaTema(
            Long id,
            String titulo,
            String contenido,
            Long categoriaId,
            String categoria,
            Long autorId,
            String autorNombre,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion) {
        public static RespuestaTema desde(Tema tema) {
            return new RespuestaTema(
                    tema.getId(),
                    tema.getTitulo(),
                    tema.getContenido(),
                    tema.getCategoria().getId(),
                    tema.getCategoria().getNombre(),
                    tema.getAutorId(),
                    tema.getAutorNombre(),
                    tema.getFechaCreacion(),
                    tema.getFechaActualizacion());
        }
    }

    public record RespuestaComentario(
            Long id,
            Long temaId,
            String contenido,
            Long autorId,
            String autorNombre,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion) {
        public static RespuestaComentario desde(Comentario comentario) {
            return new RespuestaComentario(
                    comentario.getId(),
                    comentario.getTema().getId(),
                    comentario.getContenido(),
                    comentario.getAutorId(),
                    comentario.getAutorNombre(),
                    comentario.getFechaCreacion(),
                    comentario.getFechaActualizacion());
        }
    }

    public record PaginaTemas(
            List<RespuestaTema> contenido,
            int pagina,
            int tamano,
            long totalElementos,
            int totalPaginas) {}
}
