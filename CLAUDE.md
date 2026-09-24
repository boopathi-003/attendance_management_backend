# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 3.3.3 REST API for attendance management. Java 21, Maven, MySQL, JWT auth.
Base package: `com.attendance.attendance_management`.

The React frontend for this API lives in a **separate repository** at `D:\POC\attendance_management` and has its own
`CLAUDE.md`. Features normally need changes on both sides — see "Frontend contract" below.

New to Spring Boot? `LEARNING-ROADMAP.md` walks this codebase concept by concept in build order.

## Commands

Use the Maven wrapper — `mvn` may not be on PATH: `.\mvnw.cmd` (PowerShell) or `./mvnw` (bash).

```powershell
.\mvnw.cmd spring-boot:run                          # run on :8080 (devtools is on the classpath → restart on recompile)
.\mvnw.cmd clean package                            # build jar
.\mvnw.cmd test                                     # all tests
.\mvnw.cmd test -Dtest=UserControllerTest           # single class
.\mvnw.cmd test -Dtest=UserControllerTest#getUserById  # single method
```

**JDK 21 is required** (`pom.xml:30`). If `java -version` reports below 21, point `JAVA_HOME` at a JDK 21 install or compilation fails with "release version 21 not supported". `mvnw` honours `JAVA_HOME`.

**Runtime prerequisite:** MySQL reachable at `localhost:3306`, plus two environment variables — `DB_PASSWORD` and `JWT_SECRET`. Neither has a fallback in `application.properties`, so the app fails at startup without them. `DB_URL`, `DB_USERNAME` and `JWT_EXPIRATION` are optional and default to local values. A gitignored `application-local.properties` (see the `.example` beside it) works as an alternative to environment variables. The schema is created automatically (`createDatabaseIfNotExist=true` plus `spring.jpa.hibernate.ddl-auto=update`) — there are no migrations, no `schema.sql`/`data.sql`, and no profile-specific property files, so the entities in `table/` *are* the schema definition.

**Tests need neither Spring nor a database.** All tests are plain JUnit 5 + Mockito with `@Mock`/`@InjectMocks` — mostly `@ExtendWith(MockitoExtension.class)`, with `AttendanceServiceTest` using `MockitoAnnotations.openMocks` instead. There is no `@SpringBootTest`, `@WebMvcTest`, or `@DataJpaTest` anywhere, and no context-load test. Controller tests invoke controller methods directly; MockMvc is not used.

The `Dockerfile` in the root is empty — container builds are not set up.

## Architecture

### Layering

`controller → services → mapper → repository → table` (entities live in `table/`, not `entity/`). Constructor injection everywhere via Lombok `@RequiredArgsConstructor`.

Mappers are hand-written `@Component` classes, not MapStruct, and some of them hit the database: `AttendanceMapper.setEntity` resolves the `UserInfo` via `UserRepository` rather than trusting the incoming DTO. Method naming is inconsistent between mappers (`setDto`/`setEntity` vs `toDto`/`toEntity`).

### Two unrelated user models

This is the most common source of confusion:

- `UserAuth` → table `user`. Login credentials + `role` string. Implements `UserDetails`.
- `UserInfo` → table `user_info`. Domain person: `name`, `roll`, `department`, `isActive`, `isMarked`.

There is no foreign key or join between them. `AttendanceInfo` and `LeaveInfo` reference `UserInfo`; authentication and `@PreAuthorize` operate on `UserAuth`.

### Auth flow

`POST /login` → `UserAuthService.verifyLogin` checks the user exists, is active, and the bcrypt password matches, then delegates to `AuthenticationManager` → `JwtService.getToken(username, role)` mints an HS256 token carrying a `role` claim. On subsequent requests `JwtFilter` (registered before `UsernamePasswordAuthenticationFilter`) reads the `Bearer` header, loads the user through `MyUserDetailService`, and populates the `SecurityContext`. `UserAuth.getAuthorities()` returns `"ROLE_" + role.toUpperCase()`, so a stored role of `admin` satisfies `hasRole('ADMIN')`.

