# Resonix 📞✨

> **Next-Generation Android Dialer & Smart Call Screening System with Material You Expressive Design.**

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?logo=android&logoColor=white)](https://android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-Android%2014%20(API%2034)-34A853?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%20100%25-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Release](https://img.shields.io/github/v/release/hunterdp11/resonix-releases?color=orange&logo=github)](https://github.com/hunterdp11/resonix-releases/releases)

---

## 🌟 Overview

**Resonix** is a modern, open-source Android dialer and call screening platform crafted entirely in **Kotlin** and **Jetpack Compose**. Inspired by Google Pixel's Call Screen and Xiaomi AI Call Assistant, Resonix combines an expressive Material You design with an advanced dual-engine architecture — delivering a sleek default dialer experience for everyday use and an optional root-enhanced direct hardware audio injection engine.

---

## ✨ Key Features

### 🎨 Material You Expressive Design
* **Dynamic Monet Theming:** Automatically adapts to wallpaper colors with dark and light palette matching.
* **Ambient Liquid Backdrop:** Flowing ambient color animations during ringing and active call states.
* **Alphabetical Contacts Quick-Scroller:** High-speed alphabetical index slider with smooth tactile scroll jump.
* **Speed Dial & Call History:** Dual-tab call log with spring-animated toggle between All and Missed calls.
* **Dynamic Call Capsule:** Floating system-wide dynamic island capsule overlay for effortless multitasking during active calls.

### 🤖 Smart Live Call Screening
* **Live Call Transcription:** Real-time speech-to-text transcript feed with conversational bubble UI.
* **Instant Quick Replies:** One-tap contextual automated responses ("Who is this?", "Call back later", "Can I take a message?").
* **Live AI Speech Relay:** Type custom replies that are synthesized in real-time into natural speech directly to the caller.
* **Multi-Persona Voice Studio:** Select from multiple neural voice personas:
  * 👩 **Sarah:** Calm, professional neural assistant.
  * 💼 **Emma:** Corporate executive receptionist.
  * ✨ **Lily:** Warm, friendly natural conversationalist.

### ⚡ Dual Engine Architecture
Resonix adapts automatically to your device's environment:

| Feature | Standard Non-Root Mode | Root-Enhanced Priv-App Mode |
| :--- | :--- | :--- |
| **Requirements** | Any Android 8.0+ device | Magisk / KernelSU / APatch |
| **Audio Path** | Standard Android Telephony audio | Direct 16-bit 16kHz PCM digital uplink injection |
| **Room Audio Leakage** | Normal speakerphone output | **100% SILENT in room. Loudspeaker is OFF.** |
| **Caller Audio Quality** | Standard microphone pickup | Pristine studio-quality digital injection |
| **Privileged Permissions** | Standard user-space dialer permissions | `MODIFY_AUDIO_ROUTING`, `CAPTURE_AUDIO_OUTPUT`, `MODIFY_PHONE_STATE` |

### 🛡️ Privacy, Security & Call Management
* **Local Number Blocking:** Native database to identify, silence, and reject unwanted callers and spam numbers.
* **Speech Jammer Tool:** Configurable audio latency tool to deter aggressive telemarketers.
* **Call Recording Engine:** Built-in audio recording capabilities for important conversations.
* **Full In-Call Telecom Suite:** Conference call merge and swap, hold/resume, microphone mute, speakerphone toggle, and DTMF dialpad.

### 🔄 Built-in GitHub OTA Engine
* Integrated in-app updater connected directly to GitHub Releases.
* Background update check and progress tracking.
* **1-Tap Silent Update:** Instant seamless background installation on rooted devices without user prompts.
* Standard system package installer fallback for non-root users.

---

## 🛠️ Architecture & Tech Stack

Resonix is built using modern Android architecture components:

* **Language:** 100% Kotlin with Coroutines & StateFlow.
* **UI Toolkit:** Jetpack Compose + Material 3 with custom canvas animations and Monet dynamic theming.
* **Telephony & Telecom:**
  * Android Telecom Framework (`InCallService`) for complete call lifecycle management.
  * Android `CallScreeningService` for spam filtering and automated screening.
* **Audio Engineering:**
  * `AudioTrack` initialized with `STREAM_VOICE_CALL` / `USAGE_VOICE_COMMUNICATION`.
  * Direct 16-bit 16kHz linear PCM streaming into active cellular uplink hardware mixer.
* **System Integration:**
  * Magisk / KernelSU magic-mount module generator (`RootPrivilegeManager`).
  * Privapp permissions whitelist configuration generator.

---

## 📥 Installation

### Option 1: Direct APK Download
Grab the latest signed release APK from the [Releases Page](https://github.com/hunterdp11/resonix-releases/releases/latest).

### Option 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/hunterdp11/resonix-releases.git
cd resonix-releases

# Build Release APK
./gradlew assembleRelease
```
The compiled APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

---

## 🚀 Activating Root Priv-App Mode (Optional)

If your device is rooted with **Magisk**, **KernelSU**, or **APatch**:

1. Launch **Resonix** and set it as your default Phone app.
2. On the dashboard, navigate to the **Root Priv-App Mode** card.
3. Tap **"Activate Magisk / KernelSU Priv-App Module"** and grant Superuser access.
4. **Reboot your phone.**
5. Once rebooted, Resonix will automatically detect the system module and display:
   > **System-Level Audio Injection: ACTIVE**  
   > Zero room noise — audio synthesizes directly into the live cellular voice line.

---

## 🤝 Contributing

Contributions, bug reports, and feature requests are welcome!
1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your Changes (`git commit -m 'feat: add AmazingFeature'`).
4. Push to the Branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

## 📄 License

Resonix is open-source software licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.

---

## 👤 Author

* **DpXarma** ([@hunterdp11](https://github.com/hunterdp11))
