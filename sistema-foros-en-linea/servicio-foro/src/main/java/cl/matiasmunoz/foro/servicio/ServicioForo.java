package cl.matiasmunoz.foro.servicio;

import cl.matiasmunoz.comun.ExcepcionNegocio;
import cl.matiasmunoz.foro.dto.DatosForo.*;
import cl.matiasmunoz.foro.modelo.*;
import cl.matiasmunoz.foro.repositorio.*;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ServicioForo {
    private final RepositorioCategorias categorias;
    private final RepositorioTemas temas;
    private final RepositorioComentarios comentarios;

    public ServicioForo(
            RepositorioCategorias categorias,
            RepositorioTemas temas,
            RepositorioComentarios comentarios) {
        this.categorias = categorias;
        this.temas = temas;
        this.comentarios = comentarios;
    }

    @Transactional(readOnly = true)
    public List<RespuestaCategoria> listarCategorias() {
        return categorias.findAll(Sort.by("id")).stream().map(RespuestaCategoria::desde).toList();
    }

    @Transactional(readOnly = true)
    public RespuestaCategoria obtenerCategoria(Long id) {
        return RespuestaCategoria.desde(buscarCategoria(id));
    }

    public RespuestaCategoria crearCategoria(DatosCategoria datos) {
        if (categorias.existeNombre(datos.nombre().strip())) {
            throw new ExcepcionNegocio(HttpStatus.CONFLICT, "La categoría ya existe");
        }
        var categoria = new Categoria();
        categoria.setNombre(datos.nombre().strip());
        categoria.setDescripcion(datos.descripcion().strip());
        return RespuestaCategoria.desde(categorias.saveAndFlush(categoria));
    }

    public RespuestaCategoria modificarCategoria(Long id, DatosCategoria datos) {
        var categoria = buscarCategoria(id);
        if (categorias.existeNombreEnOtraCategoria(datos.nombre().strip(), id)) {
            throw new ExcepcionNegocio(HttpStatus.CONFLICT, "La categoría ya existe");
        }
        categoria.setNombre(datos.nombre().strip());
        categoria.setDescripcion(datos.descripcion().strip());
        return RespuestaCategoria.desde(categorias.saveAndFlush(categoria));
    }

    public void eliminarCategoria(Long id) {
        var categoria = buscarCategoria(id);
        if (temas.contieneCategoria(id)) {
            throw new ExcepcionNegocio(
                    HttpStatus.CONFLICT, "La categoría contiene temas asociados");
        }
        if (categorias.count() <= 5) {
            throw new ExcepcionNegocio(
                    HttpStatus.CONFLICT, "El foro debe conservar al menos cinco categorías");
        }
        categorias.delete(categoria);
    }

    @Transactional(readOnly = true)
    public PaginaTemas buscarTemas(String texto, Long categoriaId, int pagina, int tamano) {
        if (pagina < 0 || tamano < 1 || tamano > 50) {
            throw new ExcepcionNegocio(
                    HttpStatus.BAD_REQUEST,
                    "La página debe ser positiva o cero y el tamaño debe estar entre 1 y 50");
        }
        if (categoriaId != null) {
            buscarCategoria(categoriaId);
        }
        Specification<Tema> filtro =
                (raiz, consulta, constructor) -> {
                    var condiciones = new ArrayList<Predicate>();
                    if (categoriaId != null) {
                        condiciones.add(
                                constructor.equal(raiz.get("categoria").get("id"), categoriaId));
                    }
                    if (texto != null && !texto.isBlank()) {
                        String patron =
                                "%"
                                        + texto.strip()
                                                .toLowerCase(Locale.ROOT)
                                                .replace("!", "!!")
                                                .replace("%", "!%")
                                                .replace("_", "!_")
                                        + "%";
                        condiciones.add(
                                constructor.or(
                                        constructor.like(
                                                constructor.lower(raiz.get("titulo")), patron, '!'),
                                        constructor.like(
                                                constructor.lower(raiz.get("contenido")),
                                                patron,
                                                '!')));
                    }
                    return constructor.and(condiciones.toArray(Predicate[]::new));
                };
        var resultado =
                temas.findAll(
                        filtro,
                        PageRequest.of(
                                pagina,
                                tamano,
                                Sort.by(Sort.Direction.DESC, "fechaCreacion", "id")));
        return new PaginaTemas(
                resultado.getContent().stream().map(RespuestaTema::desde).toList(),
                pagina,
                tamano,
                resultado.getTotalElements(),
                resultado.getTotalPages());
    }

    @Transactional(readOnly = true)
    public RespuestaTema obtenerTema(Long id) {
        return RespuestaTema.desde(buscarTema(id));
    }

    public RespuestaTema publicar(DatosTema datos, Jwt sesion) {
        var tema = new Tema();
        tema.setAutorId(Long.valueOf(sesion.getSubject()));
        tema.setAutorNombre(sesion.getClaimAsString("nombre"));
        asignarDatos(tema, datos);
        return RespuestaTema.desde(temas.saveAndFlush(tema));
    }

    public RespuestaTema modificarTema(Long id, DatosTema datos, Jwt sesion) {
        var tema = buscarTema(id);
        comprobarAutor(tema.getAutorId(), sesion);
        asignarDatos(tema, datos);
        return RespuestaTema.desde(temas.saveAndFlush(tema));
    }

    public void eliminarTema(Long id, Jwt sesion) {
        var tema = buscarTema(id);
        comprobarAutor(tema.getAutorId(), sesion);
        comentarios.eliminarPorTema(id);
        comentarios.flush();
        temas.delete(tema);
    }

    @Transactional(readOnly = true)
    public List<RespuestaComentario> listarComentarios(Long temaId) {
        buscarTema(temaId);
        return comentarios.listarPorTema(temaId).stream().map(RespuestaComentario::desde).toList();
    }

    public RespuestaComentario comentar(Long temaId, DatosComentario datos, Jwt sesion) {
        validarTamano(datos.contenido(), 2000);
        var comentario = new Comentario();
        comentario.setTema(buscarTema(temaId));
        comentario.setContenido(datos.contenido().strip());
        comentario.setAutorId(Long.valueOf(sesion.getSubject()));
        comentario.setAutorNombre(sesion.getClaimAsString("nombre"));
        return RespuestaComentario.desde(comentarios.saveAndFlush(comentario));
    }

    public RespuestaComentario modificarComentario(
            Long temaId, Long id, DatosComentario datos, Jwt sesion) {
        validarTamano(datos.contenido(), 2000);
        var comentario = buscarComentario(temaId, id);
        comprobarAutor(comentario.getAutorId(), sesion);
        comentario.setContenido(datos.contenido().strip());
        return RespuestaComentario.desde(comentarios.saveAndFlush(comentario));
    }

    public void eliminarComentario(Long temaId, Long id, Jwt sesion) {
        var comentario = buscarComentario(temaId, id);
        comprobarAutor(comentario.getAutorId(), sesion);
        comentarios.delete(comentario);
    }

    private void asignarDatos(Tema tema, DatosTema datos) {
        validarTamano(datos.contenido(), 4000);
        tema.setTitulo(datos.titulo().strip());
        tema.setContenido(datos.contenido().strip());
        tema.setCategoria(buscarCategoria(datos.categoriaId()));
    }

    private Categoria buscarCategoria(Long id) {
        return categorias
                .findById(id)
                .orElseThrow(
                        () -> new ExcepcionNegocio(HttpStatus.NOT_FOUND, "La categoría no existe"));
    }

    private Tema buscarTema(Long id) {
        return temas.findById(id)
                .orElseThrow(() -> new ExcepcionNegocio(HttpStatus.NOT_FOUND, "El tema no existe"));
    }

    private Comentario buscarComentario(Long temaId, Long id) {
        return comentarios
                .findById(id)
                .filter(comentario -> comentario.getTema().getId().equals(temaId))
                .orElseThrow(
                        () ->
                                new ExcepcionNegocio(
                                        HttpStatus.NOT_FOUND,
                                        "El comentario no existe en el tema indicado"));
    }

    private void comprobarAutor(Long autorId, Jwt sesion) {
        if (!autorId.toString().equals(sesion.getSubject())) {
            throw new ExcepcionNegocio(
                    HttpStatus.FORBIDDEN,
                    "Solo el autor puede modificar o eliminar este contenido");
        }
    }

    private void validarTamano(String contenido, int limiteBytes) {
        if (contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > limiteBytes) {
            throw new ExcepcionNegocio(
                    HttpStatus.BAD_REQUEST,
                    "El contenido supera el límite de almacenamiento de "
                            + limiteBytes
                            + " bytes en UTF-8");
        }
    }
}
