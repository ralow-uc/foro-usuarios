package com.foro.usuarios.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones de la aplicacion en respuestas HTTP con un cuerpo
 * claro, para que Postman muestre el motivo exacto del error.
 */
@RestControllerAdvice
public class ManejadorGlobalDeErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalDeErrores.class);

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ProblemDetail manejarUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        ProblemDetail detalle = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detalle.setTitle("Usuario no encontrado");
        detalle.setDetail(ex.getMessage());
        return detalle;
    }

    @ExceptionHandler(RolNoEncontradoException.class)
    public ProblemDetail manejarRolNoEncontrado(RolNoEncontradoException ex) {
        ProblemDetail detalle = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detalle.setTitle("Rol no encontrado");
        detalle.setDetail(ex.getMessage());
        return detalle;
    }

    @ExceptionHandler(DatosDuplicadosException.class)
    public ProblemDetail manejarDuplicados(DatosDuplicadosException ex) {
        ProblemDetail detalle = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        detalle.setTitle("Datos duplicados");
        detalle.setDetail(ex.getMessage());
        return detalle;
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ProblemDetail manejarCredenciales(CredencialesInvalidasException ex) {
        log.debug("Intento de inicio de sesion fallido");
        ProblemDetail detalle = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        detalle.setTitle("Credenciales invalidas");
        detalle.setDetail(ex.getMessage());
        return detalle;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail detalle = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detalle.setTitle("Datos del usuario invalidos");
        detalle.setDetail("Revisa los campos indicados en 'errores'");
        detalle.setProperty("errores", errores);
        return detalle;
    }
}
