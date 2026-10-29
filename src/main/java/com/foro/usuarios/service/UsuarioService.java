package com.foro.usuarios.service;

import com.foro.usuarios.dto.CredencialesRequest;
import com.foro.usuarios.dto.UsuarioRequest;
import com.foro.usuarios.dto.UsuarioResponse;
import com.foro.usuarios.model.Usuario;

import java.util.List;

/**
 * Contrato de la capa de servicio del microservicio de usuarios:
 * el CRUD completo, la administracion de roles y el inicio de sesion.
 */
public interface UsuarioService {

    /** Devuelve todos los usuarios registrados. */
    List<UsuarioResponse> listar();

    /** Devuelve los usuarios que tienen el rol indicado. */
    List<UsuarioResponse> buscarPorRol(String nombreDelRol);

    /** Devuelve los usuarios segun el estado de su cuenta. */
    List<UsuarioResponse> buscarPorEstado(boolean activo);

    /** Obtiene la informacion de un usuario por su ID. */
    UsuarioResponse obtenerPorId(Long id);

    /** Registra un usuario nuevo con el rol USUARIO y la contrasena hasheada. */
    UsuarioResponse crear(UsuarioRequest solicitud);

    /** Actualiza los datos de un usuario existente. */
    UsuarioResponse actualizar(Long id, UsuarioRequest solicitud);

    /** Elimina un usuario de la base de datos por su ID. */
    void eliminar(Long id);

    /** Reemplaza los roles de un usuario. Operacion reservada a un ADMIN. */
    UsuarioResponse asignarRoles(Long id, List<String> nombresDeRoles);

    /**
     * Comprueba las credenciales y devuelve el usuario autenticado.
     * Lanza CredencialesInvalidasException si fallan o si la cuenta esta inactiva.
     */
    Usuario autenticar(CredencialesRequest credenciales);
}
