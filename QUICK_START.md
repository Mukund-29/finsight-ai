# Quick Start - Port Configuration

## Provide Your Open Ports

Please provide your list of open ports, and I'll update the configuration automatically.

**Current default ports:**
- Backend: 8081
- Frontend: 4200

## Manual Configuration (If you prefer)

### Option 1: Quick Script (Linux/Mac)

```bash
chmod +x setup-ports.sh
./setup-ports.sh <BACKEND_PORT> <FRONTEND_PORT> [BACKEND_API_URL]

# Example:
./setup-ports.sh 9000 9001 http://192.168.1.100:9000
```

### Option 2: Manual Edit

1. **Edit docker-compose.yml:**
   ```yaml
   services:
     backend:
       ports:
         - "YOUR_BACKEND_PORT:8081"
     frontend:
       ports:
         - "YOUR_FRONTEND_PORT:80"
   ```

2. **Edit frontend-angular/src/environments/environment.prod.ts:**
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'http://YOUR_SERVER_IP:YOUR_BACKEND_PORT/api'
   };
   ```

3. **Rebuild:**
   ```bash
   docker-compose down
   docker-compose up -d --build
   ```

## What I've Already Configured

✅ **docker-compose.yml** - Updated with flexible port configuration
✅ **Environment files** - Created for frontend API URL configuration
✅ **Services** - Updated to use environment-based API URLs
✅ **Documentation** - Created PORT_CONFIGURATION.md and DEPLOYMENT.md

## Next Steps

1. **Provide your open ports** (or use the defaults)
2. **Update the configuration** (I can do this once you provide ports)
3. **Configure database** in `backend-springboot/src/main/resources/application.properties`
4. **Build and deploy:**
   ```bash
   docker-compose up -d --build
   ```

## Files Updated

- `docker-compose.yml` - Main deployment configuration
- `frontend-angular/src/environments/environment.ts` - Development API URL
- `frontend-angular/src/environments/environment.prod.ts` - Production API URL
- `frontend-angular/src/app/services/auth.service.ts` - Uses environment config
- `frontend-angular/src/app/services/request.service.ts` - Uses environment config
- `frontend-angular/angular.json` - Configured for environment file replacement
