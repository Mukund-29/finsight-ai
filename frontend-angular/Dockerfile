FROM node:20 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
# Copy the built Angular app from the correct output directory
COPY --from=build /app/dist/frontend-angular/browser /usr/share/nginx/html
# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
