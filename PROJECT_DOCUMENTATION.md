# NotifyHub Backend Documentation

## 1. Project Overview

NotifyHub is a personal task, reminder, and notification backend. From a user's
perspective, it is similar to Microsoft To Do. From an engineering perspective,
its main purpose is to demonstrate a production-style backend that grows from a
simple task system into an asynchronous notification platform.

The project should be developed incrementally. Each phase must work correctly
before features from the next phase are introduced.

### Project Identity

- Project name: NotifyHub
- Repository name: `notifyhub-backend`
- Base package: `com.ahmad.notifyhub`
- Build tool: Maven
- Java version: 21
- Framework: Spring Boot 3.x
- Primary database: PostgreSQL

## 2. Learning and Development Rules

- Build one small feature at a time.
- Understand each class before adding another layer.
- Keep controllers thin and business logic in services.
- Use DTOs at API boundaries; never expose JPA entities directly.
- Use constructor injection.
- Validate incoming requests.
- Every query for private data must enforce ownership.
- PostgreSQL remains the source of truth.
- Add infrastructure such as RabbitMQ, Elasticsearch, and WebSocket only after
  the core application is stable.
- Add focused tests as each important behavior is introduced.
- Never store plain-text passwords or commit secrets to Git.

## 3. Technology Roadmap

### Core Stack

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Spring Security
- Bean Validation
- PostgreSQL
- JWT authentication
- Maven
- Lombok where it improves readability

### Later Additions

- OpenAPI/Swagger for API documentation
- Docker Compose for local infrastructure
- RabbitMQ for asynchronous notification processing
- Elasticsearch for search and analytics
- WebSocket for real-time in-app delivery
- Spring Boot Actuator for health and operational endpoints
- GitHub Actions for continuous integration

These later technologies must solve an existing requirement. They should not be
added simply because they appear in the final architecture.

## 4. Layered Architecture

The code should use a feature-friendly layered architecture under
`com.ahmad.notifyhub`.

```text
com.ahmad.notifyhub
|-- config
|-- controller
|-- dto
|   |-- request
|   `-- response
|-- entity
|-- exception
|-- mapper
|-- repository
|-- security
`-- service
```

As the project becomes larger, packages may be grouped by feature while still
preserving clear layers. Do not restructure early without a concrete need.

### Layer Responsibilities

#### Controller

- Defines HTTP routes and status codes.
- Receives and validates request DTOs.
- Reads the authenticated user's identity.
- Delegates work to a service.
- Returns response DTOs.
- Contains no database queries or business rules.

#### Service

- Implements business rules and use cases.
- Controls transactions.
- Checks ownership and permissions.
- Coordinates repositories and mappers.
- Throws meaningful application exceptions.

#### Repository

- Provides database access through Spring Data JPA.
- Contains ownership-aware queries.
- Does not contain business decisions.

#### Entity

- Represents persisted domain data.
- Defines database relationships and constraints.
- Is not returned directly from controllers.

#### DTO

- Defines the public API contract.
- Separates request and response shapes.
- Holds request validation annotations.
- Prevents accidental exposure of sensitive entity fields.

#### Mapper

- Converts entities to response DTOs.
- Applies request DTO values to new or existing entities.
- Should remain simple and contain no database access.

#### Security

- Configures public and protected endpoints.
- Validates JWTs.
- Creates Spring Security authentication objects.
- Supplies the current authenticated user.

#### Exception

- Defines domain-specific errors.
- Converts exceptions into consistent API error responses.

## 5. Core Domain Model

All user-owned records must be isolated by authenticated user. Knowing another
record's ID must never be enough to access it.

### User

Purpose: represents an account that owns lists, tasks, reminders, and
notifications.

Recommended fields:

| Field | Type | Rules |
|---|---|---|
| `id` | `Long` | Primary key |
| `email` | `String` | Required, unique, normalized |
| `passwordHash` | `String` | Required, never returned by the API |
| `createdAt` | `LocalDateTime` | Required |
| `updatedAt` | `LocalDateTime` | Optional early, useful later |

