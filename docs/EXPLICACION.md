# EXPLICACION — denunciaservice (API REST Spring Boot)

> Documento de apoyo técnico y conceptual para la sustentación del proyecto.
> Explica la arquitectura, flujo de datos, decisiones de diseño y preguntas típicas del evaluador.

---

## 1. Arquitectura por Capas

La aplicación sigue el patrón clásico de **Arquitectura en Capas (Layered Architecture)**, separando estrictamente responsabilidades:

```text
       +---------------------------------------------------------------+
       |                      CLIENTE HTTP / APP                       |
       |             (Android Retrofit / Postman / curl)               |
       +---------------------------------------------------------------+
                                      |
                                      v
  ============================ CAPA CONTROLLER =========================
  |  DenunciaController                                                |
  |  - Expone rutas REST (/denuncias y /denuncias/)                    |
  |  - Valida payload (@Valid DenunciaRequest)                         |
  |  - Controla códigos HTTP (200, 201, 400, 404)                      |
  |  - @RestControllerAdvice (GlobalExceptionHandler captura fallos)   |
  ======================================================================
                                      |
                                      v
  ============================= CAPA SERVICE ===========================
  |  DenunciaService / DenunciaServiceImpl                             |
  |  - Lógica de negocio (reglas de estado, fecha automática)          |
  |  - Validaciones de dominio (offset >= 0, limit 1..50, DNI 8 dígitos)|
  |  - Transaccionalidad (@Transactional)                              |
  |  - Mapeo de/hacia DTOs (DenunciaRequest -> Entity -> Response)     |
  ======================================================================
                                      |
                                      v
  =========================== CAPA REPOSITORY ==========================
  |  DenunciaRepository (Spring Data JPA)                              |
  |  - Operaciones CRUD automáticas                                    |
  |  - Paginación nativa SQL: SELECT * ... LIMIT :limit OFFSET :offset |
  |  - Búsqueda por DNI ordenada: findByDniOrderByIdDesc               |
  ======================================================================
                                      |
                                      v
  ============================= CAPA ENTITY ============================
  |  Denuncia (@Entity, @Table("denuncias"))                           |
  |  - Mapeo Objeto-Relacional (ORM) con Hibernate                     |
  |  - @PrePersist: autollenado de fecha (America/Lima) y estado=1     |
  ======================================================================
                                      |
                                      v
  ========================= BASE DE DATOS ==============================
  |  PostgreSQL (Render en la nube) / H2 en memoria (en tests)        |
  ======================================================================
```

---

## 2. Recorrido de una Petición POST de Punta a Punta

Flujo detallado al registrar una denuncia:

1. **Recepción:** El cliente envía `POST /denuncias/` con un JSON (`dni`, `tipo`, `direccion`, `descripcion`).
2. **Filtrado y Despacho:** Spring WebMvc enruta la petición a `DenunciaController.registrar()`.
3. **Validación Declarativa:** `@Valid` en `DenunciaRequest` verifica con Hibernate Validator:
   - `dni`: exactamente 8 dígitos (`^\d{8}$`).
   - `tipo`: uno de `ROBO`, `ASALTO`, `VANDALISMO`, `ACOSO`, `OTRO`.
   - `direccion`: no vacía, máx. 200 caracteres.
   - `descripcion`: no vacía, máx. 255 caracteres.
   - *Si falla:* se interrumpe y `GlobalExceptionHandler` retorna HTTP `400` con `detalles`.
4. **Capa de Servicio:** El controlador invoca `denunciaService.registrar(request)`.
5. **Normalización y Negocio:**
   - Convierte el request a entidad `Denuncia`.
   - Normaliza `tipo` a mayúsculas (`.toUpperCase()`).
   - Fija `estado = 1` (pendiente).
   - Asigna fecha actual en zona `America/Lima` truncada a segundos si no vino dada.
6. **Persistencia:** Se invoca `denunciaRepository.save(denuncia)`. Hibernate ejecuta `INSERT INTO denuncias ...` y obtiene el ID generado por la secuencia PostgreSQL (`IDENTITY`).
7. **Transformación a DTO:** Se convierte la entidad a `DenunciaResponse` (un `record` inmutable).
8. **Respuesta HTTP:** El controlador devuelve HTTP `201 Created` con el JSON de `DenunciaResponse`.

---

## 3. Paginación por Offset y Limit

A diferencia de la paginación estándar de Spring (que usa números de página `page=0,1,2...`), el requerimiento exige **desplazamiento de registros** (`offset` y `limit`):

- `offset`: cuántos registros saltar desde el inicio (default: `0`).
- `limit`: cuántos registros traer (default: `5`, máx. `50`).
- **Implementación SQL Nativa:**
  ```sql
  SELECT * FROM denuncias ORDER BY id DESC LIMIT :limit OFFSET :offset
  ```
- **Ventajas:**
  - Control exacto para scroll infinito o botones "Cargar 5 siguientes" en la app móvil.
  - Orden decreciente estricto (`id DESC`) asegurando que las denuncias más recientes aparezcan primero.
  - Portabilidad entre PostgreSQL, MySQL y H2.

---

## 4. Manejo Global de Errores

Implementado mediante `@RestControllerAdvice` (`GlobalExceptionHandler`):

