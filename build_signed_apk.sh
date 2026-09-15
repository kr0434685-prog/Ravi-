#!/usr/bin/env bash
# ==============================================================================
# JARVIS Android Assistant - Signed APK Generation Script
#
# Generates a signed APK (Debug or Release) ready for manual sideloading
# onto a physical Android device.
# ==============================================================================

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

MODE="${1:-debug}"

echo "=================================================="
echo "  JARVIS APK Build & Sign Tool"
echo "  Target Mode: ${MODE}"
echo "=================================================="

# Check for Java installation
if ! command -v keytool &> /dev/null; then
    echo "Warning: keytool not found in PATH. Ensure JDK is installed if creating a keystore."
fi

GRADLE_CMD="./gradlew"
if [ ! -f "$GRADLE_CMD" ]; then
    if command -v gradle &> /dev/null; then
        GRADLE_CMD="gradle"
    else
        echo "Error: Neither ./gradlew nor gradle was found in PATH."
        exit 1
    fi
fi

if [ "$MODE" == "debug" ]; then
    echo ">> Building signed Debug APK (signed with debug.keystore)..."
    $GRADLE_CMD :app:assembleDebug

    APK_PATH="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
    if [ ! -f "$APK_PATH" ]; then
        # Check alternative output location
        APK_PATH="$ROOT_DIR/.build-outputs/app-debug.apk"
    fi

    echo ""
    echo "=================================================="
    echo "  SUCCESS: Signed Debug APK generated!"
    echo "  Location: $APK_PATH"
    echo "=================================================="
    echo ""
    echo "To install on your connected device via ADB:"
    echo "  adb install -r \"$APK_PATH\""
    echo ""
    echo "To manually sideload via USB/Drive:"
    echo "  1. Copy the .apk file to your Android phone."
    echo "  2. Open Files app on your phone and tap the APK."
    echo "  3. Allow 'Install unknown apps' if prompted."
    echo "=================================================="

elif [ "$MODE" == "release" ]; then
    KEYSTORE_FILE="${KEYSTORE_PATH:-$ROOT_DIR/my-upload-key.jks}"
    ALIAS="upload"
    PASSWORD="${STORE_PASSWORD:-jarvis12345}"

    export KEYSTORE_PATH="$KEYSTORE_FILE"
    export STORE_PASSWORD="$PASSWORD"
    export KEY_PASSWORD="$PASSWORD"

    if [ ! -f "$KEYSTORE_FILE" ]; then
        echo ">> Keystore not found at $KEYSTORE_FILE."
        echo ">> Generating a new signing keystore..."
        keytool -genkeypair \
            -v \
            -keystore "$KEYSTORE_FILE" \
            -alias "$ALIAS" \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -storepass "$PASSWORD" \
            -keypass "$PASSWORD" \
            -dname "CN=JARVIS Assistant, OU=AI, O=Assistant, L=City, S=State, C=US"
        echo ">> Keystore created successfully at: $KEYSTORE_FILE"
    else
        echo ">> Using existing keystore: $KEYSTORE_FILE"
    fi

    echo ">> Building signed Release APK..."
    $GRADLE_CMD :app:assembleRelease

    APK_PATH="$ROOT_DIR/app/build/outputs/apk/release/app-release-unsigned.apk"
    SIGNED_APK="$ROOT_DIR/app/build/outputs/apk/release/app-release.apk"

    if [ -f "$SIGNED_APK" ]; then
        FINAL_APK="$SIGNED_APK"
    else
        FINAL_APK="$APK_PATH"
    fi

    echo ""
    echo "=================================================="
    echo "  SUCCESS: Signed Release APK generated!"
    echo "  Location: $FINAL_APK"
    echo "=================================================="
    echo ""
    echo "To install on your connected device via ADB:"
    echo "  adb install -r \"$FINAL_APK\""
    echo ""
    echo "To manually sideload via USB/Drive:"
    echo "  1. Copy the .apk file to your Android phone."
    echo "  2. Open Files app on your phone and tap the APK."
    echo "  3. Allow 'Install unknown apps' if prompted."
    echo "=================================================="
else
    echo "Unknown mode: $MODE"
    echo "Usage: ./build_signed_apk.sh [debug|release]"
    exit 1
fi