Important rules:

- Store only a strong password hash, using BCrypt initially.
- Normalize email consistently before checking or saving it.
- Do not place list or task collections on `User` unless navigation from the
  entity is genuinely needed.

### TaskList

Purpose: groups a user's tasks.

Recommended fields:

| Field | Type | Rules |
|---|---|---|
| `id` | `Long` | Primary key |
| `name` | `String` | Required, length-limited |
| `user` | `User` | Required owner |
| `createdAt` | `LocalDateTime` | Required |
| `updatedAt` | `LocalDateTime` | Required |

Possible later fields:

- Display color
- Sort order
- Archived flag

### Task

Purpose: represents one actionable item.

Recommended fields:

| Field | Type | Rules |
|---|---|---|
| `id` | `Long` | Primary key |
| `title` | `String` | Required, length-limited |
| `notes` | `String` or text column | Optional |
| `priority` | `TaskPriority` | Required or defaulted |
| `status` | `TaskStatus` | Required |
| `dueDate` | `LocalDateTime` | Optional |
| `completedAt` | `LocalDateTime` | Null unless completed |
| `user` | `User` | Required owner |
| `taskList` | `TaskList` | Optional |
| `createdAt` | `LocalDateTime` | Required |
| `updatedAt` | `LocalDateTime` | Required |

Recommended enums:

```java
public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}
```

```java
public enum TaskStatus {
    PENDING,
    COMPLETED
}
```

Business rules:

- A task always belongs to one user.
- A task list is optional.
- If a list is assigned, the list and task must have the same owner.
- Completing a task sets its status and `completedAt`.
- Reopening a task resets its status and clears `completedAt`.
- A task is overdue when it is incomplete and its due date is before the
  current time.

### Later Entities

These entities should not be implemented during the early Phase 1 work:

- `SubTask`
- `Tag`
- `Reminder`
- `Notification`
- `NotificationAttempt`
- `AuditLog`

Their intended roles are documented in the later phases below.

## 6. Database Relationship Plan

```text
User 1 -------- * TaskList
User 1 -------- * Task
TaskList 1 ---- * Task
TaskList is optional on Task
```

Ownership is intentionally stored directly on `Task`, even when a task belongs
to a list. This allows unlisted tasks and straightforward ownership checks.

Recommended foreign keys:

- `task_lists.user_id -> users.id`
- `tasks.user_id -> users.id`
- `tasks.task_list_id -> task_lists.id`, nullable

Recommended indexes:

- Unique index on normalized user email
- Index on `task_lists.user_id`
- Index on `tasks.user_id`
- Index on `tasks.task_list_id`
- Index supporting overdue queries, such as user, status, and due date

Avoid eager-loading large collections. Prefer `LAZY` relationships and fetch
only what a use case requires.

## 7. API Conventions

### Base Path

```text
/api
```

### JSON Naming

Use consistent camelCase JSON fields:

```json
{
  "title": "Finish NotifyHub entity",
  "dueDate": "2026-06-15T18:00:00",
  "priority": "HIGH"
}
```

### Time Handling

- Use ISO-8601 request and response values.
- Choose and document a timezone policy before reminders are implemented.
- A production-friendly option is storing instants in UTC and converting at
  the client boundary.
- Do not silently mix server-local time with user-local reminder time.

### Standard Error Shape

Use a consistent response such as:

```json
{
  "timestamp": "2026-06-13T12:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/tasks",
  "fieldErrors": {
    "title": "Title is required"
  }
}
```

Do not return stack traces or internal exception details to clients.

### Common Status Codes

- `200 OK`: successful read or update
- `201 Created`: resource created
- `204 No Content`: successful deletion
- `400 Bad Request`: validation or malformed input
- `401 Unauthorized`: missing or invalid authentication
- `403 Forbidden`: authenticated but not permitted
- `404 Not Found`: resource absent or not accessible to this user
- `409 Conflict`: duplicate email or conflicting state

