package com.foro.usuarios.controller;

import com.foro.usuarios.dto.CredencialesRequest;
import com.foro.usuarios.dto.TokenResponse;
import com.foro.usuarios.model.Rol;
import com.foro.usuarios.model.Usuario;
import com.foro.usuarios.security.ServicioJwt;
import com.foro.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del inicio de sesion.
 *
 * Endpoint expuesto:
 *   POST /api/auth/login   Valida las credenciales y devuelve un JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private static final Logger log = LoggerFactory.getLogger(AutenticacionController.class);

    private final UsuarioService usuarioService;
    private final ServicioJwt servicioJwt;

    public AutenticacionController(UsuarioService usuarioService, ServicioJwt servicioJwt) {
        this.usuarioService = usuarioService;
        this.servicioJwt = servicioJwt;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody CredencialesRequest credenciales) {
        Usuario usuario = usuarioService.autenticar(credenciales);
        log.debug("Sesion iniciada por {}", usuario.getNombreUsuario());

        return ResponseEntity.ok(new TokenResponse(
                servicioJwt.generar(usuario),
                "Bearer",
                servicioJwt.calcularExpiracion(),
                usuario.getNombreUsuario(),
                usuario.getRoles().stream().map(Rol::getNombre).sorted().toList()));
    }
}
