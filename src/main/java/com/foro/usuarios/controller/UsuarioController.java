package com.foro.usuarios.controller;

import com.foro.usuarios.dto.RolesRequest;
import com.foro.usuarios.dto.UsuarioRequest;
import com.foro.usuarios.dto.UsuarioResponse;
import com.foro.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Controlador REST que maneja las solicitudes HTTP sobre los usuarios del foro.
 *
 * Endpoints expuestos:
 *   POST   /api/usuarios            Registra un usuario nuevo (publico).
 *   GET    /api/usuarios            Lista los usuarios. Filtros ?rol= y ?activo=. Solo ADMIN.
 *   GET    /api/usuarios/{id}       Consulta un usuario. El dueno o un ADMIN.
 *   PUT    /api/usuarios/{id}       Actualiza un usuario. El dueno o un ADMIN.
 *   DELETE /api/usuarios/{id}       Elimina un usuario. Solo ADMIN.
 *   PUT    /api/usuarios/{id}/roles Reemplaza los roles. Solo ADMIN.
 *
 * Los dos roles con privilegios distintos se expresan en las anotaciones
 * @PreAuthorize: "principal.id == #id" es la comprobacion de que el usuario
 * esta operando sobre su propia cuenta y no sobre la de otro.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioRequest solicitud,
                                                     UriComponentsBuilder uriBuilder) {
        UsuarioResponse creado = usuarioService.crear(solicitud);
        return ResponseEntity
                .created(uriBuilder.path("/api/usuarios/{id}").buildAndExpand(creado.id()).toUri())
                .body(creado);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listar(
            @RequestParam(name = "rol", required = false) String rol,
            @RequestParam(name = "activo", required = false) Boolean activo) {

        List<UsuarioResponse> usuarios;
        if (rol != null && !rol.isBlank()) {
            usuarios = usuarioService.buscarPorRol(rol);
        } else if (activo != null) {
            usuarios = usuarioService.buscarPorEstado(activo);
        } else {
            usuarios = usuarioService.listar();
        }
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.id == #id")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.id == #id")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody UsuarioRequest solicitud) {
        return ResponseEntity.ok(usuarioService.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> asignarRoles(@PathVariable Long id,
                                                        @Valid @RequestBody RolesRequest solicitud) {
        return ResponseEntity.ok(usuarioService.asignarRoles(id, solicitud.roles()));
    }
}
