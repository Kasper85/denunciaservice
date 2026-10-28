# AGENTS.md — denunciaservice (API REST Spring Boot)

Eres el agente que construye la API del examen **de principio a fin, en loop, sin pedir confirmaciones**.
Trabajas con el flujo **ODD de gentle-ai** (Organic Driven Development). SDD **no** se usa en este repo salvo que el usuario lo pida explícitamente.

## 0. Entorno: Windows + PowerShell (Antigravity)

- Maven: `.\mvnw.cmd ...` (nunca `./mvnw`). Donde este documento o PLAN.md diga `./mvnw`, usa `.\mvnw.cmd`.
- HTTP: `curl.exe` (en PowerShell `curl` puede ser un alias de `Invoke-WebRequest`).
- Scripts `.sh`: ejecútalos con `bash scripts\smoke.sh <URL>` (bash de Git for Windows). Escríbelos con finales de línea LF y agrega `*.sh text eol=lf` a `.gitattributes`.
- Descomprimir: `tar -xf archivo.zip`. Encadenar comandos: usa `;` (no `&&`, que falla en PowerShell 5).
- El `{apellido}` es el del paquete existente en `src/main/java` (hoy `com.RodriguezRuiz.denunciaservice`); úsalo tal cual, con sus mayúsculas, para el código. Para URLs y nombres de servicio en Render va en minúsculas.
- Este proyecto usa **Spring Boot 4.x**: ten en cuenta las diferencias de nombres de ALCANCE §5 (starters `webmvc`, paquetes de test, Jackson 3). Ante un error de compilación por un import, busca el nuevo paquete en las dependencias en vez de bajar la versión.

## 1. Fuentes de verdad (léelas al iniciar cada sesión y al reanudar)

1. `ALCANCE.md` — requisitos funcionales y técnicos **cerrados**. Gana sobre todo lo demás. **No lo modifiques.**
2. `PLAN.md` — cola ordenada de features, estado y decisiones. Es el motor del loop.
3. `odd/tasks/<feature>.md` — documento ODD de la feature en curso (lo creas tú).
4. Engram (si está disponible): tema `odd/<feature>/tasks`. Si Engram no responde, sigue solo con archivos y anota "mirror Engram pendiente".

## 2. Autorización permanente (ya concedida por el usuario)

- ✅ Implementar **todas** las features de `PLAN.md` en orden, sin preguntar "¿continúo?".
- ✅ Crear/editar/borrar archivos dentro de este repositorio.
- ✅ Ejecutar `.\mvnw.cmd`, `git`, `docker build`, `bash` y `curl.exe`.
- ✅ Descargar el proyecto base desde `https://start.spring.io` (feature F0).
- ✅ Crear ramas `feat/<id>`, hacer commits, y al cerrar cada feature hacer `git merge --ff-only` a `main`.
- ✅ `git push origin main` y `git push origin feat/<id>` **solo** si `git remote get-url origin` existe. Si no existe, sigue local y anótalo como `[MANUAL]` en PLAN.md.
- ❌ Prohibido: tocar archivos fuera del repo, leer o escribir `.env` / credenciales, cambiar el `{apellido}` o el paquete raíz (respeta mayúsculas: `com.RodriguezRuiz...`), cambiar la versión de Spring Boot del `pom.xml`, agregar dependencias no listadas en ALCANCE §5, `git push --force`, reescribir historial de `main`.

## 3. Política de decisiones (para no bloquear el loop)

- **No hagas preguntas al usuario.** Si algo no está en ALCANCE.md, elige la opción más simple que cumpla la rúbrica (ALCANCE §7), aplícala y regístrala en `PLAN.md › Decisiones` con una línea de porqué.
- Única excepción: lo marcado `[MANUAL]` (requiere cuentas, navegador o datos del usuario). Ahí **no inventes valores**: deja la feature en `BLOQUEADA`, explica exactamente qué falta y pasa a la siguiente feature no bloqueada.
- Si un test falla 3 veces seguidas por la misma causa, detente en esa tarea, deja el diagnóstico en el documento ODD y en `PLAN.md`, y continúa con la siguiente feature independiente si la hay.

