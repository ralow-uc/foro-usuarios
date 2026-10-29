-- ===========================================================================
--  Microservicio foro-usuarios
--  Elimina SUS objetos del esquema FORO para poder recrearlos desde cero.
--
--  Ejecutar con el usuario FORO. El orden importa por las claves foraneas.
--  No toca las tablas del otro microservicio (CATEGORIA, TEMA, COMENTARIO).
-- ===========================================================================

DROP TABLE USUARIO_ROL  CASCADE CONSTRAINTS;
DROP TABLE USUARIO      CASCADE CONSTRAINTS;
DROP TABLE ROL          CASCADE CONSTRAINTS;

DROP SEQUENCE USUARIO_SEQ;
DROP SEQUENCE ROL_SEQ;
