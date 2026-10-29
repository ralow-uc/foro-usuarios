package com.foro.usuarios.repository;

import com.foro.usuarios.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA de los roles del foro.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /** Busca un rol por su nombre, sin distinguir mayusculas. */
    Optional<Rol> findByNombreIgnoreCase(String nombre);
}
