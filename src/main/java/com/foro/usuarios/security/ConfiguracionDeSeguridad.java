package com.foro.usuarios.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion de seguridad del microservicio de usuarios.
 *
 * El servicio es sin estado: no hay sesion ni cookies, la identidad viaja en el
 * JWT de cada peticion. Por eso se desactiva CSRF, que protege formularios con
 * sesion y no aporta nada a una API consumida con token.
 *
 * @EnableMethodSecurity habilita las anotaciones @PreAuthorize de los
 * controladores, que es donde viven las reglas por rol.
 */
@Configuration
@EnableMethodSecurity
public class ConfiguracionDeSeguridad {

    private final FiltroDeAutenticacionJwt filtroJwt;

    public ConfiguracionDeSeguridad(FiltroDeAutenticacionJwt filtroJwt) {
        this.filtroJwt = filtroJwt;
    }

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(peticiones -> peticiones
                        // El reenvio interno a /error tiene que pasar sin autenticar.
                        // Sin esto, cuando @PreAuthorize deniega el acceso Spring
                        // reenvia a /error y, al no haber sesion, ese segundo paso
                        // llega sin identidad: el 403 se convierte en un 401 y se
                        // pierde la distincion entre "no autenticado" y "sin permiso".
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.ASYNC).permitAll()
                        // Publicos: registrarse, iniciar sesion y consultar el estado.
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/estado", "/actuator/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // Todo lo demas exige un JWT valido.
                        .anyRequest().authenticated())
                // Sin token se responde 401 (no autenticado); con token pero sin
                // privilegio, Spring responde 403 (no autorizado).
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Algoritmo con el que se hashean las contrasenas. BCrypt incorpora su propia
     * sal, de modo que dos usuarios con la misma contrasena tienen hashes distintos.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS: permite que un frontend servido en otro origen consuma la API.
     * En S3 el ramo incorpora Angular, que correra en otro puerto.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOriginPatterns(List.of("http://localhost:*"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }
}
