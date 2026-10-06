# Concepts used in this backend

An inventory of every Spring Boot and Java concept this codebase actually uses, where it lives, and what it really
does here. This is the companion to [`LEARNING-ROADMAP.md`](LEARNING-ROADMAP.md): the roadmap is *how to learn the
concepts in order*, this file is *what you already used and what it does*.

Each entry is: **what it is** → **where** → **the twist in your code**.

---

## 1. Bootstrap and configuration

### `spring-boot-starter-parent`
The parent POM that fixes the version of every Spring library, so you write `<artifactId>` without a
`<version>`. — `pom.xml:5-9`

Only three dependencies in your `pom.xml` carry their own version: jjwt, mockito and junit. Those are not managed by
the parent, which is why they need one.

### `@SpringBootApplication`
Three annotations in one — `@Configuration`, `@ComponentScan`, `@EnableAutoConfiguration`. —
`AttendanceManagementApplication.java:7`

`@ComponentScan` scans **this package and everything below it**. That is the real reason every class you wrote sits
under `com.attendance.attendance_management`. A class outside that package would be invisible to Spring, no matter
how many annotations it had.

### Starters and auto-configuration
Adding a starter to `pom.xml` makes Spring configure beans for you, based on what it finds on the classpath.

You never wrote a `DataSource`, an `EntityManager`, a Jackson `ObjectMapper` or a Tomcat server. They exist because
`spring-boot-starter-data-jpa` and `spring-boot-starter-web` are in your POM. That is auto-configuration.

### `@Value` and property placeholders
Injects a property into a field. — `JwtService.java:26,29`

```java
@Value("${jwt.secret:defaultSecretKey}")
```

The part after `:` is a default used when the property is missing. **This default is now unreachable** — since the
secrets change, `jwt.secret` is always present in `application.properties` (it resolves from `${JWT_SECRET}`), so the
fallback can never apply. Worth deleting: a silent fallback to a publicly known key is exactly the kind of thing that
hides a misconfiguration.

### Profiles
A named set of properties, activated at runtime. — `application-local.properties`

You used this today. Running with `-Dspring-boot.run.profiles=local` makes Spring load
`application-local.properties` **on top of** `application.properties`. The profile file wins for any key it defines,
which is why your real password there overrides the `${DB_PASSWORD}` placeholder — the placeholder is never even
evaluated.

---

## 2. Web layer

### `@RestController` and `@RequestMapping`
`@RestController` = `@Controller` + `@ResponseBody`: return values become JSON instead of view names. A class-level
`@RequestMapping` prefixes every method path. — all four controllers

`UserAuthController` is mapped at the root, so its paths are `/login`, `/register`, `/addregister`. The other three
sit under `/user`, `/attendance`, `/leaverecord`.

### `@PathVariable` and `@RequestBody`
`@PathVariable` pulls a value out of the URL; `@RequestBody` deserializes the JSON body into an object. —
`UserController.java:26,46`

Note you accept `@PathVariable Long id` in `UserController` but `@PathVariable String id` in
`AttendanceController`, then parse it by hand. The `Long` version is better: Spring converts it for you and returns
400 automatically if it isn't a number.

### `ResponseEntity` vs a plain return value
Return the object and Spring sends 200. Return a `ResponseEntity` and you choose the status. — both styles are in
`UserController.java:25-34`

```java
public List<UserDto> getAllUsers()                       // always 200
public ResponseEntity<UserDto> getUserById(...)          // 200 or 404
```

### `@RequestMapping` without a method
`@GetMapping` is shorthand for `@RequestMapping(method = GET)`. Plain `@RequestMapping` matches **every** HTTP
method. — `AttendanceController.java:38`

So `/attendance/id/5` answers POST, PUT and DELETE as well as GET. Not intended; worth changing to `@GetMapping`.

### CORS, configured twice
A browser on one origin calling another needs the server's permission. — `SecurityConfig.java:60`, plus
`@CrossOrigin` on the controllers

The `CorsConfigurationSource` bean is what actually applies, because it is wired into the security filter chain.
`@CrossOrigin(origins = "*")` on `UserAuthController` looks like it opens `/login` to everyone, but it does not —
the bean still restricts it to `localhost:3000` and `:3001`.

---

## 3. Persistence

### `@Entity`, `@Table`, `@Column`
Maps a class to a table and its fields to columns. — `table/UserInfo.java`, `AttendanceInfo.java`,
`LeaveInfo.java`, `UserAuth.java`

With `ddl-auto=update`, these classes **are** your schema. There is no `schema.sql` and no migration tool. Add a
field, restart, the column appears. Note it never drops: remove a field and the column stays behind forever.

