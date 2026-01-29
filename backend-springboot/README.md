# Finsight Backend - Spring Boot

Spring Boot backend service for Finsight AI application.

## Features

- RESTful API with FlowAiController
- Health check endpoints
- Docker support
- CI/CD integration (GitHub Actions & GitLab CI)

## API Endpoints

### FlowAiController

- `GET /api/flow-ai/health` - Health check endpoint
- `GET /api/flow-ai/status` - Service status
- `POST /api/flow-ai/process` - Process flow AI requests

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
│   │   │   │   └── FlowAiController.java
│   │   │   └── FinsightApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/finsight/
│           └── controller/
│               └── FlowAiControllerTest.java
├── Dockerfile
├── pom.xml
└── README.md
```
