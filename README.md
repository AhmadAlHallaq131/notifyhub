# NotifyHub

NotifyHub is a personal task app with a React frontend and Spring Boot backend for reminders, in-app notifications, email delivery, and real-time updates.

## Features

- JWT-based registration and login
- User-owned task lists, tasks, subtasks, tags, and My Day
- Task completion, reopening, overdue tasks, normal text search, and paged filtering
- One-time and repeating reminders
- Persistent notifications with read and unread state
- RabbitMQ email delivery with retry and dead-letter queues
- STOMP/WebSocket updates for live in-app notifications
- OpenAPI documentation and actuator health checks
- A responsive React interface with a focused, Microsoft To Do-inspired task workflow

## Architecture

```text
React client
    |
    v
Controllers -> Services -> Repositories -> PostgreSQL
                   |
                   +-> Reminder scheduler -> RabbitMQ -> Email consumer -> SMTP provider
                   |
                   +-> Notification event -> WebSocket /user/queue/notifications
```

## Technology

- React, TypeScript, Vite, Axios, and STOMP.js
- Java 21 and Spring Boot 3.5
- Spring Security and JWT
- Spring Data JPA with PostgreSQL
- RabbitMQ, Spring Mail, and WebSocket/STOMP
- Maven, Docker Compose, Springdoc OpenAPI, and Actuator

## Run Locally

### Prerequisites

- Java 21
- Docker Desktop, for the Compose stack

### With Docker Compose

1. Copy `.env.example` to `.env`.
2. Set a real Base64 JWT secret and any SMTP credentials you want to use.
3. Run:

```powershell
docker compose up --build
```

The frontend is available at `http://localhost:5173` and the API at `http://localhost:8080`. PostgreSQL runs on port `5432`, RabbitMQ on `5672`, and the RabbitMQ management UI on `http://localhost:15672`.

Stop the stack with:

```powershell
docker compose down
```

Add `-v` only when you intentionally want to remove the local PostgreSQL data volume.

### From IntelliJ or Maven

Set the required values in your ignored `.env` file, start PostgreSQL and RabbitMQ, then run:

```powershell
.\mvnw.cmd spring-boot:run
```

### Frontend Development

With the API running on `http://localhost:8080`, start the React development server in a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies `/api` and `/ws` to the backend, so the frontend uses the same authenticated REST and WebSocket routes in development and Docker.

## Configuration

`.env.example` lists every environment variable used by the application. Never commit `.env` or provider credentials.

For a secure JWT secret, generate at least 32 random bytes and Base64 encode them. For example:

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

`JPA_DDL_AUTO=update` is appropriate for this local learning stack. Use database migrations before a production deployment.
SMTP health is disabled by default because sending mail is optional during local development. Set `MANAGEMENT_HEALTH_MAIL_ENABLED=true` when the configured SMTP server should be required for the overall health check.

## API Documentation and Operations

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

Most endpoints require a JWT. In Swagger UI, use **Authorize** and paste `Bearer <access-token>`.

### Paged Task Query

`GET /api/tasks/page` supports these optional query parameters:

| Parameter | Default | Allowed values |
|---|---:|---|
| `page` | `0` | zero or greater |
| `size` | `20` | `1` to `100` |
| `sortBy` | `createdAt` | `createdAt`, `updatedAt`, `title`, `priority`, `status`, `dueDate` |
| `direction` | `DESC` | `ASC`, `DESC` |
| `status` | | `PENDING`, `COMPLETED` |
| `priority` | | `LOW`, `MEDIUM`, `HIGH` |
| `taskListId` | | an owned task-list ID |
| `q` | | title or notes text |

Example:

```text
GET /api/tasks/page?status=PENDING&priority=HIGH&sortBy=dueDate&direction=ASC&page=0&size=20
```

## Test and Build

Run the focused unit test suite:

```powershell
.\mvnw.cmd test
```

Run the same verification used in CI:

```powershell
.\mvnw.cmd verify
```

Build the frontend:

```powershell
cd frontend
npm run build
```

GitHub Actions runs this verification for every push and pull request.

## Important Notes

- All user-owned resources are queried through the authenticated email, so another user's resources are not exposed.
- RabbitMQ handles delivery retries and routes exhausted messages to a dead-letter queue.
- SMTP is an external dependency. A successful message through RabbitMQ confirms queue handling; the SMTP provider must also accept the configured credentials and connection.
- WebSocket delivery is real-time only. The notification REST endpoints remain the source of truth for missed messages.
