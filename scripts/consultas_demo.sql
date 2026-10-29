-- ===========================================================================
--  Microservicio foro-usuarios
--  Consultas para demostrar en vivo que los datos estan en Oracle.
--
--  Ejecutar con el usuario FORO, desde Database Actions o con:
--      ./scripts/sql.sh foro scripts/consultas_demo.sql
-- ===========================================================================


-- ---------------------------------------------------------------------------
-- 1. Cuantos registros hay en cada tabla de este microservicio
-- ---------------------------------------------------------------------------
SELECT 'ROL' AS TABLA, COUNT(*) AS REGISTROS FROM ROL
UNION ALL SELECT 'USUARIO', COUNT(*) FROM USUARIO
UNION ALL SELECT 'USUARIO_ROL', COUNT(*) FROM USUARIO_ROL;


-- ---------------------------------------------------------------------------
-- 2. Usuarios con sus roles
--    Demuestra la relacion muchos a muchos y que la contrasena esta hasheada:
--    de CONTRASENA_HASH solo se muestran los primeros caracteres, que son el
--    prefijo de BCrypt ($2a$10$).
-- ---------------------------------------------------------------------------
SELECT u.ID,
       u.NOMBRE_USUARIO,
       u.CORREO,
       CASE u.ACTIVO WHEN 1 THEN 'activa' ELSE 'de baja' END AS CUENTA,
       SUBSTR(u.CONTRASENA_HASH, 1, 10) || '...' AS HASH_BCRYPT,
       LISTAGG(r.NOMBRE, ', ') WITHIN GROUP (ORDER BY r.NOMBRE) AS ROLES
  FROM USUARIO u
  JOIN USUARIO_ROL ur ON ur.USUARIO_ID = u.ID
  JOIN ROL r          ON r.ID = ur.ROL_ID
 GROUP BY u.ID, u.NOMBRE_USUARIO, u.CORREO, u.ACTIVO, u.CONTRASENA_HASH
 ORDER BY u.ID;


-- ---------------------------------------------------------------------------
-- 3. Cuantos usuarios tiene cada rol
--    Es el mismo conteo que respalda el filtro GET /api/usuarios?rol=
-- ---------------------------------------------------------------------------
SELECT r.NOMBRE AS ROL, COUNT(ur.USUARIO_ID) AS USUARIOS
  FROM ROL r
  LEFT JOIN USUARIO_ROL ur ON ur.ROL_ID = r.ID
 GROUP BY r.NOMBRE
 ORDER BY USUARIOS DESC, r.NOMBRE;


-- ---------------------------------------------------------------------------
-- 4. La cuenta registrada mas recientemente
--    Es la consulta que conviene correr justo despues del POST /api/usuarios
--    en Postman: muestra el dato recien creado dentro de Oracle.
-- ---------------------------------------------------------------------------
SELECT ID,
       NOMBRE_USUARIO,
       CORREO,
       TO_CHAR(FECHA_REGISTRO, 'DD-MM-YYYY HH24:MI:SS') AS REGISTRADO
  FROM USUARIO
 ORDER BY ID DESC
 FETCH FIRST 3 ROWS ONLY;


-- ---------------------------------------------------------------------------
-- 5. Comprobacion de que ninguna contrasena esta en texto plano
--    Todas las filas deben empezar por el prefijo de BCrypt. Si alguna no lo
--    hiciera, esta consulta la delataria.
-- ---------------------------------------------------------------------------
SELECT COUNT(*) AS TOTAL_USUARIOS,
       SUM(CASE WHEN CONTRASENA_HASH LIKE '$2a$%' THEN 1 ELSE 0 END) AS CON_HASH_BCRYPT
  FROM USUARIO;