### `@GeneratedValue(strategy = GenerationType.IDENTITY)`
Lets the database assign the primary key — for MySQL, `AUTO_INCREMENT`. — every entity's `@Id`

This is why you never set an id when creating a record.

### `@ManyToOne` and `@JoinColumn`
Many attendance rows belong to one person. `@JoinColumn(name = "user_id")` names the foreign key column. —
`AttendanceInfo.java:22-24`, `LeaveInfo.java:22-24`

The relationship is one-directional: `UserInfo` has no `@OneToMany` back to attendance. That is a deliberate
simplification and keeps JSON serialization from looping.

### `CascadeType.PERSIST`
"When you save this, save the related object too." — `AttendanceInfo.java:22`

**This never fires in your code.** `AttendanceMapper.setEntity` re-fetches the `UserInfo` from the database before
saving, so the user always already exists and there is nothing to cascade. Harmless, but it is not doing what its
name suggests.

### Derived query methods
Spring Data reads the *method name* and writes the SQL. — `repository/UserRepository.java`,
`AttendanceRepository.java`

```java
List<UserInfo> findByDepartment(String department);
boolean existsByUser_UserIdAndDate(Long userId, String date);
```

The underscore in the second one is the interesting part: it walks from `AttendanceInfo` into `user`, then into
`userId`. Without it Spring would look for a field literally called `userUserId`.

These are validated **at startup**, not at call time. Misspell a property and the app refuses to boot — a fast,
useful failure.

### `@Query` with JPQL, and `@Modifying`
When a method name can't express the query, write JPQL. — `UserRepository.java:20-30`

```java
@Query("UPDATE UserInfo u SET u.isActive = false WHERE u.userId = :userId")
```

JPQL queries **entities, not tables** — `UserInfo` and `u.isActive`, not `user_info` and `is_active`. Any query that
writes needs `@Modifying`, or Spring tries to run it as a SELECT and fails.

### `@Transactional` — from two different packages
Makes several database operations commit or roll back together.

This is worth knowing: your repository uses `org.springframework.transaction.annotation.Transactional`
(`UserRepository.java:21`) while your services use `jakarta.transaction.Transactional`
(`UserService.java:5`, `AttendanceService.java:11`). Both work. Spring's version has more options — `readOnly`,
`propagation`, `isolation` — so prefer it.

Where it matters most: `AttendanceService.postAttendanceRecord` writes up to three things (attendance row,
`isMarked` flag, leave row). `@Transactional` is what stops a failure halfway through leaving half-written data.

---

## 4. Beans and dependency injection

### `@Service`, `@Repository`, `@Component`
Stereotype annotations. They all mean "Spring, manage this class as a bean"; the different names are for humans and
for tooling.

Your mappers are `@Component` (`mapper/UserMapper.java:8`). Your repositories carry `@Repository`, although
extending `JpaRepository` already registers them — the annotation is optional there.

### Constructor injection via `@RequiredArgsConstructor`
Lombok generates a constructor taking every `final` field; Spring passes the beans in. — every service, controller
and mapper

```java
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
}
```

No `@Autowired` on fields anywhere — good. And this is precisely why your tests work without Spring: `@InjectMocks`
can call that same constructor with mocks. Field injection would have made that impossible.

### `@Component` on entities — a smell
`UserInfo`, `UserAuth` and `AttendanceInfo` carry `@Component` **as well as** `@Entity`.

That makes Spring create one bean instance of each entity class at startup. It is harmless here because nothing
injects them, but it is conceptually wrong: entities are data, created with `new` and managed by Hibernate, not
beans. Don't copy the pattern.

---

## 5. Errors and response shape

### Custom exceptions
Four classes, each extending `RuntimeException`. — `exceptionhandler/customexceptions/`

They extend *Runtime*Exception (unchecked) deliberately — no `throws` clauses needed up the call stack.

`UserAlreadyExistException` exists but is **never thrown anywhere**. Duplicate users throw a bare `RuntimeException`
instead (`UserService.java:70`).

### `@RestControllerAdvice` and `@ExceptionHandler`
A global interceptor for exceptions thrown out of any controller. — `GlobalExceptionHandler.java`

This is why there is not a single try/catch in your controllers. Throw `UserNotFoundException` anywhere and it comes
back as a 404 in the standard envelope.

### Generics — `ApiResponse<T>`
One wrapper class that can carry any payload type. — `dto/ApiResponse.java`

`ApiResponse<String>` for errors, `ApiResponse<List<UserAuth>>` for the register list. Same envelope, different
payload, checked at compile time.

