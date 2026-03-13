# Email Service Configuration ✉️

This document explains how the email service is configured in the MAISON LUXE E-Commerce application, covering the dual-provider strategy, required environment variables, application properties, and the REST API endpoints available to verify and test the configuration.

---

## 1) Architecture Overview 🏗️

The application uses a **dual-mode email system**:

1. **Primary — Mailjet REST API** (`MiljetEmailClient`): Sends email via HTTP calls to `https://api.mailjet.com/v3.1/send` using Basic Authentication (API key + secret).
2. **Fallback — Spring Mail SMTP** (`JavaMailSender`): Used when the Mailjet REST API is not configured or when a call to it fails. The application ships Mailjet's SMTP relay settings as the recommended SMTP provider, but any SMTP server works.

**Decision logic in `EmailService.sendHtmlEmail()`:**

```
if (MiljetEmailClient.isConfigured())
    → try Mailjet REST API
    → if REST call fails → fall through to SMTP
else
    → send via SMTP directly
```

---

## 2) Key Source Files 📂

| File | Role |
|------|------|
| `src/main/java/za/ac/styling/service/EmailService.java` | Orchestrates email sending; builds HTML templates |
| `src/main/java/za/ac/styling/service/MiljetEmailClient.java` | Mailjet REST API client |
| `src/main/java/za/ac/styling/controller/EmailController.java` | `/api/email/config-check` and `/api/email/test` endpoints |
| `src/main/resources/application.properties` | Property bindings for environment variables |

---

## 3) Required Environment Variables 🔑

Set these in your deployment platform's secret manager (e.g., Render Environment, AWS Secrets Manager). **Never commit real values to source control.**

### Mailjet REST API (Primary)

| Variable | Description | Example |
|----------|-------------|---------|
| `MAILJET_API_KEY` | Mailjet API Key (Public Key) | `abcdef1234567890...` |
| `MAILJET_API_SECRET` | Mailjet Secret Key | `secretkey...` |

Both values come from your **Mailjet account → API Key Management** page. The REST API client is automatically enabled when both values are present and non-blank. You can explicitly disable it by setting the property `mailjet.enabled=false`.

### Sender Identity

| Variable | Description | Default (fallback) |
|----------|-------------|-------------------|
| `MAIL_SENDER_EMAIL` | The "From" email address | `hloniyacho@gmail.com` |
| `MAIL_SENDER_NAME` | The display name shown in the inbox | `MAISON LUXE` |

> **Important:** The sender address must be a verified sender domain in your Mailjet account. If you use an unverified address, Mailjet will reject the request.

### Spring Mail SMTP (Fallback)

| Variable | Description | Mailjet SMTP value |
|----------|-------------|-------------------|
| `SPRING_MAIL_HOST` | SMTP server hostname | `in-v3.mailjet.com` |
| `SPRING_MAIL_PORT` | SMTP port | `587` |
| `SPRING_MAIL_USERNAME` | SMTP username | Same as `MAILJET_API_KEY` |
| `SPRING_MAIL_PASSWORD` | SMTP password | Same as `MAILJET_API_SECRET` |

The SMTP transport always uses `STARTTLS` (configured statically in `application.properties`).

---

## 4) `application.properties` Bindings ⚙️

```properties
# ===============================
# MAILJET REST API CONFIG
# ===============================
mailjet.api.key=${MAILJET_API_KEY}
mailjet.api.secret=${MAILJET_API_SECRET}
# mailjet.enabled=true          # Defaults to true; set false to force SMTP-only mode
# mailjet.api.url=https://api.mailjet.com/v3.1/send   # Override only if required

# ===============================
# SENDER IDENTITY
# ===============================
mail.sender.email=${MAIL_SENDER_EMAIL}
mail.sender.name=${MAIL_SENDER_NAME}

# ===============================
# SPRING MAIL SMTP (Fallback)
# ===============================
spring.mail.host=${SPRING_MAIL_HOST}
spring.mail.port=${SPRING_MAIL_PORT}
spring.mail.username=${SPRING_MAIL_USERNAME}
spring.mail.password=${SPRING_MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

---

## 5) How `MiljetEmailClient` Validates Configuration 🔍

`MiljetEmailClient.isConfigured()` returns `true` only when **all** of the following are satisfied:

```java
String apiKey    = env.getProperty("mailjet.api.key");
String apiSecret = env.getProperty("mailjet.api.secret");
boolean enabled  = "true".equalsIgnoreCase(env.getProperty("mailjet.enabled", "true"));

