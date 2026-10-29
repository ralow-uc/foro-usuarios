-- ===========================================================================
--  Microservicio foro-usuarios
--  Creacion de SUS tablas en el esquema FORO de Oracle Autonomous Database.
--
--  Ejecutar como script completo con el usuario FORO, despues de haber corrido
--  scripts/00_crear_esquema_foro.sql como ADMIN.
--
--  Este microservicio es dueno de tres tablas: ROL, USUARIO y USUARIO_ROL.
--  Las tablas del contenido del foro (CATEGORIA, TEMA, COMENTARIO) las crea el
--  otro microservicio, foro-temas, con su propio script. Los dos esquemas
--  conviven en la misma base sin dependencias entre ellos.
--
--  Las secuencias llevan INCREMENT BY 1 porque JPA las usa con
--  @SequenceGenerator(allocationSize = 1).
-- ===========================================================================


-- ---------------------------------------------------------------------------
-- 1. Tabla ROL
--    Tipos de usuario que maneja la aplicacion. Se cargan tres: USUARIO,
--    MODERADOR y ADMIN, con privilegios distintos sobre el foro.
-- ---------------------------------------------------------------------------
CREATE TABLE ROL (
    ID      NUMBER(19)     NOT NULL,
    NOMBRE  VARCHAR2(30)   NOT NULL,
    CONSTRAINT PK_ROL           PRIMARY KEY (ID),
    CONSTRAINT UK_ROL_NOMBRE    UNIQUE (NOMBRE)
);

COMMENT ON TABLE  ROL        IS 'Roles o tipos de usuario con privilegios diferenciados';
COMMENT ON COLUMN ROL.ID     IS 'Identificador unico del rol';
COMMENT ON COLUMN ROL.NOMBRE IS 'Nombre del rol: USUARIO, MODERADOR o ADMIN';

CREATE SEQUENCE ROL_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


-- ---------------------------------------------------------------------------
-- 2. Tabla USUARIO
--    La contrasena se guarda siempre como hash BCrypt, nunca en texto plano.
-- ---------------------------------------------------------------------------
CREATE TABLE USUARIO (
    ID               NUMBER(19)      NOT NULL,
    NOMBRE_USUARIO   VARCHAR2(50)    NOT NULL,
    CORREO           VARCHAR2(150)   NOT NULL,
    CONTRASENA_HASH  VARCHAR2(100)   NOT NULL,
    ACTIVO           NUMBER(1)       DEFAULT 1 NOT NULL,
    FECHA_REGISTRO   TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_USUARIO          PRIMARY KEY (ID),
    CONSTRAINT UK_USUARIO_NOMBRE   UNIQUE (NOMBRE_USUARIO),
    CONSTRAINT UK_USUARIO_CORREO   UNIQUE (CORREO),
    CONSTRAINT CK_USUARIO_ACTIVO   CHECK (ACTIVO IN (0, 1))
);

COMMENT ON TABLE  USUARIO                 IS 'Usuarios registrados en el foro';
COMMENT ON COLUMN USUARIO.ID              IS 'Identificador unico del usuario';
COMMENT ON COLUMN USUARIO.NOMBRE_USUARIO  IS 'Nombre con el que el usuario inicia sesion y firma sus temas';
COMMENT ON COLUMN USUARIO.CORREO          IS 'Correo electronico, unico por usuario';
COMMENT ON COLUMN USUARIO.CONTRASENA_HASH IS 'Hash BCrypt de la contrasena; nunca se guarda en texto plano';
COMMENT ON COLUMN USUARIO.ACTIVO          IS '1 si la cuenta esta habilitada, 0 si fue dada de baja';
COMMENT ON COLUMN USUARIO.FECHA_REGISTRO  IS 'Momento en que se creo la cuenta';

CREATE SEQUENCE USUARIO_SEQ START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


-- ---------------------------------------------------------------------------
-- 3. Tabla USUARIO_ROL
--    Relacion muchos a muchos: un usuario puede tener varios roles y un rol
--    lo comparten varios usuarios.
-- ---------------------------------------------------------------------------
CREATE TABLE USUARIO_ROL (
    USUARIO_ID  NUMBER(19)  NOT NULL,
    ROL_ID      NUMBER(19)  NOT NULL,
    CONSTRAINT PK_USUARIO_ROL  PRIMARY KEY (USUARIO_ID, ROL_ID),
    CONSTRAINT FK_UR_USUARIO   FOREIGN KEY (USUARIO_ID) REFERENCES USUARIO (ID) ON DELETE CASCADE,
    CONSTRAINT FK_UR_ROL       FOREIGN KEY (ROL_ID)     REFERENCES ROL (ID)
);

COMMENT ON TABLE USUARIO_ROL IS 'Roles asignados a cada usuario';


-- ---------------------------------------------------------------------------
-- 4. Indice de apoyo al inicio de sesion
--    findByNombreUsuario se resuelve con la clave unica UK_USUARIO_NOMBRE,
--    que Oracle ya respalda con su propio indice, de modo que no hace falta
--    crear ninguno adicional.
-- ---------------------------------------------------------------------------
