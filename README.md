[![Java](https://img.shields.io/badge/Java-21+-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)
[![Type](https://img.shields.io/badge/type-self--hosted--identity--server-orange)]()

# AuthX

> A self-hosted identity server. Consumed over HTTP. Works with any stack.

AuthX handles authentication for your application — regardless of what your application is built with. A React frontend, a mobile app, a Django service, a Go microservice: if it can make an HTTP request, it can use AuthX. Call `POST /api/auth/login`, receive a standard JWT, verify it with any JWT library in your language.

AuthX is not a hosted service. It is not competing with Auth0, Supabase, Firebase, Clerk, or Okta. The closer comparison is Keycloak — also implemented in Java, consumed over HTTP by applications in any language. AuthX is lighter, simpler, and designed for developers who want to understand and own every line of their auth stack.

You run it. You own the database. You own the user data. No vendor dependency, no usage limits, no pricing tiers.

---

## Who This Is For

---

### Any developer, any stack

Your application calls AuthX over HTTP and receives standard JWTs. That is the entire integration surface.

```
POST /api/auth/login
→ { "accessToken": "eyJ...", "refreshToken": "..." }
```

Verify the access token using any standard JWT library — `python-jose`, `jsonwebtoken`, `golang-jwt`, `nimbus-jose-jwt`, or whichever library your stack provides. AuthX signs tokens with HMAC-SHA256. The signing secret is yours.

No SDK to install. No framework constraint. If your application speaks HTTP, it works with AuthX.

---

### Spring Boot developers who fork AuthX

Forking AuthX gives you more than authentication. You inherit a production-grade hexagonal architecture where every external concern is behind a port, and cross-cutting behaviour is enforced through AOP — not scattered across controllers and services.

**Adding new behaviour means writing a new adapter or aspect. Nothing else changes.**

| Port | Current implementation | To extend |
|---|---|---|
| `MailSender` | JavaMailSender | Write one class to swap in AWS SES |
| `HumanVerifier` | Cloudflare Turnstile | Write one class to swap in reCAPTCHA |
| `ChallengeSecretGenerator` | SecureRandom numeric OTP | Write one class to add magic link tokens |
| `ChallengeSender` | Email | Write one class to add SMS delivery |
| `RateLimitPolicy` | Per-email and per-IP policies | Add one enum constant for a new policy |

You also inherit: distributed rate limiting, declarative human verification via `@RequiresHumanVerification`, declarative rate limiting via `@RateLimit`, structured error responses, Flyway-managed schema, and horizontal scaling with nginx — none of which need to be rebuilt.

---

## Why This Exists

Every non-trivial application needs the same set of things: registration, email verification, login, JWT issuance, refresh token rotation, federated login, password reset, rate limiting, and role-based authorization. Most developers either piece this together from tutorials or hand it off to a hosted service they have no control over.

AuthX is the third option — a complete, working identity server you run yourself.

---

## What AuthX Provides

**Credential flows**
- Registration with email, username, and password
- Email verification via OTP
- Login with username or email — single normalised query, both stored lowercase
- JWT access tokens — short-lived, stateless
- Opaque refresh tokens — server-side, SHA-256 hashed, 7-day TTL
- Single-use rotation with stolen token detection
- Logout with server-side revocation

**Federated flows — Google and GitHub**
- Google OIDC login
- GitHub OAuth2 login with private email fallback via `/user/emails` API
- Four decision outcomes: `ALLOW_LOGIN`, `REQUIRE_REGISTRATION`, `REQUIRE_LINKING`, `BLOCKED`
- Flow tokens for federated registration and provider linking — signed JWT, 5-minute TTL, intent-stamped, single-use
- Auto-activation of `VERIFICATION_PENDING` accounts on provider linking

**Password management**
- Forgot password via OTP
- Reset password — social-only accounts can set their first password via this flow
- Change password — authenticated, current password required
- All sessions revoked on password change and reset

**Security**
- Stateless JWT authentication via `JwtFilter`
- `typ` claim verified first on every parse — token substitution attacks closed by design
- `ACCOUNT_NOT_ACTIVE` covers both unverified and blocked — no account state leakage
- `INVALID_CREDENTIALS` covers both wrong password and unknown email — no enumeration
- Role-based authorization via `@PreAuthorize`

**Rate limiting**
- Per-email: 1 request per 30s, max 5 per 5min
- Per-IP: 3 requests per 30s, max 20 per 5min
- Redis-backed via Bucket4j distributed proxy manager, interval refill
- `Retry-After` and `X-Retry-At` headers on 429 responses
- Declarative via `@RateLimit` and `@RateLimitKey` — AOP-enforced at method level
- `IpRateLimitFilter` for filter-chain-level IP rate limiting

**Human verification**
- Cloudflare Turnstile, abstracted behind the `HumanVerifier` port
- Declarative via `@RequiresHumanVerification` — AOP-enforced, per-endpoint action parameter
- Swap providers without touching controllers or application services
- Protected: register, login, forgot password, reset password

**Infrastructure**
- Flyway migrations — schema managed, `ddl-auto: validate`
- Docker Compose with PostgreSQL 17, Redis 7, nginx, and app
- nginx horizontal scaling: `docker compose up --scale app=3`
- Multi-stage Dockerfile — Maven build + JRE runtime, Alpine base
- PostgreSQL healthcheck before app starts — no Flyway race condition on startup
- Named volumes for data persistence

---

## Architecture

AuthX is built with Hexagonal Architecture (Ports and Adapters) and Domain-Driven Design, organised into bounded contexts. Cross-cutting concerns are enforced through Spring AOP — rate limiting and human verification are aspect-driven, keeping that logic out of controllers and application services entirely.

```
account/
├── domain/          Pure Java. Zero framework dependencies.
│                    Account aggregate, value objects, domain port interfaces.
├── application/     Use cases and application services.
├── infrastructure/  JPA persistence, Spring Security, JWT adapters,
│                    federation adapters, refresh token management.
└── web/             REST controllers and request records.

challenge/
├── domain/          Challenge aggregate, OTP lifecycle.
├── application/     Issue and verify use cases.
├── infrastructure/  Redis-backed repository, OTP generator, email sender.
└── web/             Challenge controller.

kernel/              Shared value objects (Email).
                     Zero dependencies on bounded contexts.

platform/            Cross-cutting technical concerns:
                     cache, notification, human verification,
                     rate limiting, configuration.
```

The domain layer has zero Spring dependencies. The `Account` aggregate enforces all invariants. Infrastructure adapts external systems to domain ports — the domain never reaches outward. The bounded contexts do not depend on each other.

---

## Token Architecture

Three JWT types are in use. Every token carries a `typ` claim verified first on every parse to prevent substitution attacks.

| Type | Carries | Purpose |
|---|---|---|
| `ACCESS` | `accountId`, `email`, `roles` | Authenticates API requests |
| `FLOW` | `email`, `intent`, `provider` | Authorises a specific federated flow step |
| `RE_AUTH` | `email`, `intent` | Designed for sensitive action confirmation — roadmap |

Refresh tokens are opaque strings, not JWTs. Stored server-side as SHA-256 hashes. The raw token is returned to the client once and never stored. Every use rotates the token — if a stolen token is replayed, the next legitimate refresh fails, surfacing the compromise.

---

## Account Lifecycle

| State | Description |
|---|---|
| `VERIFICATION_PENDING` | Credential registration — email not yet verified |
| `ACTIVE` | Email verified, federated registration, or provider linking on a pending account |
| `BLOCKED` | Suspended — all operations rejected |

---

## Quick Start

**Prerequisites:** Docker and Docker Compose.

```bash
git clone https://github.com/dhanesh76/authx.git
cd authx
cp .env.example .env
# Fill in your values — see Configuration below
docker compose up --build
```

Scale horizontally:

```bash
docker compose up --scale app=3
```

OAuth2 flows must be initiated from a browser. They cannot be triggered from Postman or curl — the provider redirect requires a real browser session.

---

## API Reference

Full request and response documentation — body schemas, required headers, error codes, and examples:

**[AuthX API — Postman Docs](https://documenter.getpostman.com/view/45135482/2sBXqNkyDM)**

The Postman collection and environment are also available locally in `docs/postman/`.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register with email, username, and password |
| `POST` | `/api/auth/verify-email` | Verify email with OTP |
| `POST` | `/api/auth/login` | Login with username or email |
| `POST` | `/api/auth/refresh` | Rotate refresh token, receive new access token |
| `POST` | `/api/auth/logout` | Revoke refresh token |
| `POST` | `/api/auth/federated/register` | Complete federated registration — flow token required |
| `POST` | `/api/auth/federated/link` | Link provider to existing account — flow token required |
| `POST` | `/api/auth/password/forgot` | Request password reset OTP |
| `POST` | `/api/auth/password/reset` | Reset password with OTP |
| `POST` | `/api/auth/password/change` | Change password — authenticated |
| `POST` | `/api/challenges` | Request or resend an OTP challenge |
| `GET` | `/oauth2/authorization/google` | Initiate Google OIDC flow — browser only |
| `GET` | `/oauth2/authorization/github` | Initiate GitHub OAuth2 flow — browser only |

For complete flow documentation — edge cases, design decisions, and domain invariants at each step — see [`docs/FLOWS.md`](docs/FLOWS.md).

---

## Configuration

Copy `.env.example` to `.env` and fill in your values before running.

**Database**

| Variable | Description |
|---|---|
| `DB_HOST` | PostgreSQL host |
| `DB_PORT` | PostgreSQL port (default: `5432`) |
| `DB_NAME` | Database name |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |

**Redis**

| Variable | Description |
|---|---|
| `REDIS_HOST` | Redis host |
| `REDIS_PORT` | Redis port (default: `6379`) |

**JWT**

| Variable | Description |
|---|---|
| `JWT_SECRET` | HMAC-SHA256 signing secret — **minimum 32 characters** |
| `JWT_ACCESS_TOKEN_EXPIRY_MINUTES` | Access token TTL in minutes (default: `5`) |
| `JWT_FLOW_TOKEN_EXPIRY_MINUTES` | Flow token TTL in minutes (default: `5`) |
| `JWT_REFRESH_TOKEN_EXPIRY_DAYS` | Refresh token TTL in days (default: `7`) |

**OTP**

| Variable | Description |
|---|---|
| `OTP_EXPIRY_SECONDS` | OTP lifetime in seconds (default: `300`) |
| `OTP_LENGTH` | Number of digits (default: `6`) |

**Google OAuth2**

| Variable | Description |
|---|---|
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |

**GitHub OAuth2**

| Variable | Description |
|---|---|
| `GITHUB_CLIENT_ID` | GitHub OAuth2 client ID |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 client secret |

**Mail**

| Variable | Description |
|---|---|
| `MAIL_HOST` | SMTP host (e.g. `smtp.gmail.com`) |
| `MAIL_PORT` | SMTP port (e.g. `587`) |
| `MAIL_USERNAME` | Sender email address |
| `MAIL_PASSWORD` | App password — Gmail requires an App Password, not the account password |
| `MAIL_FROM` | Display name / from address |

**Turnstile**

| Variable | Description |
|---|---|
| `TURNSTILE_SECRET_KEY` | Cloudflare Turnstile secret key. For local development use the always-pass test key: `1x0000000000000000000000000000000AA` |

---

## Tech Stack

| | |
|---|---|
| Runtime | Java 21, Spring Boot 4 |
| Security | Spring Security — OAuth2 Client, JWT filter, method security |
| Persistence | PostgreSQL, Spring Data JPA, Hibernate, Flyway |
| Cache / Rate limiting | Redis, Bucket4j — distributed, multi-bandwidth, interval refill |
| JWT | jjwt — HMAC-SHA256 signing and verification |
| AOP | Spring AOP — rate limiting and human verification enforced at method level |
| Human verification | Cloudflare Turnstile — behind `HumanVerifier` port |
| Email | JavaMailSender — SES-compatible, swap by writing one adapter |
| Error handling | [`d76-spring-boot-starter-exception`](https://github.com/dhanesh76/d76-spring-boot-starter-exception) |
| Infrastructure | Docker, Docker Compose, nginx |

---

## Roadmap

**Next milestone — `authx-security-starter`**

A Spring Boot starter for the consumer side. Spring Boot applications that call AuthX will be able to add one dependency, configure the JWT secret, and have the verification filter auto-wired into their security chain — no Spring Security boilerplate required.

```xml
<!-- Planned — not yet available -->
<dependency>
    <groupId>dev.d76</groupId>
    <artifactId>authx-security-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Further roadmap**

- Full starter extraction — embed AuthX into a Spring Boot app as a single dependency
- ProblemDetail response format — RFC 9457
- Magic link authentication
- SMS OTP delivery
- Admin endpoints — block account, role management
- Account deletion — `RE_AUTH` token is already designed for this
- Refresh token family tracking — full rotation chain compromise detection
- Maven Central publication
- Integration test suite

---

## Why Self-Hosted

- **Zero vendor dependency** — your server, your database, your rules, your uptime
- **No usage limits** — no MAU pricing, no feature tiers, no request caps
- **Full source ownership** — read every line, change anything, audit the security yourself
- **Any stack** — your frontend, mobile app, backend service, or CLI tool calls it over HTTP
- **Your data stays with you** — user records live in your infrastructure

---

## Contributing

The most needed contribution right now is the `authx-security-starter` — packaging AuthX's JWT verification as a Spring Boot auto-configuration that consumer applications can drop in as a dependency. If you have experience with Spring Boot starter development, auto-configuration, or Maven Central publication, that is where the help matters most.

For any contribution:

```bash
git clone https://github.com/dhanesh76/authx.git
cd authx
cp .env.example .env
docker compose up --build
```

Read [`docs/FLOWS.md`](docs/FLOWS.md) before writing code. It covers every flow, every edge case, and the reasoning behind each design decision.

---

## License

Licensed under the [MIT License](LICENSE).