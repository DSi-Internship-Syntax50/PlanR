<div align="center">

# PlanR

**Academic Resource, Scheduling & Facility Management Platform**

[![Live Demo](https://img.shields.io/badge/Live%20Demo-planr--8co3.onrender.com-4f46e5?style=for-the-badge&logo=render&logoColor=white)](https://planr-8co3.onrender.com/login)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

A project built to manage the complexity of university operations.

</div>

---

## The Problem It Solves

University scheduling is a mess. Rooms get double-booked, faculty get assigned to overlapping slots, exam seat plans take hours to produce manually, and event bookings happen over email threads. PlanR consolidates all of this into a single, role-aware platform — with conflict detection, automated routine generation, AI-assisted queries, and PDF export baked in.

---

## Architecture

PlanR follows a classic Spring MVC layered architecture. The security filter chain is the first line of defense; no request reaches a controller without passing through Spring Security's authentication and authorization checks. Rate limiting sits at the API boundary to prevent abuse.

```
┌────────────────────────────────────────────────────────────┐
│                    Web Browser / Client                    │
└──────────────────────────┬─────────────────────────────────┘
                           │ HTTP
┌──────────────────────────▼─────────────────────────────────┐
│            Spring Security Filter Chain                    │
│         (Session-based Auth + Role Enforcement)            │
└──────────────────────────┬─────────────────────────────────┘
                           │ Authenticated Request
┌──────────────────────────▼─────────────────────────────────┐
│              Bucket4j Rate Limiter (API layer)              │
└──────────────────────────┬─────────────────────────────────┘
                           │
           ┌───────────────┼────────────────────┐
           │               │                    │
  ┌────────▼────────┐  ┌───▼────────────┐  ┌───▼───────────────┐
  │ REST Controllers│  │ MVC Controllers│  │  AI Controller    │
  │ (JSON APIs)     │  │ (Thymeleaf UI) │  │ (AiService)       │
  └────────┬────────┘  └───┬────────────┘  └───┬───────────────┘
           └───────────────┼────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────────┐
│                    Service Layer                            │
│  ScheduleService  │  RoomService  │  SeatPlanPdfService    │
│  EventBookingService  │  RecommendationService  │  etc.    │
└──────────────────────────┬─────────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────────┐
│           Spring Data JPA Repositories                     │
└──────────────────────────┬─────────────────────────────────┘
                           │ Hibernate / JPA
┌──────────────────────────▼─────────────────────────────────┐
│                  PostgreSQL Database                       │
└────────────────────────────────────────────────────────────┘
```

---

## Domain Model

```
┌───────────┐        ┌──────────────┐       ┌────────────────┐
│   User    │        │  Department  │       │     Course     │
│-----------│        │--------------│       │----------------│
│ id        │        │ id           │       │ id             │
│ username  │  1     │ name         │  1    │ courseCode     │
│ email     ├───────►│ shortCode    ├──────►│ title          │
│ role      │  *     │              │  *    │ credits        │
│ password  │        └──────────────┘       │ year/semester  │
└─────┬─────┘                               │ batch          │
      │                                     └───────┬────────┘
      │                                             │ 1
      │ 1                                           │
      ▼ *                                           ▼ *
┌─────────────┐      ┌──────────────┐       ┌────────────────┐
│EventBooking │      │     Room     │       │ MasterRoutine  │
│-------------│      │--------------│       │----------------│
│ id          │  *   │ id           │  1    │ id             │
│ eventName   ├─────►│ roomNumber   ├──────►│ dayOfWeek      │
│ date        │      │ capacity     │       │ startTime      │
│ status      │      │ type         │       │ endTime        │
│ bookedBy    │      │ block/floor  │       │ slotIndex      │
└─────────────┘      └──────────────┘       └────────────────┘
```

---

## Tech Stack

| Layer | Technology | Purpose |
|---|---|---|
| Language | Java 25 | Core application runtime |
| Framework | Spring Boot 4 | Application scaffolding and auto-configuration |
| Web | Spring WebMVC + Thymeleaf | Server-side rendered UI with Spring-integrated templates |
| Security | Spring Security + Spring Session JDBC | Session-based authentication, role-based access control, persistent sessions via PostgreSQL |
| Persistence | Spring Data JPA + Hibernate | ORM with type-safe repositories |
| Database | PostgreSQL | Relational data store for all application state |
| Styling | Tailwind CSS | Utility-first styling for Thymeleaf templates |
| Rate Limiting | Bucket4j 8.3.0 | Token-bucket rate limiting on REST endpoints |
| PDF Generation | OpenPDF 1.3.32 | Programmatic seat plan and report export |
| Containerization | Docker + Docker Compose | Reproducible deployment environment |

---

## User Roles

The platform enforces four distinct roles, each with scoped access:

| Role | Scope |
|---|---|
| `SUPERADMIN` | Full platform access — manages users, rooms, courses, approvals, and all admin routes |
| `COORDINATOR` | Can approve bookings, manage room allocations, and reschedule routines |
| `TEACHER` | Views routines and faculty-specific data |
| `STUDENT` | Read-only access to routines, schedules, and seat plans |

---

## API Reference

All REST endpoints are secured. Requests must carry a valid session cookie. Rate-limited endpoints will return `429 Too Many Requests` when the token bucket is exhausted.

### Schedule

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/schedule/auto-generate` | SUPERADMIN, COORDINATOR | Triggers automated routine generation for a given department and batch |
| `GET` | `/api/schedule/routine` | All | Fetches the full routine grid for a specific department and batch |
| `GET` | `/api/schedule/courses` | All | Returns courses available for scheduling in a batch |
| `POST` | `/api/schedule/allocate-class` | All | Manually places a course into a specific day/time slot with optional room |
| `DELETE` | `/api/schedule/routine/{id}` | All | Removes a scheduled slot and frees the room |
| `POST` | `/api/schedule/unassign/{routineId}` | SUPERADMIN, COORDINATOR | Detaches the room from a routine without deleting the slot |

### Schedule Management

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/schedule/management/suggest-rooms` | All | Returns AI-ranked room recommendations for a given course, day, and time slot |
| `GET` | `/api/schedule/management/routines/room/{roomId}` | All | Lists all routines occupying a specific room |
| `GET` | `/api/schedule/management/routines/unassigned` | All | Fetches all scheduled slots that have no room assigned |
| `POST` | `/api/schedule/management/allocate` | SUPERADMIN, COORDINATOR | Directly assigns a room to a routine slot |
| `POST` | `/api/schedule/management/reschedule` | SUPERADMIN, COORDINATOR | Moves a routine to a different day, time slot, or room |
| `POST` | `/api/schedule/management/requests` | All | Submits a change request (room swap, reschedule) for coordinator approval |
| `POST` | `/api/schedule/management/requests/{requestId}/approve` | SUPERADMIN, COORDINATOR | Approves a pending schedule change request |

### Rooms

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/v1/rooms` | All | Returns all rooms in the system with full details |
| `POST` | `/api/rooms` | SUPERADMIN, COORDINATOR | Creates a new room record |
| `PUT` | `/api/rooms/{id}` | SUPERADMIN, COORDINATOR | Updates an existing room's attributes |
| `DELETE` | `/api/rooms/{id}` | SUPERADMIN, COORDINATOR | Permanently removes a room |

### Event Bookings

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/v1/bookings` | All | Lists bookings for a given month; admins see all, users see their own |
| `POST` | `/api/v1/bookings` | All | Submits a new room booking request for a specific date and time |
| `POST` | `/api/v1/bookings/{id}/approve` | SUPERADMIN, COORDINATOR | Approves a pending booking; rejects non-admin callers with 403 |
| `GET` | `/api/v1/bookings/room-occupancy` | All | Returns all occupied time slots for a room on a specific date |

### Seat Plans

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/seatplan/generate` | All | Runs the seat allocation algorithm and persists the result |
| `GET` | `/api/seatplan/export/pdf/{id}` | All | Streams the seat plan as a downloadable PDF (`Content-Disposition: attachment`) |
| `GET` | `/api/seatplan/room/{roomId}` | All | Fetches the most recent seat plan generated for a given room |

### AI Assistant

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/ai/ask` | Authenticated | Accepts a natural-language query and returns a context-aware answer scoped to the authenticated user's role and department |

---

## Running Locally

### Option A — Maven

```bash
# 1. Clone
git clone <repository-url>
cd PlanR

# 2. Configure your database in src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/planr
spring.datasource.username=your_user
spring.datasource.password=your_password

# 3. Run
./mvnw spring-boot:run
```

Application starts at `http://localhost:8080`.

### Option B — Docker Compose

```bash
docker-compose up --build
```

The `docker-compose.yml` provisions the application and a PostgreSQL container together. No local database setup required.

---

## Key Design Decisions

**Why Thymeleaf over a SPA?** For an academic platform with strict session-based security, server-side rendering eliminates an entire category of CSRF and token-management complexity. Spring Security integrates natively with Thymeleaf's security dialect.

**Why Bucket4j?** The AI endpoint and PDF generation are computationally expensive. Token-bucket rate limiting is applied at the controller layer, before any service logic executes, ensuring the server stays responsive under load.

**Why OpenPDF over iText?** OpenPDF is the actively maintained LGPL fork of iText 4. It avoids iText's commercial licensing constraints while providing the same PDF generation API.

**Why Spring Session JDBC?** Storing sessions in PostgreSQL instead of in-memory means the application can be restarted or redeployed on Render without invalidating active user sessions.

---

<div align="center">



</div>
