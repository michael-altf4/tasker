

# NoMoreProcrastination.app - demo project
[Русская версия / Russian version - README_RU.md](README_RU.md)

> **Project goal** - to demonstrate the practical use of modern development technologies and practices: from local build to CI/CD, monitoring, and logging.  
> The application implements a simple yet complete development cycle: **backend + frontend + security + testing + deployment**.

**Live demo**: [https://tasker-tlu7.onrender.com/](https://tasker-tlu7.onrender.com/)
> **Disclaimer:** The project is hosted on **Render's free tier**.  
> After being idle, the container takes **~2–3 minutes to start** on the first request.  
> If the page loads slowly - please wait; this is **normal behavior** for free-tier hosting.

---

## Table of Contents

- [About the Project](#about-the-project)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Local Setup](#local-setup)
- [CI / CD](#ci--cd)
- [Monitoring](#monitoring)
- [Logging](#logging)
- [Testing](#testing)
- [API Documentation](#api-documentation)

---

## About the Project

**NoMoreProcrastination.app** is a task management web application (To-Do List) featuring:
- User authentication
- Full CRUD operations for tasks
- Priority levels (Low / Medium / High)
- Completion status tracking
- Automatic creation timestamp

The application consists of:
- **Backend**: Spring Boot (REST API)
- **Frontend**: Vanilla JavaScript + Thymeleaf
- **Database**: PostgreSQL (production) / H2 (testing)

---

## Architecture


1. User interacts with the UI (Thymeleaf + JS).
2. Frontend sends requests to `/api/**`.
3. Spring Boot processes requests and validates authentication.
4. Data is persisted in PostgreSQL.

---

## Technologies

| Category         | Stack                                                                 |
|------------------|----------------------------------------------------------------------|
| **Backend**      | Spring Boot 3, Spring Security, JPA / Hibernate                      |
| **Database**     | PostgreSQL (prod), H2 (tests), **Flyway (schema migrations)**        |
| **Frontend**     | Thymeleaf, Vanilla JavaScript, CSS                                   |
| **Build Tool**   | **Gradle**                                                           |
| **Container**    | Docker                                                               |
| **Deployment**   | Render (free hosting)                                                |
| **CI/CD**        | Jenkins (local), Render API                                          |
| **Monitoring**   | Spring Boot Actuator + Prometheus                                    |
| **Logging**      | SLF4J + Logback                                                      |

> **Flyway** is a database migration tool.  
> All schema changes (tables, indexes, etc.) are defined in SQL files located in `src/main/resources/db/migration/`.  
> On startup, Flyway automatically applies migrations in the correct order.

---

## Local Setup

1. Ensure you have installed:
    - Java 17+
    - Gradle

2. Run the application:
   ```bash
   ./gradlew bootRun
   ```

3. Open your browser at [http://localhost:8080](http://localhost:8080)

To run, you need PostgreSQL, specify the settings in application-local.properties.
Flyway applies migrations from `db/migration/` on startup.

---

## CI / CD

The project supports a **full continuous integration and delivery pipeline**:

### 1. **Jenkins Pipeline** (local)
You can launch Jenkins with:
```bash
docker run -p 8080:8080 -p 50000:50000 jenkins/jenkins:lts
```

The pipeline includes the following stages:
- **Build**: `./gradlew build -x test`
- **Test**:
    - Unit tests (`./gradlew test --tests "*Test"`)
    - API tests (integration tests using `TestRestTemplate`)
    - (planned) UI tests via Selenium
- **Deploy**: Trigger Render API for automatic deployment

### 2. **Render (Production)**
- Build is triggered from GitHub (main branch)
- Deployment is performed via Render’s REST API (configured in Jenkins)

---

## Monitoring

**Spring Boot Actuator** is integrated for diagnostics:

- **Health-check**:
    - Locally: `http://localhost:8081/actuator/health`
    - Production: [`https://tasker-tlu7.onrender.com/actuator/health`](https://tasker-tlu7.onrender.com/actuator/health)  
      Includes database connectivity status (`UP`/`DOWN`).

- **Prometheus-compatible metrics**:
    - Locally: `http://localhost:8081/actuator/prometheus`
    - Production: [`https://tasker-tlu7.onrender.com/actuator/prometheus`](https://tasker-tlu7.onrender.com/actuator/prometheus)

> Actuator runs on a **dedicated port `8081`** locally and is accessible without authentication.  
> On Render, it shares the main port (`8080`), but monitoring endpoints remain publicly accessible.

---

## Logging

- SLF4J + Logback is used.
- Logs are written to:
    - **Console** (`stdout`)
    - File: **`logs/app.log`**
- In **production**:
    - Logs are available via Render’s web interface (**Logs** tab)
    - When running locally in Docker, view logs with:
      ```bash
      docker logs <container>
      ```

---

## Testing

The project includes three testing layers:

| Level            | Technology                             | Examples                                      |
|------------------|----------------------------------------|----------------------------------------------|
| **Unit Tests**   | JUnit 5 + Mockito                      | `TodoService` logic, validation              |
| **API Tests**    | `@SpringBootTest` + `TestRestTemplate` | Task creation/retrieval via REST API         |
| **UI Tests**     | Selenium (planned)                     | Browser-based user scenario automation       |

Run all tests:
```bash
./gradlew test
```

---

## API Documentation

The project supports auto-generated API documentation via OpenAPI 3.0.

- **Interactive Swagger UI**:  
  [https://tasker-tlu7.onrender.com/swagger-ui.html](https://tasker-tlu7.onrender.com/swagger-ui.html)

- **Machine-readable OpenAPI spec (JSON)**:  
  [https://tasker-tlu7.onrender.com/v3/api-docs](https://tasker-tlu7.onrender.com/v3/api-docs)