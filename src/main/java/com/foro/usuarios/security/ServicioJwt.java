package com.foro.usuarios.security;

import com.foro.usuarios.model.Rol;
import com.foro.usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Emite y valida los JWT del foro.
 *
 * El token se firma con HS256 y un secreto compartido con foro-temas, de modo que
 * el otro microservicio puede validarlo sin llamar a este por red.
 * Dentro del token viaja el ID del usuario, su nombre y sus roles.
 */
@Component
public class ServicioJwt {

    private static final String CLAIM_ID = "uid";
    private static final String CLAIM_ROLES = "roles";

    private final SecretKey clave;
    private final long minutosDeVigencia;

    public ServicioJwt(@Value("${foro.jwt.secreto}") String secreto,
                       @Value("${foro.jwt.minutos-de-vigencia:120}") long minutosDeVigencia) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.minutosDeVigencia = minutosDeVigencia;
    }

    /** Genera el token de un usuario que acaba de iniciar sesion correctamente. */
    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getNombreUsuario())
                .claim(CLAIM_ID, usuario.getId())
                .claim(CLAIM_ROLES, usuario.getRoles().stream().map(Rol::getNombre).sorted().toList())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(minutosDeVigencia * 60)))
                .signWith(clave)
                .compact();
    }

    /** Momento en que caduca un token emitido ahora. */
    public LocalDateTime calcularExpiracion() {
        return LocalDateTime.ofInstant(
                Instant.now().plusSeconds(minutosDeVigencia * 60), ZoneId.systemDefault());
    }

    /**
     * Valida la firma y la vigencia del token y devuelve su contenido.
     * Lanza JwtException si el token es invalido, esta manipulado o caduco.
     */
    public Claims leer(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extraerId(Claims contenido) {
        return contenido.get(CLAIM_ID, Number.class).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<String> extraerRoles(Claims contenido) {
        return (List<String>) contenido.get(CLAIM_ROLES, List.class);
    }
}
