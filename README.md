# EventHub

Event management platform for creating, discovering, and managing events. Pairs an Angular frontend with a Spring Boot backend and PostgreSQL database.

## Tech Stack

**Frontend** — Angular 17 · TypeScript · Angular Material · NgRx  
**Backend** — Java 17 · Spring Boot 3 · Spring Security · JPA/Hibernate · PostgreSQL  
**Cache** — Redis  
**Infrastructure** — Docker · Docker Compose

## Features

- JWT authentication with role-based access (Admin / Organizer / Attendee)
- Event creation and publishing workflow
- Multi-tier ticketing with capacity management
- Event session scheduling
- Registration and attendee management
- Glassmorphic earthy dark-mode UI (Angular Material)

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Node.js 18+
- Java 17+

### Run with Docker

```bash
docker-compose up -d
```

Frontend: http://localhost:4200  
Backend API: http://localhost:8080

### Run manually

**Backend**
```bash
cd backend
./mvnw spring-boot:run
```

**Frontend**
```bash
cd frontend
npm install
ng serve
```

## Project Structure

```
eventhub/
├── backend/
│   └── src/main/java/com/eventhub/
│       ├── controller/
│       ├── service/
│       ├── entity/
│       └── repository/
├── frontend/
│   └── src/app/
│       ├── features/
│       │   ├── auth/
│       │   ├── events/
│       │   └── dashboard/
│       └── core/
│           ├── services/
│           ├── guards/
│           └── models/
└── docker-compose.yml
```

## Demo Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Organizer | organizer | org123 |
| Attendee | attendee | att123 |
