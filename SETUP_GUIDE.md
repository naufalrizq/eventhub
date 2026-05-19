# EventHub Setup Guide

**Tech Stack: Angular 17 + Java Spring Boot + PostgreSQL**

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Java JDK** (v17 or higher) - [Download](https://adoptium.net/)
- **Node.js** (v18 or higher) - [Download](https://nodejs.org/)
- **Angular CLI** (v17) - `npm install -g @angular/cli`
- **Maven** (v3.8 or higher) - [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL** (v13 or higher) - [Download](https://www.postgresql.org/download/)
- **Docker & Docker Compose** (optional but recommended) - [Download](https://www.docker.com/)
- **Git** - [Download](https://git-scm.com/)

## 🚀 Quick Start (Docker - Recommended)

### Step 1: Navigate to Project Directory
```bash
cd /home/nrizq/Documents/Codes/Learning/AngularSpring/eventhub
```

### Step 2: Start All Services
```bash
# Start PostgreSQL, Redis, Backend, and Frontend
docker-compose up -d

# View logs (optional)
docker-compose logs -f
```

### Step 3: Access the Application
- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Database**: localhost:5434 (postgres/postgres123)

---

## 🛠️ Manual Setup (Development)

### Backend Setup (Spring Boot + Java)

#### Step 1: Navigate to Backend Directory
```bash
cd backend
```

#### Step 2: Verify Java Installation
```bash
# Check Java version
java -version

# Check Maven version
mvn -version
```

#### Step 3: Setup PostgreSQL Database
```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE eventhub;

# Create user (optional)
CREATE USER eventhub_user WITH PASSWORD 'postgres123';
GRANT ALL PRIVILEGES ON DATABASE eventhub TO eventhub_user;

# Exit PostgreSQL
\q
```

#### Step 4: Configure Application Properties
The application is pre-configured with the following profiles:

**Development (default)**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/eventhub
    username: postgres
    password: postgres123
```

**Docker**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/eventhub
```

#### Step 5: Install Dependencies and Run
```bash
# Install dependencies
mvn clean install

# Run with development profile
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or build and run JAR
mvn clean package
java -jar target/eventhub-backend-1.0.0.jar
```

**Backend should be running on**: http://localhost:8080

### Frontend Setup (Angular 17)

#### Step 1: Navigate to Frontend Directory
```bash
cd ../frontend
```

#### Step 2: Install Angular CLI (if not installed)
```bash
# Install Angular CLI globally
npm install -g @angular/cli@17

# Verify installation
ng version
```

#### Step 3: Create Angular Project
```bash
# Create new Angular project
ng new eventhub-frontend --routing --style=scss --strict

# Navigate to project
cd eventhub-frontend

# Install additional dependencies
npm install @angular/material @angular/cdk @angular/animations
npm install @ngrx/store @ngrx/effects @ngrx/store-devtools
npm install chart.js ng2-charts
npm install rxjs
```

#### Step 4: Configure Environment
```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};

// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080/api'
};
```

#### Step 5: Run Frontend Development Server
```bash
# Start development server
ng serve

# Or with specific port
ng serve --port 4200

# Or with host binding
ng serve --host 0.0.0.0 --port 4200
```

**Frontend should be running on**: http://localhost:4200

---

## 🧪 Testing

### Backend Tests
```bash
cd backend

# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run integration tests
mvn test -Dtest="**/*IntegrationTest"

# Run specific test class
mvn test -Dtest=AuthControllerTest
```

### Frontend Tests
```bash
cd frontend

# Run unit tests
ng test

# Run tests with coverage
ng test --code-coverage

# Run e2e tests
ng e2e

# Run tests in headless mode
ng test --watch=false --browsers=ChromeHeadless
```

---

## 📦 Production Build

### Backend Production Build
```bash
cd backend

# Build for production
mvn clean package -Pprod

# Run production JAR
java -jar target/eventhub-backend-1.0.0.jar --spring.profiles.active=prod

# Or using Docker
docker build -t eventhub-backend .
```

### Frontend Production Build
```bash
cd frontend

# Build for production
ng build --configuration production

# Serve production build (for testing)
npx http-server dist/eventhub-frontend -p 4200

# Or using Docker
docker build -t eventhub-frontend .
```

---

## 🐳 Docker Commands

### Development
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f [service_name]

# Stop all services
docker-compose down

# Rebuild services
docker-compose up --build

# Access database
docker-compose exec postgres psql -U postgres -d eventhub
```

### Production
```bash
# Build production images
docker-compose -f docker-compose.prod.yml build

# Start production services
docker-compose -f docker-compose.prod.yml up -d
```

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Java Version Issues
```bash
# Check Java version
java -version

# Set JAVA_HOME (Linux/Mac)
export JAVA_HOME=/path/to/java17

# Set JAVA_HOME (Windows)
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

#### 2. Maven Issues
```bash
# Clear Maven cache
mvn dependency:purge-local-repository

# Force update dependencies
mvn clean install -U

# Skip tests if needed
mvn clean install -DskipTests
```

#### 3. Database Connection Issues
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Start PostgreSQL
sudo systemctl start postgresql

# Test connection
psql -h localhost -p 5434 -U postgres -d eventhub
```

#### 4. Angular Issues
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Update Angular CLI
npm install -g @angular/cli@latest
```

#### 5. Port Conflicts
```bash
# Check what's using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Use different port
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

---

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - Logout user

### Event Endpoints
- `GET /api/events` - Get events with filters
- `POST /api/events` - Create event
- `GET /api/events/{id}` - Get event by ID
- `PUT /api/events/{id}` - Update event
- `DELETE /api/events/{id}` - Delete event

### Registration Endpoints
- `POST /api/events/{id}/register` - Register for event
- `GET /api/registrations` - Get user registrations
- `PUT /api/registrations/{id}` - Update registration
- `DELETE /api/registrations/{id}` - Cancel registration

### Full API documentation available at: http://localhost:8080/swagger-ui.html

---

## 🏗️ Project Structure

### Backend Structure
```
backend/
├── src/main/java/com/eventhub/
│   ├── EventHubApplication.java
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── config/
│   └── exception/
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
└── pom.xml
```

### Frontend Structure
```
frontend/
├── src/
│   ├── app/
│   │   ├── components/
│   │   ├── services/
│   │   ├── models/
│   │   ├── guards/
│   │   └── store/
│   ├── assets/
│   ├── environments/
│   └── styles/
├── angular.json
└── package.json
```

---

## 🎯 Next Steps

1. **Complete the Angular frontend** implementation
2. **Add more Spring Boot entities** (TicketType, Review, etc.)
3. **Implement remaining controllers** and services
4. **Add comprehensive testing**
5. **Set up CI/CD pipeline**
6. **Deploy to cloud platform**

---

## 🆘 Support

If you encounter any issues:

1. Check the troubleshooting section above
2. Review the logs: `docker-compose logs -f`
3. Ensure all prerequisites are installed correctly
4. Verify Java and Node.js versions
5. Check database connectivity

**Project Status**: 🟡 60% Complete - Backend Foundation Ready
**Last Updated**: December 2024