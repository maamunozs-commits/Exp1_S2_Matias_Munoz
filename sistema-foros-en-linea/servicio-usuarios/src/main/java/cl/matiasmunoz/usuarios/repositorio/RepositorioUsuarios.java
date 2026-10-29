package cl.matiasmunoz.usuarios.repositorio;

import cl.matiasmunoz.usuarios.modelo.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RepositorioUsuarios extends JpaRepository<Usuario, Long> {
    @Query("select u from Usuario u where u.correo = :correo")
    Optional<Usuario> buscarPorCorreo(String correo);

    @Query("select (count(u) > 0) from Usuario u where u.correo = :correo")
    boolean existeCorreo(String correo);

    @Query("select count(u) from Usuario u where u.rol.nombre = :nombre")
    long contarPorRol(String nombre);
}
