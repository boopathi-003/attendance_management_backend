# Learning Spring Boot with this codebase

A build order for the attendance app, one concept per step. Each step names the concept, points at the files in
**this** repo where it already lives, and ends with an exercise you can actually run.

The app is already finished — so use this two ways:

1. **Read-along:** open the listed files, understand that one concept, move on.
2. **Rebuild:** start an empty project and add one step at a time, checking your work against these files.

Steps 1–13 are backend only. Step 14 connects the React app at `D:\POC\attendance_management`; step 15 is
full-stack work across both repos.

Nothing here requires the frontend to be running. Use curl, Postman, or the IntelliJ HTTP client to hit endpoints.

---

## Prerequisites

- JDK 21 (`java -version` must report 21 — `pom.xml` sets `<java.version>21</java.version>`)
- MySQL running on `localhost:3306` with the credentials in `src/main/resources/application.properties`
- Run with `.\mvnw.cmd spring-boot:run`, test with `.\mvnw.cmd test`

---

## Step 1 — Project setup, starters, and auto-configuration

**Concept:** what a Spring Boot "starter" is, and what `@SpringBootApplication` actually turns on.

**Files:** `pom.xml`, `AttendanceManagementApplication.java`, `src/main/resources/application.properties`

`@SpringBootApplication` is three annotations in one: `@Configuration`, `@ComponentScan` (scans this package and
below — which is why everything lives under `com.attendance.attendance_management`), and `@EnableAutoConfiguration`
(configures beans based on what is on the classpath). Adding `spring-boot-starter-data-jpa` to `pom.xml` is what
creates the `DataSource` and `EntityManager` — you never write that wiring.

Properties are the external knobs: `spring.datasource.*`, `spring.jpa.hibernate.ddl-auto=update`, and the custom
`jwt.secret` / `jwt.expiration` this app reads with `@Value`.

**Exercise:** comment out `spring-boot-starter-data-jpa`, rebuild, and read the failure. Then add
`spring.jpa.show-sql=true` and watch Hibernate print SQL on every request.

---

## Step 2 — REST controllers and request mapping

**Concept:** `@RestController`, `@RequestMapping`, `@GetMapping`/`@PostMapping`/`@DeleteMapping`, `@PathVariable`,
`@RequestBody`, and `ResponseEntity`.

**Files:** `controller/UserController.java` (cleanest example), then `controller/AttendanceController.java`

`@RestController` = `@Controller` + `@ResponseBody`: return values are serialized to JSON by Jackson instead of being
treated as view names. `@RequestMapping("/user")` at class level prefixes every method path.

Note the two return styles in `UserController`: `List<UserDto>` (Spring wraps it in a 200) versus
`ResponseEntity<UserDto>` (you control the status — `ResponseEntity.notFound().build()`).

Two things in this repo to learn *from* rather than copy: `AttendanceController` is declared package-private, and
`getAttendanceById` uses bare `@RequestMapping` instead of `@GetMapping`, so it answers POST and DELETE too.

**Exercise:** add `GET /user/count` returning the number of users. Start with a hardcoded `0` — no service, no
database — just to see the mapping work. Then delete it.

---

## Step 3 — JPA entities and the database

