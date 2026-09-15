# JARVIS - Personal AI Voice Assistant

JARVIS is a futuristic, production-structured Android AI voice assistant built with modern technologies. It allows users to control their phone using natural language in Hindi, Hinglish, and English.

## Features

- **Futuristic UI**: Premium dark theme with an animated AI orb representing different states (Listening, Processing, Speaking).
- **Natural Language Command Engine**: Powered by Gemini AI to convert voice/text into structured action plans.
- **Voice Interaction**: Integrated Android SpeechRecognizer and Text-to-Speech (TTS).
- **Automation Service**: Built-in Accessibility Service for user-authorized UI assistance.
- **Secure Action System**: Strict validation of AI commands and confirmation dialogs for sensitive actions (Calls, SMS).
- **Command History**: Local history storage using Room database.
- **Customizable Settings**: Control voice settings, language, speech speed, and pitch.

## Technology Stack

- **Kotlin**: 100% Kotlin codebase.
- **Jetpack Compose**: Modern declarative UI with Material 3.
- **Gemini API**: Advanced natural language understanding.
- **Room Database**: Local persistence for command history.
- **DataStore**: Secure storage for user preferences.
- **Coroutines & Flow**: Reactive and asynchronous programming.
- **Retrofit & kotlinx-serialization**: Type-safe networking and JSON parsing.

## Project Setup

### 1. Gemini API Configuration
To enable the AI command engine:
1. Get a Gemini API key from [Google AI Studio](https://aistudio.google.com/).
2. In the AI Studio project, open the **Secrets** panel.
3. Add a new secret named `GEMINI_API_KEY` and paste your key.
4. JARVIS uses the Secrets Gradle Plugin to inject this key into `BuildConfig` securely.

### 2. Accessibility Service Setup
For JARVIS to perform UI actions (like clicking or scrolling):
1. Open Android **Settings**.
2. Navigate to **Accessibility**.
3. Find **JARVIS** and toggle it **ON**.
4. Grant the requested permissions.

### 3. Permissions
The app requires the following permissions:
- `INTERNET`: For Gemini AI calls and web searches.
- `RECORD_AUDIO`: For voice command recognition.

## Architecture

The project follows Clean Architecture principles:
- `ui/`: Compose screens, ViewModels, and animated components.
- `ai/`: Gemini API client and natural language command parser.
- `actions/`: Intent-based action execution logic.
- `automation/`: Accessibility service implementation.
- `voice/`: Managers for Speech Recognition and TTS.
- `data/`: Room database entities, DAOs, and settings repository.
- `security/`: (Integrated in Parser/Executor) Validation of AI outputs.

## Security Notes

- **Action Allowlist**: JARVIS only executes a predefined set of actions. It never runs arbitrary shell commands.
- **JSON Validation**: All outputs from Gemini are validated against a strict schema before execution.
- **Sensitive Actions**: Actions like `MAKE_PHONE_CALL` or `SEND_SMS` require explicit user confirmation via a dialog.
- **Local Privacy**: Command history and settings are stored locally on the device.

## Known Limitations

- **Android Restrictions**: JARVIS respects all Android system security boundaries. It cannot bypass lock screens or perform actions in apps that explicitly block accessibility services.
- **Internet Dependency**: Voice recognition (online mode) and Gemini AI require an active internet connection.

## Building & Sideloading the App

### Option A: Using the Automated Script
A ready-to-use script `build_signed_apk.sh` is provided in the root directory.

1. **Build a signed Debug APK** (ready for instant sideloading without needing keys):
   ```bash
   ./build_signed_apk.sh debug
   ```
2. **Build a signed Release APK** (auto-generates or uses a release keystore):
   ```bash
   ./build_signed_apk.sh release
   ```

### Option B: Manual Command-Line Build

#### 1. Debug APK (Already Signed with standard debug keystore)
Android debug APKs are automatically signed with `debug.keystore` and can be installed on any physical Android device:
```bash
./gradlew :app:assembleDebug
```
The resulting APK is at:
`app/build/outputs/apk/debug/app-debug.apk` (or `/.build-outputs/app-debug.apk` in AI Studio).

#### 2. Release APK with Custom Keystore
1. Generate your release keystore (one-time setup):
   ```bash
   keytool -genkeypair -v -keystore my-upload-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000 -storepass your_password -keypass your_password
   ```
2. Set environment variables:
   ```bash
   export KEYSTORE_PATH="$(pwd)/my-upload-key.jks"
   export STORE_PASSWORD="your_password"
   export KEY_PASSWORD="your_password"
   ```
3. Assemble the release APK:
   ```bash
   ./gradlew :app:assembleRelease
   ```
The signed APK will be output to:
`app/build/outputs/apk/release/app-release.apk`

---

## Installing (Sideloading) onto Your Physical Android Device

### Method 1: ADB (Recommended for Developers)
1. Enable **Developer Options** and **USB Debugging** on your phone:
   - Go to **Settings > About phone** and tap **Build number** 7 times.
   - Go to **Settings > System > Developer options** and turn on **USB debugging**.
2. Connect your phone to your computer via USB.
3. Verify connection:
   ```bash
   adb devices
   ```
4. Install the APK:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Method 2: Direct File Transfer (No PC/ADB needed)
1. Transfer the `.apk` file to your phone via:
   - Google Drive, Telegram, email attachment, or direct USB file transfer.
2. On your phone, open the **Files** / **My Files** app and tap the `.apk`.
3. If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings** in the popup.
   - Toggle **Allow from this source** to **ON**.
4. Tap **Install** and then **Open**.

---
*Created by JARVIS Team - Inspired by the future.*