For private resources, returning `404` for another user's record can avoid
revealing that the record exists.

## 8. Phase 1: Core Task System

### Phase Goal

Deliver a secure REST API where users can register, log in, and manage only
their own lists and tasks.

### Authentication Endpoints

#### `POST /api/auth/register`

Request:

```json
{
  "email": "user@example.com",
  "password": "strong-password"
}
```

Responsibilities:

- Validate email and password.
- Normalize the email.
- Reject duplicate accounts.
- Hash the password.
- Save the user.
- Return a safe response without `passwordHash`.

#### `POST /api/auth/login`

Request:

```json
{
  "email": "user@example.com",
  "password": "strong-password"
}
```

Responsibilities:

- Authenticate credentials.
- Generate a signed JWT.
- Return the token and necessary expiry information.

### Task List Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/lists` | Get the current user's lists |
| `POST` | `/api/lists` | Create a list |
| `PUT` | `/api/lists/{id}` | Rename or update an owned list |
| `DELETE` | `/api/lists/{id}` | Delete an owned list |

Before implementing list deletion, choose and test a clear task policy:

- Move tasks to no list, or
- Delete the tasks with the list.

Moving tasks to no list is generally safer for user data.

### Task Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/tasks` | Get the current user's tasks |
| `POST` | `/api/tasks` | Create a task |
| `GET` | `/api/tasks/{id}` | Get one owned task |
| `PUT` | `/api/tasks/{id}` | Update one owned task |
| `DELETE` | `/api/tasks/{id}` | Delete one owned task |
| `PATCH` | `/api/tasks/{id}/complete` | Complete an owned task |
| `PATCH` | `/api/tasks/{id}/reopen` | Reopen an owned task |
| `GET` | `/api/tasks/overdue` | Get incomplete overdue tasks |

Possible query parameters can be introduced after the basic endpoints work:

- `listId`
- `status`
- `priority`
- `dueBefore`
- `dueAfter`
- Pagination and sorting

### Phase 1 Security Rules

Public:

- `POST /api/auth/register`
- `POST /api/auth/login`

Protected:

- Every other API endpoint

Required properties:

- Stateless security session policy
- CSRF handling appropriate for a token-based REST API
- Password hashing through `PasswordEncoder`
- JWT signature and expiry validation
- Authentication recreated for each request
- Ownership enforced in repository/service operations

Never trust a user ID supplied in an API request. The owner must come from the
authenticated principal.

### Recommended Phase 1 Implementation Order

Complete these in order and verify each step before continuing:

1. Maven setup and root package.
2. Basic `User` entity.
3. PostgreSQL database and application configuration.
4. Verify application startup and JPA schema behavior.
5. `UserRepository`.
6. Registration request and response DTOs.
7. Password encoder configuration.
8. Registration service.
9. Registration controller and exception handling.
10. Login DTOs and Spring Security authentication.
11. JWT service, filter, and security configuration.
12. Test public and protected endpoint behavior.
13. `TaskList` entity and repository.
14. Task list DTOs, mapper, service, and controller.
15. `Task` enums and entity.
16. Task repository with ownership-aware queries.
17. Task DTOs and mapper.
18. Task service and CRUD controller.
19. Complete, reopen, and overdue operations.
20. Focused Phase 1 tests and cleanup.

Do not implement this entire list at once. Each item should be divided into
small mentoring steps.

### Phase 1 Definition of Done

- A new user can register.
- Duplicate email registration is rejected cleanly.
- A registered user can log in and receive a valid JWT.
- Protected endpoints reject missing or invalid tokens.
- A user can create and manage lists.
- A user can create and manage tasks.
- A user cannot access another user's data.
- Complete and reopen operations preserve valid task state.
- Overdue queries return only the authenticated user's incomplete tasks.
- API entities are not exposed directly.
- Validation and error responses are consistent.
- Important service and security behaviors have tests.

