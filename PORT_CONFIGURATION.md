# Port Configuration Guide

## Current Default Ports

- **Backend**: 8081
- **Frontend**: 4200

## How to Change Ports

### Step 1: Update docker-compose.yml

Edit `docker-compose.yml` and change the port mappings:

```yaml
services:
  backend:
    ports:
      - "YOUR_BACKEND_PORT:8081"  # Change YOUR_BACKEND_PORT to your open port
  
  frontend:
    ports:
      - "YOUR_FRONTEND_PORT:80"   # Change YOUR_FRONTEND_PORT to your open port
```

### Step 2: Update Frontend API URL (if backend port changed)

Edit `frontend-angular/src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'http://YOUR_SERVER_IP:YOUR_BACKEND_PORT/api'
};
```

**For same server deployment:**
- If frontend and backend are on the same server, you can use:
  ```typescript
  apiUrl: '/api'  // Relative URL (recommended for same server)
  ```
- Or use the server's IP/hostname:
  ```typescript
  apiUrl: 'http://localhost:YOUR_BACKEND_PORT/api'
  ```

### Step 3: Update Backend Port in application.properties (if needed)

Edit `backend-springboot/src/main/resources/application.properties`:

```properties
server.port=8081  # Internal container port (usually keep as 8081)
```

**Note:** The internal port (8081) should stay the same. Only change the host port mapping in docker-compose.yml.

## Example: Using Ports 9000 and 9001

If your open ports are:
- Backend: 9000
- Frontend: 9001

### 1. Update docker-compose.yml:
```yaml
services:
  backend:
    ports:
      - "9000:8081"  # Host port 9000 maps to container port 8081
  
  frontend:
    ports:
      - "9001:80"    # Host port 9001 maps to container port 80
```

### 2. Update environment.prod.ts:
```typescript
export const environment = {
  production: true,
  apiUrl: 'http://your-server-ip:9000/api'
};
```

### 3. Rebuild and restart:
```bash
docker-compose down
docker-compose up -d --build
```

## Using Environment Variables (Alternative)

You can also use environment variables in docker-compose.yml:

```yaml
services:
  backend:
    ports:
      - "${BACKEND_PORT:-8081}:8081"
  
  frontend:
    ports:
      - "${FRONTEND_PORT:-4200}:80"
```

Then create a `.env` file:
```bash
BACKEND_PORT=9000
FRONTEND_PORT=9001
```

## Quick Reference

| Component | Container Port | Default Host Port | Config File |
|-----------|--------------|------------------|-------------|
| Backend | 8081 | 8081 | docker-compose.yml |
| Frontend | 80 | 4200 | docker-compose.yml |
| Frontend API URL | - | - | environment.prod.ts |

## After Changing Ports

1. Rebuild containers:
   ```bash
   docker-compose down
   docker-compose up -d --build
   ```

2. Verify:
   ```bash
   # Check backend
   curl http://localhost:YOUR_BACKEND_PORT/actuator/health
   
   # Check frontend
   curl http://localhost:YOUR_FRONTEND_PORT
   ```

3. Access URLs:
   - Frontend: `http://your-server-ip:YOUR_FRONTEND_PORT`
   - Backend: `http://your-server-ip:YOUR_BACKEND_PORT/api`
