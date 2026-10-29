package cl.matiasmunoz.foro.repositorio;

import cl.matiasmunoz.foro.modelo.Categoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RepositorioCategorias extends JpaRepository<Categoria, Long> {
    @Query("select (count(c) > 0) from Categoria c where lower(c.nombre) = lower(:nombre)")
    boolean existeNombre(String nombre);

    @Query(
            "select (count(c) > 0) from Categoria c where lower(c.nombre) = lower(:nombre) and c.id"
                + " <> :id")
    boolean existeNombreEnOtraCategoria(String nombre, Long id);
}
