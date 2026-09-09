#!/bin/sh
set -e

BACKEND_URL="${BACKEND_URL:-http://api:8080}"

echo "Starting Kyofuse Frontend Nginx with BACKEND_URL: ${BACKEND_URL}"

# If BACKEND_URL ends with a slash, strip it
BACKEND_URL=$(echo "$BACKEND_URL" | sed 's|/*$||')

# Replace http://api:8080 with BACKEND_URL in nginx conf
sed -i "s|http://api:8080|${BACKEND_URL}|g" /etc/nginx/conf.d/default.conf

# Test nginx configuration
nginx -t

exec nginx -g 'daemon off;'
