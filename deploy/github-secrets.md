# GitHub Secrets Setup for CI/CD

Go to: `https://github.com/YOUR_USERNAME/ferme-intelligente/settings/secrets/actions`

## Required Secrets

| Secret Name | Value | How to get it |
|---|---|---|
| `VPS_HOST` | Your server IP or domain (e.g. `185.123.45.67`) | Your VPS provider dashboard |
| `VPS_USER` | SSH username (e.g. `ferme` or `root`) | Your VPS setup |
| `VPS_SSH_KEY` | Private SSH key (full content of `deploy_key`) | Output from `vps-bootstrap.sh` |

## Optional Secrets (for production hardening)

| Secret Name | Value |
|---|---|
| `JWT_SECRET` | Random 64-char string |
| `POSTGRES_PASSWORD` | Strong DB password |
| `SENTRY_DSN` | From sentry.io (error tracking) |

## How to generate a strong JWT secret

```bash
openssl rand -base64 64
```

## How to get the SSH private key

After running `vps-bootstrap.sh` on your server, it prints the private key.
Copy the full block including `-----BEGIN OPENSSH PRIVATE KEY-----` to `VPS_SSH_KEY`.

## How the CD pipeline uses them

```yaml
# .github/workflows/cd.yml
- uses: appleboy/ssh-action@v1
  with:
    host: ${{ secrets.VPS_HOST }}
    username: ${{ secrets.VPS_USER }}
    key: ${{ secrets.VPS_SSH_KEY }}
    script: |
      cd /opt/ferme-intelligente
      docker compose pull
      docker compose up -d --remove-orphans
      docker image prune -f
```

Every push to `main`:
1. CI runs: backend tests + frontend build + AI service check
2. CD runs: builds Docker images → pushes to GitHub Container Registry
3. CD SSHs into VPS → pulls new images → restarts containers

**Zero downtime:** `docker compose up -d` with `--remove-orphans` does a rolling
replace — old containers serve traffic until new ones are healthy.
