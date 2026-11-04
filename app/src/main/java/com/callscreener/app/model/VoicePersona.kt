package com.callscreener.app.model

import java.util.Locale

enum class VoiceEffectType {
    NONE,
    ROBOTIC_RING_MOD,
    WALKIE_TALKIE_RADIO,
    DEEP_ANONYMOUS
}

enum class VoiceGender {
    FEMALE,
    MALE,
    NEUTRAL
}

data class VoicePersona(
    val id: String,
    val name: String,
    val tag: String,
    val code: String,
    val gender: VoiceGender,
    val pitch: Float = 1.0f, // Natural human pitch for TTS (1.0f = realistic female, no chipmunk effect)
    val speed: Float = 1.0f,
    val effectType: VoiceEffectType = VoiceEffectType.NONE,
    val description: String,
    val emoji: String = "",
    val dspPitchFactor: Float = 1.30f, // Live mic pitch shift factor for DSP
    val voiceKeywords: List<String> = emptyList(), // Specific neural voice matching keywords
    val isPureGirlVoice: Boolean = false, // Tag for the ultra-realistic pure girl voices
    val languageCode: String = "en",
    val countryCode: String = "US",
    val localeTag: String = "en-US",
    val isHindi: Boolean = false
) {
    val locale: Locale get() = Locale(languageCode, countryCode)

    companion object {
        // Industry-Ready Neural Hindi & Hinglish Voice Models (भारतीय हिंदी आवाज़ें)
        val HINDI_PERSONAS = listOf(
            VoicePersona(
                id = "hindi_pooja",
                name = "Pooja (पूजा)",
                tag = "Hindi • Real Girl",
                code = "HN1",
                gender = VoiceGender.FEMALE,
                pitch = 1.0f,
                speed = 1.0f,
                dspPitchFactor = 1.30f,
                voiceKeywords = listOf("hi-in", "hi_in", "hia", "hie", "hic", "hindi", "female", "hi-in-x-hia"),
                isPureGirlVoice = true,
                isHindi = true,
                languageCode = "hi",
                countryCode = "IN",
                localeTag = "hi-IN",
                effectType = VoiceEffectType.NONE,
                description = "Ultra-lifelike, authentic Indian young woman voice. Natural breathing and conversational Hindi pauses. 100% real girl feel for daily Hindi calls and pranks."
            ),
            VoicePersona(
                id = "hindi_ananya",
                name = "Ananya (अनन्या)",
                tag = "Hindi • Soft Spoken",
                code = "HN2",
                gender = VoiceGender.FEMALE,
                pitch = 1.02f,
                speed = 0.98f,
                dspPitchFactor = 1.32f,
                voiceKeywords = listOf("hi-in", "hie", "hic", "hia", "female", "hi-in-x-hie"),
                isPureGirlVoice = true,
                isHindi = true,
                languageCode = "hi",
                countryCode = "IN",
                localeTag = "hi-IN",
                effectType = VoiceEffectType.NONE,
                description = "Sweet, calm young female Hindi voice. Highly articulate Hindi pronunciation with a warm and friendly presence."
            ),
            VoicePersona(
                id = "hindi_kavya",
                name = "Kavya (काव्या)",
                tag = "Hinglish • Conversational",
                code = "HN3",
                gender = VoiceGender.FEMALE,
                pitch = 1.03f,
                speed = 1.0f,
                dspPitchFactor = 1.31f,
                voiceKeywords = listOf("hi-in", "en-in", "en_in", "cfl", "enc", "female", "hi-in-x-hic"),
                isPureGirlVoice = true,
                isHindi = true,
                languageCode = "hi",
                countryCode = "IN",
                localeTag = "hi-IN",
                effectType = VoiceEffectType.NONE,
                description = "Modern bilingual Indian female voice. Fluently switches between Hindi and English phrases just like everyday conversations in India."
            ),
            VoicePersona(
                id = "hindi_aarav",
                name = "Aarav (आरव)",
                tag = "Hindi • Pro Male",
                code = "HN4",
                gender = VoiceGender.MALE,
                pitch = 0.74f,
                speed = 1.0f,
                dspPitchFactor = 0.75f,
                voiceKeywords = listOf("hi-in-x-hid", "hi-in-x-hib", "hid", "hib", "hi-in-male", "male", "-m-"),
                isHindi = true,
                languageCode = "hi",
                countryCode = "IN",
                localeTag = "hi-IN",
                effectType = VoiceEffectType.NONE,
                description = "Respectful, crisp Hindi male assistant voice. Ideal for formal call screening, parcel deliveries, and official calls."
            ),
            VoicePersona(
                id = "hindi_vikram",
                name = "Vikram (विक्रम)",
                tag = "Hindi • Desi Guard",
                code = "HN5",
                gender = VoiceGender.MALE,
                pitch = 0.64f,
                speed = 0.93f,
                dspPitchFactor = 0.65f,
                voiceKeywords = listOf("hi-in-x-hid", "hi-in-x-hib", "hid", "hib", "hi-in-male", "male", "-m-"),
                isHindi = true,
                languageCode = "hi",
                countryCode = "IN",
                localeTag = "hi-IN",
                effectType = VoiceEffectType.DEEP_ANONYMOUS,
                description = "Low resonant, stern Hindi deterrent voice engineered to immediately deflect spam calls, telemarketers, and fraudsters."
            )
        )

        // Dedicated Ultra-Realistic "Pure Girl" Personas (English)
        val PURE_GIRL_PERSONAS = listOf(
            VoicePersona(
                id = "girl_chloe",
                name = "Chloe",
                tag = "Real Girl • Conversational",
                code = "G1",
                gender = VoiceGender.FEMALE,
                pitch = 1.0f,
                speed = 1.0f,
                dspPitchFactor = 1.30f,
                voiceKeywords = listOf("sfg", "female_1", "cfl", "iob", "fem", "en-us-x-sfg"),
                isPureGirlVoice = true,
                languageCode = "en",
                countryCode = "US",
                localeTag = "en-US",
                effectType = VoiceEffectType.NONE,
                description = "Ultra-lifelike, casual young woman with natural breathing and human warmth. Sounds 100% like a real girl for calls & pranks."
            ),
            VoicePersona(
                id = "girl_zoe",
                name = "Zoe",
                tag = "Real Girl • Cheerful",
                code = "G2",
                gender = VoiceGender.FEMALE,
                pitch = 1.02f,
                speed = 1.0f,
                dspPitchFactor = 1.32f,
                voiceKeywords = listOf("iob", "female_2", "tpf", "fem", "en-us-x-iob"),
                isPureGirlVoice = true,
                languageCode = "en",
                countryCode = "US",
                localeTag = "en-US",
                effectType = VoiceEffectType.NONE,
                description = "Sweet, expressive young girl voice with friendly inflections and natural conversational pauses."
            ),
            VoicePersona(
                id = "girl_mia",
                name = "Mia",
                tag = "Real Girl • Soft Spoken",
                code = "G3",
                gender = VoiceGender.FEMALE,
                pitch = 0.98f,
                speed = 0.98f,
                dspPitchFactor = 1.28f,
                voiceKeywords = listOf("tpf", "female_3", "rjs", "cfl", "en-us-x-tpf"),
                isPureGirlVoice = true,
                languageCode = "en",
                countryCode = "US",
                localeTag = "en-US",
                effectType = VoiceEffectType.NONE,
                description = "Soft, calm, ultra-natural female cadence with gentle acoustic presence."
            )
        )

        // Classic Personas
        val CLASSIC_PERSONAS = listOf(
            VoicePersona(
                id = "female_assistant",
                name = "Sarah",
                tag = "Studio Pro",
                code = "F1",
                gender = VoiceGender.FEMALE,
                pitch = 1.0f,
                speed = 1.0f,
                dspPitchFactor = 1.28f,
                voiceKeywords = listOf("sfg", "female", "en-us"),
                languageCode = "en",
                countryCode = "US",
                localeTag = "en-US",
                effectType = VoiceEffectType.NONE,
                description = "Warm conversational cadence, balanced studio clarity"
            ),
            VoicePersona(
                id = "female_executive",
                name = "Emma",
                tag = "Executive",
                code = "F2",
                gender = VoiceGender.FEMALE,
                pitch = 1.02f,
                speed = 1.02f,
                dspPitchFactor = 1.26f,
                voiceKeywords = listOf("iob", "female", "en-us"),
                languageCode = "en",
                countryCode = "US",
                localeTag = "en-US",
                effectType = VoiceEffectType.NONE,
                description = "Crisp corporate receptionist tone, articulate delivery"
            ),
            VoicePersona(
                id = "female_friendly",
                name = "Lily",
                tag = "Bright Youth",
                code = "F3",
                gender = VoiceGender.FEMALE,
                pitch = 1.04f,
                speed = 1.02f,
                dspPitchFactor = 1.33f,
                voiceKeywords = listOf("tpf", "female", "en-us"),
                languageCode = "en",
                countryCode = "US",
                localeTag = "en-US",
                effectType = VoiceEffectType.NONE,
                description = "Bright youthful acoustic profile with presence boost"
            ),
            VoicePersona(
                id = "deep_guard",
                name = "Marcus",
                tag = "Deep Guard",
                code = "M1",
                gender = VoiceGender.MALE,
                pitch = 0.72f,
                speed = 0.95f,
                dspPitchFactor = 0.72f,
                languageCode = "en",
                countryCode = "US",
                localeTag = "en-US",
                effectType = VoiceEffectType.DEEP_ANONYMOUS,
                description = "Low resonant acoustic filter designed for deterrence"
            ),
            VoicePersona(
                id = "cyber_cyborg",
                name = "Cyber Synth",
                tag = "Robotic DSP",
                code = "DSP1",
                gender = VoiceGender.NEUTRAL,
                pitch = 0.90f,
                speed = 1.10f,
                dspPitchFactor = 1.0f,
                languageCode = "en",
                countryCode = "US",
                localeTag = "en-US",
                effectType = VoiceEffectType.ROBOTIC_RING_MOD,
                description = "Harmonic ring-modulated metallic synthesizer"
            ),
            VoicePersona(
                id = "walkie_talkie",
                name = "Radio Dispatch",
                tag = "Bandpass",
                code = "DSP2",
                gender = VoiceGender.MALE,
                pitch = 1.0f,
                speed = 1.15f,
                dspPitchFactor = 1.0f,
                languageCode = "en",
                countryCode = "US",
                localeTag = "en-US",
                effectType = VoiceEffectType.WALKIE_TALKIE_RADIO,
                description = "Band-limited dispatcher emergency communications filter"
            )
        )

        val ALL_PERSONAS = HINDI_PERSONAS + PURE_GIRL_PERSONAS + CLASSIC_PERSONAS

        val DEFAULT = HINDI_PERSONAS[0] // Default is Pooja (Real Hindi Girl)
    }
}
