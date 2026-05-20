# AuthX — Identity Flows

This document describes the complete set of identity flows implemented in AuthX.
Each flow covers the happy path, the edge cases, and the domain invariants enforced
at every step. Read this before reading the source code.

---

## Table of Contents

1. [Credential Registration](#1-credential-registration)
2. [vo.kernel.dev.d76.authx.Email Verification](#2-email-verification)
3. [Credential Login](#3-credential-login)
4. [Token Refresh](#4-token-refresh)
5. [Logout](#5-logout)
6. [Federated Login](#6-federated-login)
7. [Federated Registration](#7-federated-registration)
8. [Federated Provider Linking](#8-federated-provider-linking)
9. [Forgot Password](#9-forgot-password)
10. [Reset Password](#10-reset-password)
11. [Change Password](#11-change-password)
12. [Cross-Cutting Concerns](#12-cross-cutting-concerns)

---

## 1. Credential Registration

**Endpoint:** `POST /api/auth/register`
**Human verification required:** action `register`

The user provides a username, email address, and password. The system creates an
account in `VERIFICATION_PENDING` state and immediately issues an OTP to the
provided email address. The account cannot be used for login until the email is
verified.

**Steps:**

1. Human verification token validated.
2. vo.kernel.dev.d76.authx.Email uniqueness checked — `EMAIL_ALREADY_REGISTERED` (409) if taken.
3. vo.domain.account.dev.d76.authx.Username uniqueness checked — `USERNAME_TAKEN` (409) if taken.
4. Account created in `VERIFICATION_PENDING` state with `EMAIL` as the identity
   provider.
5. OTP issued and sent to the email address.

**OTP issuance is outside the transaction boundary.** If mail dispatch fails after
the account is saved, the account remains persisted. The client can request a
resend via `POST /api/challenges`. Rolling back the account creation for a mail
failure would force the user to re-register unnecessarily.

**Password rules:**
- Minimum 8 characters
- Must contain at least one uppercase letter, one lowercase letter, one digit,
  and one special character

**vo.domain.account.dev.d76.authx.Username rules:**
- 3 to 30 characters
- Alphanumeric and underscore only
- Case-insensitive — stored as lowercase

---

## 2. vo.kernel.dev.d76.authx.Email Verification

**Endpoint:** `POST /api/auth/verify-email`
**Human verification required:** none

The user submits the OTP received by email. On success the account transitions
from `VERIFICATION_PENDING` to `ACTIVE` and becomes available for login.

**Steps:**

1. OTP verified against the active challenge for this email and purpose.
2. OTP verification fails fast — if the challenge is not found or the secret does
   not match, the account is never loaded.
3. Account loaded and activated.

**Challenge storage and expiry:**

Challenges are stored in Redis with a TTL derived from the configured OTP
lifetime (default 300 seconds). When the TTL expires, Redis deletes the key
automatically. An expired OTP and a consumed OTP are indistinguishable from the
system's perspective — both result in `CHALLENGE_NOT_FOUND` (404).

**Resending an OTP:**

Call `POST /api/challenges` with `purpose: EMAIL_VERIFICATION`. Any previously
pending challenge for this email and purpose is invalidated before the new one
is issued. Only one active challenge exists per email per purpose at any time.

**Edge case — already active:**

If the account is already active (for example the user calls verify twice),
`ACCOUNT_ALREADY_ACTIVE` (409) is returned.

---

## 3. Credential Login

**Endpoint:** `POST /api/auth/login`
**Human verification required:** action `login`

The user submits a username or email address together with their password. On
success, an access token and a refresh token are issued.

**Steps:**

1. Human verification token validated.
2. Account looked up by email or username in a single database query.
   Both are stored lowercase — the identifier is normalised before querying.
3. Password verified against the stored hash via `Account.verifyPassword()`.
4. Access token issued (short-lived JWT).
5. Refresh token issued (opaque, server-side, 7-day TTL).

**What `login` accepts:**

The `usernameOrEmail` field accepts either format. The system tries the input as
an email address first, then as a username, in a single OR query.

**Social-only accounts:**

Accounts created via federated registration have no password set. Attempting
credential login returns `INVALID_CREDENTIALS` (401). The system does not expose
whether the account exists or whether it lacks a password.

**Inactive accounts:**

Accounts in `VERIFICATION_PENDING` or `BLOCKED` state return `ACCOUNT_NOT_ACTIVE`
(403). The system does not distinguish between these two states in the response.

**Session management:**

This system is stateless. The access token is a signed JWT carrying identity
claims. The refresh token is an opaque token stored server-side as a SHA-256 hash.
No session is created on the server for credential login.

---

## 4. Token Refresh

**Endpoint:** `POST /api/auth/refresh`
**Human verification required:** none

The client presents a refresh token. The system validates it, loads the account
fresh, issues a new access token, and rotates the refresh token.

**Steps:**

1. Refresh token hash looked up in the database.
2. Token validated — not expired, not revoked.
3. Account loaded from the database. Roles are re-evaluated on every refresh,
   so role changes take effect on the next refresh without requiring a new login.
4. New access token issued.
5. Old refresh token deleted. New refresh token issued. This is atomic rotation.

**Rotation and stolen token detection:**

Refresh tokens are single-use. The moment a token is presented, it is deleted and
a replacement is issued. If the same token is presented a second time, it is not
found in the database and `INVALID_TOKEN` (401) is returned. This detects stolen
token reuse — if an attacker uses a stolen token before the legitimate client
refreshes, the next legitimate refresh attempt will fail, alerting the user that
their session may be compromised.

**Access token validity after logout:**

The access token remains valid until its natural expiry after logout or password
change. This is a deliberate tradeoff of the stateless JWT model. Clients should
discard access tokens immediately on logout. The refresh token is the revocable
credential — revoking it terminates the ability to extend the session.

---

## 5. Logout

**Endpoint:** `POST /api/auth/logout`
**Human verification required:** none

The client presents its refresh token. The token is revoked server-side.

**Behaviour:**

- If the token is found and active, it is marked as revoked.
- If the token is not found or already revoked, the operation completes silently.
  Logout must never fail the client regardless of token state.

**Applies to all authentication methods.** Whether the user logged in with
credentials, Google, or GitHub, all sessions are managed identically through
refresh tokens after authentication.

**Access token note:** The access token is not invalidated by logout. It remains
valid until its configured TTL expires. Clients must discard it on logout.

---

## 6. Federated Login

**Endpoints:**
- `GET /oauth2/authorization/google` — initiates Google OIDC flow
- `GET /oauth2/authorization/github` — initiates GitHub OAuth2 flow

**Human verification required:** none — the provider has verified the user

The user is redirected to the identity provider's consent page. After the provider
authenticates the user and redirects back, the system evaluates the account state
and produces one of four outcomes.

**Decision outcomes:**

| Outcome | Condition | Response |
|---|---|---|
| `ALLOW_LOGIN` | Account exists, provider already linked | Access token + refresh token |
| `REQUIRE_REGISTRATION` | No account exists for this email | Error (404) + flow token |
| `REQUIRE_LINKING` | Account exists, provider not linked | Error (409) + flow token |
| `BLOCKED` | Account is blocked | Error (403) |

**Flow tokens:**

When the outcome is `REQUIRE_REGISTRATION` or `REQUIRE_LINKING`, a short-lived
flow token is embedded in the error response body under the key `flowToken`. This
token is a signed JWT with a 5-minute TTL and a stamped intent — either
`OPEN_ACCOUNT` or `LINK_IDENTITY`. The client must present this token in the
appropriate follow-up request.

Flow tokens are single-use and intent-specific. A `LINK_IDENTITY` token cannot be
used at the registration endpoint and vice versa.

**GitHub email handling:**

GitHub users may set their email to private, in which case it is absent from the
standard OAuth2 user attributes. The system falls back to the
`GET https://api.github.com/user/emails` API and selects the primary verified
address. If no verified primary email is found, the login fails.

---

## 7. Federated Registration

**Endpoint:** `POST /api/auth/federated/register`
**Human verification required:** none — provider already verified the user

The user provides a chosen username together with the flow token received from
the federated login outcome. On success, the account is created and tokens are
issued immediately.

**Steps:**

1. Flow token verified — signature, expiry, and intent (`OPEN_ACCOUNT`).
2. vo.domain.account.dev.d76.authx.Username uniqueness checked — `USERNAME_TAKEN` (409) if taken. This is a race
   condition guard: the username may have been taken between the provider
   callback and this request.
3. Account created in `ACTIVE` state. No OTP verification is required because the
   provider has already verified email ownership.
4. Access token and refresh token issued. The user is logged in immediately.

**Why ACTIVE immediately:**

The identity provider's assertion of email ownership is treated as authoritative
proof. Google and GitHub do not issue identity tokens for unverified email
addresses. Their verification process is at least as rigorous as OTP verification.

---

## 8. Federated Provider Linking

**Endpoint:** `POST /api/auth/federated/link`
**Human verification required:** none — provider already verified the user

The user provides the flow token received from the federated login outcome. The
provider is linked to the existing account and tokens are issued immediately.

**Steps:**

1. Flow token verified — signature, expiry, and intent (`LINK_IDENTITY`).
2. Account loaded by email extracted from the flow token.
3. `Account.linkProvider()` called — enforces:
    - Blocked accounts cannot link providers.
    - A provider cannot be linked twice.
4. Access token and refresh token issued. The user is logged in immediately.

**Auto-activation of pending accounts:**

If the account is in `VERIFICATION_PENDING` state — registered with credentials
but email not yet verified — linking a federated provider activates the account
immediately. The provider's email verification satisfies the verification
requirement. This mirrors the behaviour of GitHub and other major identity
platforms.

This means a user who registers with email and password but has not verified their
email can complete their account activation by logging in via Google or GitHub,
provided the provider's email matches the registered email.

---

## 9. Forgot Password

**Endpoint:** `POST /api/auth/password/forgot`
**Human verification required:** action `password.forgot`

The user provides their email address. The system confirms the account exists and
issues a password reset OTP.

**Steps:**

1. Human verification token validated.
2. Account existence confirmed by email — `ACCOUNT_NOT_FOUND` (404) if not found.
   Unlike some systems, AuthX does not silently succeed for unknown emails. This
   is a deliberate choice to prevent unintended OTP floods to arbitrary addresses.
3. OTP issued with purpose `PASSWORD_RESET`.

**Any previously pending `PASSWORD_RESET` challenge for this email is invalidated**
before the new one is issued.

---

## 10. Reset Password

**Endpoint:** `POST /api/auth/password/reset`
**Human verification required:** action `password.reset`

The user provides their email address, the OTP received, and a new password. On
success the password is updated and all active sessions are terminated.

**Steps:**

1. Human verification token validated.
2. OTP verified against the active `PASSWORD_RESET` challenge for this email.
   Verification fails fast — the account is not loaded if the OTP is wrong.
3. Account loaded.
4. Password updated via `Account.changePassword()`.
5. All refresh tokens for the account revoked — all active sessions terminated.

**Social-only accounts setting a first password:**

Accounts created via federated registration have no password set. These accounts
may use the forgot password flow to establish a credential password for the first
time. After a successful reset, credential login is available alongside the
federated provider. The `SAME_PASSWORD` check is skipped for accounts with no
existing password — there is nothing to compare against.

**Session termination on reset:**

All active sessions are revoked after a password reset as a security measure.
Any access tokens issued before the reset remain valid until their natural expiry.
The client should discard them.

---

## 11. Change Password

**Endpoint:** `POST /api/auth/password/change`
**Authentication required:** valid access token

The authenticated user provides their current password and desired new password.
On success the password is updated and all active sessions are terminated.

**Steps:**

1. Account email extracted from the access token claims — the request body does
   not accept an email field. The identity of the caller is established by the
   token, not by what the caller claims.
2. Current password verified via `Account.verifyPassword()`.
3. New password set via `Account.changePassword()` — `SAME_PASSWORD` (409) if the
   new password matches the current one.
4. All refresh tokens for the account revoked.

**Social-only accounts:**

Accounts with no password set return `INVALID_CREDENTIALS` (401) on current
password verification. These accounts should use the forgot password flow instead
to establish a first password.

**Note on current password validation:**

The current password field is not validated against the password format rules.
The current password already exists in the system and was validated at creation
time. Applying format rules to an existing credential would incorrectly reject
passwords set before stricter rules were introduced.

---

## 12. Cross-Cutting Concerns

### Human Verification

Selected endpoints require a Cloudflare Turnstile verification token in the
`X-Verification-Token` request header. The token is validated server-side before
the endpoint logic runs. If the token is absent or fails verification, the
request is rejected with `400` before any business logic executes.

Protected endpoints and their required actions:

| Endpoint | Action |
|---|---|
| `POST /api/auth/register` | `register` |
| `POST /api/auth/login` | `login` |
| `POST /api/auth/password/forgot` | `password.forgot` |
| `POST /api/auth/password/reset` | `password.reset` |

The action string must match what was passed to `turnstile.render()` on the
client side. The server verifies this against the action recorded in the token
by Cloudflare. Tokens generated for one action cannot be replayed on a different
endpoint.

The verification mechanism is abstracted behind the `HumanVerifier` port.
Turnstile is the current implementation. Swapping to a different provider
requires only a new implementation of `HumanVerifier` — no controller or
application logic changes.

### Rate Limiting

OTP issuance via `POST /api/challenges` is rate limited at two independent levels:

**Per email address:**
- Maximum 1 request per 30 seconds
- Maximum 5 requests per 5 minutes

**Per IP address:**
- Maximum 3 requests per 30 seconds
- Maximum 20 requests per 5 minutes

Both limits must pass for a request to proceed. Exceeded limits return `429` with
`Retry-After` (seconds until retry) and `X-Retry-At` (Unix timestamp of when to
retry) headers.

Rate limiting is backed by Redis via Bucket4j. Buckets use interval refill rather
than greedy refill — the full capacity is restored at the end of each window, not
gradually.

### Token Architecture

Three JWT token types are in use. Each token carries a `typ` claim that is
verified first on every parse to prevent token substitution attacks.

| Type | Purpose | TTL | Subject |
|---|---|---|---|
| `ACCESS` | Authenticates API requests | Configurable (default 5 min) | Account UUID |
| `FLOW` | Authorises a specific federated flow step | Configurable (default 5 min) | vo.kernel.dev.d76.authx.Email address |
| `RE_AUTH` | Authorises a sensitive action (future) | Configurable | vo.kernel.dev.d76.authx.Email address |

Refresh tokens are opaque strings, not JWTs. They are stored server-side as
SHA-256 hashes. The raw token value is returned to the client and never stored.
Rotation is enforced — each token is single-use.

### Account States

| State | Description |
|---|---|
| `VERIFICATION_PENDING` | Account created, email not yet verified |
| `ACTIVE` | Account verified and usable |
| `BLOCKED` | Account suspended, all operations rejected |

Federated provider linking on a `VERIFICATION_PENDING` account transitions it
directly to `ACTIVE` — see Flow 8 for the rationale.

### Password Encoding

The `PasswordEncoder` port accepts raw strings rather than the `vo.domain.account.dev.d76.authx.RawPassword`
value object. This separation is intentional — encoding is an infrastructure
concern. The `vo.domain.account.dev.d76.authx.RawPassword` value object enforces format rules at domain
boundaries (registration, password change). At verification time the current
password is passed as a plain string because re-validating format rules against
an existing credential would incorrectly reject passwords set before stricter
rules were introduced.

---

*This document describes AuthX as implemented. For configuration reference see
`README.md`. For the HTTP API contract see the Postman collection in
`docs/postman/`.*