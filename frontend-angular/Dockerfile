# Multi-stage build for Angular frontend
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build the application
RUN npm run build -- --configuration production

# Production stage
FROM nginx:alpine

# Copy built assets from build stage
# Angular 20 outputs to dist/frontend-angular/browser
COPY --from=build /app/dist/frontend-angular/browser /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
