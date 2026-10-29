package com.foro.usuarios.repository;

import com.foro.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA de la entidad Usuario.
 *
 * Al extender JpaRepository se obtienen las operaciones CRUD contra Oracle sin
 * escribir SQL: findAll, findById, save, deleteById, existsById, etc.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca un usuario por su nombre exacto. Lo usa el inicio de sesion. */
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    /** Comprueba si el nombre de usuario ya esta ocupado. */
    boolean existsByNombreUsuario(String nombreUsuario);

    /** Igual que el anterior pero sin distinguir mayusculas, para el registro. */
    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    /** Comprueba si el correo ya esta registrado. */
    boolean existsByCorreo(String correo);

    /** Lista los usuarios que tienen un rol determinado. */
    List<Usuario> findByRoles_NombreIgnoreCase(String nombreDelRol);

    /** Lista los usuarios segun el estado de su cuenta. */
    List<Usuario> findByActivo(boolean activo);
}
