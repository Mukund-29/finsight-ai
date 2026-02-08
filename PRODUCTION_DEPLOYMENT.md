# Production Deployment Guide

This guide provides step-by-step instructions to build Docker images and deploy them to production.

## Prerequisites

- Docker installed on your local machine
- Access to production server
- SSH/SCP access to production server
- Docker installed on production server

## Step 1: Build Docker Images Locally

### Build Backend Image
```bash
cd backend-springboot
docker build -t finsight-backend:latest .
cd ..
```

**Description:** Build backend Docker image with latest tag

### Build Frontend Image
```bash
cd frontend-angular
docker build -t finsight-frontend:latest .
cd ..
```

**Description:** Build frontend Docker image with latest tag

### Verify Images
```bash
docker images | grep finsight
```

**Description:** Verify both images are created successfully

## Step 2: Save Images as Tar Files

### Save Backend Image
```bash
docker save finsight-backend:latest -o finsight-backend.tar
```

**Description:** Export backend image to tar file for transfer

### Save Frontend Image
```bash
docker save finsight-frontend:latest -o finsight-frontend.tar
```

**Description:** Export frontend image to tar file for transfer

### Check File Sizes
```bash
ls -lh finsight-*.tar
```

**Description:** Verify tar files are created (check sizes)

## Step 3: Transfer Images to Production Server

### Option A: Using SCP (Secure Copy)
```bash
scp finsight-backend.tar finsight-frontend.tar user@production-server:/path/to/destination/
```

**Description:** Transfer both tar files to production server via SCP

**Example:**
```bash
scp finsight-backend.tar finsight-frontend.tar root@10.20.232.31:/vstusr2/sst/csm/vstwrk1/mk/finsight-ai/
```

### Option B: Using SFTP
```bash
sftp user@production-server
put finsight-backend.tar
put finsight-frontend.tar
exit
```

**Description:** Transfer files using SFTP interactive mode

### Option C: Using USB/External Drive
1. Copy tar files to USB drive
2. Transfer to production server
3. Copy files to destination directory

## Step 4: Load Images on Production Server

### SSH into Production Server
```bash
ssh user@production-server
```

**Description:** Connect to production server

### Navigate to Project Directory
```bash
cd /path/to/finsight-ai
```

**Description:** Navigate to project directory on production server

### Load Backend Image
```bash
docker load -i finsight-backend.tar
```

**Description:** Load backend image from tar file into Docker

### Load Frontend Image
```bash
docker load -i finsight-frontend.tar
```

**Description:** Load frontend image from tar file into Docker

### Verify Images Loaded
```bash
docker images | grep finsight
```

**Description:** Verify both images are loaded successfully

## Step 5: Update docker-compose.yml for Production

### Option A: Use Image Tags (Recommended)
Ensure `docker-compose.yml` uses `image:` instead of `build:`:

```yaml
services:
  backend:
    image: finsight-backend:latest  # Use image instead of build
    container_name: finsight-backend
    # ... rest of config

  frontend:
    image: finsight-frontend:latest  # Use image instead of build
    container_name: finsight-frontend
    # ... rest of config
```

### Option B: Keep Build Configuration
If you want to keep build config, ensure Dockerfiles are present on production server.

## Step 6: Configure Environment Variables

### Create/Update .env File
```bash
nano .env
```

**Description:** Create or edit environment file

### Required Variables
```bash
# Ports
BACKEND_PORT=8081
FRONTEND_PORT=4200

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@indlin4661:1521:INDDB001
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=oracle.jdbc.OracleDriver
SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA=your_schema

# Spring Profile
SPRING_PROFILE=local
```

**Description:** Configure ports and database connection

## Step 7: Stop Existing Containers (If Any)

### Stop and Remove Existing Containers
```bash
docker-compose down
```

**Description:** Stop and remove existing containers

### Remove Old Images (Optional)
```bash
docker rmi finsight-backend:old-tag finsight-frontend:old-tag
```

**Description:** Remove old image versions to free space

## Step 8: Start Services

