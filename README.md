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

## Run with Docker
Below are step‑by‑step instructions to build and run the backend using the provided Dockerfile.

## Deploying on Railway – Environment Variables to Set
Set these in your Railway service Variables. Secrets should be added as “Encrypted” variables.

Required (choose ONE database option):
- Option A – Single URL (easiest):
  - DATABASE_URL = postgresql://postgres:YOUR_PASSWORD@crossover.proxy.rlwy.net:57390/railway
- Option B – Explicit JDBC trio:
  - SPRING_DATASOURCE_URL = jdbc:postgresql://crossover.proxy.rlwy.net:57390/railway
  - SPRING_DATASOURCE_USERNAME = postgres
  - SPRING_DATASOURCE_PASSWORD = YOUR_PASSWORD

Required (security):
- APP_JWT_SECRET = base64_encoded_secret   (e.g., generate 64+ random bytes and Base64 encode)
- SPRING_PROFILES_ACTIVE = prod            (ensures server runs on port 8080)

Email (for verification/reset codes):
- EMAIL_ADDRESS = your_email@gmail.com
- EMAIL_PASSWORD = your_app_password       (App Password if using Gmail)
- SMTP_HOST = smtp.gmail.com               (optional; defaults to smtp.gmail.com)
- SMTP_PORT = 465                          (optional; defaults to 465)
- SMTP_SECURE = true                       (optional; true enables SSL)
- EMAIL_FROM_NAME = Your App Name          (optional; shown as sender name)

Optional overrides:
- APP_JWT_ACCESS_EXP_MS = 900000           (15 minutes)
- APP_JWT_REFRESH_EXP_MS = 1209600000      (14 days)
- APP_BRAND_LOGO_URL, APP_BRAND_SITE_URL, APP_BRAND_SUPPORT_EMAIL, APP_BRAND_ADDRESS,
  APP_BRAND_INSTAGRAM, APP_BRAND_LINKEDIN, APP_BRAND_COLOR_BG, APP_BRAND_COLOR_TEXT,
  APP_BRAND_COLOR_PRIMARY, APP_BRAND_COLOR_ACCENT

Notes for Railway:
- This app listens on port 8080 under the `prod` profile. The Dockerfile exposes 8080, which matches Railway’s default expectations for Docker deployments.
- You don’t need to set PORT manually when deploying with this Dockerfile; Spring Boot uses server.port=8080 in application-prod.properties.
- Use either DATABASE_URL or the SPRING_DATASOURCE_* trio, not both.
- If you enable a custom domain or HTTPS proxy, no extra config is needed for the backend.

1) Build the image
- Windows PowerShell:
  - `docker build -t chatapp-backend:latest .`
- macOS/Linux:
  - `docker build -t chatapp-backend:latest .`

2) Use a remote PostgreSQL (Railway example)
Your Railway URL:
```
postgresql://postgres:QZQwpGZVQlXWEMHeHjHDoRTlsjcMoQYU@crossover.proxy.rlwy.net:57390/railway
```
You have two secure options (no secrets in code):
- Option A: Set SPRING_DATASOURCE_* env vars with JDBC URL form
  - JDBC URL: `jdbc:postgresql://crossover.proxy.rlwy.net:57390/railway`
  - Username: `postgres`
  - Password: `QZQwpGZVQlXWEMHeHjHDoRTlsjcMoQYU`
- Option B: Set a single DATABASE_URL env var with the original URL
  - The app now auto‑parses `DATABASE_URL` at startup.

