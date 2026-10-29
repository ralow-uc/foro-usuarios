package com.foro.usuarios.repository;

import com.foro.usuarios.model.Rol;
import com.foro.usuarios.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprueba el mapeo JPA de Usuario y Rol, incluida la relacion muchos a muchos,
 * contra una base de datos real (H2 en modo Oracle).
 */
@DataJpaTest
// Sin esto, @DataJpaTest sustituye el DataSource por uno embebido y descarta
// la URL del perfil de pruebas, con lo que H2 dejaria de emular a Oracle y
// las pruebas validarian una semantica distinta a la de la base real.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Rol rolUsuario;
    private Rol rolAdmin;

    @BeforeEach
    void prepararRoles() {
        rolUsuario = rolRepository.save(new Rol(null, "USUARIO"));
        rolAdmin = rolRepository.save(new Rol(null, "ADMIN"));
    }

    @Test
    @DisplayName("JPA genera el ID con la secuencia y persiste los datos del usuario")
    void guardaYRecuperaElUsuario() {
        Usuario guardado = usuarioRepository.save(new Usuario(
                null, "feliks", "feliks@foro.cl", "$2a$10$hashficticio", true, Set.of(rolUsuario)));

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getFechaRegistro()).isNotNull();

        Usuario recuperado = usuarioRepository.findById(guardado.getId()).orElseThrow();
        assertThat(recuperado.getNombreUsuario()).isEqualTo("feliks");
        assertThat(recuperado.getCorreo()).isEqualTo("feliks@foro.cl");
        assertThat(recuperado.isActivo()).isTrue();
        assertThat(recuperado.getRoles()).extracting(Rol::getNombre).containsExactly("USUARIO");
    }

    @Test
    @DisplayName("Un usuario puede acumular mas de un rol")
    void unUsuarioPuedeTenerVariosRoles() {
        Usuario guardado = usuarioRepository.save(new Usuario(
                null, "max", "max@foro.cl", "$2a$10$hashficticio", true, Set.of(rolUsuario, rolAdmin)));

        Usuario recuperado = usuarioRepository.findById(guardado.getId()).orElseThrow();

        assertThat(recuperado.getRoles()).extracting(Rol::getNombre)
                .containsExactlyInAnyOrder("USUARIO", "ADMIN");
    }

    @Test
    @DisplayName("Busca por nombre de usuario para el inicio de sesion")
    void buscaPorNombreDeUsuario() {
        usuarioRepository.save(new Usuario(
                null, "yiheng", "yiheng@foro.cl", "$2a$10$hashficticio", true, Set.of(rolUsuario)));

        assertThat(usuarioRepository.findByNombreUsuario("yiheng")).isPresent();
        assertThat(usuarioRepository.findByNombreUsuario("inexistente")).isEmpty();
    }

    @Test
    @DisplayName("Detecta nombres de usuario y correos ya ocupados")
    void detectaDuplicados() {
        usuarioRepository.save(new Usuario(
                null, "tymon", "tymon@foro.cl", "$2a$10$hashficticio", true, Set.of(rolUsuario)));

        assertThat(usuarioRepository.existsByNombreUsuario("tymon")).isTrue();
        assertThat(usuarioRepository.existsByNombreUsuarioIgnoreCase("TYMON")).isTrue();
        assertThat(usuarioRepository.existsByCorreo("tymon@foro.cl")).isTrue();
        assertThat(usuarioRepository.existsByCorreo("otro@foro.cl")).isFalse();
    }

    @Test
    @DisplayName("Filtra los usuarios por rol y por estado de la cuenta")
    void filtraPorRolYPorEstado() {
        usuarioRepository.save(new Usuario(
                null, "admin", "admin@foro.cl", "$2a$10$hashficticio", true, Set.of(rolAdmin)));
        usuarioRepository.save(new Usuario(
                null, "cubero", "cubero@foro.cl", "$2a$10$hashficticio", true, Set.of(rolUsuario)));
        usuarioRepository.save(new Usuario(
                null, "retirado", "retirado@foro.cl", "$2a$10$hashficticio", false, Set.of(rolUsuario)));

        List<Usuario> administradores = usuarioRepository.findByRoles_NombreIgnoreCase("admin");
        List<Usuario> activos = usuarioRepository.findByActivo(true);

        assertThat(administradores).extracting(Usuario::getNombreUsuario).containsExactly("admin");
        assertThat(activos).hasSize(2);
    }

    @Test
    @DisplayName("Los roles se consultan por nombre sin distinguir mayusculas")
    void buscaRolPorNombre() {
        assertThat(rolRepository.findByNombreIgnoreCase("usuario")).isPresent();
        assertThat(rolRepository.findByNombreIgnoreCase("MODERADOR")).isEmpty();
    }
}
