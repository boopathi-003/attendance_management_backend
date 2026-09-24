## What changed

<!-- One or two sentences. What does this PR do, and why? -->

## How to verify

<!-- The commands or steps a reviewer should run. -->

```bash
./mvnw -B test
```

## Checklist

- [ ] `./mvnw verify` passes locally (JDK 21)
- [ ] New endpoints are gated by both a `SecurityConfig` URL rule and `@PreAuthorize`
- [ ] Response shape matches the surrounding controller (`ApiResponse` vs raw DTO)
- [ ] Frontend updated if the API contract changed — see boopathi-003/attendance_management
