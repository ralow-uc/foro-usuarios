package com.foro.usuarios.service;

import com.foro.usuarios.dto.CredencialesRequest;
import com.foro.usuarios.dto.UsuarioRequest;
import com.foro.usuarios.dto.UsuarioResponse;
import com.foro.usuarios.exception.CredencialesInvalidasException;
import com.foro.usuarios.exception.DatosDuplicadosException;
import com.foro.usuarios.exception.RolNoEncontradoException;
import com.foro.usuarios.exception.UsuarioNoEncontradoException;
import com.foro.usuarios.model.Rol;
import com.foro.usuarios.model.Usuario;
import com.foro.usuarios.repository.RolRepository;
import com.foro.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas del CRUD de usuarios y del inicio de sesion a traves de la capa de
 * servicio y del repositorio JPA.
 */
@SpringBootTest
@ActiveProfiles("test")
class UsuarioServiceImplTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder codificador;

    @BeforeEach
    void prepararBase() {
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
        rolRepository.save(new Rol(null, Rol.USUARIO));
        rolRepository.save(new Rol(null, Rol.MODERADOR));
        rolRepository.save(new Rol(null, Rol.ADMIN));
    }

    @Test
    @DisplayName("POST: registra el usuario, le asigna el rol USUARIO y guarda la contrasena hasheada")
    void crearRegistraElUsuario() {
        UsuarioResponse creado = usuarioService.crear(
                new UsuarioRequest("feliks", "feliks@foro.cl", "Cubo12345*"));

        assertThat(creado.id()).isNotNull();
        assertThat(creado.nombreUsuario()).isEqualTo("feliks");
        assertThat(creado.activo()).isTrue();
        assertThat(creado.roles()).containsExactly(Rol.USUARIO);

        Usuario enBase = usuarioRepository.findByNombreUsuario("feliks").orElseThrow();
        assertThat(enBase.getContrasenaHash()).isNotEqualTo("Cubo12345*");
        assertThat(codificador.matches("Cubo12345*", enBase.getContrasenaHash())).isTrue();
    }

    @Test
    @DisplayName("POST: rechaza un nombre de usuario ya ocupado")
    void crearConNombreDuplicadoFalla() {
        usuarioService.crear(new UsuarioRequest("max", "max@foro.cl", "Cubo12345*"));

        assertThatThrownBy(() -> usuarioService.crear(
                new UsuarioRequest("MAX", "otro@foro.cl", "Cubo12345*")))
                .isInstanceOf(DatosDuplicadosException.class)
                .hasMessageContaining("nombre de usuario");
    }

    @Test
    @DisplayName("POST: rechaza un correo ya registrado")
    void crearConCorreoDuplicadoFalla() {
        usuarioService.crear(new UsuarioRequest("max", "max@foro.cl", "Cubo12345*"));

        assertThatThrownBy(() -> usuarioService.crear(
                new UsuarioRequest("otro", "max@foro.cl", "Cubo12345*")))
                .isInstanceOf(DatosDuplicadosException.class)
                .hasMessageContaining("correo");
    }

    @Test
    @DisplayName("GET: obtiene un usuario por su ID y nunca expone el hash")
    void obtenerPorIdDevuelveElUsuario() {
        Long id = usuarioService.crear(new UsuarioRequest("yiheng", "yiheng@foro.cl", "Cubo12345*")).id();

        UsuarioResponse encontrado = usuarioService.obtenerPorId(id);

        assertThat(encontrado.nombreUsuario()).isEqualTo("yiheng");
        assertThat(encontrado.correo()).isEqualTo("yiheng@foro.cl");
        assertThat(encontrado.fechaRegistro()).isNotNull();
    }

    @Test
    @DisplayName("GET: un ID inexistente lanza UsuarioNoEncontradoException")
    void obtenerPorIdInexistenteFalla() {
        assertThatThrownBy(() -> usuarioService.obtenerPorId(9999L))
                .isInstanceOf(UsuarioNoEncontradoException.class)
                .hasMessageContaining("9999");
    }

    @Test
    @DisplayName("PUT: actualiza el correo y vuelve a hashear la contrasena nueva")
    void actualizarModificaElUsuario() {
        Long id = usuarioService.crear(new UsuarioRequest("tymon", "tymon@foro.cl", "Cubo12345*")).id();

        UsuarioResponse actualizado = usuarioService.actualizar(
                id, new UsuarioRequest("tymon", "tymon.nuevo@foro.cl", "OtraClave99*"));

        assertThat(actualizado.correo()).isEqualTo("tymon.nuevo@foro.cl");

        Usuario enBase = usuarioRepository.findById(id).orElseThrow();
        assertThat(codificador.matches("OtraClave99*", enBase.getContrasenaHash())).isTrue();
    }

    @Test
    @DisplayName("DELETE: elimina el usuario de la base de datos")
    void eliminarBorraElUsuario() {
        Long id = usuarioService.crear(new UsuarioRequest("retirado", "retirado@foro.cl", "Cubo12345*")).id();

        usuarioService.eliminar(id);

        assertThat(usuarioRepository.existsById(id)).isFalse();
        assertThatThrownBy(() -> usuarioService.eliminar(id))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    @DisplayName("GET: lista todos los usuarios y filtra por rol y por estado")
    void listarYFiltrar() {
        Long idAdmin = usuarioService.crear(new UsuarioRequest("admin", "admin@foro.cl", "Cubo12345*")).id();
        usuarioService.asignarRoles(idAdmin, List.of(Rol.ADMIN));
        usuarioService.crear(new UsuarioRequest("cubero", "cubero@foro.cl", "Cubo12345*"));

        assertThat(usuarioService.listar()).hasSize(2);
        assertThat(usuarioService.buscarPorRol("admin"))
                .extracting(UsuarioResponse::nombreUsuario).containsExactly("admin");
        assertThat(usuarioService.buscarPorEstado(true)).hasSize(2);
        assertThat(usuarioService.buscarPorEstado(false)).isEmpty();
    }

    @Test
    @DisplayName("PUT: asignar roles reemplaza los roles anteriores del usuario")
    void asignarRolesReemplazaLosAnteriores() {
        Long id = usuarioService.crear(new UsuarioRequest("moderador", "mod@foro.cl", "Cubo12345*")).id();

        UsuarioResponse actualizado = usuarioService.asignarRoles(id, List.of(Rol.MODERADOR, Rol.ADMIN));

        assertThat(actualizado.roles()).containsExactlyInAnyOrder(Rol.MODERADOR, Rol.ADMIN);
    }

    @Test
    @DisplayName("PUT: asignar un rol que no existe lanza RolNoEncontradoException")
    void asignarRolInexistenteFalla() {
        Long id = usuarioService.crear(new UsuarioRequest("cubero", "cubero@foro.cl", "Cubo12345*")).id();

        assertThatThrownBy(() -> usuarioService.asignarRoles(id, List.of("SUPERUSUARIO")))
                .isInstanceOf(RolNoEncontradoException.class)
                .hasMessageContaining("SUPERUSUARIO");
    }

    @Test
    @DisplayName("LOGIN: autentica al usuario con la contrasena correcta")
    void autenticarConCredencialesCorrectas() {
        usuarioService.crear(new UsuarioRequest("feliks", "feliks@foro.cl", "Cubo12345*"));

        Usuario autenticado = usuarioService.autenticar(
                new CredencialesRequest("feliks", "Cubo12345*"));

        assertThat(autenticado.getNombreUsuario()).isEqualTo("feliks");
    }

    @Test
    @DisplayName("LOGIN: rechaza la contrasena incorrecta y el usuario inexistente")
    void autenticarConCredencialesInvalidas() {
        usuarioService.crear(new UsuarioRequest("feliks", "feliks@foro.cl", "Cubo12345*"));

        assertThatThrownBy(() -> usuarioService.autenticar(
                new CredencialesRequest("feliks", "equivocada")))
                .isInstanceOf(CredencialesInvalidasException.class);

        assertThatThrownBy(() -> usuarioService.autenticar(
                new CredencialesRequest("nadie", "Cubo12345*")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("LOGIN: una cuenta dada de baja no puede iniciar sesion")
    void autenticarCuentaInactiva() {
        Long id = usuarioService.crear(new UsuarioRequest("retirado", "retirado@foro.cl", "Cubo12345*")).id();
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        assertThatThrownBy(() -> usuarioService.autenticar(
                new CredencialesRequest("retirado", "Cubo12345*")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }
}
