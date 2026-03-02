#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(pwd)"
GRADLE_TASKS="${GRADLE_TASKS:-clean assembleDebug testDebugUnitTest lintDebug}"
WRITE_LOCAL_PROPERTIES="${WRITE_LOCAL_PROPERTIES:-auto}"
RUN_ANDROID_TESTS="${RUN_ANDROID_TESTS:-0}"
ANDROID_SDK_ROOT_INPUT="${ANDROID_SDK_ROOT:-}"

if [ -z "${ANDROID_SDK_ROOT_INPUT}" ]; then
  ANDROID_SDK_ROOT_INPUT="$(printenv 'ANDROID-SDK-ROOT' 2>/dev/null || true)"
fi

while [ $# -gt 0 ]; do
  case "$1" in
    --project-dir)
      PROJECT_DIR="$2"
      shift 2
      ;;
    --gradle-tasks)
      GRADLE_TASKS="$2"
      shift 2
      ;;
    --write-local-properties)
      WRITE_LOCAL_PROPERTIES="$2"
      shift 2
      ;;
    --run-android-tests)
      RUN_ANDROID_TESTS="$2"
      shift 2
      ;;
    --android-sdk-root)
      ANDROID_SDK_ROOT_INPUT="$2"
      shift 2
      ;;
    -*)
      echo "Unknown option: $1"
      exit 1
      ;;
    *)
      PROJECT_DIR="$1"
      shift
      ;;
  esac
done

if [ ! -d "${PROJECT_DIR}" ]; then
  echo "Project directory not found: ${PROJECT_DIR}"
  exit 1
fi

if [ ! -f "${PROJECT_DIR}/gradlew" ]; then
  echo "gradlew not found in ${PROJECT_DIR}"
  exit 1
fi

export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT_INPUT:-${ANDROID_HOME:-${HOME}/android-sdk}}"
export ANDROID_HOME="${ANDROID_SDK_ROOT}"
export PATH="${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/emulator:${PATH}"

if [ "${WRITE_LOCAL_PROPERTIES}" = "auto" ]; then
  if [[ "${PROJECT_DIR}" == /workspace/* ]]; then
    WRITE_LOCAL_PROPERTIES=1
  else
    WRITE_LOCAL_PROPERTIES=0
  fi
fi

if [ "${WRITE_LOCAL_PROPERTIES}" = "1" ]; then
  printf "sdk.dir=%s\n" "${ANDROID_SDK_ROOT}" > "${PROJECT_DIR}/local.properties"
  echo "Wrote ${PROJECT_DIR}/local.properties with sdk.dir=${ANDROID_SDK_ROOT}"
fi

echo "== Tool versions =="
java -version || true
adb version || true
sdkmanager --version || true

echo
echo "== Gradle wrapper version =="
cd "${PROJECT_DIR}"
chmod +x ./gradlew
./gradlew --version

echo
echo "== Running Gradle tasks =="
./gradlew ${GRADLE_TASKS}

if [ "${RUN_ANDROID_TESTS}" = "1" ]; then
  echo
  echo "== Running connected Android tests =="
  ./gradlew connectedDebugAndroidTest
fi

echo
echo "Verification complete."
