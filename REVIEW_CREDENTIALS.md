# EventHub — Review Credentials

## Demo Account (works without backend)

| Field    | Value                  |
|----------|------------------------|
| Email    | `demo@eventhub.com`    |
| Password | `demo123456`           |

## How to Login

1. Open the app at `http://localhost:4200`
2. Navigate to `/auth/login`
3. Click **"Use Demo"** button — fills credentials and submits automatically
4. If the Spring Boot backend is not running, the app falls back to Demo Mode on any login attempt

## Seeded Backend Users (requires `docker-compose up`)

| Role  | Email                   | Password       |
|-------|-------------------------|----------------|
| User  | `demo@eventhub.com`     | `demo123456`   |
| Admin | `admin@eventhub.com`    | `Admin123!`    |

> **Note:** The frontend works standalone in demo mode. Backend users only work when the Spring Boot + PostgreSQL containers are running.
