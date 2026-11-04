package com.callscreener.app.model

data class QuickReply(
    val id: String,
    val title: String,
    val spokenText: String,
    val isHindi: Boolean = false
) {
    companion object {
        // Industry-Ready Native Hindi & Hinglish Quick Replies
        val HINDI_REPLIES = listOf(
            QuickReply(
                id = "hi_who",
                title = "Kaun bol rahe hain? (कौन हैं?)",
                spokenText = "नमस्ते, आप कौन बोल रहे हैं और आपको किससे बात करनी है?",
                isHindi = true
            ),
            QuickReply(
                id = "hi_reason",
                title = "Kyun call kiya? (काम क्या है?)",
                spokenText = "कृपया बताइए आपने किस सिलसिले में कॉल किया है?",
                isHindi = true
            ),
            QuickReply(
                id = "hi_busy",
                title = "Main busy hoon (व्यस्त हूँ)",
                spokenText = "माफ़ कीजिए, मैं अभी थोड़ा व्यस्त हूँ। क्या आप बाद में कॉल कर सकते हैं?",
                isHindi = true
            ),
            QuickReply(
                id = "hi_message",
                title = "Message chhod dijiye (संदेश)",
                spokenText = "मैं अभी बात नहीं कर पाऊंगा, आप अपना संदेश बोल दीजिए, मैं बाद में वापस कॉल करूँगा।",
                isHindi = true
            ),
            QuickReply(
                id = "hi_delivery",
                title = "Parcel / Delivery? (डिलीवरी)",
                spokenText = "नमस्ते, क्या यह कोई डिलीवरी या कूरियर है? कृपया पार्सल दरवाज़े पर रख दीजिए।",
                isHindi = true
            ),
            QuickReply(
                id = "hi_not_interested",
                title = "Interest nahi hai (कॉल मत करो)",
                spokenText = "धन्यवाद, मुझे इसमें कोई रुचि नहीं है। कृपया इस नंबर पर दोबारा कॉल न करें।",
                isHindi = true
            ),
            QuickReply(
                id = "hi_wrong_number",
                title = "Wrong number hai (गलत नंबर)",
                spokenText = "यह गलत नंबर है। कृपया नंबर दोबारा जांचकर सही जगह कॉल करें।",
                isHindi = true
            ),
            QuickReply(
                id = "hi_call_later",
                title = "Baad mein call karein (शाम को)",
                spokenText = "कृपया मुझे शाम को कॉल कीजिए, धन्यवाद।",
                isHindi = true
            )
        )

        // Industry-Ready Native Assamese Quick Replies (অসমীয়া)
        val ASSAMESE_REPLIES = listOf(
            QuickReply(
                id = "as_who",
                title = "Kon koise? (কোন কৈছে?)",
                spokenText = "নমস্কাৰ, আপুনি কোন কৈছে আৰু কাৰ লগত কথা পাতিব বিচাৰিছে?",
                isHindi = false
            ),
            QuickReply(
                id = "as_reason",
                title = "Kiba kaam aase? (কি কাৰণে?)",
                spokenText = "অনুগ্ৰহ কৰি কওক আপুনি কি সম্পৰ্কে ফোন কৰিছে?",
                isHindi = false
            ),
            QuickReply(
                id = "as_busy",
                title = "Moi byasto aasu (ব্যস্ত আছোঁ)",
                spokenText = "বেয়া নাপাব, মই এতিয়া অকণমান ব্যস্ত আছোঁ। পাছত ফোন কৰিব পাৰিবনে?",
                isHindi = false
            ),
            QuickReply(
                id = "as_delivery",
                title = "Parcel / Delivery (দুৱাৰমুখত)",
                spokenText = "নমস্কাৰ, যদি কোনো পাৰ্চেল বা কুৰিয়াৰ আছে, দুৱাৰমুখত থৈ দিয়ক।",
                isHindi = false
            ),
            QuickReply(
                id = "as_wrong_number",
                title = "Bhul number (ভুল নম্বৰ)",
                spokenText = "এইটো ভুল নম্বৰ হয়। অনুগ্ৰহ কৰি নম্বৰটো পৰীক্ষা কৰি সঠিক নম্বৰত ফোন কৰক।",
                isHindi = false
            )
        )

        // Standard Professional English Quick Replies
        val ENGLISH_REPLIES = listOf(
            QuickReply(
                id = "who",
                title = "Who is calling?",
                spokenText = "Hello, who is calling, and how may I assist you?",
                isHindi = false
            ),
            QuickReply(
                id = "reason",
                title = "What is this regarding?",
                spokenText = "Could you please tell me what this call is regarding?",
                isHindi = false
            ),
            QuickReply(
                id = "busy",
                title = "Busy, call back later",
                spokenText = "I am currently busy right now. Please call back later or leave a text message.",
                isHindi = false
            ),
            QuickReply(
                id = "message",
                title = "Take a message",
                spokenText = "I cannot take your call at the moment. Please state your message and I will get back to you.",
                isHindi = false
            ),
            QuickReply(
                id = "delivery",
                title = "Delivery / Drop at door",
                spokenText = "Hello, if this is a delivery, please leave the package at the front door. Thank you.",
                isHindi = false
            ),
            QuickReply(
                id = "not_interested",
                title = "Not interested / Remove",
                spokenText = "I am not interested. Please remove my number from your call list. Thank you.",
                isHindi = false
            )
        )

        val DEFAULT_REPLIES = HINDI_REPLIES + ASSAMESE_REPLIES + ENGLISH_REPLIES
    }
}