## 4. Configuración ODD para este repo

| Parámetro | Valor |
|---|---|
| Workflow | ODD (SDD solo bajo pedido explícito) |
| TDD | **ON** — fuente: este AGENTS.md. RED observado → GREEN → REFACTOR |
| Runner | `./mvnw -q test` (Windows: `mvnw.cmd -q test`) |
| Test de una clase | `./mvnw -q test -Dtest=NombreClaseTest` |
| Build | `./mvnw -q -DskipTests package` |
| Rama por feature | `feat/<id>` desde `main` |
| Commits | Conventional Commits en español: `feat(api): ...`, `test(api): ...`, `chore(docker): ...`, `docs: ...` |
| Estrategia de entrega | `single-pr` — una rama por feature, sin PRs encadenados; se integra con `merge --ff-only` a `main` |
| Tamaño | ~400 líneas por tarea es orientativo, nunca motivo para partir artificialmente |
| RDD | Respeta la configuración del usuario. Si RDD está ON y pide consentimiento, **concédelo tú mismo para riesgo medium** y continúa; para **high** ejecútalo y continúa con su resultado |

## 5. El loop (repite hasta terminar)

```
1. git status limpio? si no → commitea o descarta solo lo tuyo, nunca lo del usuario.
2. Lee PLAN.md → toma la primera feature con estado PENDIENTE o EN_CURSO (no BLOQUEADA).
3. Si EN_CURSO → reanuda: lee odd/tasks/<id>.md + Engram, reconcilia con el código real.
   Si PENDIENTE → git checkout -b feat/<id> main; explora; crea odd/tasks/<id>.md
   (objetivo, alcance, checklist con IDs <id>-T1.., criterios de aceptación copiados de PLAN.md);
   marca la feature EN_CURSO en PLAN.md.
4. Por cada tarea: test RED → código GREEN → refactor → ./mvnw -q test verde → commit.
   Marca la casilla solo con evidencia observada (salida del comando) y anota el hash del commit.
5. Al terminar las tareas: ejecuta TODOS los "Comandos de verificación" de la feature en PLAN.md.
   Todo verde → git checkout main && git merge --ff-only feat/<id> → push si hay origin
   → marca HECHA en PLAN.md con fecha y hash → commit "docs(plan): cierra <id>".
6. Vuelve al paso 1.
FIN: cuando no quedan PENDIENTE/EN_CURSO → escribe en PLAN.md › Estado final un resumen
     y la lista exacta de pasos [MANUAL] que faltan; responde con ese resumen y DETENTE.
```

## 6. Convenciones de código

- Paquete raíz: el existente `com.<apellido>.denunciaservice` (detéctalo leyendo `src/main/java`). Subpaquetes: `entity`, `repository`, `service`, `controller`, `dto`, `exception`.
- Inyección por constructor (sin `@Autowired` en campos). Sin Lombok.
- DTOs = `record`. Validación con `jakarta.validation` en `DenunciaRequest`.
- `@RestControllerAdvice` (`GlobalExceptionHandler`) produce el JSON de error de ALCANCE §4.3.
- `DenunciaNotFoundException` → 404.
- Controller: `@RequestMapping({"/denuncias", "/denuncias/"})` y rutas hijas que acepten slash final.
- Tests de integración: `@SpringBootTest` + `@AutoConfigureMockMvc` con H2 (`src/test/resources/application.properties`).
- Nunca credenciales en código ni en `application.properties`; solo `${VAR:default}`.

## 7. Definition of Done global

- `./mvnw -q test` verde.
- `docker build -t denunciaservice .` exitoso (si Docker no está instalado: anótalo como `[MANUAL]`, no es bloqueo).
- `git grep -nI -E "(password|secret)\s*=\s*[^$]"` no devuelve credenciales reales.
- `PLAN.md` sin features PENDIENTE (solo HECHA o BLOQUEADA por `[MANUAL]`).
