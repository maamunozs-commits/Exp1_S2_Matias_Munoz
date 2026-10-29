package cl.matiasmunoz.usuarios.controlador;

import cl.matiasmunoz.usuarios.dto.DatosUsuarios.*;
import cl.matiasmunoz.usuarios.servicio.ServicioUsuarios;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sesiones")
public class ControladorSesiones {
    private final ServicioUsuarios servicio;

    public ControladorSesiones(ServicioUsuarios servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public Sesion iniciarSesion(@Valid @RequestBody InicioSesion datos) {
        return servicio.iniciarSesion(datos);
    }
}
