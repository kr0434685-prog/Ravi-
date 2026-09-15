package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TTSManager(
    context: Context,
    private val onSpeechStart: () -> Unit = {},
    private val onSpeechDone: () -> Unit = {},
    private val onSpeechError: (String) -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var currentLangCode: String = "hi-IN"

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setLanguage(currentLangCode)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeechStart()
                }

                override fun onDone(utteranceId: String?) {
                    onSpeechDone()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeechDone()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    onSpeechDone()
                    Log.e("TTSManager", "TTS error code: $errorCode")
                }
            })
        } else {
            isInitialized = false
            onSpeechError("Text-to-speech initialization failed on this device.")
        }
    }

    fun isReady(): Boolean = isInitialized && tts != null

    fun setLanguage(langCode: String): Boolean {
        currentLangCode = langCode
        if (!isInitialized || tts == null) return false

        val locale = when {
            langCode.startsWith("hi", ignoreCase = true) -> Locale("hi", "IN")
            langCode.equals("en-IN", ignoreCase = true) -> Locale("en", "IN")
            else -> Locale.US
        }

        val result = tts?.setLanguage(locale)
        return if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to English if Hindi voice data isn't installed
            tts?.setLanguage(Locale.US)
            false
        } else {
            true
        }
    }

    fun speak(text: String, pitch: Float = 1.0f, speed: Float = 1.0f, language: String? = null) {
        if (!isInitialized || tts == null) return

        if (language != null && language != currentLangCode) {
            setLanguage(language)
        }

        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
        tts?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))
        val utteranceId = "JARVIS_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("TTSManager", "Error stopping TTS", e)
        }
        onSpeechDone()
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TTSManager", "Error destroying TTS", e)
        }
        tts = null
        isInitialized = false
    }
}
