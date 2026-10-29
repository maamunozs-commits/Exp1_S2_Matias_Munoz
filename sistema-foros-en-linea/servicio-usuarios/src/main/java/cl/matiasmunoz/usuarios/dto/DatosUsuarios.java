package cl.matiasmunoz.usuarios.dto;

import cl.matiasmunoz.usuarios.modelo.Usuario;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public final class DatosUsuarios {
    private DatosUsuarios() {}

    public record Registro(
            @NotBlank(message = "El nombre es obligatorio")
                    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
                    String nombre,
            @NotBlank(message = "El correo es obligatorio")
                    @Email(message = "El correo debe ser válido")
                    @Size(max = 150, message = "El correo admite hasta 150 caracteres")
                    String correo,
            @NotBlank(message = "La contraseña es obligatoria")
                    @Size(
                            min = 10,
                            max = 64,
                            message = "La contraseña debe tener entre 10 y 64 caracteres")
                    String contrasena) {}

    public record Edicion(
            @NotBlank(message = "El nombre es obligatorio")
                    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
                    String nombre,
            @NotBlank(message = "El correo es obligatorio")
                    @Email(message = "El correo debe ser válido")
                    @Size(max = 150, message = "El correo admite hasta 150 caracteres")
                    String correo) {}

    public record CambioRol(@NotBlank(message = "El rol es obligatorio") String rol) {}

    public record InicioSesion(
            @NotBlank(message = "El correo es obligatorio")
                    @Email(message = "El correo debe ser válido")
                    String correo,
            @NotBlank(message = "La contraseña es obligatoria") String contrasena) {}

    public record Respuesta(
            Long id, String nombre, String correo, String rol, LocalDateTime fechaCreacion) {
        public static Respuesta desde(Usuario usuario) {
            return new Respuesta(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getCorreo(),
                    usuario.getRol().getNombre(),
                    usuario.getFechaCreacion());
        }
    }

    public record Sesion(String token, String tipo, long duracionSegundos, Respuesta usuario) {}
}
