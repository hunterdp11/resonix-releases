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
            ),

            // Hindi Funny & Prank Presets
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

            // English Presets (Professional & Everyday)
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

            // English Funny & Prank Presets
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
            ),

            // Assamese / অসমীয়া Presets
            AudioPreset(
                id = "preset_as_who",
                title = "নমস্কাৰ, কোন কৈছে?",
                textOrPath = "নমস্কাৰ, আপুনি কোন কৈছে আৰু কাৰ লগত কথা পাতিব বিচাৰিছে?",
                iconEmoji = "",
                category = "Assamese / অসমীয়া"
            ),
            AudioPreset(
                id = "preset_as_busy",
                title = "মই ব্যস্ত আছোঁ (পাছত কৰক)",
                textOrPath = "বেয়া নাপাব, মই এতিয়া অকণমান ব্যস্ত আছোঁ। পাছত ফোন কৰিব পাৰিবনে?",
                iconEmoji = "",
                category = "Assamese / অসমীয়া"
            ),
            AudioPreset(
                id = "preset_as_delivery",
                title = "পাৰ্চেলটো দুৱাৰমুখত থওক",
                textOrPath = "নমস্কাৰ, যদি কোনো পাৰ্চেল বা কুৰিয়াৰ আছে, দুৱাৰমুখত বা ওচৰ-চুবুৰীয়াৰ তাত থৈ দিয়ক। ধন্যবাদ।",
                iconEmoji = "",
                category = "Assamese / অসমীয়া"
            ),
            AudioPreset(
                id = "preset_as_wrong_number",
                title = "ভুল নম্বৰত ফোন কৰিলে",
                textOrPath = "এইটো ভুল নম্বৰ হয়। অনুগ্ৰহ কৰি নম্বৰটো পৰীক্ষা কৰি সঠিক নম্বৰত ফোন কৰক।",
                iconEmoji = "",
                category = "Assamese / অসমীয়া"
            ),
            AudioPreset(
                id = "preset_as_hold",
                title = "এক চেকেণ্ড ৰ'ব",
                textOrPath = "এক চেকেণ্ড ৰ'ব, ফোনটো নাকাটিব।",
                iconEmoji = "",
                category = "Assamese / অসমীয়া"
            ),
            AudioPreset(
                id = "preset_as_driving",
                title = "গাড়ী চলাই আছোঁ",
                textOrPath = "মই এতিয়া গাড়ী চলাই আছোঁ, গৈ পাই নিজেই ফোন কৰিম।",
                iconEmoji = "",
                category = "Assamese / অসমীয়া"
            ),
            AudioPreset(
                id = "preset_as_not_interested",
                title = "প্ৰয়োজন নাই (ফোন নকৰিব)",
                textOrPath = "ধন্যবাদ, মোৰ কোনো প্ৰয়োজন নাই। অনুগ্ৰহ কৰি এই নম্বৰত আৰু ফোন নকৰিব।",
                iconEmoji = "",
                category = "Assamese / অসমীয়া"
            ),
            AudioPreset(
                id = "preset_as_funny",
                title = "মাছৰ বজাৰ নহয় ভাই! (Funny)",
                textOrPath = "আৰে ভাই, ভুল নম্বৰত ফোন কৰিলা! ইয়াত মাছৰ বজাৰ নহয়, ভালকৈ নম্বৰ চাই ফোন কৰা!",
                iconEmoji = "",
                category = "Assamese / অসমীয়া"
            )
        )
    }
}
