package cl.matiasmunoz.foro;

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
@Sql("/datos-servicio-foro.sql")
class PruebasForo {
    @Autowired private MockMvc cliente;

    private RequestPostProcessor sesion(String id, String rol) {
        return jwt().jwt(
                        datos ->
                                datos.subject(id).claim("rol", rol).claim("nombre", "Matias Muñoz"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Test
    void consultarCincoCategoriasPublicas() throws Exception {
        cliente.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void buscarPorTextoYCategoria() throws Exception {
        cliente.perform(get("/api/temas").param("texto", "Oracle").param("categoriaId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].categoriaId").value(2));
    }

    @Test
    void validarLimiteDePaginacion() throws Exception {
        cliente.perform(get("/api/temas").param("tamano", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exigirSesionParaPublicar() throws Exception {
        cliente.perform(
                        post("/api/temas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"titulo\":\"Nuevo tema\",\"contenido\":\"Contenido del"
                                            + " tema\",\"categoriaId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicarTemaConAutorDeLaSesion() throws Exception {
        cliente.perform(
                        post("/api/temas")
                                .with(sesion("3", "USUARIO"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"titulo\":\"Nuevo tema\",\"contenido\":\"Contenido del"
                                            + " tema\",\"categoriaId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autorId").value(3));
    }

    @Test
    void impedirEdicionDeTemaAjeno() throws Exception {
        cliente.perform(
                        put("/api/temas/1")
                                .with(sesion("3", "USUARIO"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"titulo\":\"Cambio"
                                            + " ajeno\",\"contenido\":\"Contenido\",\"categoriaId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rechazarCategoriaInexistente() throws Exception {
        cliente.perform(
                        post("/api/temas")
                                .with(sesion("3", "USUARIO"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"titulo\":\"Nuevo"
                                            + " tema\",\"contenido\":\"Contenido\",\"categoriaId\":9999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reservarCategoriasParaRolesAutorizados() throws Exception {
        String contenido = "{\"nombre\":\"Redes\",\"descripcion\":\"Protocolos de comunicación\"}";
        cliente.perform(
                        post("/api/categorias")
                                .with(sesion("3", "USUARIO"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(contenido))
                .andExpect(status().isForbidden());
        cliente.perform(
                        post("/api/categorias")
                                .with(sesion("2", "MODERADOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(contenido))
                .andExpect(status().isCreated());
    }

    @Test
    void conservarCategoriasConTemas() throws Exception {
        cliente.perform(delete("/api/categorias/1").with(sesion("1", "ADMINISTRADOR")))
                .andExpect(status().isConflict());
    }

    @Test
    void asociarComentarioAlTema() throws Exception {
        cliente.perform(
                        post("/api/temas/1/comentarios")
                                .with(sesion("3", "USUARIO"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"contenido\":\"Comentario de evaluación\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.temaId").value(1))
                .andExpect(jsonPath("$.autorId").value(3));
    }

    @Test
    void impedirModificarComentarioDeOtroTema() throws Exception {
        cliente.perform(
                        put("/api/temas/2/comentarios/1")
                                .with(sesion("2", "MODERADOR"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"contenido\":\"Cambio\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarTemaJuntoConSusComentarios() throws Exception {
        cliente.perform(delete("/api/temas/1").with(sesion("1", "ADMINISTRADOR")))
                .andExpect(status().isNoContent());
        cliente.perform(get("/api/temas/1")).andExpect(status().isNotFound());
        cliente.perform(get("/api/temas/1/comentarios")).andExpect(status().isNotFound());
    }
}
