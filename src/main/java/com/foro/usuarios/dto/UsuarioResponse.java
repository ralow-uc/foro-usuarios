package com.foro.usuarios.dto;

import com.foro.usuarios.model.Rol;
import com.foro.usuarios.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representacion de un usuario devuelta por el microservicio.
 *
 * No incluye la contrasena ni su hash: es la garantia de que el CRUD no puede
 * filtrar credenciales por mas que la entidad las tenga.
 */
public record UsuarioResponse(
        Long id,
        String nombreUsuario,
        String correo,
        boolean activo,
        LocalDateTime fechaRegistro,
        List<String> roles) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getCorreo(),
                usuario.isActivo(),
                usuario.getFechaRegistro(),
                usuario.getRoles().stream().map(Rol::getNombre).sorted().toList());
    }
}
