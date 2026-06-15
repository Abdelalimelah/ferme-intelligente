#!/bin/bash
# ============================================================
#  SSL/TLS Setup — Let's Encrypt (Certbot)
#  Run on your VPS after domain DNS is pointing to the server
#  Usage: bash ssl-setup.sh your-domain.com your@email.com
# ============================================================
DOMAIN=${1:?"Usage: $0 <domain> <email>"}
EMAIL=${2:?"Usage: $0 <domain> <email>"}
APP_DIR="/opt/ferme-intelligente"

# Install Certbot
apt-get install -y certbot

# Stop Nginx temporarily to free port 80
cd "$APP_DIR"
docker compose stop nginx

# Obtain certificate (standalone mode)
certbot certonly \
  --standalone \
  --non-interactive \
  --agree-tos \
  --email "$EMAIL" \
  -d "$DOMAIN"

# Copy certs to app directory
mkdir -p "$APP_DIR/nginx/certs"
cp /etc/letsencrypt/live/"$DOMAIN"/fullchain.pem "$APP_DIR/nginx/certs/cert.pem"
cp /etc/letsencrypt/live/"$DOMAIN"/privkey.pem   "$APP_DIR/nginx/certs/key.pem"
chmod 600 "$APP_DIR/nginx/certs/key.pem"

# Replace Nginx config with HTTPS version
cat > "$APP_DIR/nginx/nginx.conf" << NGINX
upstream backend  { server backend:8080; }
upstream ai_service { server ai-service:8001; }

# Redirect HTTP → HTTPS
server {
    listen 80;
    server_name $DOMAIN;
    return 301 https://\$host\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name $DOMAIN;

    ssl_certificate     /etc/nginx/certs/cert.pem;
    ssl_certificate_key /etc/nginx/certs/key.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript;

    location /api/ {
        proxy_pass         http://backend/api/;
        proxy_set_header   Host \$host;
        proxy_set_header   X-Real-IP \$remote_addr;
        proxy_set_header   X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto https;
        proxy_http_version 1.1;
        proxy_set_header   Upgrade \$http_upgrade;
        proxy_set_header   Connection "upgrade";
    }

    location /ai/ {
        proxy_pass       http://ai_service/;
        proxy_set_header Host \$host;
        proxy_read_timeout 120s;
    }

    location / {
        proxy_pass http://frontend:80;
        proxy_set_header Host \$host;
    }

    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN";
    add_header X-Content-Type-Options "nosniff";
}
NGINX

# Restart Nginx
docker compose start nginx

# Auto-renewal cron job
(crontab -l 2>/dev/null; echo "0 3 * * * certbot renew --quiet && cp /etc/letsencrypt/live/$DOMAIN/fullchain.pem $APP_DIR/nginx/certs/cert.pem && cp /etc/letsencrypt/live/$DOMAIN/privkey.pem $APP_DIR/nginx/certs/key.pem && docker compose -f $APP_DIR/docker-compose.yml restart nginx") | crontab -

echo "✅ SSL configured for https://$DOMAIN"
echo "   Auto-renewal: cron runs at 3am daily"
