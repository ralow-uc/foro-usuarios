package com.foro.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.type.NumericBooleanConverter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad JPA que representa un usuario registrado en el foro.
 *
 * La contrasena se guarda siempre como hash BCrypt: la entidad no conoce nunca
 * la contrasena en texto plano. Se mapea a la tabla USUARIO y la relacion con
 * los roles a la tabla USUARIO_ROL (ver scripts/01_crear_tablas.sql).
 */
@Entity
@Table(name = "USUARIO")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq_gen")
    @SequenceGenerator(name = "usuario_seq_gen", sequenceName = "USUARIO_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE_USUARIO", nullable = false, length = 50, unique = true)
    private String nombreUsuario;

    @Column(name = "CORREO", nullable = false, length = 150, unique = true)
    private String correo;

    @Column(name = "CONTRASENA_HASH", nullable = false, length = 100)
    private String contrasenaHash;

    /**
     * 1 si la cuenta esta habilitada, 0 si fue dada de baja.
     *
     * El convertidor numerico es necesario: la columna es NUMBER(1) y Oracle 23
     * tiene un tipo BOOLEAN nativo, asi que sin esta anotacion Hibernate espera
     * BOOLEAN y la validacion del esquema falla contra la tabla real.
     */
    @Column(name = "ACTIVO", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean activo = true;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime fechaRegistro;

    /**
     * Roles del usuario. Se cargan con EAGER porque el inicio de sesion necesita
     * los roles para construir el JWT en la misma operacion.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "USUARIO_ROL",
            joinColumns = @JoinColumn(name = "USUARIO_ID"),
            inverseJoinColumns = @JoinColumn(name = "ROL_ID"))
    private Set<Rol> roles = new LinkedHashSet<>();

    protected Usuario() {
        // Constructor requerido por JPA.
    }

    public Usuario(Long id, String nombreUsuario, String correo, String contrasenaHash,
                   boolean activo, Set<Rol> roles) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.activo = activo;
        this.roles = roles == null ? new LinkedHashSet<>() : new LinkedHashSet<>(roles);
    }

    /** La fecha de registro la fija la aplicacion al insertar, no el cliente. */
    @PrePersist
    void alInsertar() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Set<Rol> getRoles() {
        return roles;
    }

    public void setRoles(Set<Rol> roles) {
        this.roles = roles == null ? new LinkedHashSet<>() : new LinkedHashSet<>(roles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario otro)) {
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
        return "Usuario{id=" + id
                + ", nombreUsuario='" + nombreUsuario + '\''
                + ", correo='" + correo + '\''
                + ", activo=" + activo + '}';
    }
}