3) Run the container (examples)
- Windows PowerShell (Option A – explicit JDBC):
```
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://crossover.proxy.rlwy.net:57390/railway"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "QZQwpGZVQlXWEMHeHjHDoRTlsjcMoQYU"
$env:APP_JWT_SECRET = "change_me_base64"   # base64(HMAC key), e.g. from https://www.base64encode.org/
$env:SPRING_PROFILES_ACTIVE = "prod"

docker run --name chatapp-backend -p 8080:8080 ^
  -e SPRING_DATASOURCE_URL=$env:SPRING_DATASOURCE_URL ^
  -e SPRING_DATASOURCE_USERNAME=$env:SPRING_DATASOURCE_USERNAME ^
  -e SPRING_DATASOURCE_PASSWORD=$env:SPRING_DATASOURCE_PASSWORD ^
  -e APP_JWT_SECRET=$env:APP_JWT_SECRET ^
  -e SPRING_PROFILES_ACTIVE=$env:SPRING_PROFILES_ACTIVE ^
  -v ${PWD}\uploads:/app/uploads ^
  chatapp-backend:latest
```
- Windows PowerShell (Option B – single var):
```
$env:DATABASE_URL = "postgresql://postgres:QZQwpGZVQlXWEMHeHjHDoRTlsjcMoQYU@crossover.proxy.rlwy.net:57390/railway"
$env:APP_JWT_SECRET = "change_me_base64"
$env:SPRING_PROFILES_ACTIVE = "prod"

docker run --name chatapp-backend -p 8080:8080 ^
  -e DATABASE_URL=$env:DATABASE_URL ^
  -e APP_JWT_SECRET=$env:APP_JWT_SECRET ^
  -e SPRING_PROFILES_ACTIVE=$env:SPRING_PROFILES_ACTIVE ^
  -v ${PWD}\uploads:/app/uploads ^
  chatapp-backend:latest
```
- macOS/Linux bash (Option A – explicit JDBC):
```
export SPRING_DATASOURCE_URL="jdbc:postgresql://crossover.proxy.rlwy.net:57390/railway"
export SPRING_DATASOURCE_USERNAME="postgres"
yourPassword='QZQwpGZVQlXWEMHeHjHDoRTlsjcMoQYU'
export SPRING_DATASOURCE_PASSWORD="$yourPassword"
export APP_JWT_SECRET="change_me_base64"
export SPRING_PROFILES_ACTIVE="prod"

docker run --name chatapp-backend -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
  -e SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
  -e SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
  -e APP_JWT_SECRET="$APP_JWT_SECRET" \
  -e SPRING_PROFILES_ACTIVE="$SPRING_PROFILES_ACTIVE" \
  -v "$(pwd)/uploads:/app/uploads" \
  chatapp-backend:latest
```
- macOS/Linux bash (Option B – single var):
```
export DATABASE_URL="postgresql://postgres:QZQwpGZVQlXWEMHeHjHDoRTlsjcMoQYU@crossover.proxy.rlwy.net:57390/railway"
export APP_JWT_SECRET="change_me_base64"
export SPRING_PROFILES_ACTIVE="prod"

docker run --name chatapp-backend -p 8080:8080 \
  -e DATABASE_URL="$DATABASE_URL" \
  -e APP_JWT_SECRET="$APP_JWT_SECRET" \
  -e SPRING_PROFILES_ACTIVE="$SPRING_PROFILES_ACTIVE" \
  -v "$(pwd)/uploads:/app/uploads" \
  chatapp-backend:latest
```
Notes:
- The app listens on port 8080 in the container (Dockerfile EXPOSE 8080). The `-p 8080:8080` publishes it on your host at http://localhost:8080.
- Mounting `uploads/` keeps files (like avatars) on your host machine. Create the folder if it doesn’t exist.
- You can pass JVM options via `-e JAVA_OPTS="-XX:MaxRAMPercentage=75 -Xms256m -Xmx512m"`.

4) Access the API and Swagger
- Swagger UI: http://localhost:8080/swagger-ui.html (or http://localhost:8080/swagger-ui/)
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health (if enabled): http://localhost:8080/actuator/health

5) Quick test flow
- Register: POST http://localhost:8080/api/v1/auth/register
- Verify: POST http://localhost:8080/api/v1/auth/verify
- Login: POST http://localhost:8080/api/v1/auth/login
- Click “Authorize” in Swagger and paste `Bearer <accessToken>` to call protected endpoints.

6) Container lifecycle & troubleshooting
- View logs: `docker logs -f chatapp-backend`
- Stop: `docker stop chatapp-backend`
- Start again: `docker start chatapp-backend`
- Remove: `docker rm -f chatapp-backend`
- Database connection errors: ensure the container can reach your DB host and credentials are correct.
- If running Postgres in a container, put both containers on the same network and use the Postgres container name in URL (e.g., `jdbc:postgresql://postgres:5432/chatapp`).

## License
This project is for demonstration purposes. Add your preferred license here.
