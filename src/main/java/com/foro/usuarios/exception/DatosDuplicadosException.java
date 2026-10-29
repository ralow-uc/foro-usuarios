package com.foro.usuarios.exception;

/**
 * Se lanza cuando se intenta registrar un nombre de usuario o un correo que ya
 * esta ocupado por otra cuenta.
 */
public class DatosDuplicadosException extends RuntimeException {

    public DatosDuplicadosException(String mensaje) {
        super(mensaje);
    }
}
