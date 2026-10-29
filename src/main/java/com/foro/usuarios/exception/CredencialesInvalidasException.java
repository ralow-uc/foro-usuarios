package com.foro.usuarios.exception;

/**
 * Se lanza cuando el inicio de sesion falla.
 *
 * El mensaje es deliberadamente generico: no distingue entre usuario inexistente,
 * contrasena incorrecta y cuenta dada de baja, para no revelar que nombres de
 * usuario existen en el foro.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Nombre de usuario o contrasena incorrectos");
    }
}
