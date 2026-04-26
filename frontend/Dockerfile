# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM node:24-alpine AS build
WORKDIR /app

# Copy package files first so npm install layer is cached separately
COPY package*.json ./
RUN npm ci

# Copy source and build
COPY . .
RUN npm run build

# ─── Stage 2: Serve with nginx ────────────────────────────────────────────────
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx static files
RUN rm -rf ./*

# Copy React build output
COPY --from=build /app/build .

# Copy nginx config (handles React Router client-side routing)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

ENTRYPOINT ["nginx", "-g", "daemon off;"]
