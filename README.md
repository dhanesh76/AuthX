# Authx

### A Stateless Authentication & Authorization Service Built with Spring Boot

Authx is a backend authentication system built using **Spring Boot and Spring Security**, supporting multi-provider login, stateless JWT authorization, and secure account workflows.

The project focuses on clean identity modeling, OAuth2/OIDC integration, and scalable authentication design rather than just implementing login endpoints.

---

## Overview

Authx supports:

* Email & Password Authentication
* GitHub OAuth2 Login
* Google OIDC Login
* JWT-based Stateless Authorization
* OTP-based Email Verification
* Secure Password Reset
* Social Account Linking
* Role-Based Access Control (RBAC)

The system is designed with separation of concerns, extensibility, and scalability in mind.

---

## Architectural Approach

Authx is structured with clear layering:

```
Controller → Service → Security → Persistence
```

### Key Design Decisions

* Unified `UserPrincipal` across all authentication mechanisms
* Stateless request authentication using JWT
* JWT subject (`sub`) uses immutable `userId` instead of email
* No database lookup required for validating authenticated requests
* Centralized exception handling and structured API responses

---

## Stateless JWT Authentication

After a successful login, the system issues a signed JWT.

For every protected request:

1. The token signature is verified
2. Expiration is validated
3. Claims are extracted
4. `Authentication` is reconstructed from claims
5. The principal is injected into `SecurityContext`

No repository lookup is required to authenticate a request.

### JWT Claim Structure

```json
{
  "sub": "userId",
  "email": "user@email.com",
  "provider": "GOOGLE",
  "roles": ["ROLE_USER"]
}
```

### Why `userId` as Subject?

* Email is a login identifier and may change
* `userId` is immutable system identity
* Cleaner alignment with microservice patterns
* Prevents identity drift

---

## Unified Identity Model

All login methods produce the same internal representation:

```
UserPrincipal
```

This class implements:

* `UserDetails`
* `OAuth2User`
* `OidcUser`

This removes type branching (`instanceof`) in the security layer and keeps the authentication mechanism independent from business logic.

---

## Authentication Flows

### Email Login

```
Credentials
   ↓
AuthenticationManager
   ↓
UserPrincipal
   ↓
Access Token Issued
```

### OAuth2 / OIDC Login

```
Provider Handshake
   ↓
Custom OAuth Service
   ↓
Account Verification
   ↓
UserPrincipal
   ↓
Access Token Issued
```

### Stateless API Request

```
JWT
   ↓
JwtFilter
   ↓
Signature Validation
   ↓
Principal Reconstruction
   ↓
Authorization
```

---

## Security Deep Dive

### 1. Token Integrity

All JWTs are signed using HMAC-SHA256 and verified on every request.
If a token is tampered, malformed, or expired, it is rejected before reaching business logic.

Authentication trust is derived from cryptographic verification rather than database comparison.

---

### 2. Zero Database Hits for Authentication

The `JwtFilter` reconstructs the authenticated principal directly from validated claims.

This means:

* No `UserDetailsService` lookup per request
* No DB call required to validate identity
* Horizontal scalability without session storage

---

### 3. Purpose-Scoped Tokens

Authx uses purpose-based tokens for controlled workflows:

* Access tokens for API authorization
* Social registration tokens
* Account linking tokens
* Re-authentication tokens

Each token contains a purpose claim and is validated against the expected intent before execution.

---

### 4. Provider Linking Safety

When logging in with OAuth:

* Email presence is validated
* User existence is verified
* Provider linkage is checked

If the provider is not linked, a controlled linking flow is enforced using action tokens.
This prevents unintended account takeover via external providers.

---

### 5. OTP System Design

* OTPs generated using `SecureRandom`
* Stored in Redis with TTL
* Purpose-scoped validation
* Automatically invalidated after successful verification

This design prevents replay and enforces temporal validity.

---

### 6. Structured Error Modeling

The system uses:

* `BusinessException`
* `ErrorCode` abstraction
* Global exception handling

All errors return structured responses with consistent fields and status codes.

---

## Modular Structure

```
auth/
core/
notification/
oauth/
security/
user/
```

Domain-oriented packaging improves readability and maintainability.

---

## Tech Stack

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* JJWT
* Redis
* Maven

---

## Running Locally

1. Clone the repository

```
git clone https://github.com/dhanesh76/AuthX
cd AuthX
```

2. Configure `application.yaml`

Provide configuration for:

* Database connection
* Redis connection
* JWT secret
* OAuth2 credentials (GitHub, Google)

3. Run the application

```
mvn spring-boot:run
```

The application will start on:

```
http://localhost:8080
```

---

## Extensibility

Authx is designed to evolve toward:

* Dedicated Auth Server separation
* API Gateway-level JWT validation
* Refresh token implementation
* Token revocation strategy
* Multi-factor authentication

---

## What This Project Demonstrates

* Custom Spring Security configuration
* OAuth2 and OIDC integration
* Stateless authentication architecture
* Identity modeling best practices
* Clean separation between authentication and business logic
* Scalable backend security design
