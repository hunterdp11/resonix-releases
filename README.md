# Call Screener AI (Direct System Audio Injection with Root)

An advanced Android call screening application built in **Kotlin** and **Jetpack Compose**. Equipped with **Root / Magisk System Priv-App** capabilities to unlock **100% silent, direct digital audio injection** into live cellular phone calls (identical to Google Pixel Call Screen and Xiaomi AI Call Assistant).

---

## ⚡ The Root Power Difference

| Feature | Standard Non-Root | **Root System Priv-App (New)** |
| :--- | :--- | :--- |
| **Audio Transmission Path** | Bounces audio through loudspeaker into mic | **Direct PCM byte stream into cellular modem uplink mixer** |
| **Room Noise / Annoyance** | Loud, blasts speaker into room | **100% SILENT in room. Phone speaker is OFF.** |
| **Caller Audio Quality** | Echoey, suppressed by hardware AEC | **Pristine, 16kHz digital studio quality** |
| **Permissions** | Basic user-space dialer | **`MODIFY_AUDIO_ROUTING` + `CAPTURE_AUDIO_OUTPUT` + `MODIFY_PHONE_STATE`** |

---

## How It Works

1. **System Priv-App Module:**  
   The app uses Magisk/KernelSU's `magic mount` to mount the APK into `/system/priv-app/CallScreener/CallScreener.apk` along with a privileged permissions whitelist in `/system/etc/permissions/privapp-permissions-com.callscreener.app.xml`.
2. **Direct Audio Injection (`RootCallAudioInjector`):**  
   When you type a response or an AI greeting triggers, the app synthesizes the speech to raw 16-bit PCM bytes and feeds them directly into an `AudioTrack` initialized with `USAGE_VOICE_COMMUNICATION`. Because the app holds `MODIFY_AUDIO_ROUTING`, Android routes these digital samples straight into the active cellular call uplink mixer.
3. **Zero Loudspeaker Usage:**  
   The loudspeaker stays off. The person sitting next to you hears nothing. The caller hears the natural female voice directly in their ear.

---

## Installation & Setup

### Step 1: Install APK
```powershell
cd d:\Projects_Android\CallScreener
.\run.ps1
```
*(Or transfer `app-debug.apk` directly to your phone and install it).*

### Step 2: Activate Root Priv-App Mode
1. Open **Call Screener AI**.
2. On the dashboard, look at the **"⚡ Root Priv-App Mode (Direct Audio)"** card.
3. Tap **"🚀 Activate Magisk / KernelSU Priv-App Module"** and grant Root (Superuser) permissions when prompted.
4. **Reboot your phone.**

Once rebooted, the card will display:
> **✅ System-Level Audio Injection: ACTIVE**  
> Running as OEM Privileged App. Zero loudspeaker noise — audio injects directly into cellular line!

---

## Natural Female Voice Selection
In the **Real-Time Voice Studio**, select:
* 👩 **Sarah (Female AI):** Calm, professional, natural female neural voice.
* 💼 **Emma (Executive):** Polite corporate receptionist.
* ✨ **Lily (Friendly):** Warm, youthful female voice.