`jwt.secret` and `jwt.expiration` resolve from the `JWT_SECRET` and `JWT_EXPIRATION` environment variables via `application.properties`; the secret is Base64-decoded for verification.

### Two authorization layers

Endpoints are gated twice, and new endpoints need both considered:

1. URL rules in `SecurityConfig.securityConfigure` — `/admin/**` requires ADMIN, `/user/**` requires ADMIN or USER, only `POST /login` and `POST /addregister` are public, everything else requires authentication. Session policy is stateless, CSRF disabled, HTTP Basic also enabled.
2. Method-level `@PreAuthorize` on essentially every controller method (`@EnableGlobalMethodSecurity(prePostEnabled = true)`).

Controller roots: `UserAuthController` is mapped at the application root (`/register`, `/addregister`, `/login`), while the others sit under `/user`, `/attendance`, and `/leaverecord`.

### Attendance write side effects

`AttendanceService.postAttendanceRecord` does more than insert a row:

- rejects a duplicate `(user, date)` pair;
- flips `UserInfo.isMarked` to `true` via `UserRepository.markAttendance` (a `@Modifying` JPQL update) — this is what `GET /user/unmarked` reads;
- if the status is `absent`, `Planned leave`, or `Sick leave` **and** both `recordIn` and `recordOut` are the literal string `"-"`, it additionally creates a `LeaveInfo` through `LeaveService`.

Status values are compared as free-text strings (`equalsIgnoreCase`), and dates/times are stored as `String`, not temporal types.

Deletion of `UserInfo` is soft (`UserRepository.softDelete` sets `isActive = false`); deletion of `UserAuth` and attendance records is hard.

### Response and error shape

`ApiResponse<T>` is the envelope: `code`, `status`, `message`, `validationErrors`, `requestedTime`, `executionTime`, `response`. `GlobalExceptionHandler` (`@RestControllerAdvice`) maps `UserNotFoundException`, `UserAlreadyExistException`, `InvalidException`, `InputMandatoryException`, and `InputMismatchException` (→ 401) into it, and `JwtFilter` writes the same envelope directly for expired tokens.

Success responses are *not* consistent: `UserAuthController` returns `ApiResponse`, while the user/attendance/leave controllers return raw DTO lists or plain strings. Match the surrounding controller when adding endpoints.

Several failures escape the handler and surface as **500**, not the status you would expect:

- `JwtFilter` catches `ExpiredJwtException` → 401 envelope, but its second `catch (Exception e)` turns *everything else* into `sendError(500)`. A malformed/tampered token or an unknown username in a valid token gives 500, not 401.
- `AttendanceService.postAttendanceRecord` throws a bare `RuntimeException` for a duplicate `(user, date)`, and `UserService.addUser` throws one for a duplicate id. Neither is registered in `GlobalExceptionHandler` → 500. (`UserAlreadyExistException` exists but is never thrown.)
- `AttendanceMapper.setDto` throws `NullPointerException` when `attendanceId` is null → 500.

`POST /login` is asymmetric: success is a raw JWT string, failure is the `ApiResponse` envelope.

### Endpoints

