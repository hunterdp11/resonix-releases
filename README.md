<p align="center">
  <img src="assets/icon.png" width="96" height="96" alt="Resonix Icon" />
</p>

<h1 align="center">Resonix</h1>

<p align="center">
  <strong>Advanced Android Telephony Platform and Hardware Audio Screening Engine</strong>
</p>

<p align="center">
  <a href="https://android.com"><img src="https://img.shields.io/badge/Platform-Android%2012%2B%20(API%2031%2B)-3DDC84?logo=android&logoColor=white" alt="Platform" /></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Target%20SDK-Android%2017%20(API%2037)-34A853?logo=android&logoColor=white" alt="Target SDK" /></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-4285F4?logo=jetpackcompose&logoColor=white" alt="Compose" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License" /></a>
  <a href="https://github.com/hunterdp11/resonix-releases/releases"><img src="https://img.shields.io/github/v/release/hunterdp11/resonix-releases?color=orange&logo=github" alt="Release" /></a>
</p>

<p align="center">
  <a href="https://github.com/hunterdp11/resonix-releases/releases/download/v1.0.0/Resonix-v1.0.0-release.apk">
    <img src="https://img.shields.io/badge/Download-APK%20(v1.0.0)-2EAA55?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" />
  </a>
</p>

---

## Overview

Resonix is an open-source Android dialer and telephony audio routing platform built with Kotlin and Jetpack Compose. It operates in two tiers: a standard Android Telecom dialer for unrooted devices, and a root-privileged hardware ALSA audio injection engine for Magisk, KernelSU, and APatch environments.

---

## Visual Interface

<p align="center">
  <img src="screenshots/1.PNG" width="32%" alt="Incoming Call Screen" />
  <img src="screenshots/2.PNG" width="32%" alt="In-Call Controls Suite" />
  <img src="screenshots/3.PNG" width="32%" alt="Live AI Screening Studio" />
</p>
<p align="center">
  <img src="screenshots/4.PNG" width="32%" alt="Contact Details & Rules" />
  <img src="screenshots/5.PNG" width="32%" alt="Smart T9 Keypad & Dialer" />
  <img src="screenshots/6.PNG" width="32%" alt="Telephony Settings & Theme" />
</p>

---

## Powerful Features

### 1. Root-Privileged Hardware Audio Engine (Magisk / KernelSU / APatch)

When superuser access is granted, Resonix installs an integrated system module that mounts the application into `/system/priv-app/Resonix/` with system-level privileged permissions:

* **Direct Baseband Uplink Injection:**
  * Uses privileged `MODIFY_AUDIO_ROUTING` and `CAPTURE_AUDIO_OUTPUT` system permissions.
  * Streams 16-bit 16kHz linear PCM digital audio directly into the cellular modem uplink ALSA mixer.
* **System Audio and Media Injection into Active Calls:**
  * Streams system audio, media files, and soundboards directly into the active voice call transmission line without loudspeaker bleed.
* **Zero Room Noise / Silent Screening:**
  * Synthesized voice and audio playback do not play through the physical loudspeaker.
  * Audio is injected directly into the voice line, keeping screening sessions silent to nearby listeners.
* **Hardware Noise Suppression and Echo Cancellation:**
  * Fluence dual-mic tuning and hardware acoustic echo cancellation (AEC) and noise suppression (NS).
  * Prevents downlink-to-uplink loopback, ensuring crystal-clear, noise-free calls without background hiss or static.
* **Lossless Two-Way Call Recording:**
  * Direct digital audio capture from cellular uplink and downlink streams without microphone speaker bleed or distortion.
* **Dual-Engine Speech Jammer & Echo Chamber:**
  * **Delayed Auditory Feedback (DAF):** Configurable millisecond speech collision (100ms - 350ms) to disrupt telemarketers and robocallers.
  * **Infinite Echo Chamber:** Multi-tap recursive acoustic reflections with adjustable decay and warm low-pass damping.
  * **Two-Way Echo:** Optional microphone loopback for acoustic reflection testing.
* **Interactive AI Call Screening and Relay:**
  * Real-time on-device speech-to-text transcript feed with conversational message bubbles.
  * Instant contextual quick replies for immediate responses with a single tap.
  * Custom text relay: Type any message to speak it directly to the caller via synthetic voice.
  * Multiple built-in neural voice personas (Sarah, Emma, Lily).