return enabled && apiKey != null && !apiKey.isBlank()
               && apiSecret != null && !apiSecret.isBlank();
```

If the check returns `false`, every email is routed through Spring Mail SMTP instead.

---

## 6) Mailjet REST API Request Format 📤

Each call to `MiljetEmailClient.sendEmail()` sends a `POST` request to `https://api.mailjet.com/v3.1/send` with:

- **Authorization:** `Basic <Base64(apiKey:apiSecret)>`
- **Content-Type:** `application/json`
- **Body:**

```json
{
  "Messages": [
    {
      "From": { "Email": "sender@example.com", "Name": "MAISON LUXE" },
      "To":   [ { "Email": "recipient@example.com", "Name": "First Name" } ],
      "Subject": "Email subject",
      "HTMLPart": "<html>...</html>"
    }
  ]
}
```

A 2xx response from Mailjet is treated as success; any other status code throws a `RuntimeException`, which causes `EmailService` to fall back to SMTP.

---

## 7) Emails Sent by the Application 📧

| Method | Subject | Trigger |
|--------|---------|---------|
| `sendOrderInvoice(user, order)` | `Your Order Invoice - Order #<num>` | New order placed |
| `sendOrderStatusUpdate(user, order)` | `Order Status Update - Order #<num>` | Order status changed |
| `sendPasswordResetEmail(to, link, name)` | `Password Reset Request - MAISON LUXE` | Password reset requested |
| `sendWelcomeEmail(user)` | `Welcome to MAISON LUXE - Let's Get Started!` | New user registration |
| `sendTestEmail(to)` | `Test Email - MAISON LUXE` | Manual test via API |

> **Note:** `sendPasswordResetEmail` always uses Spring Mail SMTP directly (bypasses the Mailjet REST client).

---

## 8) REST Endpoints for Configuration Verification 🌐

Both endpoints are defined in `EmailController` under the `/api/email` base path.

### `GET /api/email/config-check`

Returns the current Mailjet REST API configuration status without sending any email.

**Example response (configured):**
```json
{
  "mailjetRestApiConfigured": true,
  "mailjetEnabled": "✅ Enabled",
  "message": "Mailjet REST API is properly configured and ready to use.",
  "recommendation": "Use POST /api/email/test with {\"email\":\"your@email.com\"} to send a test email."
}
```

**Example response (not configured):**
```json
{
  "mailjetRestApiConfigured": false,
  "mailjetEnabled": "❌ Disabled or Missing Credentials",
  "message": "Mailjet REST API is NOT configured. Check application.properties.",
  "requiredProperties": "mailjet.api.key, mailjet.api.secret, mailjet.enabled=true"
}
```

### `POST /api/email/test`

Sends a test email to any address using whichever provider is active.

**Request body:**
```json
{ "email": "test@example.com" }
```

**Success response:**
```json
{ "success": true, "message": "Test email sent (check inbox/spam)." }
```

**Failure response (HTTP 500):**
```json
{ "success": false, "message": "Failed to send test email. Check server logs for details." }
```

---

## 9) Maven Dependency 📦

Email support is provided by the Spring Boot Mail starter declared in `pom.xml`:

```xml
<!-- EMAIL SUPPORT -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

This pulls in `jakarta.mail` (MIME message building) and `spring-context-support` (JavaMailSender).

---

## 10) Operational Recommendations ✅

- Verify your Mailjet sender domain before going live — unverified senders cause API rejections.
- Store `MAILJET_API_KEY` and `MAILJET_API_SECRET` in your platform's secret manager; never hardcode them.
- After deploying, call `GET /api/email/config-check` to confirm the configuration is picked up correctly.
- Use `POST /api/email/test` to send an end-to-end test email and verify deliverability.
- Monitor the application logs for `"Email sent via Mailjet REST API"` (success) or `"falling back to SMTP"` (REST failure) messages.
- If only SMTP is needed (no Mailjet REST API), set `mailjet.enabled=false` and supply only the SMTP variables.

---

## 11) Quick Reference: Where to Look in Code 🔍

| Concern | Location |
|---------|----------|
| REST API client | `za.ac.styling.service.MiljetEmailClient` |
| Sending orchestration & fallback | `za.ac.styling.service.EmailService.sendHtmlEmail()` |
| HTML template builders | `EmailService.buildInvoiceHtml()`, `buildStatusUpdateHtml()`, `buildWelcomeEmailHtml()`, `buildPasswordResetEmail()` |
| Config-check & test endpoints | `za.ac.styling.controller.EmailController` |
| Property bindings | `src/main/resources/application.properties` |
