# Security Overview ✅

This document summarizes the main security controls present in the application, why they are used, where the relevant code lives, and recommended operational practices.

---

## 1) Authentication & Authorization 🔐

- Mechanism: JSON Web Tokens (JWT) using JJWT library.
  - Files: `za.ac.styling.security.JwtUtil`, `za.ac.styling.security.JwtAuthenticationFilter`
  - Tokens are HMAC-signed using the `jwt.secret` property (must be set to a strong, random secret in production).
  - Default expiration: Access tokens ~15 minutes (`security.jwt.expirationMillis`), Refresh tokens ~7 days (`security.jwt.refreshExpirationMillis`).
- Authorization: Roles stored in JWT (claim `roles`) are mapped to Spring Security authorities.
- Session management: Stateless (no server sessions) — `SessionCreationPolicy.STATELESS` in `SecurityConfig`.

> Note: Keep the signing secret out of source control and rotate it periodically.

---

## 2) Passwords & Account Safety 🔑

- Password storage: BCrypt (`BCryptPasswordEncoder`) used for hashing and verification.
  - Files: `UserController`, `PasswordResetService`, `UserFactory`.
- Password reset flow:
  - Secure random token (32 bytes), OTP (6 digits), 1 hour expiry.
  - Token stored server-side and marked used after reset.
  - Behavior avoids revealing whether an email exists (helps prevent user enumeration).
  - **Password reuse prevention:** newly chosen password **must not** match the previous password; server enforces this during reset and returns a clear error when violated.
  - Files: `PasswordResetService`, `PasswordResetToken`.
- **Account lockout protection:**
  - After 3 failed login attempts, account is locked for 10 minutes.
  - User receives clear message: "Your account has been locked for 10 minutes."
  - Failed attempts counter resets on successful login.
  - Fields: `failedLoginAttempts`, `accountLockedUntil` in `User` entity.
  - Logic: `User.incrementFailedAttempts()`, `User.isAccountLocked()`, `User.resetFailedAttempts()`.

---

## 3) Transport & Network Security 🌐

- HTTPS enforcement: Requests proxied with `X-Forwarded-Proto` will be redirected to HTTPS in production via `HttpsEnforcementFilter`.
  - File: `HttpsEnforcementFilter`
- CORS: Configured via `SecurityConfig.corsConfigurationSource()` and supports environment-driven allowed origins (env `APP_CORS_ALLOWED_ORIGINS`).
  - Ensure allowed origins list is restricted in production.

---

## 4) Request & Abuse Protection 🛡️

- Rate limiting: Bucket4j-based `RateLimitFilter` enforces per-path limits (e.g., login: 5 req/min, uploads: 3 req/min, default: 50 req/min).
  - File: `RateLimitFilter`
- No-cache headers: `NoCacheFilter` sets strong `Cache-Control`, `Pragma`, `Expires` headers for `/api/*` to prevent sensitive data from being cached.
  - File: `NoCacheFilter`

---

## 5) Browser & Response Protection 🔒

- Content Security Policy (CSP): A baseline CSP header is added to responses to restrict script/style/img sources.
  - File: `CspFilter` (header example: `default-src 'self'; img-src 'self' https://*.supabase.co https://images.unsplash.com; ...`).
- Other headers: No-cache headers and CSP help mitigate XSS and data leakage.

---

## 6) CSRF Considerations ⚠️

- CSRF is disabled in `SecurityConfig` (`csrf.disable()`). This is acceptable for stateless JWT-based APIs (no cookies used for auth). If you rely on cookie-based auth, re-enable and use an appropriate `CookieCsrfTokenRepository`.

---

## 7) Secret & Configuration Management 🔐

- Important environment/config values that must be set securely:
  - `jwt.secret` — strong random key (do not commit to VCS)
  - Mail/Mailjet credentials (used in email service)
  - DB credentials
  - `APP_CORS_ALLOWED_ORIGINS` (restrict origins in production)
- Recommendation: Store secrets in your deployment platform's secret manager and avoid storing sensitive values in plaintext files.

---

## 8) Operational Recommendations & Best Practices ✅

- Rotate `jwt.secret` when needed and monitor token usage.
- Use HTTPS at the load balancer/ingress level and enable HSTS at the edge.
- Limit CORS origins to the exact production domains.
- Monitor rate-limit hits and set alerting for spikes (potential brute force / abuse).
- Regularly review CSP and expand/lock down any third-party sources.
- Enable security logging and periodic access reviews.
- Add automated security scans (SAST/DAST) and dependency vulnerability checks.

---

## 9) Where to look in code (quick references)

- Security config: `za.ac.styling.config.SecurityConfig`
- JWT: `za.ac.styling.security.JwtUtil`, `za.ac.styling.security.JwtAuthenticationFilter`
- CSP: `za.ac.styling.filter.CspFilter`
- Rate limit: `za.ac.styling.filter.RateLimitFilter`
- No-cache: `za.ac.styling.filter.NoCacheFilter`
- HTTPS enforcement: `za.ac.styling.config.HttpsEnforcementFilter`
- Password reset and hashing: `za.ac.styling.service.PasswordResetService`, `BCryptPasswordEncoder` usages

---

If you'd like, I can also:
- Add a short checklist for production hardening (HSTS, secure cookie flags, secrets rotation)
- Or add CI checks for secret detection and dependency vulnerabilities

If you want any of the optional follow-ups, tell me which one and I'll add it. ✨
