# Docker Commands Used in Finsight AI Project

This document lists all Docker commands used in the project with brief descriptions.

## Docker Compose Commands

### Service Management
```bash
docker-compose up -d
```
**Description:** Start all services in detached mode (background)

```bash
docker-compose up -d --build
```
**Description:** Build images and start services in detached mode

```bash
docker-compose down
```
**Description:** Stop and remove all containers, networks, and volumes

```bash
docker-compose restart
```
**Description:** Restart all running services

```bash
docker-compose ps
```
**Description:** List status of all services

### Logs
```bash
docker-compose logs
```
**Description:** View logs from all services

```bash
docker-compose logs -f
```
**Description:** Follow logs from all services (real-time)

```bash
docker-compose logs backend
```
**Description:** View logs from backend service only

```bash
docker-compose logs frontend
```
**Description:** View logs from frontend service only

## Docker Build Commands

### Build Images
```bash
docker build -t finsight-backend:latest ./backend-springboot
```
**Description:** Build backend Docker image

```bash
docker build -t finsight-frontend:latest ./frontend-angular
```
**Description:** Build frontend Docker image

```bash
docker build -t finsight-backend:latest .
```
**Description:** Build backend image from current directory (when inside backend-springboot folder)

```bash
docker build -t finsight-frontend:latest .
```
**Description:** Build frontend image from current directory (when inside frontend-angular folder)

### Image Management
```bash
docker images
```
**Description:** List all Docker images

```bash
docker images | grep finsight
```
**Description:** List only finsight-related images

```bash
docker save finsight-backend:latest -o finsight-backend.tar
```
**Description:** Save backend image as tar file for transfer

```bash
docker save finsight-frontend:latest -o finsight-frontend.tar
```
**Description:** Save frontend image as tar file for transfer

```bash
docker load -i finsight-backend.tar
```
**Description:** Load backend image from tar file

```bash
docker load -i finsight-frontend.tar
```
**Description:** Load frontend image from tar file

## Docker Container Commands

### Container Management
```bash
docker ps
```
**Description:** List running containers

```bash
docker ps -a
```
**Description:** List all containers (including stopped)

```bash
docker ps -a | grep finsight-backend
```
**Description:** List containers filtered by name

```bash
docker stop finsight-backend
```
**Description:** Stop backend container

```bash
docker stop finsight-frontend
```
**Description:** Stop frontend container

```bash
docker rm finsight-backend
```
**Description:** Remove backend container

```bash
docker rm finsight-frontend
```
**Description:** Remove frontend container

```bash
docker rm test-backend
```
**Description:** Remove test container (used in CI/CD)

### Container Execution
```bash
docker run -d -p 8081:8081 --name test-backend finsight-backend:latest
```
**Description:** Run backend container in detached mode for testing

```bash
docker exec finsight-frontend ls -la /usr/share/nginx/html
```
**Description:** Execute command inside frontend container to check files

### Container Logs
```bash
docker logs finsight-backend
```
**Description:** View backend container logs

```bash
docker logs finsight-frontend
```
**Description:** View frontend container logs

```bash
docker logs test-backend
```
**Description:** View test container logs (used in CI/CD)

## Docker Network Commands

### Network Management
```bash
docker network ls
```
**Description:** List all Docker networks

```bash
docker network inspect finsight-ai_finsight-network
```
**Description:** Inspect project network details

## Health Check Commands

### Testing Endpoints
```bash
curl http://localhost:8081/actuator/health
```
**Description:** Test backend health endpoint

```bash
curl http://localhost:4200
```
**Description:** Test frontend endpoint

```bash
curl -f http://localhost:8081/actuator/health || exit 1
```
**Description:** Test backend health with exit on failure (used in CI/CD)

## CI/CD Specific Commands

### GitHub Actions
```bash
docker run -d -p 8081:8081 --name test-backend finsight-backend:latest
```
**Description:** Run test container in CI/CD pipeline

```bash
docker stop test-backend && docker rm test-backend
```
**Description:** Clean up test container after CI/CD tests

```bash
docker compose up -d
```
**Description:** Start services in CI/CD (alternative syntax)

```bash
docker compose ps
```
**Description:** Check service status in CI/CD

```bash
docker compose down
```
**Description:** Stop services in CI/CD

## Common Workflows

### Initial Setup
```bash
docker-compose up -d --build
```
**Description:** First-time setup - build and start all services

### After Code Changes
```bash
docker-compose down
docker-compose up -d --build
```
**Description:** Rebuild and restart after code changes

### View Logs While Debugging
```bash
docker-compose logs -f backend
```
**Description:** Monitor backend logs in real-time

### Check Service Status
```bash
docker-compose ps
docker ps
```
**Description:** Verify all services are running

### Transfer Images to Server
```bash
docker save finsight-backend:latest -o finsight-backend.tar
docker save finsight-frontend:latest -o finsight-frontend.tar
# Transfer files to server
docker load -i finsight-backend.tar
docker load -i finsight-frontend.tar
```
**Description:** Save images, transfer, and load on server

## Notes

- All commands assume you're in the project root directory unless specified
- Use `docker-compose` or `docker compose` (both work, newer Docker uses `docker compose`)
- Ports can be configured via `.env` file or environment variables
- Backend default port: 8081
- Frontend default port: 4200 (maps to container port 80)
