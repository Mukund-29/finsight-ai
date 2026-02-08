# Production Deployment Troubleshooting

## Issue: Updated Features Not Showing in Production

### Root Cause
Your `docker-compose.yml` is using `build:` instead of `image:`, which causes Docker Compose to rebuild from source code instead of using the loaded images.

## Solution: Update docker-compose.yml

### Step 1: Update docker-compose.yml on Production Server

Change from:
```yaml
services:
  backend:
    build: ./backend-springboot  # ❌ This rebuilds from source
    container_name: finsight-backend
    # ...

  frontend:
    build: ./frontend-angular  # ❌ This rebuilds from source
    container_name: finsight-frontend
    # ...
```

To:
```yaml
services:
  backend:
    image: finsight-backend:latest  # ✅ Use loaded image
    container_name: finsight-backend
    # ...

  frontend:
    image: finsight-frontend:latest  # ✅ Use loaded image
    container_name: finsight-frontend
    # ...
```

### Step 2: Force Remove Old Containers and Images

```bash
# Stop and remove containers
docker-compose down

# Remove old images (optional, to force using new ones)
docker rmi finsight-backend:latest finsight-frontend:latest

# Reload images from tar files
docker load -i finsight-backend.tar
docker load -i finsight-frontend.tar

# Verify images are loaded
docker images | grep finsight
```

### Step 3: Start Services with New Images

```bash
# Start services (will now use image: instead of build:)
docker-compose up -d

# Verify containers are using correct images
docker-compose ps
docker inspect finsight-frontend | grep Image
```

## Alternative: Use --no-build Flag (Temporary Fix)

If you can't modify docker-compose.yml immediately:

```bash
# Stop services
docker-compose down

# Start without building (uses existing images)
docker-compose up -d --no-build

# Or force recreate containers
docker-compose up -d --force-recreate --no-build
```

## Verify Deployment

### Check Which Image Container is Using
```bash
# Check frontend container image
docker inspect finsight-frontend | grep -A 5 "Image"

# Check backend container image
docker inspect finsight-backend | grep -A 5 "Image"
```

### Check Image Creation Date
```bash
# See when images were created
docker images finsight-backend:latest --format "table {{.Repository}}\t{{.Tag}}\t{{.CreatedAt}}"
docker images finsight-frontend:latest --format "table {{.Repository}}\t{{.Tag}}\t{{.CreatedAt}}"
```

### Test Frontend Content
```bash
# Check if new features are in the built files
docker exec finsight-frontend ls -la /usr/share/nginx/html

# Check main.js file size (should match your new build)
docker exec finsight-frontend ls -lh /usr/share/nginx/html/main*.js
```

## Complete Deployment Checklist

### On Local Machine (After Code Changes)
```bash
# 1. Build fresh images
cd backend-springboot && docker build -t finsight-backend:latest . && cd ..
cd frontend-angular && docker build -t finsight-frontend:latest . && cd ..

# 2. Verify build
docker images | grep finsight

# 3. Save images
docker save finsight-backend:latest -o finsight-backend.tar
docker save finsight-frontend:latest -o finsight-frontend.tar

# 4. Transfer to production
scp finsight-*.tar user@production-server:/path/to/finsight-ai/
```

### On Production Server
```bash
# 1. Navigate to project
cd /path/to/finsight-ai

# 2. Stop services
docker-compose down

# 3. Remove old images (optional)
docker rmi finsight-backend:latest finsight-frontend:latest 2>/dev/null || true

# 4. Load new images
docker load -i finsight-backend.tar
docker load -i finsight-frontend.tar

# 5. Verify images loaded
docker images | grep finsight

# 6. Update docker-compose.yml (change build: to image:)
# Edit docker-compose.yml and replace build: with image:

# 7. Start services
docker-compose up -d

# 8. Check logs
docker-compose logs -f frontend
```

## Common Issues and Fixes

### Issue 1: Container Still Using Old Image
**Fix:**
```bash
docker-compose down
docker-compose up -d --force-recreate
```

### Issue 2: Browser Cache Showing Old Frontend
**Fix:**
- Hard refresh: `Ctrl+F5` or `Cmd+Shift+R`
- Clear browser cache
- Test in incognito/private mode

### Issue 3: Images Not Loading Properly
**Fix:**
```bash
# Verify tar file integrity
file finsight-backend.tar
file finsight-frontend.tar

# Try loading with verbose output
docker load -i finsight-backend.tar
docker load -i finsight-frontend.tar

# Check for errors
docker images | grep finsight
```

### Issue 4: docker-compose Still Building
**Fix:**
```bash
# Ensure docker-compose.yml uses image: not build:
grep -E "build:|image:" docker-compose.yml

# If you see "build:", change it to "image: finsight-xxx:latest"
```

## Quick Verification Script

```bash
#!/bin/bash
echo "=== Checking Docker Images ==="
docker images | grep finsight

echo -e "\n=== Checking Running Containers ==="
docker ps | grep finsight

echo -e "\n=== Checking Container Images ==="
echo "Frontend container using:"
docker inspect finsight-frontend --format '{{.Config.Image}}' 2>/dev/null || echo "Container not running"

echo "Backend container using:"
docker inspect finsight-backend --format '{{.Config.Image}}' 2>/dev/null || echo "Container not running"

echo -e "\n=== Checking docker-compose.yml ==="
if grep -q "build:" docker-compose.yml; then
    echo "⚠️  WARNING: docker-compose.yml still uses 'build:' instead of 'image:'"
    echo "   This will cause it to rebuild from source instead of using loaded images"
else
    echo "✅ docker-compose.yml correctly uses 'image:'"
fi
```

## Summary

**The main issue:** `docker-compose.yml` has `build:` which makes Docker Compose rebuild from source code on the server instead of using your transferred images.

**The fix:** Change `build:` to `image: finsight-xxx:latest` in `docker-compose.yml` on the production server.

**You do NOT need to update source code on production** - you're using Docker images, so the code is baked into the images you built locally and transferred.