The gap: anything not registered in the handler falls through to a 500. See the "Response and error shape" section
of [`CLAUDE.md`](CLAUDE.md) for the current list.

---

## 6. Security — read this section closely

### The filter chain
Spring Security is a chain of servlet filters running **before** your controllers. By the time a controller method
executes, the allow/deny decision is already made. — `SecurityConfig.java:42`

That is why no controller in this app contains a login check.

### `UserDetails` and `UserDetailsService`
`UserDetails` is Spring's idea of a logged-in user; `UserDetailsService` is the one method it calls to find one. —
`UserAuth.java:23`, `MyUserDetailService.java:20`

You made your JPA entity implement `UserDetails` — one class doing two jobs. Common and convenient; the cost is that
your database model and your security model are now welded together. (And it causes the leak described in
"Consequences" below.)

### `DaoAuthenticationProvider` and `AuthenticationManager`
The provider takes a username and password, calls your `UserDetailsService`, and BCrypt-compares. The manager is the
front door to it. — `SecurityConfig.java:73,82`

### BCrypt
A deliberately slow one-way hash. — `SecurityConfig.java:37`

```java
new BCryptPasswordEncoder(12)
```

12 is the *strength* — the hash is recomputed 2¹² times. Slowness is the feature: it makes brute-forcing expensive.
Hashes are never reversed, only re-computed and compared, which is why `encoder.matches(raw, hashed)` exists and
there is no `decode`.

### The `ROLE_` prefix
`getAuthorities()` returns `"ROLE_" + role.toUpperCase()`. — `UserAuth.java:48`

`hasRole('ADMIN')` silently looks for the authority `ROLE_ADMIN`. `hasAuthority('ADMIN')` would look for exactly
`ADMIN` and fail. That one line is why a database row storing `admin` satisfies `hasRole('ADMIN')`.

### `SessionCreationPolicy.STATELESS`
Tells Spring never to create an HTTP session. — `SecurityConfig.java:52`

This is *why* you need JWT. With no session, the server remembers nothing between requests, so every request must
carry its own proof of identity.

### CSRF disabled
`csrf(AbstractHttpConfigurer::disable)` — `SecurityConfig.java:45`

Usually alarming, correct here. CSRF attacks work because browsers attach **cookies** automatically. Your token
lives in `sessionStorage` and is attached by your own JavaScript, so a forged cross-site request carries no
credentials. Stateless + no auth cookie = no CSRF risk.

### `@PreAuthorize` and SpEL
Method-level authorization, written in Spring Expression Language. — every controller method

```java
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
```

Note this is a **second** layer on top of the URL rules in `SecurityConfig`. They overlap but are not identical:
`/attendance/**` has no URL rule, so only `@PreAuthorize` separates admin from user there. Forget the annotation on
a new attendance endpoint and any logged-in user can reach it.

`@EnableGlobalMethodSecurity(prePostEnabled = true)` (`SecurityConfig.java:31`) is **deprecated** — the current name
is `@EnableMethodSecurity`, which has `prePostEnabled` on by default. Useful to know when reading modern docs.

### `OncePerRequestFilter` and `SecurityContextHolder`
Your JWT filter extends `OncePerRequestFilter` so it runs exactly once per request even with internal forwards, and
stores the result in `SecurityContextHolder` — a thread-local that `@PreAuthorize` reads later. —
`JwtFilter.java:24,52`

JWT internals are covered in Step 10 of the roadmap.

---

## 7. Lombok

| Annotation | Where | What it generates |
| --- | --- | --- |
| `@Getter` / `@Setter` | entities, DTOs | accessors for every field |
| `@NoArgsConstructor` | entities, DTOs | required by JPA and Jackson |
| `@AllArgsConstructor` | entities, DTOs | constructor with every field |
| `@RequiredArgsConstructor` | services, controllers, mappers | constructor with `final` fields — the DI mechanism |
| `@Slf4j` | `AttendanceController:21` | a `log` field |

`@Slf4j` is used in exactly one class. Meanwhile there is a live `System.out.println` in
`UserAuthService.java:79`, inside the login failure path — it prints a password comparison result to the console.
Remove it.

---

## 8. Testing

### Mockito without Spring
`@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`. — `src/test/java/.../services/`

No test in this project starts Spring or touches a database. There is no `@SpringBootTest`, `@WebMvcTest`,
`@DataJpaTest`, and no MockMvc. Controller tests call controller methods directly, as plain Java objects.

That makes the suite very fast (CI runs it in about 20 seconds) but means **nothing verifies your wiring** — a
broken `@PreAuthorize` expression, a bad JPQL query or a missing bean would pass every test and fail at runtime.
Adding a single `@SpringBootTest` context-load test would catch most of that.

