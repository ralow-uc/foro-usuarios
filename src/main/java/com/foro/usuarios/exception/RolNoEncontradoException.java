package com.foro.usuarios.exception;

/**
 * Se lanza cuando se intenta asignar un rol que la aplicacion no reconoce.
 */
public class RolNoEncontradoException extends RuntimeException {

    public RolNoEncontradoException(String nombre) {
        super("No existe el rol " + nombre);
    }
}
