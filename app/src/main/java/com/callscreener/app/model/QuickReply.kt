package com.callscreener.app.model

data class QuickReply(
    val id: String,
    val title: String,
    val spokenText: String,
    val isHindi: Boolean = false
) {
    companion object {
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

        val DEFAULT_REPLIES = HINDI_REPLIES + ENGLISH_REPLIES
    }
}