`AttendanceServiceTest` uses the older `MockitoAnnotations.openMocks(this)` in `@BeforeEach` instead of the
extension. Same effect, older style.

---

## 9. Java language level

Compiled for **Java 21**, but written in Java 8/9 style. The newest feature used anywhere is `List.of(...)`.

Not used: records, `var`, switch expressions, text blocks, sealed types, pattern matching. `UserService` uses
`.collect(Collectors.toList())` where Java 16+ allows the shorter `.toList()`.

What you *do* use well: streams with method references (`userRepository.findAll().stream().map(userMapper::toDto)`),
`Optional` with `orElseThrow` and `map`, and a `Function<Claims, T>` as a parameter in
`JwtService.extractClaim` — a genuinely neat piece of generic code.

Nothing here is wrong. But `UserDto` as a `record` would replace 20 lines of Lombok with one.

---

## Consequences you may not have noticed

These follow directly from concepts above. **Do not fix them yet** — they are the exercises in
[`LEARNING-ROADMAP.md`](LEARNING-ROADMAP.md). Verify them first, so you see the effect before the cause.

### 1. Your `isActive` field probably arrives as `active` in JSON

Lombok turns `private boolean isActive` into a getter called `isActive()`. Jackson strips the `is` prefix when
naming the JSON property — so it likely serializes as **`active`**, not `isActive`. Same for `isMarked`.

Your React table reads `user.isActive` (`components/table/Usertable.jsx`). If the property is really `active`, then
`user.isActive` is `undefined`, which is falsy, and **every row shows "Inactive"** regardless of the database.

**Check it:**
```bash
curl -s http://localhost:8080/user -H "Authorization: Bearer YOUR_TOKEN"
```
Look at the key names. Then open `/user` in the React app and see whether any row says Active.

*Concepts involved:* JavaBeans naming, Jackson serialization, Lombok. *Fix later:* `@JsonProperty("isActive")`, or
rename the field to `active` on both sides.

### 2. `GET /register` may be returning password hashes

`UserAuthController.getRegisterUser` returns `UserAuth` entities directly, not DTOs. `UserAuth` implements
`UserDetails`, which requires a **public** `getPassword()` — and Jackson serializes public getters.

So the response likely includes the bcrypt hash, plus `authorities`, `enabled`, `accountNonExpired`,
`accountNonLocked`, `credentialsNonExpired`, and the username twice (`userName` from your field, `username` from the
interface method).

**Check it:**
```bash
curl -s http://localhost:8080/register -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

*Concepts involved:* entity-vs-DTO, Jackson, `UserDetails`. *Fix later:* `@JsonIgnore` on the password, or return a
DTO — which is exactly why every other controller in this app already uses DTOs.

### 3. Anyone can register themselves as an admin

`POST /addregister` is `permitAll()` (`SecurityConfig.java:49`) and binds the request body straight into `UserAuth`
— including the `role` field. Nothing validates it.

```bash
curl -X POST http://localhost:8080/addregister -H "Content-Type: application/json" \
  -d '{"userName":"anyone","password":"x","role":"admin"}'
```

That account now passes `hasRole('ADMIN')`. No run needed to confirm — it is visible in the code.

*Concepts involved:* mass assignment, entity as request body, `permitAll()`. *Fix later:* a registration DTO without
a `role` field, defaulting to `user`.

### 4. Roles are case-sensitive on one side only

Register with `role` = `"Admin"` and the backend accepts it as admin — `getAuthorities()` calls `.toUpperCase()`.
But the React app compares the JWT claim with `decodedToken.role === 'admin'`, a literal match.

Result: that user lands on the **user** dashboard while holding **admin** API rights. The UI hides the admin buttons;
the API would still allow the calls. Always store roles lowercase.

### 5. You check the password twice on login

`UserAuthService.verifyLogin` calls `encoder.matches` by hand (line 77), then `authenticationManager.authenticate`
(line 82) — which loads the user again and runs the same BCrypt comparison internally.

Not a bug, but two deliberately-slow hashes per login where one would do. Either approach alone is complete; keeping
both is a sign of code assembled from two tutorials.

---

## Where to go next

| You know it | Roadmap step |
| --- | --- |
| DI, JPA, REST — you confirmed these | Steps 2–6, skim only |
| Exception handling, transactions | Steps 7–8 |
| **Security — your stated gap** | **Step 9** |
| **JWT internals** | **Step 10** |
| `@PreAuthorize` | Step 11 |
| Testing gaps | Step 12 |
