#!/usr/bin/env bash
set -euo pipefail

ANDROID_CMDLINE_TOOLS_ZIP="${ANDROID_CMDLINE_TOOLS_ZIP:-commandlinetools-linux-13114758_latest.zip}"
ANDROID_API_LEVEL="${ANDROID_API_LEVEL:-35}"
ANDROID_BUILD_TOOLS="${ANDROID_BUILD_TOOLS:-35.0.0}"
ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-${HOME}/android-sdk}"

export ANDROID_SDK_ROOT
export ANDROID_HOME="${ANDROID_SDK_ROOT}"
export PATH="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/emulator:${PATH}"

as_root() {
  if [ "$(id -u)" -eq 0 ]; then
    "$@"
  elif command -v sudo >/dev/null 2>&1; then
    sudo "$@"
  else
    echo "Need root or sudo for: $*"
    exit 1
  fi
}

echo "Installing Linux packages..."
as_root apt-get update
as_root apt-get install -y --no-install-recommends wget unzip curl libglu1-mesa

echo "Preparing Android SDK root at ${ANDROID_SDK_ROOT}..."
as_root mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools"
if [ "$(id -u)" -ne 0 ]; then
  as_root chown -R "$(id -u):$(id -g)" "${ANDROID_SDK_ROOT}"
fi

tmp_zip="/tmp/cmdline-tools.zip"
echo "Downloading Android command-line tools..."
if command -v wget >/dev/null 2>&1; then
  wget -q "https://dl.google.com/android/repository/${ANDROID_CMDLINE_TOOLS_ZIP}" -O "${tmp_zip}"
else
  curl -fsSL "https://dl.google.com/android/repository/${ANDROID_CMDLINE_TOOLS_ZIP}" -o "${tmp_zip}"
fi

echo "Installing command-line tools..."
unzip -q -o "${tmp_zip}" -d "${ANDROID_SDK_ROOT}/cmdline-tools"
rm -f "${tmp_zip}"

if [ -d "${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools" ]; then
  rm -rf "${ANDROID_SDK_ROOT}/cmdline-tools/latest"
  mv "${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools" "${ANDROID_SDK_ROOT}/cmdline-tools/latest"
fi

if [ -d "${ANDROID_SDK_ROOT}/cmdline-tools/bin" ]; then
  mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools/latest"
  mv "${ANDROID_SDK_ROOT}/cmdline-tools/bin" "${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin"
fi

if [ -d "${ANDROID_SDK_ROOT}/cmdline-tools/lib" ]; then
  mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools/latest"
  mv "${ANDROID_SDK_ROOT}/cmdline-tools/lib" "${ANDROID_SDK_ROOT}/cmdline-tools/latest/lib"
fi

echo "Accepting licenses and installing SDK packages..."
yes | sdkmanager --licenses >/dev/null
sdkmanager --install \
  "platform-tools" \
  "platforms;android-${ANDROID_API_LEVEL}" \
  "build-tools;${ANDROID_BUILD_TOOLS}" \
  "emulator" \
  "system-images;android-${ANDROID_API_LEVEL};google_apis;x86_64"

cat <<EOF

Android SDK install complete.

Use this in each shell:
  export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT}"
  export ANDROID_HOME="\${ANDROID_SDK_ROOT}"
  export PATH="\${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:\${ANDROID_SDK_ROOT}/platform-tools:\${ANDROID_SDK_ROOT}/emulator:\${PATH}"

EOF
