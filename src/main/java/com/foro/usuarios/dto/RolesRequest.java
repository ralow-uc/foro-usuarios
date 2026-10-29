package com.foro.usuarios.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Roles que un ADMIN asigna a un usuario. Reemplazan por completo los anteriores.
 */
public record RolesRequest(

        @NotEmpty(message = "Debes indicar al menos un rol")
        List<String> roles) {
}