**Concept:** `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, and `@ManyToOne` with `@JoinColumn`.

**Files:** `table/UserInfo.java`, `table/AttendanceInfo.java`, `table/LeaveInfo.java`

These classes *are* the schema. There are no migrations and no `schema.sql` — `ddl-auto=update` makes Hibernate
create and alter tables to match the entities on startup. Change a field, restart, and the column appears. (It never
drops anything, which is why stale columns accumulate.)

`AttendanceInfo.user` is a `@ManyToOne` to `UserInfo` — many attendance rows per person — mapped onto the `user_id`
foreign key column. `UserInfo` has no `@OneToMany` back, so the relationship is one-directional.

Notice what is *not* modelled: `date`, `recordIn`, `recordOut` are `String`, not `LocalDate`/`LocalTime`. That is a
deliberate thing to notice, because it forces every comparison into string equality.

**Exercise:** add a `createdAt` `String` column to `UserInfo`, restart, and confirm in MySQL that
`DESCRIBE user_info;` shows the new column. Then remove the field and note that the column stays behind.

---

## Step 4 — Spring Data JPA repositories

**Concept:** `JpaRepository`, derived query methods, and how an interface with no implementation becomes a bean.

**Files:** `repository/UserRepository.java`, `repository/AttendanceRepository.java`,
`repository/LeaveRepository.java`

Extending `JpaRepository<UserInfo, Long>` gives you `findAll`, `findById`, `save`, `deleteById`, `count`, `existsById`
for free — Spring generates the implementation at runtime.

Derived queries are parsed from the method *name*: `findByDepartment`, `findByRoll`, `findByIsMarked(boolean)`,
`existsByName`. Nested properties use an underscore — `existsByUser_UserIdAndDate(Long, String)` walks from
`AttendanceInfo` into `user.userId` and ANDs it with `date`.

**Exercise:** add `List<UserInfo> findByDepartmentAndIsActive(String department, boolean isActive)` to
`UserRepository` and call it from a temporary controller method. Then misspell the property name and read the
startup error — derived queries fail at boot, not at call time. That fast feedback is the point.

---

## Step 5 — DTOs and mappers

**Concept:** why you don't return entities from controllers, and hand-written mapping.

**Files:** `dto/UserDto.java`, `mapper/UserMapper.java`, `mapper/AttendanceMapper.java`, `mapper/LeaveMapper.java`

A DTO is the shape the API promises; the entity is the shape the database has. Keeping them separate means a column
rename doesn't break clients, and lazy-loaded relations don't explode during JSON serialization.

Mappers here are plain `@Component` classes, not MapStruct. `AttendanceMapper.setEntity` is worth studying: it does
**not** trust the `UserInfo` in the incoming DTO — it re-reads the user from `UserRepository` by id. That is the
correct instinct (never persist client-supplied relations blindly), even though a mapper is an odd place for it.

Method naming is inconsistent across mappers (`setDto`/`setEntity` vs `toDto`/`toEntity`) — match whichever file
you're editing. `UserMapper.toEntity` deliberately drops `userId`, `isActive` and `isMarked`: a client must not be
able to set the primary key or resurrect a soft-deleted user by POSTing those fields.

**Exercise:** `AttendanceDto` currently exposes the whole `UserInfo` entity. Add a `userName` field to the DTO,
populate it in `setDto`, and observe that the JSON now carries both. (Leave the nested `user` in place — the
frontend reads `attendance.user.name`.)

---

## Step 6 — Service layer and dependency injection

**Concept:** constructor injection, `@Service`, and Lombok's `@RequiredArgsConstructor`.

**Files:** `services/UserService.java`, `services/AttendanceService.java`, `services/LeaveService.java`

The layering is `controller → service → mapper → repository → entity`. Controllers only translate HTTP; business
rules live in services.

Every class here declares `private final` dependencies and annotates the class `@RequiredArgsConstructor`. Lombok
generates a constructor taking exactly those final fields, and Spring injects them — no `@Autowired` on fields.
Constructor injection is what makes Step 11's tests possible without a Spring context.

`AttendanceService.postAttendanceRecord` is where the real business logic sits: reject duplicates, save, flip the
user's `isMarked` flag, and conditionally create a leave record.

**Exercise:** trace one request end to end on paper — `GET /user/roll/teacher` — naming every class it touches and
what each one converts. Then add a `UserService` method for the derived query you wrote in Step 4 and expose it.

---

## Step 7 — Exception handling and a consistent response envelope

**Concept:** `@RestControllerAdvice`, `@ExceptionHandler`, custom exceptions, and generic response wrappers.

**Files:** `exceptionhandler/GlobalExceptionHandler.java`, `exceptionhandler/customexceptions/*`,
`dto/ApiResponse.java`

`@RestControllerAdvice` is a global interceptor for exceptions thrown out of any controller. Each
`@ExceptionHandler(SomeException.class)` method converts one exception type into a `ResponseEntity` with the status
you choose — that is how `UserNotFoundException` becomes a 404 without a single try/catch in a controller.

`ApiResponse<T>` is the envelope (`code`, `status`, `message`, `validationErrors`, `requestedTime`, `executionTime`,
`response`). Generics let the same wrapper carry a `String` error or a `List<UserAuth>` payload.

The gap to notice: anything *not* registered here falls through to Spring's default 500. Duplicate attendance and
duplicate users both throw bare `RuntimeException`, so a legitimate client mistake looks like a server crash.

**Exercise (real fix):** create `DuplicateRecordException`, throw it from
`AttendanceService.postAttendanceRecord` instead of `RuntimeException`, and map it to **409 Conflict** in the
handler. Post the same attendance twice and confirm the status changes from 500 to 409.

---

## Step 8 — Transactions and modifying queries

**Concept:** `@Transactional`, `@Query` with JPQL, and `@Modifying`.

**Files:** `repository/UserRepository.java` (`softDelete`, `markAttendance`), `services/UserService.java`,
`services/AttendanceService.java`

When a derived query can't express what you need, write JPQL with `@Query`. JPQL queries *entities*, not tables —
`UPDATE UserInfo u SET u.isActive = false WHERE u.userId = :userId` uses class and field names.

Any `@Query` that writes needs `@Modifying` (otherwise Spring tries to run it as a `SELECT`) and must run inside a
transaction.

`@Transactional` on `postAttendanceRecord` is what makes its three writes — attendance row, `isMarked` flag,
optional leave row — commit or roll back together. Remove it and a failure halfway through leaves the database
inconsistent.

Note the mixed imports: `UserRepository` uses `org.springframework.transaction.annotation.Transactional`, the
services use `jakarta.transaction.Transactional`. Both work; Spring's version has more options (`readOnly`,
`propagation`).

**Exercise:** make `postAttendanceRecord` throw a `RuntimeException` after `markAttendance` but before the leave
insert. Confirm `isMarked` rolls back. Then remove `@Transactional` and confirm it does not.

---

## Step 9 — Spring Security: authentication

**Concept:** `SecurityFilterChain`, `UserDetails`/`UserDetailsService`, `DaoAuthenticationProvider`,
`AuthenticationManager`, and BCrypt.

**Files:** `configure/SecurityConfig.java`, `table/UserAuth.java`, `services/MyUserDetailService.java`,
`services/UserAuthService.java`

Security is a filter chain in front of your controllers. `SecurityConfig.securityConfigure` declares it: CORS, CSRF
disabled (stateless API), URL rules, stateless session policy.

`UserAuth` implements `UserDetails`, so the entity doubles as Spring Security's principal.
`getAuthorities()` returns `"ROLE_" + role.toUpperCase()` — that prefix is why `hasRole('ADMIN')` matches a stored
role of `admin`. `MyUserDetailService` is the bridge from a username to that principal.

Passwords are BCrypt-hashed (`BCryptPasswordEncoder(12)`) on registration and compared with `encoder.matches` on
login — hashes are never reversed.

Note the **two unrelated user models**, the most common source of confusion here: `UserAuth` (table `user`) is
credentials; `UserInfo` (table `user_info`) is the person. No foreign key joins them.

**Exercise:** create a login account with
`curl -X POST http://localhost:8080/addregister -H "Content-Type: application/json" -d "{\"userName\":\"admin\",\"password\":\"admin123\",\"role\":\"admin\"}"`,
then look at the `user` table and confirm the stored password is a `$2a$12$...` hash, not `admin123`.

---

## Step 10 — JWT: stateless authentication

**Concept:** `OncePerRequestFilter`, token signing/parsing, and `SecurityContextHolder`.

**Files:** `services/JwtService.java`, `configure/JwtFilter.java`

`POST /login` → `UserAuthService.verifyLogin` checks the user exists, is active, and the password matches, then
delegates to `AuthenticationManager` and mints a token via `JwtService.getToken(username, role)`. The token is an
HS256 JWT carrying `sub` (username), a custom `role` claim, `iat`, and `exp`.

On every later request `JwtFilter` — registered with `addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)`
— reads the `Bearer` header, extracts the username, loads the `UserDetails`, validates the token, and puts an
`Authentication` into `SecurityContextHolder`. That context is what `@PreAuthorize` reads in Step 11.

`OncePerRequestFilter` guarantees the logic runs exactly once per request even with forwards.

**Exercise (real fix):** `JwtFilter`'s second `catch (Exception e)` turns *every* non-expiry failure into a 500 — send
a garbage token and see it. Add a `catch (io.jsonwebtoken.JwtException | UserNotFoundException e)` branch that
writes the same `ApiResponse` envelope with **401** instead. (`JwtException` is the parent of `MalformedJwtException`
and `SignatureException`; note that `MyUserDetailService` throws this project's own
`exceptionhandler.customexceptions.UserNotFoundException`, **not** Spring's `UsernameNotFoundException`.)

---

## Step 11 — Method-level authorization

**Concept:** `@EnableGlobalMethodSecurity(prePostEnabled = true)` and `@PreAuthorize`.

**Files:** `configure/SecurityConfig.java`, every controller

Endpoints are gated **twice**, and a new endpoint needs both considered:

1. URL rules in `securityConfigure` — `/admin/**` needs ADMIN, `/user/**` needs ADMIN or USER, only `POST /login` and
   `POST /addregister` are public, everything else needs authentication.
2. `@PreAuthorize("hasRole('ADMIN')")` on essentially every controller method.

They overlap but are not the same: `/attendance/**` has no URL rule, so `anyRequest().authenticated()` applies and
`@PreAuthorize` alone separates admin from user. Forget the annotation there and any logged-in user can post
attendance.

`@EnableGlobalMethodSecurity` is deprecated in favour of `@EnableMethodSecurity` — worth knowing when you read
current docs.

**Exercise:** log in as a `user`-role account and call `DELETE /user/id/1`. Confirm the 403. Then remove the
`@PreAuthorize` from that method and confirm the URL rule alone still lets a USER through — that is the gap the
annotation closes.

---

## Step 12 — Testing with JUnit 5 and Mockito

**Concept:** unit tests without a Spring context — `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`,
`when(...).thenReturn(...)`, `verify(...)`.

**Files:** `src/test/java/.../services/UserServiceTest.java`, `AttendanceServiceTest.java`, and the controller tests

**No test in this project starts Spring or touches a database.** There is no `@SpringBootTest`, no `@WebMvcTest`, no
`@DataJpaTest`, and no MockMvc. Controller tests call controller methods directly as plain Java.

`@Mock` creates a fake repository; `@InjectMocks` builds the service and passes the fakes into its constructor —
which only works because of the constructor injection from Step 6. `when(...)` scripts the fake's answers;
`verify(...)` asserts the service called it.

`AttendanceServiceTest` uses `MockitoAnnotations.openMocks(this)` in a `@BeforeEach` instead of the extension — the
older style, same effect.

```powershell
.\mvnw.cmd test                                        # all
.\mvnw.cmd test -Dtest=UserServiceTest                 # one class
.\mvnw.cmd test -Dtest=UserServiceTest#TestGetUser     # one method
```

**Exercise:** write a test for the 409 behaviour you added in Step 7 — mock
`attendanceRepository.existsByUser_UserIdAndDate` to return `true` and assert your new exception is thrown.
`assertThrows` is the JUnit 5 idiom.

---

## Step 13 — Dev tooling: devtools and actuator

**Concept:** what the last two starters in `pom.xml` do.

`spring-boot-devtools` restarts the app when classes recompile — that is why the server reloads on save in an IDE.

`spring-boot-starter-actuator` adds operational endpoints, but with no `management.*` properties only
`/actuator/health` is exposed, and `anyRequest().authenticated()` puts it behind login.

**Exercise:** add
`management.endpoints.web.exposure.include=health,info,metrics` to `application.properties` and hit
`/actuator/metrics` with a valid token. Then decide whether it should be public — and write the
`requestMatchers("/actuator/health").permitAll()` rule if so.

---

## Step 14 — Connecting the React frontend

**Concept:** CORS, and how the browser actually consumes all of the above.

**Files (backend):** `SecurityConfig.corsConfigurationSource`, `@CrossOrigin` on controllers
**Files (frontend, `D:\POC\attendance_management`):** `src/redux/ApiSlice.jsx`, `src/components/forms/Login.jsx`,
`src/components/private-Router/Private-Router.jsx`

A browser at `http://localhost:3000` calling `http://localhost:8080` is a cross-origin request, so the backend must
say yes. `corsConfigurationSource` allows `:3000` and `:3001`, the `Authorization` header, and credentials. Without
it every call fails in the browser while working perfectly in curl — the classic first full-stack bug.

The round trip: React posts to `/login` with axios → receives the raw token string → stores it in `sessionStorage`
→ decodes the `role` claim with `jwt-decode` to pick a dashboard → RTK Query's `prepareHeaders` attaches
`Authorization: Bearer <token>` to every later call → `JwtFilter` validates it → `@PreAuthorize` decides.

Note that the frontend compares the role to the literal `'admin'`, so store roles lowercase.

**Exercise:** change the CORS allowlist to `http://localhost:3002`, restart both, and read the console error in the
browser. Confirm the same request still succeeds from curl. Change it back.

---

## Step 15 — Full-stack features (putting it together)

Each of these needs both repos. They are real gaps, not toy exercises — the frontend already has the hooks.

1. **Update a user.** Add `PUT /user/{id}` (controller → service → mapper → repository), then wire the existing
   `useUpdateUserMutation` to an edit modal. Covers: `@PutMapping`, `@RequestBody` + `@PathVariable` together,
   partial-update semantics.
2. **Update an attendance record.** Same, for `PUT /attendance/{id}` and `useUpdateAttendanceMutation`.
3. **Make delete actually delete.** `GetUsers.onDeleteUser` only filters local state today — call
   `useDeleteUserMutation` and refetch. Covers: RTK Query mutations and cache invalidation (`tagTypes` /
   `invalidatesTags`), which this app never set up.
4. **Build the attendance form.** `/attendance` renders an empty shell; `usePostAttendanceMutation` and
   `POST /attendance/add-attendance` both exist. Drive the person picker from `GET /user/unmarked`, an endpoint the
   frontend never calls. Note the flaw you inherit: `isMarked` is one-way — `markAttendance` sets it `true` and
   nothing ever resets it — so `/user/unmarked` really means "never had *any* attendance row", not "no record
   today". Fixing that is the better half of the exercise: add `@EnableScheduling` and a `@Scheduled(cron = "0 0 0 * * *")`
   job that clears the flag nightly. That is the one Spring concept this roadmap otherwise never touches.
5. **Bean validation.** Add `spring-boot-starter-validation`, annotate `UserDto` with `@NotBlank`, add `@Valid` to
   `addUser`, and handle `MethodArgumentNotValidException` in `GlobalExceptionHandler` — populating the
   `validationErrors` field that `ApiResponse` has always had and never used.

---

## Reference order, condensed

| # | Concept | Entry point |
| --- | --- | --- |
| 1 | Starters, auto-configuration, properties | `pom.xml`, `application.properties` |
| 2 | REST controllers | `controller/UserController.java` |
| 3 | JPA entities | `table/UserInfo.java` |
| 4 | Repositories, derived queries | `repository/UserRepository.java` |
| 5 | DTOs and mappers | `mapper/UserMapper.java` |
| 6 | Services, constructor DI | `services/UserService.java` |
| 7 | Global exception handling | `exceptionhandler/GlobalExceptionHandler.java` |
| 8 | Transactions, `@Modifying` JPQL | `repository/UserRepository.java` |
| 9 | Security: authentication | `configure/SecurityConfig.java` |
| 10 | JWT filter | `configure/JwtFilter.java` |
| 11 | `@PreAuthorize` | any controller |
| 12 | JUnit 5 + Mockito | `src/test/java/.../services/` |
| 13 | devtools, actuator | `pom.xml` |
| 14 | CORS + React wiring | `SecurityConfig.corsConfigurationSource` |
| 15 | Full-stack features | both repos |