* **Silent One-Tap OTA Updates:**
  * Background installation of newer releases directly from GitHub without system package installer prompts.

---

### 2. Standard Telephony Suite (Non-Root / All Android 8.0+ Devices)

Standard Mode requires zero modifications or root access and functions as your primary system Phone app:

* **Default Phone Dialer:** Complete Android Telecom `InCallService` integration managing all incoming and outgoing cellular calls.
* **Multi-Party Call Management:**
  * Add call capability.
  * Carrier-verified conference merging.
  * Call swapping between active and held lines.
* **Call Log Management:** Filterable call history by All and Missed calls, multi-select deletion, and speed dial favorites with hold-to-delete.
* **Contact Directory:** Fast T9 contact search and alphabetical index scroller.
* **Dynamic Call Capsule:** System-wide floating overlay pill for call management and status display while using other applications.
* **Local Spam Protection:** On-device database for blocking unwanted numbers and filtering incoming call sessions.
* **Integrated GitHub OTA Updates:** In-app release checker with direct download and installation prompts.

---

## Feature Comparison Matrix

| Capability | Standard Mode (Non-Root) | Root-Enhanced Mode |
| :--- | :--- | :--- |
| **Audio Transmission Path** | Standard Telephony Audio | Direct PCM Stream into Modem Uplink Mixer |
| **Room Audio Leakage** | Loudspeaker / Earpiece output | Completely Silent (Direct Line Injection) |
| **System Audio / Media to Call** | Not Available | Supported (Lossless 16-bit 16kHz PCM) |
| **Two-Way Call Recording** | Standard microphone capture | Direct Uplink & Downlink Digital Stream |
| **Hardware Echo & Noise Suppression** | OS Default | Hardware Fluence Dual-Mic & ALSA Tuned AEC |
| **Speech Jammer & Echo Chamber** | Not Available | Supported (DAF Engine + Multi-Tap Reflections) |
| **Live AI Screening & Voice Relay** | Not Available | Supported (Direct Uplink Synthesizer) |
| **OTA Module Installation** | System Package Installer | Silent 1-Tap Background Install |

---

## Architecture and Technical Specifications

* **Target SDK:** Android 17 (API 37)
* **Minimum SDK:** Android 12 (API 31)
* **Language:** Kotlin with Coroutines and StateFlow for reactive state management.
* **UI Toolkit:** Jetpack Compose with Material 3.
* **Telephony Services:**
  * Android Telecom `InCallService` for call session lifecycle.
  * Android `CallScreeningService` for spam filtering and verification.
* **Audio Engineering:**
  * Direct 16-bit 16kHz linear PCM digital audio injection.
  * ALSA hardware mixer tuning for Qualcomm, MediaTek, and Tensor modems.
* **Root Integration:**
  * Magisk / KernelSU / APatch module generator (`RootPrivilegeManager`).
  * Privileged permission whitelist configuration (`privapp-permissions-com.callscreener.app.xml`).

---

## Installation

### Method 1: Direct Download (Pre-built Signed APK)
Download and install the official release binary directly:
* **Direct APK Download:** [Resonix-v1.0.0-release.apk](https://github.com/hunterdp11/resonix-releases/releases/download/v1.0.0/Resonix-v1.0.0-release.apk) (2.34 MB)
* All Releases: [GitHub Releases](https://github.com/hunterdp11/resonix-releases/releases)

### Method 2: Build from Source
```bash
# Clone the repository
git clone https://github.com/hunterdp11/resonix-releases.git
cd resonix-releases

# Build the release APK
./gradlew assembleRelease
```
The resulting APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

---

## Activating Root Mode (Optional)

If your device is rooted with Magisk, KernelSU, or APatch:

1. Install and open **Resonix**, then set it as your default Phone app.
2. On the main dashboard, locate the **Root Priv-App Mode** card.
3. Tap **Activate Magisk / KernelSU Priv-App Module** and grant Superuser permissions when prompted.
4. **Reboot your device.**
5. After rebooting, Resonix will detect the active system module and unlock direct hardware audio injection, call recording, and screening capabilities.

To return to Standard Mode at any time, tap **Uninstall Module** in Settings and reboot.

---

## License

Resonix is open-source software licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for complete license terms.

---

## Author

* **DpXarma** ([@hunterdp11](https://github.com/hunterdp11))
