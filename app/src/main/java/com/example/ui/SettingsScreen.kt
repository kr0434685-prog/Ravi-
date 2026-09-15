package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsRepository
import com.example.ui.theme.CyanJarvis
import com.example.ui.theme.DarkSurface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onTestSpeak: ((text: String) -> Unit)? = null,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val voiceEnabled by settingsRepository.voiceEnabled.collectAsState(initial = true)
    val autoSpeak by settingsRepository.autoSpeak.collectAsState(initial = true)
    val language by settingsRepository.language.collectAsState(initial = "hi-IN")
    val speechSpeed by settingsRepository.speechSpeed.collectAsState(initial = 1.0f)
    val speechPitch by settingsRepository.speechPitch.collectAsState(initial = 1.0f)
    val storedApiKey by settingsRepository.customApiKey.collectAsState(initial = "")

    var apiKeyInput by remember { mutableStateOf("") }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var isEditingKey by remember { mutableStateOf(false) }

    LaunchedEffect(storedApiKey) {
        if (!isEditingKey) {
            apiKeyInput = storedApiKey
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = CyanJarvis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyanJarvis)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = CyanJarvis
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Language Selection
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ASSISTANT LANGUAGE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanJarvis,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Voice recognition and speech language (Hindi & English supported)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = language == "hi-IN",
                            onClick = {
                                scope.launch {
                                    settingsRepository.setLanguage("hi-IN")
                                    Toast.makeText(context, "हिंदी भाषा सक्रिय", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text("🇮🇳 हिंदी (Hindi)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanJarvis,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.testTag("lang_chip_hindi")
                        )
                        FilterChip(
                            selected = language == "en-IN",
                            onClick = {
                                scope.launch {
                                    settingsRepository.setLanguage("en-IN")
                                    Toast.makeText(context, "English activated", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text("🌐 English") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanJarvis,
                                selectedLabelColor = Color.Black
                            ),
                            modifier = Modifier.testTag("lang_chip_english")
                        )
                    }
                }
            }

            // Section: Voice & Speech Options
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "VOICE & SPEECH CONTROLS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanJarvis,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Voice Input (Microphone)", color = Color.White) },
                        supportingContent = { Text("Allow speech recognition for commands", color = Color.White.copy(alpha = 0.6f)) },
                        trailingContent = {
                            Switch(
                                checked = voiceEnabled,
                                onCheckedChange = { scope.launch { settingsRepository.setVoiceEnabled(it) } },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyanJarvis, checkedTrackColor = CyanJarvis.copy(alpha = 0.4f))
                            )
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text("Auto-Speak Responses", color = Color.White) },
                        supportingContent = { Text("Read JARVIS answers out loud using TTS", color = Color.White.copy(alpha = 0.6f)) },
                        trailingContent = {
                            Switch(
                                checked = autoSpeak,
                                onCheckedChange = { scope.launch { settingsRepository.setAutoSpeak(it) } },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyanJarvis, checkedTrackColor = CyanJarvis.copy(alpha = 0.4f))
                            )
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                    // Speech Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Speed", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Text("${"%.1f".format(speechSpeed)}x", style = MaterialTheme.typography.labelMedium, color = CyanJarvis)
                    }
                    Slider(
                        value = speechSpeed,
                        onValueChange = { scope.launch { settingsRepository.setSpeechSpeed(it) } },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = CyanJarvis, activeTrackColor = CyanJarvis)
                    )

                    // Speech Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Pitch", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Text("${"%.1f".format(speechPitch)}x", style = MaterialTheme.typography.labelMedium, color = CyanJarvis)
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = { scope.launch { settingsRepository.setSpeechPitch(it) } },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = CyanJarvis, activeTrackColor = CyanJarvis)
                    )

                    if (onTestSpeak != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                val sample = if (language.startsWith("hi")) {
                                    "नमस्ते! मैं जार्विस हूँ, आपका निजी सहायक।"
                                } else {
                                    "Hello! I am JARVIS, your personal Android assistant."
                                }
                                onTestSpeak(sample)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanJarvis),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanJarvis.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Test Speech", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Voice Output")
                        }
                    }
                }
            }

            // Section: Gemini AI API Key (Optional)
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "GEMINI API KEY (OPTIONAL)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanJarvis,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Standard voice actions (YouTube, Google, Chrome, Maps, Calls, Time) work offline! Enter an API key only if you want open-ended AI conversation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            isEditingKey = true
                            apiKeyInput = it
                        },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                scope.launch {
                                    settingsRepository.setCustomApiKey(apiKeyInput.trim())
                                    Toast.makeText(context, "API Key saved", Toast.LENGTH_SHORT).show()
                                    isEditingKey = false
                                }
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isApiKeyVisible) "Hide key" else "Show key",
                                    tint = CyanJarvis
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanJarvis,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                scope.launch {
                                    settingsRepository.setCustomApiKey(apiKeyInput.trim())
                                    Toast.makeText(context, "API Key saved successfully!", Toast.LENGTH_SHORT).show()
                                    isEditingKey = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_api_key_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanJarvis)
                        ) {
                            Text("Save Key", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        if (storedApiKey.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        settingsRepository.setCustomApiKey("")
                                        apiKeyInput = ""
                                        isEditingKey = false
                                        Toast.makeText(context, "API Key removed", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f))
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }

            // Section: About
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ABOUT JARVIS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanJarvis,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("JARVIS Android Assistant", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = CyanJarvis)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Your futuristic offline-ready voice companion. Built to handle YouTube, Google, Chrome, Maps, and speech commands directly on your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
