package cl.matiasmunoz.comun;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableMethodSecurity
public class ConfiguracionSeguridad {
    @Bean
    public SecretKey claveFirma(@Value("${seguridad.jwt.secreto}") String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("La clave JWT debe contener al menos 32 bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtDecoder decodificador(SecretKey claveFirma) {
        var decodificador =
                NimbusJwtDecoder.withSecretKey(claveFirma).macAlgorithm(MacAlgorithm.HS256).build();
        decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer("foro-academico"));
        return decodificador;
    }

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity seguridad) throws Exception {
        var permisos = new JwtGrantedAuthoritiesConverter();
        permisos.setAuthoritiesClaimName("rol");
        permisos.setAuthorityPrefix("ROLE_");
        var conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(permisos);
        return seguridad
                .csrf(csrf -> csrf.disable())
                .sessionManagement(
                        sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        reglas ->
                                reglas.requestMatchers("/actuator/health")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/usuarios/registro",
                                                "/api/sesiones")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/categorias",
                                                "/api/categorias/*",
                                                "/api/temas",
                                                "/api/temas/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .oauth2ResourceServer(
                        recurso ->
                                recurso.jwt(jwt -> jwt.jwtAuthenticationConverter(conversor))
                                        .authenticationEntryPoint(
                                                (solicitud, respuesta, error) -> {
                                                    respuesta.setStatus(401);
                                                    respuesta.setContentType(
                                                            "application/json;charset=UTF-8");
                                                    respuesta
                                                            .getWriter()
                                                            .write(
                                                                    "{\"estado\":401,\"mensaje\":\"Se"
                                                                        + " requiere una sesión"
                                                                        + " válida\"}");
                                                })
                                        .accessDeniedHandler(
                                                (solicitud, respuesta, error) -> {
                                                    respuesta.setStatus(403);
                                                    respuesta.setContentType(
                                                            "application/json;charset=UTF-8");
                                                    respuesta
                                                            .getWriter()
                                                            .write(
                                                                    "{\"estado\":403,\"mensaje\":\"No"
                                                                        + " dispone de permisos"
                                                                        + " para realizar esta"
                                                                        + " operación\"}");
                                                }))
                .build();
    }
}
