# denunciaservice — API REST de Serenazgo

API REST desarrollada con Spring Boot 4 y Java 17 para el registro y gestión de denuncias ciudadanas de serenazgo.

## Requisitos

- Java 17 (Eclipse Temurin recomendado)
- Git
- Docker (opcional para ejecución en contenedor)
- PostgreSQL (o H2 en memoria para pruebas automáticas)

## Endpoints

Base: `/denuncias` (responde con y sin slash final `/denuncias/`)

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/denuncias/` | Registra una nueva denuncia (`estado=1`) |
| `GET` | `/denuncias/?offset=0&limit=5` | Lista denuncias paginadas por desplazamiento en orden `id DESC` |
| `GET` | `/denuncias/{id}` | Obtiene una denuncia por su ID |
| `GET` | `/denuncias/ciudadano/{dni}` | Lista las denuncias de un ciudadano por DNI en orden `id DESC` |
| `PUT` | `/denuncias/{id}` | Marca una denuncia como atendida (`estado=0`, idempotente) |

## Ejecución Local

Para compilar y ejecutar localmente con Maven:

```powershell
.\mvnw.cmd spring-boot:run
```

Para ejecutar las pruebas:

```powershell
.\mvnw.cmd -B -ntp test
```

## Despliegue en Render

El servicio se despliega como un **Web Service (Docker)** en Render conectado a una base de datos PostgreSQL:

### Variables de Entorno en Render

| Variable | Descripción | Ejemplo |
|---|---|---|
| `PORT` | Puerto HTTP expuesto | `8080` (o asignado automáticamente por Render) |
| `DB_URL` | URL JDBC de conexión a PostgreSQL | `jdbc:postgresql://dpg-xxxxxxxx-a:5432/denuncias_xxxx` |
| `DB_USERNAME` | Usuario de la base de datos | `denuncias_user` |
| `DB_PASSWORD` | Contraseña de la base de datos | `<secreto>` |

> **Nota:** La URL de base de datos debe tener el formato `jdbc:postgresql://<host>:5432/<db>`.