## 9. Phase 2: Better Task Management

### Subtasks

Introduce a `SubTask` entity:

- Belongs to one parent task.
- Has a title and completion state.
- Must be accessible only through the parent task's owner.

Decide whether completing all subtasks should automatically complete the parent.
Do not add automatic behavior without an explicit rule.

### Tags

Introduce a `Tag` entity:

- Owned by a user.
- Has a unique name per user.
- Has a many-to-many relationship with tasks.

Avoid returning bidirectional entity graphs. DTOs should control the response.

### My Day

Possible design:

- A nullable `myDayDate` on `Task`, or
- A separate daily selection record.

A simple date field is acceptable initially. Define what happens when the day
changes and which timezone determines "today."

### Phase 2 Definition of Done

- Users can manage subtasks on owned tasks.
- Users can create and assign their own tags.
- Users cannot assign another user's tags.
- My Day behaves consistently according to the chosen timezone policy.

## 10. Phase 3: Reminder Engine

### Reminder Entity

Recommended fields:

| Field | Purpose |
|---|---|
| `id` | Primary key |
| `task` | Task being reminded |
| `user` | Owner, useful for secure queries |
| `remindAt` | Next scheduled reminder time |
| `repeatType` | None, daily, weekly, or selected policy |
| `status` | Scheduled, processed, cancelled, failed |
| `lastTriggeredAt` | Previous trigger time |
| `createdAt` | Audit timestamp |
| `updatedAt` | Audit timestamp |

### Scheduler Behavior

Start with a simple scheduled job:

1. Query reminders that are due and still scheduled.
2. Claim or lock work safely to prevent duplicate processing.
3. Create notification records.
4. Calculate the next occurrence for repeating reminders.
5. Mark one-time reminders as processed.

Important design concerns:

- Application restarts
- Duplicate processing
- Multiple application instances
- Transaction boundaries
- Timezones and daylight-saving changes
- Idempotency

The first scheduler may be simple, but duplicate prevention must be considered
before production-style scaling.

## 11. Phase 4: Notification System

### Notification Entity

Represents a user-visible notification, independent of delivery channel.

Recommended fields:

- `id`
- `user`
- `task`, optional
- `type`
- `title`
- `message`
- `readAt`, nullable
- `createdAt`

Suggested endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/notifications` | Get owned notifications |
| `GET` | `/api/notifications/unread` | Get unread notifications |
| `PATCH` | `/api/notifications/{id}/read` | Mark one as read |
| `PATCH` | `/api/notifications/read-all` | Mark all as read |

### Email Notifications

Email should be a delivery channel for an existing notification event. Core
task logic should not send email directly.

### Notification History

Introduce `NotificationAttempt` when delivery tracking is needed:

- Notification reference
- Channel
- Attempt number
- Status
- Provider response or sanitized failure reason
- Attempt timestamp
- Next retry time

This creates an audit trail and supports retries later.

## 12. Phase 5: Advanced Backend

### RabbitMQ

Use RabbitMQ after synchronous notification creation works.

Intended flow:

```text
Reminder scheduler
    -> publishes notification event
    -> RabbitMQ
    -> notification consumer
    -> creates/sends notification
