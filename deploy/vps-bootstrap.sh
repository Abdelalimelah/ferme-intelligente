#!/bin/bash
# ============================================================
#  VPS Bootstrap Script — Ferme Intelligente
#  Run once on a fresh Ubuntu 22.04 / Debian 12 server
#  as root: curl -fsSL <url>/vps-bootstrap.sh | bash
# ============================================================
set -e

APP_DIR="/opt/ferme-intelligente"
APP_USER="ferme"
REPO_URL="https://github.com/YOUR_USERNAME/ferme-intelligente"   # ← change this

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Ferme Intelligente — VPS Bootstrap"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ── 1. System update ─────────────────────────────────────────
apt-get update -q && apt-get upgrade -y -q

# ── 2. Docker ────────────────────────────────────────────────
if ! command -v docker &>/dev/null; then
  echo "→ Installing Docker..."
  curl -fsSL https://get.docker.com | sh
  systemctl enable --now docker
fi

# ── 3. Docker Compose plugin ─────────────────────────────────
if ! docker compose version &>/dev/null; then
  echo "→ Installing Docker Compose plugin..."
  apt-get install -y docker-compose-plugin
fi

# ── 4. Create app user ───────────────────────────────────────
if ! id "$APP_USER" &>/dev/null; then
  useradd -m -s /bin/bash "$APP_USER"
  usermod -aG docker "$APP_USER"
  echo "→ Created user: $APP_USER"
fi

# ── 5. Create app directory ──────────────────────────────────
mkdir -p "$APP_DIR"
chown "$APP_USER:$APP_USER" "$APP_DIR"

# ── 6. Generate SSH key for GitHub Actions deployment ─────────
SSH_KEY="$APP_DIR/.ssh/deploy_key"
mkdir -p "$APP_DIR/.ssh"
if [ ! -f "$SSH_KEY" ]; then
  ssh-keygen -t ed25519 -C "ferme-deploy" -f "$SSH_KEY" -N ""
  cat "$SSH_KEY.pub" >> /home/"$APP_USER"/.ssh/authorized_keys
  chmod 600 /home/"$APP_USER"/.ssh/authorized_keys
  echo ""
  echo "━━━━ COPY THIS PRIVATE KEY TO GITHUB SECRETS (VPS_SSH_KEY) ━━━━"
  cat "$SSH_KEY"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
fi

# ── 7. Firewall ──────────────────────────────────────────────
if command -v ufw &>/dev/null; then
  ufw allow ssh
  ufw allow 80/tcp
  ufw allow 443/tcp
  ufw --force enable
  echo "→ Firewall: ports 22, 80, 443 open"
fi

# ── 8. Clone / pull repo ─────────────────────────────────────
if [ ! -d "$APP_DIR/.git" ]; then
  sudo -u "$APP_USER" git clone "$REPO_URL" "$APP_DIR"
else
  sudo -u "$APP_USER" git -C "$APP_DIR" pull
fi

# ── 9. Create .env from template ─────────────────────────────
if [ ! -f "$APP_DIR/.env" ]; then
  cp "$APP_DIR/.env.example" "$APP_DIR/.env"
  echo ""
  echo "⚠️  Edit $APP_DIR/.env and set:"
  echo "   POSTGRES_PASSWORD=<strong-password>"
  echo "   JWT_SECRET=<random-64-char-string>"
  echo "   FRONTEND_URL=https://your-domain.com"
fi

# ── 10. First deploy ─────────────────────────────────────────
echo "→ Pulling images and starting services..."
cd "$APP_DIR"
docker compose pull
docker compose up -d

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  ✅ Bootstrap complete!"
echo "  App running at: http://$(curl -s ifconfig.me)"
echo ""
echo "  Next steps:"
echo "  1. Edit $APP_DIR/.env with your secrets"
echo "  2. Point your domain DNS to $(curl -s ifconfig.me)"
echo "  3. Add SSL: see deploy/ssl-setup.sh"
echo "  4. Add GitHub Secrets (see deploy/github-secrets.md)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
