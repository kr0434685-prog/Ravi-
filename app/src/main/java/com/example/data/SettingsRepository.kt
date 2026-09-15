package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "jarvis_settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val AUTO_SPEAK = booleanPreferencesKey("auto_speak")
        val LANGUAGE = stringPreferencesKey("language")
        val SPEECH_SPEED = floatPreferencesKey("speech_speed")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
    }

    val voiceEnabled: Flow<Boolean> = context.dataStore.data.map { it[VOICE_ENABLED] ?: true }
    val autoSpeak: Flow<Boolean> = context.dataStore.data.map { it[AUTO_SPEAK] ?: true }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "hi-IN" }
    val speechSpeed: Flow<Float> = context.dataStore.data.map { it[SPEECH_SPEED] ?: 1.0f }
    val speechPitch: Flow<Float> = context.dataStore.data.map { it[SPEECH_PITCH] ?: 1.0f }
    val customApiKey: Flow<String> = context.dataStore.data.map { it[CUSTOM_API_KEY] ?: "" }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VOICE_ENABLED] = enabled }
    }

    suspend fun setAutoSpeak(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_SPEAK] = enabled }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun setSpeechSpeed(speed: Float) {
        context.dataStore.edit { it[SPEECH_SPEED] = speed }
    }

    suspend fun setSpeechPitch(pitch: Float) {
        context.dataStore.edit { it[SPEECH_PITCH] = pitch }
    }

    suspend fun setCustomApiKey(apiKey: String) {
        context.dataStore.edit { it[CUSTOM_API_KEY] = apiKey.trim() }
    }
}
