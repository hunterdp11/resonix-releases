package com.callscreener.app.model

data class AudioPreset(
    val id: String,
    val title: String,
    val textOrPath: String,
    val isAudioFile: Boolean = false,
    val iconEmoji: String = "",
    val category: String = "Quick Response",
    val durationSeconds: Int = 0
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
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
            ),

            AudioPreset(
                id = "preset_hi_funny_pizza",
                title = "पिज़्ज़ा नहीं मिलता भाई! (Funny)",
                textOrPath = "अरे भाई साहब, गलत नंबर है! यहाँ कोई पिज़्ज़ा नहीं मिलता, नंबर ठीक से देखकर मिलाओ!",
                iconEmoji = "",
                category = "Prank / Voice"
            ),
            AudioPreset(
                id = "preset_hi_funny_lottery",
                title = "1 करोड़ की लॉटरी! (Prank)",
                textOrPath = "बधाई हो! आप 1 करोड़ की लॉटरी जीत चुके हैं! क्लेम करने के लिए अपना फोन जेब में रखें और सो जाएँ।",
                iconEmoji = "",
                category = "Prank / Voice"
            ),
            AudioPreset(
                id = "preset_hi_funny_bank",
                title = "बैंक लूट रहा हूँ, बाद में! (Prank)",
                textOrPath = "अरे भाई मैं अभी बैंक लूट रहा हूँ, पुलिस के सायरन बज रहे हैं! बाद में बात करता हूँ, फोन काटो!",
                iconEmoji = "",
                category = "Prank / Voice"
            ),
            AudioPreset(
                id = "preset_hi_funny_cid",
                title = "CID दया दरवाजा तोड़ दो! (Prank)",
                textOrPath = "हाँ जी CID स्पेशल सेल! दया, दरवाजा तोड़ दो, मुजरिम का फोन आ गया है!",
                iconEmoji = "",
                category = "Prank / Voice"
            ),
            AudioPreset(
                id = "preset_hi_funny_mummy",
                title = "मम्मी पास बैठी हैं! (Prank)",
                textOrPath = "अरे चुप रहो! मम्मी बिल्कुल बगल में बैठी हैं, अभी फोन मत करो, बाद में बात करती हूँ!",
                iconEmoji = "",
                category = "Prank / Voice"
            ),

            AudioPreset(
                id = "preset_en_meeting",
                title = "In a Meeting, Text Me",
                textOrPath = "I am currently in an important meeting. Please send me a text message and I will reply as soon as I am free.",
                iconEmoji = "",
                category = "English"
            ),
            AudioPreset(
                id = "preset_en_bad_reception",
                title = "Bad Reception / Text Me",
                textOrPath = "I have very weak cell reception on my end. Could you please text me instead?",
                iconEmoji = "",
                category = "English"
            ),
            AudioPreset(
                id = "preset_en_send_email",
                title = "Please Send an Email",
                textOrPath = "Could you please send that over in an email? I'll review it and get back to you shortly.",
                iconEmoji = "",
                category = "English"
            ),
            AudioPreset(
                id = "preset_en_gym",
                title = "At the Gym, Will Call",
                textOrPath = "Hey, I am currently at the gym. I will call you back once I finish my workout.",
                iconEmoji = "",
                category = "English"
            ),
            AudioPreset(
                id = "preset_en_wrong_person",
                title = "Wrong Person / Number",
                textOrPath = "I believe you have dialed the wrong person. Please verify the contact number before calling again.",
                iconEmoji = "",
                category = "English"
            ),

            AudioPreset(
                id = "preset_en_funny_alien",
                title = "Alien Hotline (Prank)",
                textOrPath = "Your call has been forwarded to the Alien Abduction Hotline. All extraterrestrial agents are busy. Please hold!",
                iconEmoji = "",
                category = "Prank / Voice"
            ),
            AudioPreset(
                id = "preset_en_funny_human",
                title = "Human Voicemail (Prank)",
                textOrPath = "Congratulations! You have reached my voicemail disguised as a live person. Speak after the beep! Beep!",
                iconEmoji = "",
                category = "Prank / Voice"
            ),
            AudioPreset(
                id = "preset_en_funny_cat",
                title = "Cat Negotiation (Prank)",
                textOrPath = "I can't talk right now. I am in the middle of a high-stakes peace treaty negotiation with my cat.",
                iconEmoji = "",
                category = "Prank / Voice"
            ),
            AudioPreset(
                id = "preset_en_funny_fbi",
                title = "FBI Cyber Unit (Prank)",
                textOrPath = "FBI Cyber Fraud Division. This connection is being monitored and recorded. State your name and badge number.",
                iconEmoji = "",
                category = "Prank / Voice"
            ),
            AudioPreset(
                id = "preset_en_funny_million",
                title = "Giving Me Millions? (Prank)",
                textOrPath = "If you are calling to give me a million dollars, press 1. If you want something from me, please hang up immediately.",
                iconEmoji = "",
                category = "Prank / Voice"
            )
        )
    }
}
