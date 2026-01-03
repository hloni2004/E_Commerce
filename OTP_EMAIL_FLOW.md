# OTP + Email Password Reset Flow (Technical)

This document explains how the OTP and email-based password-reset flow works in this project (backend endpoints at `/api/users/*`). It describes the sequence, data structures, security controls, and example requests/responses.

## High-level sequence

1. User submits their email in the frontend forgot-password form.
2. Frontend POSTs JSON { "email": "user@example.com" } to `POST /api/users/forgot-password`.
3. Backend receives the request and (silently) checks whether a user with that email exists.
4. If the email exists, backend creates a password reset record containing:
   - a cryptographically strong token (long random string) used as the reset identifier,
   - a short numeric OTP code (e.g. 6 digits) used for verification (optional but implemented here),
   - expiry timestamp (e.g. 10–30 minutes in the future),
   - boolean flags: `used`, `otpVerified`, `emailSent`, and `lastSentAt`/`createdAt`.
     The record is persisted in `password_reset_tokens` (represented by `PasswordResetToken` entity).
5. Backend attempts to send an email to the supplied address containing either:
   - a reset link that includes the token as a query parameter, e.g. `https://frontend.example.com/auth/reset-password?token=<token>`
   - and/or the OTP code inside the email body for copy/paste verification
6. Backend returns a generic success response to the client irrespective of whether an account exists (prevents user enumeration). In development/debug mode it may include the token for testing.
7. User follows the reset flow in the frontend:
   - The frontend can validate `token` using `GET /api/users/validate-reset-token/{token}`
   - If OTP is used, frontend posts `{ token, otpCode }` to `POST /api/users/verify-reset-otp`.
   - After OTP verification (or directly if OTP-less), frontend calls `POST /api/users/reset-password` with `{ token, newPassword }`.
8. Backend checks token validity, expiry, OTP verification state, and whether token has been used. If valid, backend hashes the new password (BCrypt) and updates the user record, marking token `used = true`.

## Endpoints (in this project)

- `POST /api/users/forgot-password` — accepts `{ email }` and initiates token + email send. Returns 200 OK with a generic message.
- `GET /api/users/validate-reset-token/{token}` — checks token validity (not expired, not used).
- `POST /api/users/verify-reset-otp` — accepts `{ token, otpCode }` and marks token as otpVerified when correct.
- `POST /api/users/reset-password` — accepts `{ token, newPassword }`, resets password if token valid (and OTP verified if required).
- `POST /api/users/resend-reset-email` — issues a fresh token/email for the same email address.

## Data design (example)

PasswordResetToken entity fields (conceptual):

- `id` (PK)
- `token` (string) — cryptographically random token (e.g. 32+ bytes base64/url-safe)
- `otpCode` (string) — short numeric code (6 digits)
- `otpHash` (string) — optional: store a hash of OTP rather than plaintext
- `expiryDate` (timestamp)
- `used` (boolean)
- `otpVerified` (boolean)
- `emailSent` (boolean)
- `lastSentAt`, `createdAt` (timestamps)
- `user_id` (FK to user)

Notes:

- For safety, store either a hashed OTP or mark OTP attempts separately. If storing OTP plaintext in DB is used only for short-lived tokens, restrict DB access and logs.
- The `token` should be unguessable: use a CSPRNG (e.g. `SecureRandom` bytes then base64url encode).

## Token & OTP generation best practices

- Token length: at least 128 bits of entropy (16 bytes), preferably 256 bits (32 bytes).
- OTP: usually 6 digits (10^6 possibilities). If you rely solely on OTP, consider rate-limiting and short expiry (e.g., 5–10 minutes).
- Consider hashing the OTP with an HMAC or bcrypt if you want to avoid storing it in plaintext.
- Always set a limited expiry and one-time use (`used = true` after successful reset).

## Security controls in place (project-specific)

- Endpoint exposure: `SecurityConfig` permits all clients to call the forgot-password endpoints (e.g. `/api/users/forgot-password`, `/api/users/verify-reset-otp`, `/api/users/reset-password`).
- Anti-enumeration: the server returns a generic success message even if email does not exist.
- Rate limiting: `RateLimitFilter` (Bucket4j) applies limits per remote IP + path. Ensure forgot-password is not too permissive but also not too strict to block valid users; tune limits accordingly.
- CORS: server must allow the frontend origin. If CORS blocks the request, browsers will fail the request with 403/blocked preflight — ensure `APP_CORS_ALLOWED_ORIGINS` or `allowedOriginPatterns` include the frontend origin.
- CSRF: These endpoints are stateless and accept JSON; server disables CSRF for API flows in this app.

## Email sending considerations

- Use a transactional email provider (SendGrid, SES, Mailgun) to improve deliverability.
- Configure SPF, DKIM, and DMARC for the sending domain to avoid emails being marked as spam.
- Rate-limit email sends and back-off on repeated failures.
- In dev mode, include token in response or send to a test email inbox; in production, never return token to API responses.

## Example request/response (frontend -> backend)

Request (forgot password):

POST /api/users/forgot-password
Content-Type: application/json

{
"email": "alice@example.com"
}

Successful response (generic):

200 OK
{
"success": true,
"message": "If an account exists with this email, a password reset link has been sent. Check your email."
}

Development response (only when debug enabled) may include token:

200 OK
{
"success": true,
"message": "If an account exists...",
"token": "<a-long-token-for-testing>",
"emailSent": true
}

Verify OTP request:

POST /api/users/verify-reset-otp
Content-Type: application/json

{
"token": "<token-from-email-or-response>",
"otpCode": "123456"
}

Reset request:

POST /api/users/reset-password
Content-Type: application/json

{
"token": "<token>",
"newPassword": "S3cureP@ssw0rd"
}

## Failure modes & debugging

- Browser shows 403 for the POST: check CORS preflight (OPTIONS) — confirm server responds to OPTIONS and includes `Access-Control-Allow-Origin`, `Access-Control-Allow-Credentials` (if using cookies), and `Access-Control-Allow-Headers`.
- Backend logs show token created but email not sent: check email provider credentials, queued job processor, or `emailSent` flag and `debugError` in the dev response.
- Rate limit triggered: check `RateLimitFilter` logs and tune buckets for `forgot-password` path.

## Logging & monitoring recommendations

- Log attempts at `/api/users/forgot-password` with anonymized email (or masked) and the origin header for debugging.
- Emit metrics for: reset tokens created, emails successfully sent, email send failures, OTP verifications, reset completions, and rate-limit drops.
- Alert on sudden spikes of password reset initiations from single IP ranges (possible abuse).

## Deployment notes

- Ensure `APP_CORS_ALLOWED_ORIGINS` includes the frontends used in production and staging.
- Ensure environment `SPRING_PROFILES_ACTIVE` and HTTPS enforcement are correct when hosted behind Render or other proxies (check `X-Forwarded-Proto`).

---

If you want, I can:

- add a short sequence diagram (mermaid) to this file,
- insert concrete code snippets from `PasswordResetService` to show exact implementation details,
- or add a small test script (curl examples) to exercise the endpoints.

Tell me which option you prefer and I will update the file accordingly.
