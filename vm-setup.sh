#!/bin/bash
# ── Vedu: Azure VM setup script ─────────────────────────────
# Run this ONCE on a fresh Azure VM (Ubuntu 22.04 LTS)
# Usage: bash vm-setup.sh

set -e

echo "=== 1. Installing Docker ==="
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

echo "=== 2. Installing Docker Compose plugin ==="
sudo apt-get install -y docker-compose-plugin

echo "=== 3. Opening firewall ports ==="
sudo ufw allow 22/tcp      # SSH (keep open!)
sudo ufw allow 80/tcp      # HTTP  (Caddy / Let's Encrypt challenge)
sudo ufw allow 443/tcp     # HTTPS (Caddy)
sudo ufw allow 7880/tcp    # LiveKit WebSocket (internal — can remove if only using Caddy proxy)
sudo ufw allow 7881/tcp    # LiveKit RTC TCP fallback
sudo ufw allow 3478/udp    # STUN/TURN
sudo ufw allow 1935/tcp    # RTMP Ingress

# UDP range for WebRTC media — single UFW range rule (faster than loop)
sudo ufw allow 50000:50050/udp

sudo ufw --force enable
sudo ufw status

echo "=== 4. Creating project directory ==="
mkdir -p ~/vedu

echo ""
echo "=== Done! Next steps: ==="
echo "  1. Copy your project files to ~/vedu (e.g. via scp or git clone)"
echo "  2. cd ~/vedu"
echo "  3. cp .env.example .env   # then edit .env with real values"
echo "  4. docker compose up --build -d"
echo "  5. docker compose ps      # verify all containers are healthy"
echo "  6. Visit https://vedulive.net2coder.in"
