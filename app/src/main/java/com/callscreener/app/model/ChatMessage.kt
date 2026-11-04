package com.callscreener.app.model

import java.util.UUID

enum class MessageSender {
    CALLER,
    USER_TTS,
    SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpeaking: Boolean = false
)