```

Benefits:

- Separates reminder detection from delivery.
- Allows independent retry behavior.
- Smooths traffic spikes.
- Supports multiple consumers and channels.

Requirements before adoption:

- Idempotent consumers
- Durable queues where appropriate
- Retry limits
- Dead-letter exchange and queue
- Correlation or event IDs
- Logging and monitoring

### Retry and Dead-Letter Processing

Failed delivery should not retry forever.

Recommended lifecycle:

1. Attempt delivery.
2. Record the outcome.
3. Retry transient failures with backoff.
4. Stop after a configured maximum.
5. Route exhausted messages to a dead-letter queue.
6. Provide visibility for investigation or manual replay.

### Elasticsearch

PostgreSQL remains the source of truth. Elasticsearch is a derived search/read
model.

Possible uses:

- Full-text task search
- Filtering by tags, status, and date
- Notification analytics
- Search suggestions

Data must be re-indexable from PostgreSQL. The API must tolerate temporary
search index lag or outages according to documented behavior.

### WebSocket

Use WebSocket to deliver real-time in-app notifications after notification
records and authentication work correctly.

Intended flow:

```text
Notification created
    -> delivery event
    -> authenticated WebSocket destination
    -> user's connected client
```

Reasons to add it late:

- It is a delivery mechanism, not the source of notification truth.
- JWT authentication during connection setup adds complexity.
- Reconnection and missed-message behavior must be defined.
- Users may have multiple connected devices.
- Horizontal scaling may require broker-backed message distribution.

The database notification list remains necessary because WebSocket messages can
be missed while a client is disconnected.

### Docker Compose

Add Compose when coordinating infrastructure becomes useful. Likely services:

- PostgreSQL
- RabbitMQ
- Elasticsearch
- The NotifyHub application, later

Use environment variables and local development defaults. Do not commit real
credentials.

### OpenAPI/Swagger

Add API documentation after the API patterns are stable enough to document.
Include:

- Endpoint descriptions
- Request and response schemas
- Validation constraints
- Authentication scheme
- Common error responses

### Actuator

Expose only required operational endpoints. Health and readiness information
must not reveal secrets or unnecessary infrastructure details.

### GitHub Actions

Recommended initial pipeline:

1. Check out the repository.
2. Set up Java 21.
3. Cache Maven dependencies.
4. Run compilation and tests.
5. Package the application.

Later, add integration-test infrastructure and container image builds.

## 13. Configuration Strategy

Use configuration profiles instead of hard-coding environments:

```text
application.properties
application-dev.properties
application-test.properties
application-prod.properties
```

Possible environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
MAIL_HOST
MAIL_USERNAME
MAIL_PASSWORD
```

Rules:

- Never commit production secrets.
- Use a sufficiently strong JWT signing secret.
- Keep test configuration isolated.
- Avoid `ddl-auto=create` in production.
- Introduce a database migration tool such as Flyway before schema changes must
  be safely tracked across environments.

## 14. Validation Guidelines

Validation belongs mainly on request DTOs.

Examples:

- Email: required and syntactically valid.
- Password: required with a documented minimum length.
- List name: required and length-limited.
- Task title: required and length-limited.
- Notes: optional but length-limited.
- Enum inputs: restricted to supported values.
- IDs: positive where supplied.

Database constraints are still required. DTO validation improves client
feedback, while database constraints protect persisted integrity.

## 15. Ownership and Security Checklist

For every user-owned endpoint:

- Obtain the owner from the authenticated principal.
- Query by both resource ID and owner ID where possible.
- Never call `findById(id)` and then forget the ownership check.
- Verify related records have the same owner.
- Do not accept `userId` as authority from a request body.
- Do not expose `passwordHash`.
- Do not log passwords, JWTs, secrets, or sensitive provider responses.

Example repository method:

```java
Optional<Task> findByIdAndUserId(Long id, Long userId);
```

The precise method may change depending on the authenticated principal design,
but the ownership requirement must remain.

## 16. Transaction Guidelines

- Mark multi-step writes as transactional in the service layer.
- Use read-only transactions for complex read operations when useful.
- Avoid opening transactions in controllers.
- Do not make slow external network calls while holding a database transaction
  unless the consistency tradeoff is intentional.
- For later messaging, consider an outbox pattern if database changes and event
  publication require strong reliability.

## 17. Testing Strategy

Testing is introduced progressively, not postponed until the entire application
is complete.

### Unit Tests

Focus on service business rules:

