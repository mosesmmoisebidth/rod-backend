# Rod App Backend (Spring Boot)

Production-ready backend for application built with Spring Boot, PostgreSQL, JWT authentication (access + refresh tokens), email verification by 6‑digit code, and WebSocket messaging. Includes Swagger/OpenAPI documentation and a global JSON error handler.

## Features
- User registration with email verification via 6‑digit code
- Login with username (or email) + password
- JWT auth: access token (15 min) and refresh token (14 days by default)
- Protected REST APIs for users, rooms, messages, and memberships
- Password reset via 5‑digit code (request, verify, reset)
- WebSocket configuration for realtime messaging
- Global exception handling with structured JSON errors
- Swagger UI with Bearer token support

## Tech Stack
- Java 17+
- Spring Boot 3
- Spring Security + JWT (jjwt)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Jakarta Validation
- Springdoc OpenAPI (Swagger)
- JavaMail (SMTP)

## Prerequisites
- Java 17 or newer (verify with `java -version`)
- Maven 3.9+ (or use provided wrapper `mvnw` / `mvnw.cmd`)
- PostgreSQL 13+ running locally
- An SMTP account (e.g., Gmail) for sending verification and reset codes

## Getting Started

1) Clone the repository
- Backend root: this folder

2) Create PostgreSQL database
- Create a database named `chatapp` (or change the URL below)

3) Configure application properties
- Default configuration is in `src/main/resources/application.properties`.
- For local development, you can override via environment variables or create `src/main/resources/application-local.properties` (this file is git‑ignored). Example:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/chatapp
spring.datasource.username=postgres
spring.datasource.password=your_password

# Optional: override JWT expirations
app.security.jwt.access-expiration-ms=900000       # 15 minutes
app.security.jwt.refresh-expiration-ms=1209600000  # 14 days

# SMTP (Gmail example)
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.ssl.enable=true
app.mail.from.name=Your App Name
```

- You can also use environment variables (preferred for secrets):
  - APP_JWT_SECRET (Base64-encoded key)
  - APP_JWT_ACCESS_EXP_MS (e.g., 900000)
  - APP_JWT_REFRESH_EXP_MS (e.g., 1209600000)
  - EMAIL_ADDRESS, EMAIL_PASSWORD, SMTP_HOST, SMTP_PORT, SMTP_SECURE
  - SPRING_PROFILES_ACTIVE (defaults to `local`)

4) Build and run
- Using Maven wrapper (Windows):
  - `mvnw.cmd clean spring-boot:run`
- Using Maven wrapper (macOS/Linux):
  - `./mvnw clean spring-boot:run`

The app starts on `http://localhost:8080` by default.

## API Documentation (Swagger)
- Swagger UI: `http://localhost:8080/swagger-ui.html` (or `http://localhost:8080/swagger-ui/`)
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Click the “Authorize” button in Swagger and paste your `Bearer <access_token>` to call protected endpoints.

## Authentication Flow
1) Register: `POST /api/v1/auth/register`
- Body: `{ firstName, lastName, username, email, password }`
- A 6‑digit verification code is emailed to the user.

2) Verify Email: `POST /api/v1/auth/verify`
- Body: `{ username, code }`
- After verifying, the account can log in and use protected APIs.

3) Login: `POST /api/v1/auth/login`
- Body: `{ username, password }` (email can be used in place of username)
- Returns: `{ accessToken, refreshToken, ... }`

4) Use Protected APIs
- Add header: `Authorization: Bearer <accessToken>`

5) Refresh Tokens: `POST /api/v1/auth/refresh`
- Body: `{ refreshToken }`
- Returns fresh access + refresh tokens.

6) Password Reset
- Request code: `POST /api/v1/auth/forgot-password` with `{ username }`
- Verify code: `POST /api/v1/auth/verify-reset` with `{ username, code }`
- Reset password: `POST /api/v1/auth/reset-password` with `{ username, code, newPassword }`

## Protected Endpoints
All controllers other than `/api/v1/auth/**`, Swagger, static resources, and WebSocket handshake are secured by Spring Security. You must provide a valid Bearer access token.

## Configuration Reference
Key properties (with defaults defined in `application.properties`):
- `api.prefix` = `/api/v1`
- `app.security.jwt.access-expiration-ms` = 900000 (15 min)
- `app.security.jwt.refresh-expiration-ms` = 1209600000 (14 days)
- Mail settings are sourced from env vars if provided.

## File Uploads
- Upload directory: `uploads/` (contents are git‑ignored; folder can be kept with a `.gitkeep` file)

## Error Handling
All errors are returned as JSON with a consistent structure, including validation errors, unauthorized/forbidden responses, not found, and server errors. Swagger and clients should parse them cleanly.

## Troubleshooting
- Database connection errors: verify Postgres is running and credentials are correct.
- Emails not sending: check SMTP credentials and allow “less secure app” or use app passwords for Gmail.
- 401 Unauthorized: ensure the account is verified and the `Authorization` header includes a valid access token.
- 403 Forbidden: token is valid but the action is not permitted for the user.
- Swagger shows parsing error for responses: ensure you’re on the latest code; error handling returns proper `application/json`.

## Building a Package
- `mvnw.cmd clean package` (Windows)
- `./mvnw clean package` (macOS/Linux)

The artifact will be in `target/`.

## License
This project is for demonstration purposes. Add your preferred license here.
