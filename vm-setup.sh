#!/bin/bash
# ── Vedu: Azure VM setup script ─────────────────────────────
# Run this ONCE on a fresh Azure VM (Ubuntu 22.04)

set -e

echo "=== 1. Installing Docker ==="
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker

echo "=== 2. Installing Docker Compose plugin ==="
sudo apt-get install -y docker-compose-plugin

echo "=== 3. Opening firewall ports ==="
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 7880/tcp
sudo ufw allow 7881/tcp
sudo ufw allow 3478/udp
sudo ufw allow 1935/tcp
# UDP range for WebRTC media
for port in $(seq 50000 50050); do
  sudo ufw allow ${port}/udp
done
sudo ufw --force enable

echo "=== Done! Now copy your project files and run: ==="
echo "  cd ~/vedu && docker compose up --build -d"
