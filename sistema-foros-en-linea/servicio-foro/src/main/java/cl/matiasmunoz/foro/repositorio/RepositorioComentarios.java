package cl.matiasmunoz.foro.repositorio;

import cl.matiasmunoz.foro.modelo.Comentario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RepositorioComentarios extends JpaRepository<Comentario, Long> {
    @Query("select c from Comentario c where c.tema.id = :temaId order by c.fechaCreacion, c.id")
    List<Comentario> listarPorTema(Long temaId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("delete from Comentario c where c.tema.id = :temaId")
    void eliminarPorTema(Long temaId);
}
