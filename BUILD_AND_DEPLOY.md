# Build and Deploy Guide

## Fixed Issues
1. ✅ Dockerfile now correctly copies Angular build files from `/app/dist/frontend-angular/browser`
2. ✅ Dockerfile now copies `nginx.conf` to replace default nginx config
3. ✅ Environment files updated to use server IP (`10.20.232.31:8081`) instead of localhost

## Build Images Locally

### Step 1: Build Backend Image
```bash
cd backend-springboot
docker build -t finsight-backend:latest .
cd ..
```

### Step 2: Build Frontend Image
```bash
cd frontend-angular
docker build -t finsight-frontend:latest .
cd ..
```

### Step 3: Save Images as Tar Files
```bash
# Save backend image
docker save finsight-backend:latest -o finsight-backend.tar

# Save frontend image
docker save finsight-frontend:latest -o finsight-frontend.tar
```

### Step 4: Transfer to Server
Transfer both `.tar` files to your server using SCP, SFTP, or any method:
```bash
# Example using SCP
scp finsight-backend.tar finsight-frontend.tar user@server:/path/to/finsight-ai/
```

## Deploy on Server

### Step 1: Load Images
```bash
cd /path/to/finsight-ai

# Load backend image
docker load -i finsight-backend.tar

# Load frontend image
docker load -i finsight-frontend.tar

# Verify images are loaded
docker images | grep finsight
```

### Step 2: Update docker-compose.yml
Ensure `docker-compose.yml` uses `image:` instead of `build:`:

```yaml
services:
  backend:
    image: finsight-backend:latest  # Changed from build
    # ... rest of config

  frontend:
    image: finsight-frontend:latest  # Changed from build
    # ... rest of config
```

### Step 3: Start Containers
```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

## Verify Deployment

### Backend
```bash
# Test health endpoint
curl http://localhost:8081/actuator/health
# Should return: {"status":"UP"}
```

### Frontend
```bash
# Test frontend
curl http://localhost:4200
# Should return Angular app HTML (not nginx default page)

# Access in browser
# http://10.20.232.31:4200
```

## Configuration Notes

### Backend API URL
- Frontend is configured to use: `http://10.20.232.31:8081/api`
- If your server IP changes, update:
  - `frontend-angular/src/environments/environment.ts`
  - `frontend-angular/src/environments/environment.prod.ts`
  - Rebuild frontend image

### Database Connection
- Backend uses `extra_hosts` in docker-compose.yml to resolve `indlin4661`
- Current IP mapping: `indlin4661:10.20.232.38`
- If database server IP changes, update `docker-compose.yml`

## Troubleshooting

### Frontend shows nginx default page
- Check if Angular files are in `/usr/share/nginx/html`:
  ```bash
  docker exec finsight-frontend ls -la /usr/share/nginx/html
  ```
- Should see: `index.html`, `main-*.js`, `polyfills-*.js`, etc.

### Frontend can't connect to backend
- Check environment files have correct API URL
- Check browser console (F12) for CORS errors
- Verify backend is running: `docker logs finsight-backend`

### Backend can't connect to database
- Check `extra_hosts` in docker-compose.yml
- Verify database server is reachable: `ping indlin4661`
- Check backend logs: `docker logs finsight-backend`
