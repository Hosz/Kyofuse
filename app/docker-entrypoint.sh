#!/bin/sh
set -e

BACKEND_URL="${BACKEND_URL:-http://api:8080}"

echo "Configuring Nginx with BACKEND_URL: ${BACKEND_URL}"

sed -i "s|http://api:8080|${BACKEND_URL}|g" /etc/nginx/conf.d/default.conf

if echo "$BACKEND_URL" | grep -q "^https"; then
    sed -i '/proxy_pass/a \        proxy_ssl_server_name on;\n        proxy_ssl_name $proxy_host;' /etc/nginx/conf.d/default.conf
    sed -i 's/proxy_set_header Host $host;/proxy_set_header Host $proxy_host;/g' /etc/nginx/conf.d/default.conf
fi

exec nginx -g 'daemon off;'
