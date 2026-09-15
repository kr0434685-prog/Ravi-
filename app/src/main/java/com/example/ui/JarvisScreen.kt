package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.JarvisAssistant
import com.example.ui.components.JarvisState
import com.example.ui.theme.BlueJarvis
import com.example.ui.theme.CyanJarvis
import com.example.ui.theme.DarkSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisScreen(
    viewModel: JarvisViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var textInput by remember { mutableStateOf("") }
    var showTextInput by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            viewModel.handlePermissionDenied()
            Toast.makeText(
                context,
                "Microphone permission is required. Please grant it to use voice commands.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "JARVIS",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            color = CyanJarvis,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanJarvis.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanJarvis.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = if (currentLang.startsWith("hi")) "HI" else "EN",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanJarvis,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    // Quick language toggle
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("toggle_language_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Switch Language",
                            tint = CyanJarvis
                        )
                    }
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("nav_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat & History",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("nav_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status banner / language hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (uiState.jarvisState) {
                        JarvisState.LISTENING -> if (currentLang.startsWith("hi")) "सुन रहा हूँ..." else "Listening..."
                        JarvisState.PROCESSING -> if (currentLang.startsWith("hi")) "सोच रहा हूँ..." else "Processing..."
                        JarvisState.SPEAKING -> if (currentLang.startsWith("hi")) "बोल रहा हूँ..." else "Speaking..."
                        JarvisState.IDLE -> if (currentLang.startsWith("hi")) "तैयार हूँ • हिंदी" else "Online • English"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = when (uiState.jarvisState) {
                        JarvisState.LISTENING -> CyanJarvis
                        JarvisState.PROCESSING -> BlueJarvis
                        JarvisState.SPEAKING -> Color(0xFF00E676)
                        JarvisState.IDLE -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    }
                )

                TextButton(
                    onClick = { viewModel.toggleLanguage() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (currentLang.startsWith("hi")) "Switch to English" else "हिंदी में बदलें",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanJarvis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Central Animated Orb / Assistant Avatar
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                JarvisAssistant(state = uiState.jarvisState)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Response / Command Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(
                        width = 1.dp,
                        color = if (uiState.jarvisState != JarvisState.IDLE) CyanJarvis.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.userCommand.isNotEmpty()) {
                    Text(
                        text = "You: ${uiState.userCommand}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = uiState.jarvisResponse,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = CyanJarvis,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                // Voice playback controls
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.jarvisState == JarvisState.SPEAKING) {
                        FilledTonalButton(
                            onClick = { viewModel.stopSpeaking() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFD32F2F).copy(alpha = 0.2f),
                                contentColor = Color(0xFFFF5252)
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop Speech", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop", style = MaterialTheme.typography.labelSmall)
                        }
                    } else if (uiState.jarvisResponse.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.speakCurrentResponse() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Listen to response",
                                tint = CyanJarvis.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Error notice
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                uiState.errorMessage?.let { errorText ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF3E1F1F),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorText,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.dismissError() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Actions section
            Text(
                text = "QUICK INTENTS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.PlayArrow,
                    label = "YouTube",
                    testTag = "quick_action_youtube",
                    onClick = { viewModel.executeYouTubeAction() }
                )
                QuickActionButton(
                    icon = Icons.Default.Search,
                    label = "Google",
                    testTag = "quick_action_google",
                    onClick = { viewModel.executeGoogleAction() }
                )
                QuickActionButton(
                    icon = Icons.Default.Public,
                    label = "Chrome",
                    testTag = "quick_action_chrome",
                    onClick = { viewModel.executeChromeAction() }
                )
                QuickActionButton(
                    icon = Icons.Default.Map,
                    label = "Maps",
                    testTag = "quick_action_maps",
                    onClick = { viewModel.executeMapsAction() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Voice Button
            Box(
                modifier = Modifier.size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulse ring when listening
                if (uiState.jarvisState == JarvisState.LISTENING) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, CyanJarvis, CircleShape)
                    )
                }

                Button(
                    onClick = {
                        if (uiState.jarvisState == JarvisState.LISTENING) {
                            viewModel.stopListening()
                        } else {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                viewModel.startListening()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .testTag("voice_input_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    if (uiState.jarvisState == JarvisState.LISTENING)
                                        listOf(Color(0xFFFF5252), Color(0xFFD32F2F))
                                    else
                                        listOf(CyanJarvis, BlueJarvis)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.jarvisState == JarvisState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (uiState.jarvisState == JarvisState.LISTENING) "Stop Listening" else "Voice Input",
                            modifier = Modifier.size(38.dp),
                            tint = Color.Black
                        )
                    }
                }
            }

            Text(
                text = if (uiState.jarvisState == JarvisState.LISTENING) "TAP TO STOP" else "TAP TO SPEAK",
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = CyanJarvis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Toggle Text Input field
            TextButton(
                onClick = { showTextInput = !showTextInput }
            ) {
                Icon(
                    imageVector = if (showTextInput) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                    contentDescription = "Type Command",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showTextInput) "Hide text input" else "Or type a command",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(visible = showTextInput) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("command_text_input"),
                        placeholder = {
                            Text(
                                if (currentLang.startsWith("hi")) "कमांड लिखें (जैसे: YouTube खोलो)..." else "Type command (e.g. Open Maps)...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanJarvis,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.processCommand(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(CyanJarvis, RoundedCornerShape(12.dp))
                            .testTag("send_command_button")
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Action Confirmation Dialog
        if (uiState.pendingAction != null) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelAction() },
                title = { Text("Confirm Action", color = CyanJarvis, fontWeight = FontWeight.Bold) },
                text = { Text(uiState.pendingAction?.response ?: "Do you want to continue?", color = Color.White) },
                containerColor = DarkSurface,
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmAction() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanJarvis)
                    ) {
                        Text("ALLOW", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelAction() }) {
                        Text("CANCEL", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, testTag: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .testTag(testTag),
            onClick = onClick,
            shape = CircleShape,
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyanJarvis.copy(alpha = 0.3f)),
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = CyanJarvis, modifier = Modifier.size(24.dp))
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 6.dp),
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

