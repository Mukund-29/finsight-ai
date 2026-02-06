#!/bin/bash

# Port Configuration Script for Finsight AI
# Usage: ./setup-ports.sh <BACKEND_PORT> <FRONTEND_PORT> [BACKEND_API_URL]

if [ $# -lt 2 ]; then
    echo "Usage: ./setup-ports.sh <BACKEND_PORT> <FRONTEND_PORT> [BACKEND_API_URL]"
    echo "Example: ./setup-ports.sh 9000 9001 http://192.168.1.100:9000"
    exit 1
fi

BACKEND_PORT=$1
FRONTEND_PORT=$2
BACKEND_API_URL=${3:-"http://localhost:$BACKEND_PORT"}

echo "Configuring ports:"
echo "  Backend Port: $BACKEND_PORT"
echo "  Frontend Port: $FRONTEND_PORT"
echo "  Backend API URL: $BACKEND_API_URL"
echo ""

# Update docker-compose.yml
echo "Updating docker-compose.yml..."
sed -i.bak "s/\"\${BACKEND_PORT:-8081}:8081\"/\"$BACKEND_PORT:8081\"/g" docker-compose.yml
sed -i.bak "s/\"\${FRONTEND_PORT:-4200}:80\"/\"$FRONTEND_PORT:80\"/g" docker-compose.yml

# Update frontend environment.prod.ts
echo "Updating frontend environment.prod.ts..."
sed -i.bak "s|apiUrl: 'http://localhost:8081/api'|apiUrl: '$BACKEND_API_URL/api'|g" frontend-angular/src/environments/environment.prod.ts

echo ""
echo "Configuration updated successfully!"
echo ""
echo "Next steps:"
echo "1. Review the changes in docker-compose.yml and environment.prod.ts"
echo "2. Run: docker-compose up -d --build"
echo "3. Access frontend at: http://your-server-ip:$FRONTEND_PORT"
echo "4. Access backend at: http://your-server-ip:$BACKEND_PORT"
