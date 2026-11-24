#!/usr/bin/env bash
# Скачивает ядро Xray (libv2ray.aar) в app/libs для локальной сборки.
set -euo pipefail
mkdir -p app/libs
curl -fL -o app/libs/libv2ray.aar \
  https://github.com/2dust/AndroidLibXrayLite/releases/latest/download/libv2ray.aar
echo "Готово: app/libs/libv2ray.aar"
