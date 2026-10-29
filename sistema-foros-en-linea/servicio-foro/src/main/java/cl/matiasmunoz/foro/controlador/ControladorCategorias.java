package cl.matiasmunoz.foro.controlador;

import cl.matiasmunoz.foro.dto.DatosForo.*;
import cl.matiasmunoz.foro.servicio.ServicioForo;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class ControladorCategorias {
    private final ServicioForo servicio;

    public ControladorCategorias(ServicioForo servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<RespuestaCategoria> listar() {
        return servicio.listarCategorias();
    }

    @GetMapping("/{id}")
    public RespuestaCategoria obtener(@PathVariable Long id) {
        return servicio.obtenerCategoria(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MODERADOR')")
    public ResponseEntity<RespuestaCategoria> crear(@Valid @RequestBody DatosCategoria datos) {
        var categoria = servicio.crearCategoria(datos);
        return ResponseEntity.created(URI.create("/api/categorias/" + categoria.id()))
                .body(categoria);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MODERADOR')")
    public RespuestaCategoria modificar(
            @PathVariable Long id, @Valid @RequestBody DatosCategoria datos) {
        return servicio.modificarCategoria(id, datos);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MODERADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}
