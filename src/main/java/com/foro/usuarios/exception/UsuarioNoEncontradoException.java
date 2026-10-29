package com.foro.usuarios.exception;

/**
 * Se lanza cuando se solicita un usuario cuyo ID no existe en la base de datos.
 */
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Long id) {
        super("No existe un usuario con el ID " + id);
    }
}
