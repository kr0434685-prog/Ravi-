package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.*
import com.example.data.*
import com.example.ui.components.JarvisState
import com.example.voice.*
import com.example.actions.ActionExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.room.Room

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        JarvisDatabase::class.java, "jarvis_db"
    ).build()

    private val commandDao = db.commandDao()
    private val historyManager = HistoryManager(commandDao)
    val settingsRepository = SettingsRepository(application)
    private val geminiManager = GeminiManager()
    private val commandParser = CommandParser(geminiManager)
    private val actionExecutor = ActionExecutor(application)

    val currentLanguage: StateFlow<String> = settingsRepository.language.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "hi-IN"
    )

    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    private val ttsManager: TTSManager = TTSManager(
        context = application,
        onSpeechStart = {
            _uiState.value = _uiState.value.copy(jarvisState = JarvisState.SPEAKING)
        },
        onSpeechDone = {
            if (_uiState.value.jarvisState == JarvisState.SPEAKING) {
                _uiState.value = _uiState.value.copy(jarvisState = JarvisState.IDLE)
            }
        },
        onSpeechError = { errorMsg ->
            _uiState.value = _uiState.value.copy(
                jarvisState = JarvisState.IDLE,
                errorMessage = errorMsg
            )
        }
    )

    private val voiceManager = VoiceManager(
        context = application,
        onResult = { text -> processCommand(text) },
        onError = { error, isNoticeOnly ->
            _uiState.value = _uiState.value.copy(
                jarvisResponse = error,
                errorMessage = if (isNoticeOnly) null else error,
                jarvisState = JarvisState.IDLE
            )
        },
        onPartialResult = { text ->
            _uiState.value = _uiState.value.copy(userCommand = text)
        },
        onListeningStateChanged = { isListening ->
            if (isListening) {
                _uiState.value = _uiState.value.copy(
                    jarvisState = JarvisState.LISTENING,
                    errorMessage = null
                )
            } else if (_uiState.value.jarvisState == JarvisState.LISTENING) {
                _uiState.value = _uiState.value.copy(jarvisState = JarvisState.IDLE)
            }
        }
    )

    init {
        viewModelScope.launch {
            val lang = settingsRepository.language.first()
            ttsManager.setLanguage(lang)
        }
    }

    fun handlePermissionDenied() {
        _uiState.value = _uiState.value.copy(
            jarvisState = JarvisState.IDLE,
            errorMessage = "Microphone permission denied. Please allow microphone access in your browser or app settings."
        )
    }

    fun isSpeechRecognitionAvailable(): Boolean = voiceManager.isAvailable()

    fun startListening() {
        ttsManager.stop()
        viewModelScope.launch {
            val isVoiceEnabled = settingsRepository.voiceEnabled.first()
            if (!isVoiceEnabled) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Voice input is disabled in Settings."
                )
                return@launch
            }

            val lang = settingsRepository.language.first()
            _uiState.value = _uiState.value.copy(
                jarvisState = JarvisState.LISTENING,
                userCommand = if (lang.startsWith("hi")) "सुन रहा हूँ..." else "Listening...",
                errorMessage = null
            )
            voiceManager.startListening(lang)
        }
    }

    fun stopListening() {
        voiceManager.stopListening()
        _uiState.value = _uiState.value.copy(jarvisState = JarvisState.IDLE)
    }

    fun stopSpeaking() {
        ttsManager.stop()
        _uiState.value = _uiState.value.copy(jarvisState = JarvisState.IDLE)
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = settingsRepository.language.first()
            val newLang = if (current.startsWith("hi")) "en-IN" else "hi-IN"
            settingsRepository.setLanguage(newLang)
            ttsManager.setLanguage(newLang)
            val msg = if (newLang.startsWith("hi")) "हिंदी भाषा चुनी गई।" else "English selected."
            _uiState.value = _uiState.value.copy(jarvisResponse = msg)
        }
    }

    fun executeYouTubeAction() {
        viewModelScope.launch {
            val lang = settingsRepository.language.first()
            val response = if (lang.startsWith("hi")) "यूट्यूब खोला जा रहा है" else "Opening YouTube"
            _uiState.value = _uiState.value.copy(
                userCommand = "Open YouTube",
                jarvisResponse = response
            )
            speakIfEnabled(response, lang)
            actionExecutor.openYouTube()
            historyManager.addHistory("Open YouTube", response, "YOUTUBE")
        }
    }

    fun executeGoogleAction() {
        viewModelScope.launch {
            val lang = settingsRepository.language.first()
            val response = if (lang.startsWith("hi")) "Google खोला जा रहा है" else "Opening Google"
            _uiState.value = _uiState.value.copy(
                userCommand = "Open Google",
                jarvisResponse = response
            )
            speakIfEnabled(response, lang)
            actionExecutor.openGoogle()
            historyManager.addHistory("Open Google", response, "GOOGLE")
        }
    }

    fun executeChromeAction() {
        viewModelScope.launch {
            val lang = settingsRepository.language.first()
            val response = if (lang.startsWith("hi")) "Chrome खोला जा रहा है" else "Opening Chrome"
            _uiState.value = _uiState.value.copy(
                userCommand = "Open Chrome",
                jarvisResponse = response
            )
            speakIfEnabled(response, lang)
            actionExecutor.openChrome()
            historyManager.addHistory("Open Chrome", response, "CHROME")
        }
    }

    fun executeMapsAction() {
        viewModelScope.launch {
            val lang = settingsRepository.language.first()
            val response = if (lang.startsWith("hi")) "Google Maps खोला जा रहा है" else "Opening Google Maps"
            _uiState.value = _uiState.value.copy(
                userCommand = "Open Maps",
                jarvisResponse = response
            )
            speakIfEnabled(response, lang)
            actionExecutor.openMaps()
            historyManager.addHistory("Open Maps", response, "MAPS")
        }
    }

    fun processCommand(command: String) {
        if (command.isBlank()) return
        _uiState.value = _uiState.value.copy(
            userCommand = command,
            jarvisState = JarvisState.PROCESSING,
            jarvisResponse = "Processing...",
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val customKey = settingsRepository.customApiKey.first()
                val lang = settingsRepository.language.first()
                val actionPlan = commandParser.parseCommand(command, customKey, lang)
                handleActionPlan(command, actionPlan)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    jarvisResponse = "Error: ${e.message}",
                    errorMessage = e.message,
                    jarvisState = JarvisState.IDLE
                )
            }
        }
    }

    private suspend fun handleActionPlan(command: String, actionPlan: ActionPlan) {
        val lang = settingsRepository.language.first()
        _uiState.value = _uiState.value.copy(
            jarvisResponse = actionPlan.response
        )

        speakIfEnabled(actionPlan.response, lang)
        historyManager.addHistory(command, actionPlan.response, actionPlan.action)

        if (!actionPlan.requires_confirmation) {
            actionExecutor.execute(actionPlan)
            if (_uiState.value.jarvisState != JarvisState.SPEAKING) {
                _uiState.value = _uiState.value.copy(jarvisState = JarvisState.IDLE)
            }
        } else {
            _uiState.value = _uiState.value.copy(pendingAction = actionPlan)
        }
    }

    private suspend fun speakIfEnabled(text: String, language: String) {
        val autoSpeak = settingsRepository.autoSpeak.first()
        val speed = settingsRepository.speechSpeed.first()
        val pitch = settingsRepository.speechPitch.first()
        if (autoSpeak) {
            _uiState.value = _uiState.value.copy(jarvisState = JarvisState.SPEAKING)
            ttsManager.speak(text, pitch, speed, language)
        }
    }

    fun speakCurrentResponse() {
        viewModelScope.launch {
            val lang = settingsRepository.language.first()
            val speed = settingsRepository.speechSpeed.first()
            val pitch = settingsRepository.speechPitch.first()
            val text = _uiState.value.jarvisResponse
            if (text.isNotBlank()) {
                _uiState.value = _uiState.value.copy(jarvisState = JarvisState.SPEAKING)
                ttsManager.speak(text, pitch, speed, lang)
            }
        }
    }

    fun speakText(text: String) {
        viewModelScope.launch {
            val lang = settingsRepository.language.first()
            val speed = settingsRepository.speechSpeed.first()
            val pitch = settingsRepository.speechPitch.first()
            if (text.isNotBlank()) {
                _uiState.value = _uiState.value.copy(jarvisState = JarvisState.SPEAKING)
                ttsManager.speak(text, pitch, speed, lang)
            }
        }
    }

    fun confirmAction() {
        val actionPlan = _uiState.value.pendingAction
        if (actionPlan != null) {
            actionExecutor.execute(actionPlan)
            _uiState.value = _uiState.value.copy(pendingAction = null)
        }
    }

    fun cancelAction() {
        _uiState.value = _uiState.value.copy(pendingAction = null)
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyManager.clearHistory()
        }
    }

    fun getAllHistory() = historyManager.getAllHistory()

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
        ttsManager.destroy()
    }
}

data class JarvisUiState(
    val userCommand: String = "",
    val jarvisResponse: String = "JARVIS initialized. Online and ready. / जार्विस तैयार है। बोलिए क्या आदेश है?",
    val jarvisState: JarvisState = JarvisState.IDLE,
    val pendingAction: ActionPlan? = null,
    val errorMessage: String? = null
)
