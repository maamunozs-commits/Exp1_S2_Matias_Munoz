package cl.matiasmunoz.usuarios;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("pruebas")
@Transactional
@Sql("/datos-servicio-usuarios.sql")
class PruebasUsuarios {
    @Autowired private MockMvc cliente;

    private RequestPostProcessor sesion(String id, String rol) {
        return jwt().jwt(
                        datos ->
                                datos.subject(id).claim("rol", rol).claim("nombre", "Matias Muñoz"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Test
    void registrarCuentaSinExponerContrasena() throws Exception {
        cliente.perform(
                        post("/api/usuarios/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"nombre\":\"Usuario de"
                                            + " evaluación\",\"correo\":\"evaluacion@foro.cl\",\"contrasena\":\"Evaluacion2026!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.contrasenaHash").doesNotExist())
                .andExpect(header().exists("Location"));
    }

    @Test
    void rechazarCorreoDuplicado() throws Exception {
        cliente.perform(
                        post("/api/usuarios/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"nombre\":\"Matias\",\"correo\":\"MATIAS.MUNOZ@foro.cl\",\"contrasena\":\"Evaluacion2026!\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void validarDatosDeRegistro() throws Exception {
        cliente.perform(
                        post("/api/usuarios/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"nombre\":\"\",\"correo\":\"incorrecto\",\"contrasena\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles").isArray());
    }

    @Test
    void iniciarSesionConContrasenaCifrada() throws Exception {
        cliente.perform(
                        post("/api/sesiones")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"correo\":\"matias.munoz@foro.cl\",\"contrasena\":\"ForoAcademico2026!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.duracionSegundos").value(900));
    }

    @Test
    void rechazarCredencialesIncorrectas() throws Exception {
        cliente.perform(
                        post("/api/sesiones")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"correo\":\"matias.munoz@foro.cl\",\"contrasena\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reservarListadoParaAdministrador() throws Exception {
        cliente.perform(get("/api/usuarios").with(sesion("3", "USUARIO")))
                .andExpect(status().isForbidden());
        cliente.perform(get("/api/usuarios").with(sesion("1", "ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void impedirEdicionDeCuentaAjena() throws Exception {
        cliente.perform(
                        put("/api/usuarios/1")
                                .with(sesion("3", "USUARIO"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"nombre\":\"Nombre"
                                            + " actualizado\",\"correo\":\"actualizado@foro.cl\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void modificarCuentaPropia() throws Exception {
        cliente.perform(
                        put("/api/usuarios/3")
                                .with(sesion("3", "USUARIO"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"nombre\":\"Diego Rojas"
                                            + " actualizado\",\"correo\":\"diego.actualizado@foro.cl\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Diego Rojas actualizado"));
    }

    @Test
    void protegerUltimoAdministrador() throws Exception {
        cliente.perform(delete("/api/usuarios/1").with(sesion("1", "ADMINISTRADOR")))
                .andExpect(status().isConflict());
        cliente.perform(
                        put("/api/usuarios/1/rol")
                                .with(sesion("1", "ADMINISTRADOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"rol\":\"USUARIO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void eliminarCuentaYComprobarAusencia() throws Exception {
        cliente.perform(delete("/api/usuarios/3").with(sesion("3", "USUARIO")))
                .andExpect(status().isNoContent());
        cliente.perform(get("/api/usuarios/3").with(sesion("1", "ADMINISTRADOR")))
                .andExpect(status().isNotFound());
    }
}
