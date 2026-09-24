# Attendance Management API

Spring Boot 3.3.3 REST API for tracking employee attendance and leave. Java 21, Maven, MySQL, JWT authentication.

The React frontend that consumes this API lives in
[boopathi-003/attendance_management](https://github.com/boopathi-003/attendance_management).

## Stack

| | |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.3.3 (Web, Data JPA, Security, Actuator) |
| Database | MySQL 8 |
| Auth | JWT (jjwt 0.12.6), HS256, BCrypt password hashing |
| Build | Maven (wrapper included) |
| Tests | JUnit 5 + Mockito — no Spring context, no database |

## Getting started

**Prerequisites**

- JDK 21 — the build sets `<java.version>21</java.version>` and fails on anything lower with
  *"release version 21 not supported"*. Point `JAVA_HOME` at a JDK 21 install; the Maven wrapper honours it.
- MySQL reachable at `localhost:3306`

**Run**

```bash
./mvnw spring-boot:run          # macOS/Linux
.\mvnw.cmd spring-boot:run      # Windows PowerShell
```

The API starts on `http://localhost:8080`. The schema is created automatically —
`createDatabaseIfNotExist=true` plus `spring.jpa.hibernate.ddl-auto=update` — so there are no migrations to run. The
JPA entities under `src/main/java/.../table/` are the schema definition.

**Build and test**

```bash
./mvnw clean package            # build the jar
./mvnw test                     # run all tests
./mvnw test -Dtest=UserServiceTest
```

Tests need neither a database nor a Spring context; they are plain JUnit 5 with Mockito mocks.

## Configuration

Settings live in `src/main/resources/application.properties`. There are no profile-specific files.

| Property | Purpose |
| --- | --- |
| `spring.datasource.url` / `.username` / `.password` | MySQL connection |
| `spring.jpa.hibernate.ddl-auto` | `update` — Hibernate reconciles the schema on startup |
| `jwt.secret` | Base64 HS256 signing key |
| `jwt.expiration` | Token lifetime in milliseconds (currently 10 hours) |

> **Note:** the datasource password and JWT secret are currently committed to this repository, which is why it is
> private. They should be moved to environment variables before the repo is made public — and rotated, since they
> are already in the git history.

## Creating the first account

There is no registration UI. Create a login with:

```bash
curl -X POST http://localhost:8080/addregister \
  -H "Content-Type: application/json" \
  -d '{"userName":"admin","password":"admin123","role":"admin"}'
```

Then log in — the response body is the raw JWT, not JSON:

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"admin","password":"admin123"}'
```

Send it as `Authorization: Bearer <token>` on every other request. Store roles **lowercase** (`admin`, `user`) —
the frontend compares the JWT `role` claim against the literal string `'admin'`.

## API

| Method | Path | Access |
| --- | --- | --- |
| POST | `/login` | public |
| POST | `/addregister` | public |
| GET | `/register`, `/register/id/{id}` | ADMIN / ADMIN+USER |
| DELETE | `/register/id/{id}` | ADMIN |
| GET | `/user` | ADMIN, USER |
| GET | `/user/id/{id}`, `/user/roll/{roll}`, `/user/department/{department}`, `/user/unmarked` | ADMIN, USER |
| POST | `/user/adduser` | ADMIN |
| DELETE | `/user/id/{id}` | ADMIN (soft delete) |
| GET | `/attendance`, `/attendance/id/{id}` | ADMIN, USER |
| GET | `/attendance/present`, `/absent`, `/sick`, `/planned-leave` | ADMIN, USER |
| POST | `/attendance/add-attendance` | ADMIN |
| DELETE | `/attendance/id/{id}` | ADMIN |
| GET | `/leaverecord`, `/leaverecord/id/{id}`, `/leaverecord/{date}` | ADMIN, USER |
| DELETE | `/leaverecord/id/{id}` | ADMIN |

Authorization is enforced twice — URL rules in `SecurityConfig` and `@PreAuthorize` on the controller methods.

## Project layout

```
controller/        HTTP endpoints
services/          business logic (+ JwtService, MyUserDetailService)
mapper/            hand-written entity <-> DTO mapping
repository/        Spring Data JPA interfaces
table/             JPA entities (note: not "entity/")
dto/               request/response shapes + the ApiResponse envelope
configure/         SecurityConfig, JwtFilter
exceptionhandler/  @RestControllerAdvice + custom exceptions
```

## Documentation

- [`CLAUDE.md`](CLAUDE.md) — architecture notes, conventions, and the known rough edges
- [`LEARNING-ROADMAP.md`](LEARNING-ROADMAP.md) — a 15-step guide to Spring Boot built around this codebase, one
  concept per step with the files and exercises for each
