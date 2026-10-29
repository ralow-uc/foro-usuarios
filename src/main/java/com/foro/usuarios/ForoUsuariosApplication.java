package com.foro.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de control de usuarios del foro.
 *
 * Responsabilidades: alta, consulta, modificacion y baja de usuarios, asignacion
 * de roles e inicio de sesion con emision de un JWT que despues consume foro-temas.
 */
@SpringBootApplication
public class ForoUsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForoUsuariosApplication.class, args);
    }
}
