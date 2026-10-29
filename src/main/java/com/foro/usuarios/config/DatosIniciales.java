package com.foro.usuarios.config;

import com.foro.usuarios.model.Rol;
import com.foro.usuarios.model.Usuario;
import com.foro.usuarios.repository.RolRepository;
import com.foro.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Carga los tres roles y cinco usuarios de ejemplo al iniciar con el perfil
 * "local" (H2 en memoria), para poder probar de inmediato el login y el CRUD
 * en Postman.
 *
 * No se ejecuta con el perfil "oracle": alli los datos los carga
 * scripts/02_datos_iniciales.sql y los administra la base real.
 */
@Component
@Profile("local")
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    /** Contrasena comun de los usuarios de ejemplo, solo para el perfil local. */
    private static final String CLAVE_DE_EJEMPLO = "Cubo12345*";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder codificador;

    public DatosIniciales(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder codificador) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.codificador = codificador;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Rol rolUsuario = rolRepository.save(new Rol(null, Rol.USUARIO));
        Rol rolModerador = rolRepository.save(new Rol(null, Rol.MODERADOR));
        Rol rolAdmin = rolRepository.save(new Rol(null, Rol.ADMIN));

        String hash = codificador.encode(CLAVE_DE_EJEMPLO);

        usuarioRepository.saveAll(List.of(
                new Usuario(null, "admin", "admin@foro.cl", hash, true, Set.of(rolAdmin)),
                new Usuario(null, "moderadora", "moderadora@foro.cl", hash, true, Set.of(rolModerador, rolUsuario)),
                new Usuario(null, "feliks", "feliks@foro.cl", hash, true, Set.of(rolUsuario)),
                new Usuario(null, "yiheng", "yiheng@foro.cl", hash, true, Set.of(rolUsuario)),
                new Usuario(null, "tymon", "tymon@foro.cl", hash, false, Set.of(rolUsuario))));

        log.info("Perfil local: {} roles y {} usuarios de ejemplo cargados (clave comun: {})",
                rolRepository.count(), usuarioRepository.count(), CLAVE_DE_EJEMPLO);
    }
}