| Method | Path | Access | Returns |
| --- | --- | --- | --- |
| POST | `/login` | public | raw JWT string |
| POST | `/addregister` | public | `"Success"` |
| GET | `/register` | ADMIN | `ApiResponse<List<UserAuth>>` |
| GET | `/register/id/{id}` | ADMIN, USER | `ApiResponse<UserAuth>` |
| DELETE | `/register/id/{id}` | ADMIN | `"Deleted"` / `"No match found"` |
| GET | `/user` | ADMIN, USER | `List<UserDto>` |
| GET | `/user/id/{id}` | ADMIN, USER | `UserDto` or 404 |
| GET | `/user/roll/{roll}` | ADMIN, USER | `List<UserDto>` |
| GET | `/user/department/{department}` | ADMIN, USER | `List<UserDto>` |
| GET | `/user/unmarked` | ADMIN, USER | `List<UserDto>` (`isMarked = false`) |
| POST | `/user/adduser` | ADMIN | `"User added"` |
| DELETE | `/user/id/{id}` | ADMIN | soft delete, plain string |
| GET | `/attendance` | ADMIN, USER | `List<AttendanceDto>` |
| GET | `/attendance/id/{id}` | ADMIN, USER | `AttendanceDto` (`@RequestMapping`, so any HTTP method matches) |
| GET | `/attendance/present` | ADMIN, USER | status `present` |
| GET | `/attendance/absent` | ADMIN, USER | status `absent` |
| GET | `/attendance/sick` | ADMIN, USER | status `Sick Leave` |
| GET | `/attendance/planned-leave` | ADMIN, USER | status `Planned leave` |
| POST | `/attendance/add-attendance` | ADMIN | plain string |
| DELETE | `/attendance/id/{id}` | ADMIN | `"Deleted"` |
| GET | `/leaverecord` | ADMIN, USER | `List<LeaveDto>` |
| GET | `/leaverecord/id/{id}` | ADMIN, USER | `LeaveDto` |
| GET | `/leaverecord/{date}` | ADMIN, USER | `List<LeaveDto>` |
| DELETE | `/leaverecord/id/{id}` | ADMIN | `"Deleted"` |

`AttendanceController` is package-private (`class AttendanceController`, no `public`) — it still works because Spring scans it, but it cannot be referenced from another package.

`LeaveController.addLeaveForm` has no HTTP mapping; it is dead as an endpoint. Leave rows are only ever created as a side effect of `postAttendanceRecord`.

### Frontend contract

The React app (`D:\POC\attendance_management`, `src/redux/ApiSlice.jsx`) is the only consumer.

- **Called by the frontend but missing here:** `PUT /user/{id}` and `PUT /attendance/{id}`. The hooks
  (`useUpdateUserMutation`, `useUpdateAttendanceMutation`) exist and are exported but nothing invokes them, so nothing
  is visibly broken — adding an edit screen requires adding `@PutMapping` endpoints first.
- **Exposed here but unused by the frontend:** `/register`, `/register/id/{id}`, `/addregister`,
  `/register/id/{id}` (DELETE), `/user/unmarked`, `/user/roll/{roll}`, `/user/department/{department}`,
  `/leaverecord/{date}`, `/leaverecord/id/{id}` (DELETE). Login accounts have to be created by calling
  `/addregister` by hand (curl/Postman) — there is no registration UI.
- The frontend reads the `role` claim straight out of the JWT and compares it to the literal `'admin'`, so the `role`
  column in `user` should be stored lowercase even though `getAuthorities()` upper-cases it for Spring.

### CORS

Configured twice: `SecurityConfig.corsConfigurationSource` allows `http://localhost:3000` and `:3001` with credentials, and controllers additionally carry `@CrossOrigin` annotations. `UserAuthController` uses `@CrossOrigin(origins = "*")`, but the `SecurityConfig` source is what actually applies, so `/login` is still only reachable from the two allowed origins.

### Odds and ends

- `spring-boot-starter-actuator` is on the classpath with no `management.*` properties, so only `/actuator/health` is exposed — and it sits behind `anyRequest().authenticated()`.
- `JwtService.getToken` signs with the deprecated `signWith(SignatureAlgorithm, String)` overload while verification uses a Base64-decoded `SecretKey` — the two paths disagree stylistically but interoperate.
- `JwtService` still declares `@Value("${jwt.secret:defaultSecretKey}")`. That fallback is now unreachable (the property is always present, resolving from `JWT_SECRET`), but it is misleading and worth deleting.
- Secrets used to be committed here and remain in git history at `df886c5`. The original JWT secret must be treated as compromised — never reuse that value.
- Entities in `table/` carry `@Component` alongside `@Entity`. That makes Spring register a bean per entity class; harmless here, but don't copy the pattern.
- `UserInfo`, `UserDto` and `ApiResponse` each contain an empty stub constructor that assigns nothing (e.g. `UserInfo(long, String, String, String, boolean)`). They exist only to satisfy old test code — never call them.
