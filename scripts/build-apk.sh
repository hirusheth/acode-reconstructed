#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MAIN_DIR="$ROOT_DIR/android/app/src/main"
CONTAINER_DIR="$MAIN_DIR/apk-container"
ASSETS_DIR="$MAIN_DIR/assets"
DIST_DIR="$ROOT_DIR/dist"
BUILD_TMP="$(mktemp -d)"
trap 'rm -rf "$BUILD_TMP"' EXIT

APK_NAME="acode-reconstructed-v1.13.2-debug.apk"
UNSIGNED_APK="$BUILD_TMP/unsigned.apk"
ALIGNED_APK="$BUILD_TMP/aligned.apk"
FINAL_APK="$DIST_DIR/$APK_NAME"

mkdir -p "$DIST_DIR"

# Locate Android SDK build-tools (zipalign, apksigner)
find_build_tool() {
  local tool="$1"
  if command -v "$tool" >/dev/null 2>&1; then
    command -v "$tool"
    return 0
  fi
  for sdk_dir in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" "/tmp/opencode/android-sdk" "$HOME/Android/Sdk" "/usr/lib/android-sdk"; do
    if [[ -n "$sdk_dir" && -d "$sdk_dir/build-tools" ]]; then
      local found
      found="$(find "$sdk_dir/build-tools" -name "$tool" -type f -o -name "$tool" -type l 2>/dev/null | sort -V | tail -n 1)"
      if [[ -n "$found" ]]; then
        echo "$found"
        return 0
      fi
    fi
  done
  return 1
}

ZIPALIGN="$(find_build_tool zipalign)" || { echo "ERROR: zipalign not found in PATH or Android SDK" >&2; exit 1; }
APKSIGNER="$(find_build_tool apksigner)" || { echo "ERROR: apksigner not found in PATH or Android SDK" >&2; exit 1; }

echo "[1/4] Assembling APK payload from android/app/src/main/{apk-container,assets}..."
python3 - "$CONTAINER_DIR" "$ASSETS_DIR" "$UNSIGNED_APK" <<'PY'
import os
import sys
import zipfile

container_dir, assets_dir, out_apk = sys.argv[1], sys.argv[2], sys.argv[3]

stored_list_file = os.path.join(container_dir, ".stored_entries.txt")
stored_entries = set()
if os.path.exists(stored_list_file):
    with open(stored_list_file, "r", encoding="utf-8") as f:
        stored_entries = {line.strip() for line in f if line.strip()}

def should_store(rel_path: str) -> bool:
    if rel_path in stored_entries:
        return True
    if rel_path == "resources.arsc" or rel_path.startswith("assets/dexopt/"):
        return True
    ext = os.path.splitext(rel_path)[1].lower()
    if ext in {".png", ".webp", ".jpg", ".jpeg", ".gif", ".mp3", ".ogg", ".wav", ".so"}:
        return True
    return False

with zipfile.ZipFile(out_apk, "w") as zf:
    # 1. Write container files (classes*.dex, resources.arsc, AndroidManifest.xml, res/, kotlin/, META-INF/, etc.)
    for root, _, files in os.walk(container_dir):
        for file in sorted(files):
            if file == ".stored_entries.txt":
                continue
            full_path = os.path.join(root, file)
            rel_path = os.path.relpath(full_path, container_dir).replace(os.sep, "/")
            compress_type = zipfile.ZIP_STORED if should_store(rel_path) else zipfile.ZIP_DEFLATED
            zf.write(full_path, rel_path, compress_type=compress_type, compresslevel=9 if compress_type == zipfile.ZIP_DEFLATED else None)

    # 2. Write live assets/ tree from android/app/src/main/assets/
    for root, _, files in os.walk(assets_dir):
        for file in sorted(files):
            full_path = os.path.join(root, file)
            rel_path = "assets/" + os.path.relpath(full_path, assets_dir).replace(os.sep, "/")
            compress_type = zipfile.ZIP_STORED if should_store(rel_path) else zipfile.ZIP_DEFLATED
            zf.write(full_path, rel_path, compress_type=compress_type, compresslevel=9 if compress_type == zipfile.ZIP_DEFLATED else None)
PY

echo "[2/4] Aligning APK on 4-byte boundaries with zipalign..."
"$ZIPALIGN" -p -f 4 "$UNSIGNED_APK" "$ALIGNED_APK"

KEYSTORE="$BUILD_TMP/debug.keystore"
echo "[3/4] Generating signing key & signing APK (v1 + v2 + v3 schemes)..."
keytool -genkeypair \
  -alias androiddebugkey \
  -keypass android \
  -keystore "$KEYSTORE" \
  -storepass android \
  -dname "CN=Android Debug,O=Android,C=US" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 >/dev/null 2>&1

"$APKSIGNER" sign \
  --ks "$KEYSTORE" \
  --ks-key-alias androiddebugkey \
  --ks-pass pass:android \
  --key-pass pass:android \
  --v1-signing-enabled true \
  --v2-signing-enabled true \
  --v3-signing-enabled true \
  --out "$FINAL_APK" \
  "$ALIGNED_APK"

echo "[4/4] Verifying signed APK & writing SHA-256 checksum..."
"$APKSIGNER" verify --verbose --print-certs "$FINAL_APK" | head -n 12
(cd "$DIST_DIR" && sha256sum "$APK_NAME" > "${APK_NAME}.sha256")
cat "${FINAL_APK}.sha256"
echo "Successfully built: $FINAL_APK"