- **400 Bad Request:**
  - Errores de validación de Bean Validation (`MethodArgumentNotValidException`): produce un mapa de campos y sus mensajes específicos en `"detalles"`.
  - Parámetros inválidos de paginación o DNI (`IllegalArgumentException`): produce un mensaje descriptivo.
- **404 Not Found:**
  - Registro inexistente (`DenunciaNotFoundException`): devuelve mensaje claro con el ID solicitado.
- **Formato Estándar JSON:**
  ```json
  {
    "timestamp": "2026-10-28T09:15:00",
    "status": 400,
    "error": "Bad Request",
    "mensaje": "Datos inválidos",
    "detalles": {
      "dni": "El DNI debe tener exactamente 8 dígitos"
    }
  }
  ```

---

## 5. Configuración por Variables de Entorno (Sin Credenciales)

Por seguridad (principios de *Twelve-Factor App*):
- No se suben contraseñas ni URLs con credenciales al repositorio Git.
- `application.properties` define defaults seguros o variables inyectables:
  - `PORT`: `${PORT:8080}`
  - `DB_URL`: `${DB_URL:jdbc:postgresql://localhost:5432/denuncias}`
  - `DB_USERNAME`: `${DB_USERNAME:postgres}`
  - `DB_PASSWORD`: `${DB_PASSWORD:}`
- En Render, estas 4 variables se configuran en el panel del Web Service. En tests, `src/test/resources/application.properties` levanta automáticamente H2 en memoria (`MODE=PostgreSQL`) sin requerir Postgres real.

---

## 6. Dockerfile Multi-Stage

El archivo `Dockerfile` optimiza el tamaño de la imagen y la seguridad en Render:

```dockerfile
# Etapa 1: Build (con JDK y Maven completo)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# Etapa 2: Runtime (imagen mínima solo con JRE)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

- **Por qué Multi-Stage:**
  - La primera etapa descarga dependencias y compila el código.
  - La segunda etapa solo copia el archivo `.jar` resultante sobre una imagen ligera `eclipse-temurin:17-jre`.
  - El código fuente, Maven y archivos temporales quedan fuera de la imagen final, reduciendo el consumo de memoria y ancho de banda en el plan gratuito de Render.

---

## 7. Preguntas Probables del Profesor con Respuesta Corta

1. **¿Por qué usar DTOs en lugar de retornar directamente la entidad `@Entity`?**
   *R:* Para desacoplar el modelo de base de datos del contrato público de la API. Evita sobreexposición de campos internos, previene problemas de serialización recursiva y permite versionar la API sin alterar la tabla.

2. **¿Por qué los DTOs están definidos como `record` de Java?**
   *R:* Porque son inmutables por diseño, compactos, generan automáticamente constructor, getters, `equals`, `hashCode` y `toString`, y no requieren bibliotecas externas como Lombok.

3. **¿Cómo garantizan que la API soporte URLs con y sin slash final (`/denuncias` y `/denuncias/`)?**
   *R:* Se declara `@RequestMapping({"/denuncias", "/denuncias/"})` y en los métodos `@PostMapping({"", "/"})`, porque Spring Boot 3 y 4 desactivaron por defecto el "trailing slash matching" por seguridad.

4. **¿Cómo se truncó la fecha para evitar milisegundos o nanosegundos en el JSON?**
   *R:* Se aplica `.truncatedTo(ChronoUnit.SECONDS)` al generar o recibir la fecha, y se anota con `@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")`.

5. **¿Por qué el endpoint PUT `/denuncias/{id}` es idempotente?**
   *R:* Porque sin importar cuántas veces se invoque consecutivamente, el estado final de la denuncia siempre será `0` (atendida) y devolverá HTTP 200 sin efectos secundarios adicionales.

6. **¿Cómo se gestionan las transacciones en la capa de servicio?**
   *R:* Usando `@Transactional`. Para operaciones de lectura se utiliza `@Transactional(readOnly = true)` para optimizar el rendimiento y el uso de conexiones en Hibernate.

7. **¿Cómo se garantiza que no haya contraseñas expuestas en GitHub?**
   *R:* Las credenciales se leen exclusivamente de variables de entorno mediante `${DB_PASSWORD:}` y `.gitignore` excluye archivos `.env` y perfiles locales.

8. **¿Por qué se usa H2 en tests y PostgreSQL en producción?**
   *R:* H2 en memoria permite ejecutar tests unitarios y de integración de forma rápida, aislada y sin necesidad de tener un motor de base de datos externo instalado. Con `MODE=PostgreSQL` emula la sintaxis de Postgres.

9. **¿Qué ventaja ofrece la inyección por constructor sobre `@Autowired` en campos?**
   *R:* Facilita las pruebas unitarias (se pueden pasar mocks sin Reflection), hace explícitas las dependencias de la clase y asegura la inmutabilidad de los servicios mediante atributos `final`.

10. **¿Cómo funciona el script `scripts/smoke.sh`?**
    *R:* Realiza peticiones `curl` automáticas y secuenciales a los 5 endpoints (crea denuncia, la obtiene por ID, lista paginado, busca por DNI y la atiende), verificando los códigos HTTP y fallando con código distinto de cero si alguna aserción no se cumple.
