# Deployment Guide

## Quick Start

1. **Copy the code to your server**
   ```bash
   # Transfer the entire finsight-ai folder to your server
   ```

2. **Configure ports and database**
   ```bash
   # Copy the example environment file
   cp .env.example .env
   
   # Edit .env with your open ports and database credentials
   nano .env
   ```

3. **Update frontend API URL** (if backend is on different host/port)
   - Edit `frontend-angular/src/app/services/auth.service.ts`
   - Edit `frontend-angular/src/app/services/request.service.ts`
   - Change `http://localhost:8081` to your backend server URL

4. **Build and start**
   ```bash
   docker-compose up -d --build
   ```

5. **Check status**
   ```bash
   docker-compose ps
   docker-compose logs -f
   ```

## Port Configuration

### Current Default Ports:
- **Backend**: 8081 (internal: 8081)
- **Frontend**: 4200 (internal: 80)

### To Change Ports:

**Option 1: Using .env file (Recommended)**
```bash
# Edit .env file
BACKEND_PORT=YOUR_BACKEND_PORT
FRONTEND_PORT=YOUR_FRONTEND_PORT
```

**Option 2: Using environment variables**
```bash
export BACKEND_PORT=YOUR_BACKEND_PORT
export FRONTEND_PORT=YOUR_FRONTEND_PORT
docker-compose up -d --build
```

**Option 3: Direct docker-compose.yml edit**
Edit `docker-compose.yml` and change the port mappings:
```yaml
ports:
  - "YOUR_BACKEND_PORT:8081"  # Backend
  - "YOUR_FRONTEND_PORT:80"   # Frontend
```

## Database Configuration

### Using .env file:
```bash
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@host:1521:database
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password
SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA=schema
```

### Using application.properties:
Edit `backend-springboot/src/main/resources/application.properties` or create `application-local.properties`

## Access URLs

After deployment:
- **Frontend**: `http://your-server-ip:FRONTEND_PORT`
- **Backend API**: `http://your-server-ip:BACKEND_PORT/api`
- **Backend Health**: `http://your-server-ip:BACKEND_PORT/actuator/health`

## Troubleshooting

### View logs:
```bash
docker-compose logs backend
docker-compose logs frontend
docker-compose logs -f  # Follow all logs
```

### Restart services:
```bash
docker-compose restart
```

### Rebuild after code changes:
```bash
docker-compose down
docker-compose up -d --build
```

### Check container status:
```bash
docker ps
docker-compose ps
```
