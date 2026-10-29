-- ===========================================================================
--  Microservicio foro-usuarios
--  Datos iniciales de SUS tablas.
--
--  Ejecutar como script completo con el usuario FORO, despues de
--  scripts/01_crear_tablas.sql.
--
--  Las tres tablas quedan con mas de tres registros, como exige la actividad:
--    ROL 3 | USUARIO 5 | USUARIO_ROL 6
--
--  Las contrasenas se guardan hasheadas con BCrypt. Las cinco cuentas de
--  ejemplo comparten la contrasena  Cubo12345*  para poder probar el login en
--  Postman; es un dato de demostracion, no una credencial real del sistema.
--
--  IMPORTANTE: los ID que genera este script (1 a 5, en el orden de abajo) son
--  los que el microservicio foro-temas usa como AUTOR_ID en sus datos de
--  ejemplo. Si se cambia el orden, hay que ajustar alla tambien.
-- ===========================================================================


-- ---------------------------------------------------------------------------
-- 1. Roles
-- ---------------------------------------------------------------------------
INSERT INTO ROL (ID, NOMBRE) VALUES (ROL_SEQ.NEXTVAL, 'USUARIO');
INSERT INTO ROL (ID, NOMBRE) VALUES (ROL_SEQ.NEXTVAL, 'MODERADOR');
INSERT INTO ROL (ID, NOMBRE) VALUES (ROL_SEQ.NEXTVAL, 'ADMIN');


-- ---------------------------------------------------------------------------
-- 2. Usuarios
--    tymon queda inactivo a proposito, para poder demostrar que una cuenta
--    dada de baja no puede iniciar sesion.
-- ---------------------------------------------------------------------------
INSERT INTO USUARIO (ID, NOMBRE_USUARIO, CORREO, CONTRASENA_HASH, ACTIVO)
VALUES (USUARIO_SEQ.NEXTVAL, 'admin', 'admin@foro.cl', '$2a$10$4No7rqJN8ijI6aZaMKIsKe9liK8uFDKD19SagIcD8O.OSqRty4oKa', 1);

INSERT INTO USUARIO (ID, NOMBRE_USUARIO, CORREO, CONTRASENA_HASH, ACTIVO)
VALUES (USUARIO_SEQ.NEXTVAL, 'moderadora', 'moderadora@foro.cl', '$2a$10$VjWOFEvOvaT.gAK/aOF25Ojt6etRfeHyP6/prQwhLnZHbCbQAg8/u', 1);

INSERT INTO USUARIO (ID, NOMBRE_USUARIO, CORREO, CONTRASENA_HASH, ACTIVO)
VALUES (USUARIO_SEQ.NEXTVAL, 'feliks', 'feliks@foro.cl', '$2a$10$TxGvj3pdiryiTu3tEgTB6eilA.Ua1ei2DwJzjRhkwH.ltrO9G/yme', 1);

INSERT INTO USUARIO (ID, NOMBRE_USUARIO, CORREO, CONTRASENA_HASH, ACTIVO)
VALUES (USUARIO_SEQ.NEXTVAL, 'yiheng', 'yiheng@foro.cl', '$2a$10$Muw.bUZ/DHloDXDYCkUuhuDzWtIo6eA1SDObLU.e4a34Pr4HcaLiq', 1);

INSERT INTO USUARIO (ID, NOMBRE_USUARIO, CORREO, CONTRASENA_HASH, ACTIVO)
VALUES (USUARIO_SEQ.NEXTVAL, 'tymon', 'tymon@foro.cl', '$2a$10$eegSbJ0BgtjLB9NmcZnogeihRKfAZx/onIN71pvMll38oydslqQIO', 0);


-- ---------------------------------------------------------------------------
-- 3. Roles asignados
--    moderadora acumula dos roles, para evidenciar que la relacion es
--    de muchos a muchos.
-- ---------------------------------------------------------------------------
INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID)
VALUES ((SELECT ID FROM USUARIO WHERE NOMBRE_USUARIO = 'admin'),
        (SELECT ID FROM ROL WHERE NOMBRE = 'ADMIN'));

INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID)
VALUES ((SELECT ID FROM USUARIO WHERE NOMBRE_USUARIO = 'moderadora'),
        (SELECT ID FROM ROL WHERE NOMBRE = 'MODERADOR'));

INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID)
VALUES ((SELECT ID FROM USUARIO WHERE NOMBRE_USUARIO = 'moderadora'),
        (SELECT ID FROM ROL WHERE NOMBRE = 'USUARIO'));

INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID)
VALUES ((SELECT ID FROM USUARIO WHERE NOMBRE_USUARIO = 'feliks'),
        (SELECT ID FROM ROL WHERE NOMBRE = 'USUARIO'));

INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID)
VALUES ((SELECT ID FROM USUARIO WHERE NOMBRE_USUARIO = 'yiheng'),
        (SELECT ID FROM ROL WHERE NOMBRE = 'USUARIO'));

INSERT INTO USUARIO_ROL (USUARIO_ID, ROL_ID)
VALUES ((SELECT ID FROM USUARIO WHERE NOMBRE_USUARIO = 'tymon'),
        (SELECT ID FROM ROL WHERE NOMBRE = 'USUARIO'));

COMMIT;


-- ---------------------------------------------------------------------------
-- 4. Verificacion
-- ---------------------------------------------------------------------------
SELECT 'ROL' AS TABLA, COUNT(*) AS REGISTROS FROM ROL
UNION ALL SELECT 'USUARIO', COUNT(*) FROM USUARIO
UNION ALL SELECT 'USUARIO_ROL', COUNT(*) FROM USUARIO_ROL;
