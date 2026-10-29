package cl.matiasmunoz.usuarios.repositorio;

import cl.matiasmunoz.usuarios.modelo.Rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RepositorioRoles extends JpaRepository<Rol, Long> {
    @Query("select r from Rol r where r.nombre = :nombre")
    Optional<Rol> buscarPorNombre(String nombre);
}
