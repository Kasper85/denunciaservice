# PLAN — denunciaservice

> Cola de trabajo del loop ODD. El agente actualiza **Estado**, **Evidencia** y **Decisiones**.
> Estados: `PENDIENTE` · `EN_CURSO` · `HECHA` · `BLOQUEADA` (solo por `[MANUAL]`).
> Requisitos completos en `ALCANCE.md`. No agregar features fuera de esta lista.

## Prerrequisitos [MANUAL] (los hace el usuario)

- [x] M1. JDK 17 (Temurin) y Git instalados.
- [x] M0. Proyecto Spring Boot ya generado en esta carpeta (paquete `com.RodriguezRuiz.denunciaservice`, Boot 4.1.x, Java 17).
- [ ] M2. Repo remoto `denunciaservice` en GitHub. No bloquea: sin `origin` el agente trabaja local y lo reporta al final.
- [ ] M3. (Opcional) PostgreSQL local o Docker instalado.

---

## F0 · api-scaffold — Generar el proyecto (lo hace el agente)
**Estado:** HECHA (2026-10-28, commit d11383e)
**Objetivo:** dejar el proyecto ya generado listo como repositorio git. **El proyecto YA EXISTE** (`pom.xml` presente): NO lo regeneres ni lo descargues.
**Tareas:**
- `pom.xml` ya existe → saltar directo a "git". (Solo si NO existiera: descargar desde start.spring.io como sigue.)
- El `{apellido}` lo da el usuario en el prompt (minúsculas, sin tildes ni ñ → `n`, sin espacios).
- Descargar (PowerShell, usar `curl.exe`):
  `curl.exe -fsSL "https://start.spring.io/starter.zip?type=maven-project&language=java&javaVersion=17&groupId=com.{apellido}&artifactId=denunciaservice&name=denunciaservice&packageName=com.{apellido}.denunciaservice&dependencies=web,data-jpa,postgresql,validation" -o starter.zip`
  (sin `bootVersion`: usa la estable por defecto; si sale 4.x, repetir añadiendo `&bootVersion=` con la última 3.5.x que liste `curl.exe -s https://start.spring.io/metadata/client`).
- `tar -xf starter.zip` (descomprime en la carpeta actual) y `del starter.zip`. No sobrescribir AGENTS.md / ALCANCE.md / PLAN.md.
- git: si no hay `.git` → `git init -b main`; asegurar `.gitignore`; `git add -A`; commit `chore: proyecto inicial generado con Spring Initializr`.
**Criterios de aceptación:** existe `pom.xml` con `<java.version>17</java.version>` (se conserva la versión de Boot que trae); `.\mvnw.cmd -q -DskipTests package` compila; hay un commit en `main`.
**Comandos de verificación:** `.\mvnw.cmd -q -DskipTests package`; `git log --oneline -1`
**Evidencia:** `.\mvnw.cmd -q -DskipTests package` exit code 0; `git log --oneline -1` -> `d11383e chore: proyecto inicial generado con Spring Initializr`.

## F1 · api-base — Base del proyecto y configuración
**Estado:** HECHA (2026-10-28, commit d4eed8f)
**Objetivo:** proyecto compila y testea con configuración por variables de entorno.
**Tareas sugeridas:**
- Verificar `pom.xml` (Java 17; conservar la versión de Spring Boot que trae) y agregar `com.h2database:h2` scope `test` (la versión la gestiona Boot). `validation` ya está.
- `application.properties` exactamente como ALCANCE §5.
- `src/test/resources/application.properties` con H2 `jdbc:h2:mem:denuncias;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`, `ddl-auto=create-drop`.
- `.gitignore` según ALCANCE §5.
- Crear subpaquetes vacíos con `package-info.java` no es necesario; se crean con la F2.
**Criterios de aceptación:** `./mvnw -q test` verde (el test `contextLoads` arranca con H2, sin PostgreSQL).
**Comandos de verificación:** `./mvnw -q test`
**Evidencia:** .\mvnw.cmd -B -ntp test verde en 5.48s (Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS). Commit: d4eed8f.

