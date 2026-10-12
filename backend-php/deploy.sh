#!/usr/bin/env bash
# Sinkronkan backend lokal (folder ini) -> produksi.
# Meng-upload file backend (config, public, database, v1, .htaccess, index.html, README.md, .env.example)
# ke DEPLOY_PATH di server. TIDAK menyentuh .env, media/, storage/ isi, atau file *.bak.*.
#
# Pemakaian (kredensial lewat environment, JANGAN disimpan di repo):
#   DEPLOY_HOST=100.127.234.79 \
#   DEPLOY_USER=kmzway000 \
#   DEPLOY_PASS='***' \
#   DEPLOY_PATH=/www/wwwroot/kostku.miraya.my.id \
#   DEPLOY_HOSTKEY='SHA256:...' \
#   PLINK=/path/to/plink.exe \
#   ./deploy.sh
#
# Catatan: plink untuk autentikasi password (ssh biasa tidak bisa non-interaktif).
set -euo pipefail

: "${DEPLOY_HOST:?DEPLOY_HOST wajib diisi}"
: "${DEPLOY_USER:?DEPLOY_USER wajib diisi}"
: "${DEPLOY_PASS:?DEPLOY_PASS wajib diisi}"
: "${DEPLOY_PATH:?DEPLOY_PATH wajib diisi}"
: "${DEPLOY_HOSTKEY:?DEPLOY_HOSTKEY wajib diisi}"
PLINK="${PLINK:-plink}"
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

rsh() { "$PLINK" -ssh "${DEPLOY_USER}@${DEPLOY_HOST}" -pw "${DEPLOY_PASS}" -batch -hostkey "${DEPLOY_HOSTKEY}" "$@"; }
up()  { "$PLINK" -ssh "${DEPLOY_USER}@${DEPLOY_HOST}" -pw "${DEPLOY_PASS}" -batch -hostkey "${DEPLOY_HOSTKEY}" "cat > '${DEPLOY_PATH}/$1'" < "${HERE}/$1"; }

TS="$(date +%Y%m%d_%H%M%S)"
echo "== Backup backend di server =="
rsh "cd '${DEPLOY_PATH}' && tar czf /tmp/kostku_backend_${TS}.tgz config public database v1 index.html .htaccess 2>/dev/null; echo backup=/tmp/kostku_backend_${TS}.tgz"

echo "== Upload file lokal -> server =="
( cd "${HERE}" && find config public database v1 -type f ! -name '*.bak.*'; echo .htaccess; echo index.html; echo README.md; echo .env.example ) \
  | while IFS= read -r f; do
      [ -f "${HERE}/${f}" ] || continue
      up "$f"
      echo "  uploaded ${f}"
    done

echo "== Verifikasi md5 (server) =="
rsh "cd '${DEPLOY_PATH}' && md5sum config/bootstrap.php public/index.php database/schema.sql"
echo "== Verifikasi md5 (lokal) =="
( cd "${HERE}" && md5sum config/bootstrap.php public/index.php database/schema.sql )
echo "Selesai."
