package cl.matiasmunoz.foro.repositorio;

import cl.matiasmunoz.foro.modelo.Tema;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface RepositorioTemas
        extends JpaRepository<Tema, Long>, JpaSpecificationExecutor<Tema> {
    @Query("select (count(t) > 0) from Tema t where t.categoria.id = :categoriaId")
    boolean contieneCategoria(Long categoriaId);
}
