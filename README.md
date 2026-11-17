# foro-usuarios

Microservicio de **control de usuarios e inicio de sesión** del caso *Sistema de
Foros en Línea*. Spring Boot sobre **Oracle Cloud Autonomous Database**.

Es **uno de los tres microservicios** del caso. El otro,
[`foro-temas`](#el-otro-microservicio), vive en su propio repositorio y se
encarga de las categorías, los temas y los comentarios.

| | |
|---|---|
| Puerto | `8081` |
| Responsabilidad | Alta, consulta, modificación y baja de cuentas; roles; inicio de sesión |
| Tablas propias | `ROL`, `USUARIO`, `USUARIO_ROL` |
| Rol en la pareja | **Emite** el JWT que el otro microservicio valida |

**Asignatura:** Desarrollo Full Stack III (DSY2205) · Experiencia 1, Semana 2
· Actividad sumativa *Programando nuestro BackEnd*.


> ### Novedades v2.0.0 · Experiencia 2, Semana 5
>
> - El caso pasa a **tres microservicios**: se suma
>   [`foro-moderacion`](../foro-moderacion) (puerto 8083), generado con el
>   arquetipo propio [`microservicio-arquetipo`](../microservicio-arquetipo),
>   que reúne la estructura común de este repositorio y su par.
> - **Docker**: `Dockerfile` multi-etapa (el mismo del arquetipo). El Wallet se
>   monta al ejecutar; nunca va en la imagen. Despliegue en Play with Docker en
>   [`../foro-despliegue`](../foro-despliegue).
> - **CI** (`.github/workflows/ci.yml`): pruebas y `docker build` en cada push o
>   pull request a `main`.
> - **Ramas**: Trunk Based Development ([CONTRIBUTING.md](CONTRIBUTING.md)).
>   `v1.0.0` es la entrega de S2; `v2.0.0`, la de S5.
> - **Postman**: la colección ahora recorre los tres servicios (00–59) y trae
>   entornos `local` y `docker-cloud`.
> - `FORO_CORS_ORIGENES` y `PORT` configurables por variable de entorno.

---

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| Java | 21 LTS (Temurin) |
| Maven | 3.9.16, incluido como *wrapper* |
| Spring Boot | 3.5.16 |
| Base de datos | Oracle Autonomous Database, esquema `FORO` |

No hace falta instalar Maven: el proyecto trae su propio `mvnw`.

---

## 2. Estructura

```
foro-usuarios/
├── src/main/java/com/foro/usuarios/
│   ├── controller/      AutenticacionController, UsuarioController, EstadoController
│   ├── service/         contrato + impl con la lógica de negocio
│   ├── repository/      UsuarioRepository, RolRepository (JPA)
│   ├── model/           Usuario, Rol
│   ├── dto/             entrada y salida; nunca exponen la contraseña
│   ├── security/        emisión y validación del JWT, filtro, configuración
│   ├── exception/       excepciones propias + manejador global
│   └── config/          datos de ejemplo del perfil local
├── src/test/java/...    34 pruebas automatizadas
├── scripts/
│   ├── 00_crear_esquema_foro.sql   CREATE USER FORO + permisos (como ADMIN)
│   ├── 01_crear_tablas.sql         ROL, USUARIO, USUARIO_ROL + secuencias
│   ├── 02_datos_iniciales.sql      3 roles, 5 usuarios, 6 asignaciones
│   ├── 99_eliminar_objetos.sql     para recrear desde cero
│   ├── consultas_demo.sql          SELECT para evidenciar los datos en Oracle
│   ├── sql.sh + EjecutarSql.java   ejecuta un .sql por JDBC
│   ├── entorno-oracle.sh           credenciales desde el Llavero de macOS
│   ├── run.sh                      arranca contra Oracle
│   └── test.sh                     ejecuta las pruebas
├── postman/             colección de demostración (cubre los dos servicios)
└── docs/
    ├── arquitectura.md  diagramas y decisiones de diseño de la pareja
    └── registros/       evidencias de ejecución
```

---

## 3. Base de datos

Las tablas viven en un **esquema propio llamado `FORO`**, no bajo `ADMIN`. Este
microservicio crea y puebla **solo sus tres tablas**; las del foro las crea
`foro-temas` con su propio script. No hay claves foráneas entre los dos grupos.

| Tabla | Registros iniciales |
|---|---|
| `ROL` | 3 — `USUARIO`, `MODERADOR`, `ADMIN` |
| `USUARIO` | 5 |
| `USUARIO_ROL` | 6 |

### Creación desde cero

```bash
# 1. Una sola vez, como ADMIN: crea el esquema y sus permisos.
#    Antes de ejecutarlo, reemplaza __CLAVE_FORO__ en el script.
#    Este paso es común a los dos microservicios: basta hacerlo una vez.
./scripts/sql.sh admin scripts/00_crear_esquema_foro.sql

# 2. Como FORO: las tablas de este microservicio.
./scripts/sql.sh foro scripts/01_crear_tablas.sql

# 3. Como FORO: sus datos iniciales.
./scripts/sql.sh foro scripts/02_datos_iniciales.sql
```

Los scripts también se pueden pegar directamente en **Oracle Database Actions**.

> `sql.sh` ejecuta el SQL por JDBC porque en el equipo de desarrollo no hay
> `sqlplus` ni `sqlcl` instalados.

### Credenciales

No están en el repositorio. `scripts/entorno-oracle.sh` las lee del **Llavero de
macOS**:

```bash
read -s -p "Password FORO: " P && \
  security add-generic-password -a "$USER" -s foro-online-db -w "$P" -U && \
  unset P
```

El **Wallet** vive en `~/.oracle/wallets/FULLSTACK3`, fuera del proyecto: su
ruta no puede contener espacios o el driver JDBC falla con `ORA-12263`.

---

## 4. Ejecución

```bash
./scripts/run.sh               # contra Oracle, puerto 8081
./mvnw spring-boot:run         # en local con H2 en memoria, sin credenciales
```

El perfil `local` es el de por omisión y siembra al arrancar los mismos 3 roles
y 5 usuarios de ejemplo. Las cinco cuentas comparten la contraseña `Cubo12345*`.

```bash
curl http://localhost:8081/api/estado
```

Responde con el **motor real de base de datos**, que es la forma rápida de
distinguir si se está corriendo contra H2 o contra Oracle.

---

## 5. API REST

| Método | Ruta | Descripción | Acceso | Respuestas |
|---|---|---|---|---|
| `POST` | `/api/auth/login` | Valida credenciales y devuelve un JWT | público | `200` · `401` |
| `POST` | `/api/usuarios` | Registra una cuenta | público | `201` · `400` · `409` |
| `GET` | `/api/usuarios` | Lista las cuentas. Filtros `?rol=` y `?activo=` | `ADMIN` | `200` · `401` · `403` |
| `GET` | `/api/usuarios/{id}` | Consulta una cuenta | dueño o `ADMIN` | `200` · `403` · `404` |
| `PUT` | `/api/usuarios/{id}` | Actualiza una cuenta | dueño o `ADMIN` | `200` · `400` · `403` · `404` |
| `DELETE` | `/api/usuarios/{id}` | Elimina una cuenta | `ADMIN` | `204` · `403` · `404` |
| `PUT` | `/api/usuarios/{id}/roles` | Reemplaza los roles | `ADMIN` | `200` · `400` · `403` |
| `GET` | `/api/estado` | Estado del servicio y de la conexión | público | `200` |

### Ejemplos

```bash
# Registrar una cuenta
curl -X POST http://localhost:8081/api/usuarios \
  -H 'Content-Type: application/json' \
  -d '{"nombreUsuario":"cubero","correo":"cubero@foro.cl","contrasena":"Cubo12345*"}'

# Iniciar sesión y guardar el token
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"nombreUsuario":"feliks","contrasena":"Cubo12345*"}' \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')

# Listar cuentas: solo un ADMIN
curl http://localhost:8081/api/usuarios -H "Authorization: Bearer $TOKEN"
```

---

## 6. Seguridad y roles

- **Contraseñas con BCrypt.** La entidad nunca conoce la contraseña en claro y
  ningún DTO de salida la incluye, ni ella ni su hash.
- **JWT firmado con HS256.** Lo emite este servicio; `foro-temas` lo valida con
  el mismo secreto, sin llamar aquí en ninguna petición.
- **Tres roles:** `USUARIO`, `MODERADOR` y `ADMIN`. El enunciado pide al menos
  dos; van tres porque también exige al menos tres registros por tabla.
- **Reglas por rol** en el controlador con `@PreAuthorize`; la comprobación de
  *«esta es mi propia cuenta»* se expresa con `principal.id == #id`.
- **CORS** configurado para los puertos locales, de cara al frontend Angular de
  la semana 3.

`401` y `403` significan cosas distintas y el servicio las distingue: `401` es
que no hay identidad, `403` que la identidad existe pero no alcanza.

La tabla completa de privilegios está en
[`docs/arquitectura.md`](docs/arquitectura.md#roles-y-privilegios).

---

## 7. Pruebas

```bash
./scripts/test.sh
```

**34 pruebas** sobre H2 en modo Oracle, de modo que validan el mismo mapeo JPA
que se usa contra la base real sin depender de la red:

| Clase | Pruebas | Cobertura |
|---|---|---|
| `UsuarioRepositoryTest` | 6 | Mapeo JPA, relación muchos a muchos con roles, consultas derivadas |
| `UsuarioServiceImplTest` | 13 | CRUD completo, duplicados, asignación de roles, inicio de sesión |
| `UsuarioControllerTest` | 14 | Los ocho endpoints y sus códigos, incluidos los `401` y `403` reales |
| `ForoUsuariosApplicationTests` | 1 | Arranque del contexto con seguridad y JPA |

---

## 8. Postman

1. **File ▸ Import** → `postman/foro-online.postman_collection.json`
2. Arrancar **los dos** microservicios, cada uno desde su repositorio
3. **Run collection**, o ejecutar las peticiones **00 a 24** en orden

La colección cubre los dos servicios porque la demostración los recorre juntos:
el token se obtiene aquí y se usa en `foro-temas`. Es el mismo archivo en los
dos repositorios.

---

## 9. El otro microservicio

El caso se resuelve con dos microservicios en repositorios separados:

| Repositorio | Puerto | Responsabilidad |
|---|---|---|
| **`foro-usuarios`** (este) | 8081 | Cuentas, roles e inicio de sesión |
| `foro-temas` | 8082 | Categorías, temas y comentarios |

Se comunican por el JWT: este servicio lo firma, el otro lo valida con el mismo
secreto compartido. **No hay llamadas de red entre ellos**, así que `foro-temas`
sigue sirviendo el foro aunque este servicio esté caído; lo único que no se
podrá hacer mientras tanto es iniciar sesión.

Los detalles están en [`docs/arquitectura.md`](docs/arquitectura.md).

---

## 10. Evidencias

En [`docs/registros/`](docs/registros):

| Archivo | Contenido |
|---|---|
| `01_verificacion_oracle.txt` | Recorrido de punta a punta contra Oracle sobre los dos servicios: los cuatro verbos REST, los privilegios de los roles y los casos de error |
| `02_esquema_oracle.txt` | Estado del esquema `FORO`, conteo por tabla, usuarios con sus roles y hashes, y la matriz de privilegios verificada en vivo |
| `03_pruebas_automatizadas.txt` | Salida de las pruebas de este microservicio |
| `04_coleccion_postman.txt` | Las 25 peticiones de la colección ejecutadas en orden contra Oracle |
