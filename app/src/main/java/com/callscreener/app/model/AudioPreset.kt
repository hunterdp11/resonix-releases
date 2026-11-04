package com.callscreener.app.model

data class AudioPreset(
    val id: String,
    val title: String,
    val textOrPath: String,            // Spoken phrase (TTS) or absolute file path to a recorded/imported WAV/MP3
    val isAudioFile: Boolean = false,  // true if points to an actual audio file on storage
    val iconEmoji: String = "",
    val category: String = "Quick Response", // "Quick Response", "Hindi / देसी", "Urgent", "Prank / Voice", "Custom"
    val durationSeconds: Int = 0
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            // Hindi / देसी Presets
            AudioPreset(
                id = "preset_hi_who",
                title = "नमस्ते, कौन बोल रहे हैं?",
                textOrPath = "नमस्ते, आप कौन बोल रहे हैं और किससे बात करनी है?",
                iconEmoji = "",
                category = "Hindi / देसी"
            ),
            AudioPreset(
                id = "preset_hi_courier",
                title = "पार्सल गेट पर रख दें",
                textOrPath = "भैया, पार्सल गेट पर रख दीजिए या पड़ोसी को दे दीजिए। धन्यवाद।",
                iconEmoji = "",
                category = "Hindi / देसी"
            ),
            AudioPreset(
                id = "preset_hi_driving",
                title = "गाड़ी चला रहा हूँ",
                textOrPath = "मैं अभी ड्राइविंग कर रहा हूँ, पहुँचकर खुद फोन करता हूँ।",
                iconEmoji = "",
                category = "Hindi / देसी"
            ),
            AudioPreset(
                id = "preset_hi_prank",
                title = "अरे जानू! कैसी हो? (Prank)",
                textOrPath = "अरे जानू! कैसी हो? मैं अभी तुम्हें ही याद कर रही थी!",
                iconEmoji = "",
                category = "Prank / Voice"
            ),
            AudioPreset(
                id = "preset_hi_hold",
                title = "एक सेकंड रुकिए",
                textOrPath = "एक सेकंड होल्ड कीजिए, फोन मत काटिएगा।",
                iconEmoji = "",
                category = "Hindi / देसी"
            ),
            AudioPreset(
                id = "preset_who",
                title = "Who's this?",
                textOrPath = "Hello, who is calling, please?",
                iconEmoji = "",
                category = "Quick Response"
            ),
            AudioPreset(
                id = "preset_busy",
                title = "Busy / Call Later",
                textOrPath = "I am currently busy. Please call back later or leave a text.",
                iconEmoji = "",
                category = "Quick Response"
            ),
            AudioPreset(
                id = "preset_hold",
                title = "Hold on a second",
                textOrPath = "Hold on a second, please don't hang up.",
                iconEmoji = "",
                category = "Quick Response"
            ),
            AudioPreset(
                id = "preset_driving",
                title = "I'm Driving",
                textOrPath = "I am currently driving. I will call you back once I park.",
                iconEmoji = "",
                category = "Quick Response"
            ),
            AudioPreset(
                id = "preset_record_notice",
                title = "Call Recorded Notice",
                textOrPath = "Notice: This call is being recorded for security and quality purposes.",
                iconEmoji = "",
                category = "Screening"
            ),
            AudioPreset(
                id = "preset_not_interested",
                title = "Not Interested",
                textOrPath = "I am not interested. Please remove my number from your call list. Thank you.",
                iconEmoji = "",
                category = "Screening"
            ),
            AudioPreset(
                id = "preset_yes",
                title = "Yes, absolutely",
                textOrPath = "Yes, absolutely. Please proceed.",
                iconEmoji = "",
                category = "Quick Response"
            ),
            AudioPreset(
                id = "preset_no",
                title = "No, that won't work",
                textOrPath = "No, unfortunately that won't work for me.",
                iconEmoji = "",
                category = "Quick Response"
            ),
            AudioPreset(
                id = "preset_prank_aria",
                title = "Hey sweetie!",
                textOrPath = "Hey! Who is this? I was literally just thinking about you!",
                iconEmoji = "",
                category = "Prank / Voice"
            ),
            AudioPreset(
                id = "preset_courier",
                title = "Leave with neighbor",
                textOrPath = "Please leave the parcel with my neighbor or at the doorstep. Thanks!",
                iconEmoji = "",
                category = "Quick Response"
            )
        )
    }
}