## F2 · api-denuncias — Las 4 capas y los 5 endpoints
**Estado:** HECHA (2026-10-28, commit 7e235c4)
**Objetivo:** contrato completo de ALCANCE §4 funcionando y probado.
**Tareas sugeridas (TDD, una por commit):**
- T1 `entity/Denuncia` (tabla `denuncias`, columnas y longitudes de §4.1, `@PrePersist` para `fecha` y `estado=1`).
- T2 `repository/DenunciaRepository` (paginación nativa `LIMIT/OFFSET` orden `id DESC`; `findByDniOrderByIdDesc`).
- T3 `dto/DenunciaRequest` + `DenunciaResponse` + mapeo; `exception/*` + `GlobalExceptionHandler`.
- T4 `service/DenunciaService` + `DenunciaServiceImpl` (registrar, listar(offset,limit), obtener, porDni, atender).
- T5 `controller/DenunciaController` con E1–E5 y slash final.
- T6 `DenunciaControllerIT` (MockMvc) cubriendo: 201 creación; 400 dni inválido; 400 tipo inválido; lista paginada 0/5 y 5/5 con 7 registros (5 y 2 elementos, orden desc); 400 limit=0 y limit=51; 200/404 por id; DNI con resultados y vacío; 400 DNI de 7 dígitos; PUT 200 estado=0, idempotente, 404; `/denuncias` y `/denuncias/` ambos responden.
**Criterios de aceptación:** todos los casos de T6 verdes.
**Comandos de verificación:** `./mvnw -q test`
**Evidencia:** `.\mvnw.cmd -B -ntp test` verde (28 tests pasando, BUILD SUCCESS en 7.87s). Commit: 7e235c4.

## F3 · api-docker — Contenedor listo para Render
**Estado:** HECHA (2026-10-28, commit 38ef1d9) — [MANUAL] Docker no disponible localmente
**Objetivo:** imagen Docker que arranca con `PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
**Tareas sugeridas:** `Dockerfile` multi-stage (ALCANCE §5), `.dockerignore`, sección "Despliegue en Render" en `README.md` con las 4 variables de entorno y el formato `jdbc:postgresql://<host>:5432/<db>`.
**Criterios de aceptación:** `docker build` exitoso; si hay Docker y PostgreSQL local, `docker run -e ...` responde `GET /denuncias/` con 200.
**Comandos de verificación:** `docker build -t denunciaservice .` (si Docker no está disponible → anotar `[MANUAL]` y dar la feature por HECHA con esa nota).
**Evidencia:** —

## F4 · api-tooling — Postman, smoke test y documentación
**Estado:** PENDIENTE
**Objetivo:** probar la API local o remota con un comando y preparar la exposición.
**Tareas sugeridas:**
- `postman/denunciaservice.postman_collection.json` (variable `baseUrl`, 5 requests con ejemplos).
- `scripts/smoke.sh <BASE_URL>`: crea una denuncia, la obtiene, lista `offset=0&limit=5`, busca por su DNI, la atiende y verifica `estado=0`; usa `--max-time 90`; exit≠0 si algo falla. Ejecutable (`chmod +x`).
- `README.md`: requisitos, ejecutar local, tests, Docker, Render, endpoints.
- `docs/EXPLICACION.md`: arquitectura por capas (diagrama ASCII), recorrido de una petición POST de punta a punta, paginación offset/limit, manejo de errores, por qué variables de entorno, cómo funciona el Dockerfile, 8–10 preguntas probables del profesor con respuesta corta.
**Criterios de aceptación:** si hay PostgreSQL local (o Docker para levantar uno con `docker run -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=denuncias -p 5432:5432 -d postgres:16`), la app corriendo con `./mvnw spring-boot:run` pasa `scripts/smoke.sh http://localhost:8080` con exit 0. Si no hay ni PostgreSQL ni Docker, basta `bash -n` + tests MockMvc de F2; la prueba real del script queda para F5. No cambiar el scope de `h2`.
**Comandos de verificación:** `bash -n scripts/smoke.sh`; `scripts/smoke.sh http://localhost:8080` si hay BD local.
**Evidencia:** —

## F5 · api-deploy-check — Verificación del despliegue en Render
**Estado:** BLOQUEADA — `[MANUAL]` requiere que el usuario cree la BD y el Web Service en Render y escriba la URL abajo.
**URL Render:** `https://__________.onrender.com`
**Objetivo:** demostrar que la API pública funciona.
**Tareas:** cuando la URL esté escrita arriba, ejecutar `scripts/smoke.sh <URL>`; si falla, diagnosticar con `curl -i` y corregir código (no la configuración de Render; eso se reporta como `[MANUAL]`).
**Criterios de aceptación:** smoke contra Render con exit 0; URL añadida a `README.md`.
**Evidencia:** —

---

## Decisiones
<!-- El agente agrega aquí: fecha · feature · decisión · porqué (una línea) -->

## Estado final
<!-- El agente escribe aquí el resumen al terminar el loop y los [MANUAL] pendientes -->
