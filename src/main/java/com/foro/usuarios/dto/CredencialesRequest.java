package com.foro.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales que el cliente envia para iniciar sesion.
 */
public record CredencialesRequest(

        @NotBlank(message = "El nombre de usuario es obligatorio")
        String nombreUsuario,

        @NotBlank(message = "La contrasena es obligatoria")
        String contrasena) {
}
