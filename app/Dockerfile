# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Stage 1 — dependencies
# Installs npm dependencies. Cached independently from application source
# changes.
# ---------------------------------------------------------------------------
FROM node:22-alpine AS deps

WORKDIR /app

# Copy package manifests first to leverage Docker layer caching
COPY package.json package-lock.json ./
RUN npm ci

# ---------------------------------------------------------------------------
# Stage 2 — development (optional target for containerized live-reload dev)
# ---------------------------------------------------------------------------
FROM deps AS dev

WORKDIR /app
COPY . .

EXPOSE 4200

CMD ["npx", "ng", "serve", "--host", "0.0.0.0", "--port", "4200", "--proxy-config", "proxy.docker.conf.json", "--poll", "2000"]

# ---------------------------------------------------------------------------
# Stage 3 — build
# Compiles the Angular application for production.
# ---------------------------------------------------------------------------
FROM deps AS build

WORKDIR /app
COPY . .
RUN npm run build -- --configuration production

# ---------------------------------------------------------------------------
# Stage 4 — runtime
# Lightweight Nginx Alpine image to serve production assets and reverse-proxy
# backend API & WebSocket endpoints.
# ---------------------------------------------------------------------------
FROM nginx:alpine AS runtime

# Copy compiled static assets from the build stage
COPY --from=build /app/dist/kyofuse-app/browser /usr/share/nginx/html

# Custom Nginx configuration (SPA routing + /api and /ws reverse proxy)
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 80

ENTRYPOINT ["/docker-entrypoint.sh"]
