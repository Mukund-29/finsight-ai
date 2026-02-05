# Finsight Backend - Spring Boot

Spring Boot backend service for Finsight AI application.

**Author:** Mukund Kute

## Features

- RESTful API with Authentication & User Management
- Health check endpoints (Actuator)
- Docker support
- CI/CD integration (GitHub Actions & GitLab CI)
- Oracle Database integration

## API Endpoints

### Authentication (`/api/auth`)

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### User Management (`/api/users`)

- `GET /api/users/{ntid}` - Get user by NTID
- `POST /api/users/admin` - Create admin user (ADMIN only)
- `DELETE /api/users/{ntid}` - Delete user (ADMIN only)
- `PUT /api/users/{ntid}/role` - Update user role (ADMIN only)

See `AUTHENTICATION.md` for detailed API documentation.

## Local Development

### Prerequisites

- Java 17
- Maven 3.9+

### Run Locally

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

### Build

```bash
mvn clean package
```

### Run Tests

```bash
mvn test
```

## Docker

### Build Image

```bash
docker build -t finsight-backend .
```

### Run Container

```bash
docker run -p 8081:8081 finsight-backend
```

## CI/CD

### GitHub Actions

The workflow (`.github/workflows/backend-ci.yml`) automatically:
- Builds and tests on push/PR
- Creates Docker image
- Tests the Docker container

### GitLab CI

The GitLab CI configuration (`.gitlab-ci.yml`) includes:
- Build stage
- Test stage
- Docker build and push to registry

## Project Structure

```
backend-springboot/
├── src/
│   ├── main/
│   │   ├── java/com/finsight/
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   └── UserController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthenticationService.java
│   │   │   │   └── UserService.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   └── UserRole.java
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java
│   │   │   ├── dto/
│   │   │   │   ├── UserRegistrationDTO.java
│   │   │   │   ├── LoginRequestDTO.java
│   │   │   │   └── AuthResponseDTO.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   └── FinsightApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── Dockerfile
├── pom.xml
├── README.md
├── AUTHENTICATION.md
└── POSTMAN_COLLECTION.md
```
