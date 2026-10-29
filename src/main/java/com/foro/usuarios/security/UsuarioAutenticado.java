package com.foro.usuarios.security;

/**
 * Identidad que queda en el contexto de seguridad despues de validar el JWT.
 *
 * Lleva el ID ademas del nombre para que las reglas de @PreAuthorize puedan
 * comprobar si el usuario esta operando sobre su propia cuenta.
 */
public record UsuarioAutenticado(Long id, String nombreUsuario) {
}
