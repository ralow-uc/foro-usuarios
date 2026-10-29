package com.foro.usuarios.controller;

import com.foro.usuarios.dto.CredencialesRequest;
import com.foro.usuarios.dto.RolesRequest;
import com.foro.usuarios.dto.UsuarioRequest;
import com.foro.usuarios.model.Rol;
import com.foro.usuarios.model.Usuario;
import com.foro.usuarios.repository.RolRepository;
import com.foro.usuarios.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de los endpoints HTTP del microservicio de usuarios, incluidas las
 * reglas de autorizacion: los 401 y 403 que se ven aqui son los mismos que
 * devuelve el servicio en Postman.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerTest {

    private static final String CLAVE = "Cubo12345*";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder codificador;

    private Long idDelAdmin;

    @BeforeEach
    void prepararBase() {
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
        rolRepository.save(new Rol(null, Rol.USUARIO));
        rolRepository.save(new Rol(null, Rol.MODERADOR));
        Rol rolAdmin = rolRepository.save(new Rol(null, Rol.ADMIN));

        // El primer ADMIN se siembra directamente: no existe ningun ADMIN que
        // pudiera promoverlo por API.
        idDelAdmin = usuarioRepository.save(new Usuario(
                null, "admin", "admin@foro.cl", codificador.encode(CLAVE), true, Set.of(rolAdmin))).getId();
    }

    private String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    /** Registra un usuario con el rol USUARIO y devuelve su ID. */
    private Long registrar(String nombre) throws Exception {
        String respuesta = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UsuarioRequest(nombre, nombre + "@foro.cl", CLAVE))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("id").asLong();
    }

    /** Inicia sesion y devuelve el JWT listo para la cabecera Authorization. */
    private String token(String nombre) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CredencialesRequest(nombre, CLAVE))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(respuesta).get("token").asText();
    }

    @Test
    @DisplayName("POST /api/usuarios responde 201 y nunca devuelve la contrasena")
    void postRegistraUsuario() throws Exception {
        String respuesta = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UsuarioRequest("feliks", "feliks@foro.cl", CLAVE))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombreUsuario").value("feliks"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.roles[0]").value("USUARIO"))
                .andReturn().getResponse().getContentAsString();

        assertThat(respuesta).doesNotContain(CLAVE);
        assertThat(respuesta.toLowerCase()).doesNotContain("contrasena");
        assertThat(respuesta).doesNotContain("$2a$");
    }

    @Test
    @DisplayName("POST /api/usuarios responde 400 si los datos no son validos")
    void postConDatosInvalidosEsRechazado() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UsuarioRequest("a", "no-es-un-correo", "corta"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombreUsuario").exists())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.contrasena").exists());
    }

    @Test
    @DisplayName("POST /api/usuarios responde 409 si el nombre ya esta ocupado")
    void postDuplicadoDevuelve409() throws Exception {
        registrar("feliks");

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UsuarioRequest("feliks", "otro@foro.cl", CLAVE))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Datos duplicados"));
    }

    @Test
    @DisplayName("POST /api/auth/login devuelve un JWT con el rol del usuario")
    void loginDevuelveToken() throws Exception {
        registrar("feliks");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CredencialesRequest("feliks", CLAVE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.nombreUsuario").value("feliks"))
                .andExpect(jsonPath("$.roles[0]").value("USUARIO"))
                .andExpect(jsonPath("$.expiraEn").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login responde 401 con la contrasena incorrecta")
    void loginConClaveIncorrectaDevuelve401() throws Exception {
        registrar("feliks");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CredencialesRequest("feliks", "equivocada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/usuarios sin token responde 401")
    void listarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/usuarios con rol USUARIO responde 403: no tiene el privilegio")
    void listarConRolUsuarioDevuelve403() throws Exception {
        registrar("feliks");

        mockMvc.perform(get("/api/usuarios").header("Authorization", token("feliks")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/usuarios con rol ADMIN responde 200 y admite filtrar por rol")
    void listarConRolAdminDevuelve200() throws Exception {
        registrar("feliks");
        String tokenAdmin = token("admin");

        mockMvc.perform(get("/api/usuarios").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/usuarios").param("rol", "admin").header("Authorization", tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreUsuario").value("admin"));
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} deja ver la propia cuenta pero no la de otro")
    void verCuentaPropiaSiYAjenaNo() throws Exception {
        Long idFeliks = registrar("feliks");
        Long idMax = registrar("max");
        String tokenFeliks = token("feliks");

        mockMvc.perform(get("/api/usuarios/{id}", idFeliks).header("Authorization", tokenFeliks))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("feliks"));

        mockMvc.perform(get("/api/usuarios/{id}", idMax).header("Authorization", tokenFeliks))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} responde 404 si el ID no existe")
    void verUsuarioInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 9999).header("Authorization", token("admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Usuario no encontrado"));
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} permite al usuario modificar su propia cuenta")
    void putActualizaLaCuentaPropia() throws Exception {
        Long id = registrar("tymon");

        mockMvc.perform(put("/api/usuarios/{id}", id)
                        .header("Authorization", token("tymon"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UsuarioRequest("tymon", "tymon.nuevo@foro.cl", "OtraClave99*"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("tymon.nuevo@foro.cl"));
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} solo lo puede hacer un ADMIN")
    void deleteReservadoAlAdmin() throws Exception {
        Long idFeliks = registrar("feliks");

        mockMvc.perform(delete("/api/usuarios/{id}", idFeliks)
                        .header("Authorization", token("feliks")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/usuarios/{id}", idFeliks)
                        .header("Authorization", token("admin")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/{id}", idFeliks).header("Authorization", token("admin")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id}/roles permite al ADMIN promover a un usuario")
    void adminAsignaRoles() throws Exception {
        Long idFeliks = registrar("feliks");

        mockMvc.perform(put("/api/usuarios/{id}/roles", idFeliks)
                        .header("Authorization", token("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RolesRequest(List.of(Rol.MODERADOR)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("MODERADOR"));

        mockMvc.perform(put("/api/usuarios/{id}/roles", idDelAdmin)
                        .header("Authorization", token("feliks"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RolesRequest(List.of(Rol.USUARIO)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/estado responde 200 y es publico")
    void getEstadoEsPublico() throws Exception {
        mockMvc.perform(get("/api/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OK"))
                .andExpect(jsonPath("$.servicio").value("foro-usuarios"))
                .andExpect(jsonPath("$.baseDeDatos.conectada").value(true));
    }
}
