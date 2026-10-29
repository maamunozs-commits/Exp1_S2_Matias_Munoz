package cl.matiasmunoz.usuarios.controlador;

import cl.matiasmunoz.usuarios.dto.DatosUsuarios.*;
import cl.matiasmunoz.usuarios.servicio.ServicioUsuarios;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class ControladorUsuarios {
    private final ServicioUsuarios servicio;

    public ControladorUsuarios(ServicioUsuarios servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/registro")
    public ResponseEntity<Respuesta> registrar(@Valid @RequestBody Registro datos) {
        var usuario = servicio.registrar(datos);
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.id())).body(usuario);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Respuesta> listar() {
        return servicio.listar();
    }

    @GetMapping("/actual")
    public Respuesta obtenerActual(@AuthenticationPrincipal Jwt sesion) {
        return servicio.obtener(Long.valueOf(sesion.getSubject()), sesion);
    }

    @GetMapping("/{id}")
    public Respuesta obtener(@PathVariable Long id, @AuthenticationPrincipal Jwt sesion) {
        return servicio.obtener(id, sesion);
    }

    @PutMapping("/{id}")
    public Respuesta modificar(
            @PathVariable Long id,
            @Valid @RequestBody Edicion datos,
            @AuthenticationPrincipal Jwt sesion) {
        return servicio.modificar(id, datos, sesion);
    }

    @PutMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Respuesta cambiarRol(@PathVariable Long id, @Valid @RequestBody CambioRol datos) {
        return servicio.cambiarRol(id, datos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id, @AuthenticationPrincipal Jwt sesion) {
        servicio.eliminar(id, sesion);
        return ResponseEntity.noContent().build();
    }
}
