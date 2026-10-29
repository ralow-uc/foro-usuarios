package com.foro.usuarios.service.impl;

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
import com.foro.usuarios.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementacion de la logica de negocio del CRUD de usuarios.
 *
 * Toda la interaccion con la base de datos Oracle se hace a traves de los
 * repositorios JPA. La contrasena se hashea aqui con BCrypt y nunca se devuelve.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder codificador;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              RolRepository rolRepository,
                              PasswordEncoder codificador) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.codificador = codificador;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscarPorRol(String nombreDelRol) {
        return usuarioRepository.findByRoles_NombreIgnoreCase(nombreDelRol.trim())
                .stream()
                .map(UsuarioResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscarPorEstado(boolean activo) {
        return usuarioRepository.findByActivo(activo).stream().map(UsuarioResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return UsuarioResponse.desde(buscarOFallar(id));
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest solicitud) {
        String nombreUsuario = solicitud.nombreUsuario().trim();
        String correo = solicitud.correo().trim().toLowerCase();

        if (usuarioRepository.existsByNombreUsuarioIgnoreCase(nombreUsuario)) {
            throw new DatosDuplicadosException("El nombre de usuario " + nombreUsuario + " ya esta ocupado");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new DatosDuplicadosException("El correo " + correo + " ya esta registrado");
        }

        Usuario usuario = new Usuario(
                null,
                nombreUsuario,
                correo,
                codificador.encode(solicitud.contrasena()),
                true,
                Set.of(rolPorNombre(Rol.USUARIO)));

        Usuario guardado = usuarioRepository.save(usuario);
        log.debug("Usuario registrado: {} con rol {}", guardado.getNombreUsuario(), Rol.USUARIO);
        return UsuarioResponse.desde(guardado);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest solicitud) {
        Usuario usuario = buscarOFallar(id);
        String nombreUsuario = solicitud.nombreUsuario().trim();
        String correo = solicitud.correo().trim().toLowerCase();

        if (!usuario.getNombreUsuario().equalsIgnoreCase(nombreUsuario)
                && usuarioRepository.existsByNombreUsuarioIgnoreCase(nombreUsuario)) {
            throw new DatosDuplicadosException("El nombre de usuario " + nombreUsuario + " ya esta ocupado");
        }
        if (!usuario.getCorreo().equals(correo) && usuarioRepository.existsByCorreo(correo)) {
            throw new DatosDuplicadosException("El correo " + correo + " ya esta registrado");
        }

        usuario.setNombreUsuario(nombreUsuario);
        usuario.setCorreo(correo);
        usuario.setContrasenaHash(codificador.encode(solicitud.contrasena()));

        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNoEncontradoException(id);
        }
        usuarioRepository.deleteById(id);
        log.debug("Usuario eliminado: ID {}", id);
    }

    @Override
    @Transactional
    public UsuarioResponse asignarRoles(Long id, List<String> nombresDeRoles) {
        Usuario usuario = buscarOFallar(id);

        Set<Rol> roles = new LinkedHashSet<>();
        for (String nombre : nombresDeRoles) {
            roles.add(rolPorNombre(nombre));
        }
        usuario.setRoles(roles);

        log.debug("Roles de {} reemplazados por {}", usuario.getNombreUsuario(), nombresDeRoles);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario autenticar(CredencialesRequest credenciales) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(credenciales.nombreUsuario().trim())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActivo() || !codificador.matches(credenciales.contrasena(), usuario.getContrasenaHash())) {
            log.debug("Inicio de sesion rechazado para {}", credenciales.nombreUsuario());
            throw new CredencialesInvalidasException();
        }
        return usuario;
    }

    private Usuario buscarOFallar(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    private Rol rolPorNombre(String nombre) {
        return rolRepository.findByNombreIgnoreCase(nombre.trim())
                .orElseThrow(() -> new RolNoEncontradoException(nombre));
    }
}
