package com.example.voice

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat

class VoiceManager(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String, Boolean) -> Unit,
    private val onPartialResult: (String) -> Unit = {},
    private val onListeningStateChanged: (Boolean) -> Unit = {}
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var currentLanguage: String = "hi-IN"

    init {
        mainHandler.post {
            setupRecognizer()
        }
    }

    private fun setupRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.d("VoiceManager", "SpeechRecognizer not present in this runtime.")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d("VoiceManager", "onReadyForSpeech")
                        isListening = true
                        mainHandler.post { onListeningStateChanged(true) }
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d("VoiceManager", "onBeginningOfSpeech")
                    }

                    override fun onRmsChanged(rmsdB: Float) {}

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d("VoiceManager", "onEndOfSpeech")
                        isListening = false
                        mainHandler.post { onListeningStateChanged(false) }
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        val isNoMatchOrTimeout = error == SpeechRecognizer.ERROR_NO_MATCH ||
                                error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT

                        if (isNoMatchOrTimeout) {
                            Log.d("VoiceManager", "Speech recognition idle/no-speech code: $error")
                        } else {
                            Log.w("VoiceManager", "Speech recognition operational code: $error")
                        }

                        mainHandler.post {
                            onListeningStateChanged(false)
                            val message = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording issue. Please check microphone."
                                SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer is initializing. Please tap again."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required. Please allow access."
                                SpeechRecognizer.ERROR_NETWORK -> "Network issue during speech recognition."
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network connection timed out."
                                SpeechRecognizer.ERROR_NO_MATCH -> if (currentLanguage.startsWith("hi")) {
                                    "आवाज़ सुनाई नहीं दी। दोबारा बोलने के लिए टैप करें।"
                                } else {
                                    "I didn't catch that. Tap to speak again, or type below."
                                }
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice engine is busy. Please try again."
                                SpeechRecognizer.ERROR_SERVER -> "Recognition server error. Please try again."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> if (currentLanguage.startsWith("hi")) {
                                    "कोई आवाज़ नहीं मिली। दोबारा बोलने के लिए टैप करें।"
                                } else {
                                    "No speech detected. Tap to speak again."
                                }
                                else -> "Recognition completed (code $error)."
                            }
                            onError(message, isNoMatchOrTimeout)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        mainHandler.post {
                            onListeningStateChanged(false)
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty() && matches[0].isNotBlank()) {
                                onResult(matches[0])
                            } else {
                                val emptyMsg = if (currentLanguage.startsWith("hi")) {
                                    "आवाज़ सुनाई नहीं दी। दोबारा बोलने के लिए टैप करें।"
                                } else {
                                    "I didn't catch that. Tap to speak again, or type below."
                                }
                                onError(emptyMsg, true)
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            mainHandler.post { onPartialResult(matches[0]) }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            Log.w("VoiceManager", "Could not initialize SpeechRecognizer", e)
            speechRecognizer = null
        }
    }

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening(language: String = "hi-IN") {
        currentLanguage = language

        // 1. Check microphone permission
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPermission) {
            onError("Microphone permission is required. Please grant permission.", false)
            return
        }

        // 2. Check recognizer availability
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError(
                "Speech recognition is not supported in the cloud preview. Please use the text input below, or install the APK on your phone.",
                false
            )
            return
        }

        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    setupRecognizer()
                }

                if (speechRecognizer == null) {
                    onError("Unable to initialize speech recognizer service.", false)
                    return@post
                }

                val safeLang = if (language.startsWith("hi", ignoreCase = true)) "hi-IN" else "en-IN"
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, safeLang)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, safeLang)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, if (safeLang.startsWith("hi")) "बोलिए, मैं सुन रहा हूँ..." else "Speak now...")
                }

                speechRecognizer?.startListening(intent)
                isListening = true
                onListeningStateChanged(true)
            } catch (e: Exception) {
                Log.w("VoiceManager", "Failed to start listening", e)
                isListening = false
                onListeningStateChanged(false)
                onError("Could not start microphone: ${e.message}", false)
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w("VoiceManager", "Error stopping speech recognizer", e)
            }
            isListening = false
            onListeningStateChanged(false)
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.w("VoiceManager", "Error destroying SpeechRecognizer", e)
            }
            isListening = false
        }
    }
}


