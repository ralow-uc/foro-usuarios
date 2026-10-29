package com.foro.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Entidad JPA que representa un rol o tipo de usuario del foro.
 *
 * El foro maneja tres: USUARIO (publica y comenta), MODERADOR y ADMIN
 * (administra usuarios y puede borrar contenido ajeno).
 * Se mapea a la tabla ROL (ver scripts/01_crear_tablas.sql).
 */
@Entity
@Table(name = "ROL")
public class Rol {

    /** Nombres de los roles que la aplicacion reconoce. */
    public static final String USUARIO = "USUARIO";
    public static final String MODERADOR = "MODERADOR";
    public static final String ADMIN = "ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rol_seq_gen")
    @SequenceGenerator(name = "rol_seq_gen", sequenceName = "ROL_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE", nullable = false, length = 30, unique = true)
    private String nombre;

    protected Rol() {
        // Constructor requerido por JPA.
    }

    public Rol(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Rol otro)) {
            return false;
        }
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Rol{id=" + id + ", nombre='" + nombre + '\'' + '}';
    }
}
