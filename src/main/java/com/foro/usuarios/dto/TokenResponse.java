package com.foro.usuarios.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta del inicio de sesion: el JWT que foro-temas validara en cada peticion.
 */
public record TokenResponse(
        String token,
        String tipo,
        LocalDateTime expiraEn,
        String nombreUsuario,
        List<String> roles) {
}
