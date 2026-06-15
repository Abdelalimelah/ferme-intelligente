#!/bin/bash
# ============================================================
#  PostgreSQL Backup Script
#  Cron: 0 2 * * * /opt/ferme-intelligente/deploy/backup.sh
# ============================================================
APP_DIR="/opt/ferme-intelligente"
BACKUP_DIR="$APP_DIR/backups"
DATE=$(date +%Y%m%d_%H%M%S)
KEEP_DAYS=7

mkdir -p "$BACKUP_DIR"

# Dump database from running container
docker compose -f "$APP_DIR/docker-compose.yml" exec -T postgres \
  pg_dump -U postgres ferme_intelligente | gzip > "$BACKUP_DIR/db_$DATE.sql.gz"

echo "✅ Backup saved: $BACKUP_DIR/db_$DATE.sql.gz"

# Delete backups older than KEEP_DAYS
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +$KEEP_DAYS -delete
echo "🧹 Deleted backups older than $KEEP_DAYS days"

# Optional: copy to S3
# aws s3 cp "$BACKUP_DIR/db_$DATE.sql.gz" s3://your-bucket/ferme-backups/

# Restore command (for reference):
# gunzip -c db_YYYYMMDD_HHMMSS.sql.gz | docker compose exec -T postgres psql -U postgres ferme_intelligente