- Duplicate registration
- Password encoding invocation
- Ownership rejection
- List ownership when assigning a task
- Complete and reopen transitions
- Overdue calculation
- Repeat reminder calculation

### Repository Tests

Verify:

- Ownership-aware queries
- Overdue filtering
- Unique constraints
- Relationship behavior

Use a real PostgreSQL-compatible integration environment when query behavior
matters. Testcontainers can be introduced later for reliable integration tests.

### Controller/Security Tests

Verify:

- Request validation
- Status codes
- Error response shape
- Public endpoint access
- Protected endpoint rejection
- Valid JWT access
- Cross-user data isolation

### End-to-End Tests

Later, test the complete flow:

1. Register.
2. Log in.
3. Create a list.
4. Create and complete a task.
5. Schedule a reminder.
6. Observe a notification.

## 18. Logging and Observability

Use structured, useful logs:

- Application startup
- Authentication failures without sensitive details
- Reminder jobs and result counts
- Notification event IDs
- Delivery attempts and sanitized failures
- Dead-letter processing

Avoid:

- Passwords
- JWT token values
- Database passwords
- Full private message bodies unless necessary
- Repetitive logs inside large loops

Later, add metrics for:

- Due reminders processed
- Notifications created
- Delivery success and failure
- Retry counts
- Dead-letter queue depth
- Processing latency

## 19. Git Workflow

Recommended habits:

- Keep commits small and focused.
- Use descriptive commit messages.
- Do not commit IDE-specific temporary state.
- Do not commit secrets or generated build output.
- Run relevant tests before committing.

Example commit progression:

```text
chore: initialize Spring Boot project
feat: add user entity
feat: add user registration
feat: add JWT authentication
feat: add task list creation
feat: add task CRUD operations
```

## 20. Current Project Status

As of June 13, 2026, the repository contains:

- Spring Boot 3.5.7 Maven configuration
- Java 21 configuration
- Base package `com.ahmad.notifyhub`
- Correctly named `NotifyHubApplication`
- Initial `User` entity
- PostgreSQL, JPA, Security, Validation, Web, Lombok, and test dependencies

The current learning position is:

```text
Phase 1
Step 3: Initial User entity
```

Before moving forward, the current `User` entity should be reviewed and
compiled. The next intended small step after that review is PostgreSQL
configuration, not task APIs or advanced notification infrastructure.

## 21. Master Completion Checklist

### Phase 1

- [x] Initialize Spring Boot project.
- [x] Set Java 21 and base package.
- [ ] Review and complete initial `User` entity step.
- [ ] Configure PostgreSQL.
- [ ] Build registration.
- [ ] Build login and JWT security.
- [ ] Build task lists.
- [ ] Build tasks.
- [ ] Add complete, reopen, and overdue behavior.
- [ ] Verify ownership and Phase 1 tests.

### Phase 2

- [ ] Add subtasks.
- [ ] Add tags.
- [ ] Add My Day.

### Phase 3

- [ ] Add reminders.
- [ ] Add repeat schedules.
- [ ] Add reliable scheduled processing.

### Phase 4

- [ ] Add in-app notifications.
- [ ] Add email delivery.
- [ ] Add notification history.
- [ ] Add read/unread behavior.

### Phase 5

- [ ] Add RabbitMQ processing.
- [ ] Add retries and dead-letter handling.
- [ ] Add Elasticsearch search and analytics.
- [ ] Add WebSocket real-time delivery.
- [ ] Add Docker Compose.
- [ ] Add OpenAPI documentation.
- [ ] Add Actuator.
- [ ] Expand automated tests.
- [ ] Add GitHub Actions.

## 22. Immediate Next Action

Continue in small steps. Do not implement directly from the entire roadmap.

The immediate action is to review the manually written `User` entity and run:

```powershell
.\mvnw.cmd compile
```

After it succeeds, continue with one focused PostgreSQL configuration step.
