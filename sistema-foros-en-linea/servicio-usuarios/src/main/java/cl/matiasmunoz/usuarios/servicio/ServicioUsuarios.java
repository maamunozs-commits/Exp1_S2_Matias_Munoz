package cl.matiasmunoz.usuarios.servicio;

import cl.matiasmunoz.comun.ExcepcionNegocio;
import cl.matiasmunoz.usuarios.dto.DatosUsuarios.*;
import cl.matiasmunoz.usuarios.modelo.Usuario;
import cl.matiasmunoz.usuarios.repositorio.*;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import javax.crypto.SecretKey;

@Service
@Transactional
public class ServicioUsuarios {
    private final RepositorioUsuarios usuarios;
    private final RepositorioRoles roles;
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();
    private final JwtEncoder emisor;

    public ServicioUsuarios(
            RepositorioUsuarios usuarios, RepositorioRoles roles, SecretKey claveFirma) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.emisor = new NimbusJwtEncoder(new ImmutableSecret<>(claveFirma));
    }

    public Respuesta registrar(Registro datos) {
        String correo = normalizarCorreo(datos.correo());
        if (usuarios.existeCorreo(correo)) {
            throw new ExcepcionNegocio(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (datos.contrasena().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ExcepcionNegocio(
                    HttpStatus.BAD_REQUEST, "La contraseña admite hasta 72 bytes en UTF-8");
        }
        var usuario = new Usuario();
        usuario.setNombre(datos.nombre().strip());
        usuario.setCorreo(correo);
        usuario.setContrasenaHash(codificador.encode(datos.contrasena()));
        usuario.setRol(roles.buscarPorNombre("USUARIO").orElseThrow());
        return Respuesta.desde(usuarios.saveAndFlush(usuario));
    }

    @Transactional(readOnly = true)
    public Sesion iniciarSesion(InicioSesion datos) {
        Usuario usuario =
                usuarios.buscarPorCorreo(normalizarCorreo(datos.correo()))
                        .orElseThrow(
                                () ->
                                        new ExcepcionNegocio(
                                                HttpStatus.UNAUTHORIZED,
                                                "Las credenciales son incorrectas"));
        if (!codificador.matches(datos.contrasena(), usuario.getContrasenaHash())) {
            throw new ExcepcionNegocio(HttpStatus.UNAUTHORIZED, "Las credenciales son incorrectas");
        }
        Instant ahora = Instant.now();
        var contenido =
                JwtClaimsSet.builder()
                        .issuer("foro-academico")
                        .subject(usuario.getId().toString())
                        .issuedAt(ahora)
                        .expiresAt(ahora.plusSeconds(900))
                        .claim("rol", usuario.getRol().getNombre())
                        .claim("nombre", usuario.getNombre())
                        .build();
        var encabezado = JwsHeader.with(MacAlgorithm.HS256).build();
        String token =
                emisor.encode(JwtEncoderParameters.from(encabezado, contenido)).getTokenValue();
        return new Sesion(token, "Bearer", 900, Respuesta.desde(usuario));
    }

    @Transactional(readOnly = true)
    public List<Respuesta> listar() {
        return usuarios.findAll(org.springframework.data.domain.Sort.by("id")).stream()
                .map(Respuesta::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public Respuesta obtener(Long id, Jwt sesion) {
        comprobarPropietario(id, sesion);
        return Respuesta.desde(buscar(id));
    }

    public Respuesta modificar(Long id, Edicion datos, Jwt sesion) {
        comprobarPropietario(id, sesion);
        var usuario = buscar(id);
        String correo = normalizarCorreo(datos.correo());
        usuarios.buscarPorCorreo(correo)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(
                        otro -> {
                            throw new ExcepcionNegocio(
                                    HttpStatus.CONFLICT, "El correo ya está registrado");
                        });
        usuario.setNombre(datos.nombre().strip());
        usuario.setCorreo(correo);
        return Respuesta.desde(usuarios.saveAndFlush(usuario));
    }

    public Respuesta cambiarRol(Long id, CambioRol datos) {
        var usuario = buscar(id);
        String nombreRol = datos.rol().strip().toUpperCase(Locale.ROOT);
        var rol =
                roles.buscarPorNombre(nombreRol)
                        .orElseThrow(
                                () ->
                                        new ExcepcionNegocio(
                                                HttpStatus.BAD_REQUEST,
                                                "El rol indicado no existe"));
        if (!nombreRol.equals("ADMINISTRADOR")) {
            protegerAdministrador(usuario);
        }
        usuario.setRol(rol);
        return Respuesta.desde(usuarios.saveAndFlush(usuario));
    }

    public void eliminar(Long id, Jwt sesion) {
        comprobarPropietario(id, sesion);
        var usuario = buscar(id);
        protegerAdministrador(usuario);
        usuarios.delete(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(
                        () -> new ExcepcionNegocio(HttpStatus.NOT_FOUND, "El usuario no existe"));
    }

    private void comprobarPropietario(Long id, Jwt sesion) {
        if (!id.toString().equals(sesion.getSubject())
                && !"ADMINISTRADOR".equals(sesion.getClaimAsString("rol"))) {
            throw new ExcepcionNegocio(
                    HttpStatus.FORBIDDEN, "Solo puede acceder a su propia cuenta");
        }
    }

    private void protegerAdministrador(Usuario usuario) {
        if ("ADMINISTRADOR".equals(usuario.getRol().getNombre())
                && usuarios.contarPorRol("ADMINISTRADOR") == 1) {
            throw new ExcepcionNegocio(
                    HttpStatus.CONFLICT, "El sistema debe conservar al menos un administrador");
        }
    }

    private String normalizarCorreo(String correo) {
        return correo.strip().toLowerCase(Locale.ROOT);
    }
}