### Start All Services
```bash
docker-compose up -d
```

**Description:** Start all services in detached mode using loaded images

### Check Service Status
```bash
docker-compose ps
```

**Description:** Verify all services are running

### View Logs
```bash
docker-compose logs -f
```

**Description:** Monitor logs to ensure services start correctly

## Step 9: Verify Deployment

### Test Backend Health
```bash
curl http://localhost:8081/actuator/health
```

**Description:** Test backend health endpoint (should return {"status":"UP"})

### Test Frontend
```bash
curl http://localhost:4200
```

**Description:** Test frontend endpoint (should return HTML, not nginx default page)

### Access in Browser
- **Frontend:** `http://production-server-ip:4200`
- **Backend API:** `http://production-server-ip:8081/api`
- **Health Check:** `http://production-server-ip:8081/actuator/health`

## Complete Deployment Script

### Local Machine (Build and Save)
```bash
#!/bin/bash
# Build images
cd backend-springboot && docker build -t finsight-backend:latest . && cd ..
cd frontend-angular && docker build -t finsight-frontend:latest . && cd ..

# Save images
docker save finsight-backend:latest -o finsight-backend.tar
docker save finsight-frontend:latest -o finsight-frontend.tar

# Transfer to production (update with your server details)
scp finsight-backend.tar finsight-frontend.tar user@production-server:/path/to/finsight-ai/
```

### Production Server (Load and Deploy)
```bash
#!/bin/bash
# Load images
docker load -i finsight-backend.tar
docker load -i finsight-frontend.tar

# Stop existing services
docker-compose down

# Start services
docker-compose up -d

# Check status
docker-compose ps
docker-compose logs -f
```

## Troubleshooting

### Images Not Loading
```bash
# Check tar file integrity
file finsight-backend.tar
file finsight-frontend.tar

# Try loading with verbose output
docker load -i finsight-backend.tar -v
```

**Description:** Verify tar files are valid Docker image archives

### Services Not Starting
```bash
# Check logs
docker-compose logs backend
docker-compose logs frontend

# Check container status
docker ps -a

# Check for port conflicts
netstat -tulpn | grep 8081
netstat -tulpn | grep 4200
```

**Description:** Diagnose service startup issues

### Database Connection Issues
```bash
# Check backend logs
docker logs finsight-backend

# Verify database hostname resolution
ping indlin4661

# Test database connection from container
docker exec finsight-backend nc -zv indlin4661 1521
```

**Description:** Troubleshoot database connectivity

### Frontend Shows Nginx Default Page
```bash
# Check if Angular files are present
docker exec finsight-frontend ls -la /usr/share/nginx/html

# Rebuild frontend if needed
cd frontend-angular
docker build -t finsight-frontend:latest .
docker-compose up -d frontend
```

**Description:** Fix frontend serving issues

## Cleanup After Deployment

### Remove Tar Files (Optional)
```bash
rm finsight-backend.tar finsight-frontend.tar
```

**Description:** Remove tar files after successful deployment to free space

### Remove Old Images (Optional)
```bash
docker image prune -a
```

**Description:** Remove unused images to free disk space

## Quick Reference Commands

### Build and Save (Local)
```bash
docker build -t finsight-backend:latest ./backend-springboot
docker build -t finsight-frontend:latest ./frontend-angular
docker save finsight-backend:latest -o finsight-backend.tar
docker save finsight-frontend:latest -o finsight-frontend.tar
```

### Transfer (Local)
```bash
scp finsight-*.tar user@server:/path/
```

### Load and Deploy (Production)
```bash
docker load -i finsight-backend.tar
docker load -i finsight-frontend.tar
docker-compose down
docker-compose up -d
docker-compose ps
```

## Notes

- Always test images locally before transferring to production
- Keep backup of previous images in case of rollback
- Update `.env` file with production database credentials
- Ensure production server has sufficient disk space for images
- Monitor logs after deployment to ensure services are healthy
- Use version tags (e.g., `v1.0.0`) for better image management
