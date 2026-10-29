package cl.matiasmunoz.foro.controlador;

import cl.matiasmunoz.foro.dto.DatosForo.*;
import cl.matiasmunoz.foro.servicio.ServicioForo;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/temas")
public class ControladorTemas {
    private final ServicioForo servicio;

    public ControladorTemas(ServicioForo servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public PaginaTemas buscar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {
        return servicio.buscarTemas(texto, categoriaId, pagina, tamano);
    }

    @GetMapping("/{id}")
    public RespuestaTema obtener(@PathVariable Long id) {
        return servicio.obtenerTema(id);
    }

    @PostMapping
    public ResponseEntity<RespuestaTema> publicar(
            @Valid @RequestBody DatosTema datos, @AuthenticationPrincipal Jwt sesion) {
        var tema = servicio.publicar(datos, sesion);
        return ResponseEntity.created(URI.create("/api/temas/" + tema.id())).body(tema);
    }

    @PutMapping("/{id}")
    public RespuestaTema modificar(
            @PathVariable Long id,
            @Valid @RequestBody DatosTema datos,
            @AuthenticationPrincipal Jwt sesion) {
        return servicio.modificarTema(id, datos, sesion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id, @AuthenticationPrincipal Jwt sesion) {
        servicio.eliminarTema(id, sesion);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{temaId}/comentarios")
    public List<RespuestaComentario> listarComentarios(@PathVariable Long temaId) {
        return servicio.listarComentarios(temaId);
    }

    @PostMapping("/{temaId}/comentarios")
    public ResponseEntity<RespuestaComentario> comentar(
            @PathVariable Long temaId,
            @Valid @RequestBody DatosComentario datos,
            @AuthenticationPrincipal Jwt sesion) {
        var comentario = servicio.comentar(temaId, datos, sesion);
        return ResponseEntity.created(
                        URI.create("/api/temas/" + temaId + "/comentarios/" + comentario.id()))
                .body(comentario);
    }

    @PutMapping("/{temaId}/comentarios/{id}")
    public RespuestaComentario modificarComentario(
            @PathVariable Long temaId,
            @PathVariable Long id,
            @Valid @RequestBody DatosComentario datos,
            @AuthenticationPrincipal Jwt sesion) {
        return servicio.modificarComentario(temaId, id, datos, sesion);
    }

    @DeleteMapping("/{temaId}/comentarios/{id}")
    public ResponseEntity<Void> eliminarComentario(
            @PathVariable Long temaId, @PathVariable Long id, @AuthenticationPrincipal Jwt sesion) {
        servicio.eliminarComentario(temaId, id, sesion);
        return ResponseEntity.noContent().build();
    }
}
