package com.foro.usuarios.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee la cabecera Authorization de cada peticion y, si trae un JWT valido, deja
 * al usuario autenticado en el contexto de seguridad con sus roles.
 *
 * Si no hay token o es invalido no lanza error: simplemente no autentica, y es
 * la configuracion de seguridad la que decide si el endpoint lo permite.
 */
@Component
public class FiltroDeAutenticacionJwt extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FiltroDeAutenticacionJwt.class);
    private static final String CABECERA = "Authorization";
    private static final String PREFIJO = "Bearer ";

    private final ServicioJwt servicioJwt;

    public FiltroDeAutenticacionJwt(ServicioJwt servicioJwt) {
        this.servicioJwt = servicioJwt;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest peticion,
                                    @NonNull HttpServletResponse respuesta,
                                    @NonNull FilterChain cadena) throws ServletException, IOException {
        String cabecera = peticion.getHeader(CABECERA);

        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            try {
                Claims contenido = servicioJwt.leer(cabecera.substring(PREFIJO.length()));
                List<String> roles = servicioJwt.extraerRoles(contenido);

                var autoridades = roles.stream()
                        .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol))
                        .toList();

                var autenticacion = new UsernamePasswordAuthenticationToken(
                        new UsuarioAutenticado(servicioJwt.extraerId(contenido), contenido.getSubject()),
                        null,
                        autoridades);

                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Token rechazado: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        cadena.doFilter(peticion, respuesta);
    }
}
