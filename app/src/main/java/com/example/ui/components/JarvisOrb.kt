package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CyanJarvis
import com.example.ui.theme.BlueJarvis

enum class JarvisState {
    IDLE, LISTENING, PROCESSING, SPEAKING
}

@Composable
fun JarvisAssistant(state: JarvisState) {
    val infiniteTransition = rememberInfiniteTransition(label = "assistantTransition")
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "assistantPulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "assistantRotation"
    )

    Box(
        modifier = Modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Animated Background
        Box(
            modifier = Modifier
                .size(220.dp)
                .border(
                    width = 4.dp,
                    brush = Brush.sweepGradient(
                        colors = if (state != JarvisState.IDLE) 
                            listOf(CyanJarvis, BlueJarvis, CyanJarvis)
                        else 
                            listOf(CyanJarvis.copy(alpha = 0.3f), Color.Transparent, CyanJarvis.copy(alpha = 0.3f)),
                    ),
                    shape = CircleShape
                )
                .padding(4.dp)
        )

        // Avatar Image
        Image(
            painter = painterResource(id = R.drawable.img_assistant_avatar_1789371705121),
            contentDescription = "AI Assistant Avatar",
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (state == JarvisState.LISTENING) CyanJarvis else Color.Transparent,
                    shape = CircleShape
                ),
            contentScale = ContentScale.Crop
        )
        
        // Listening / Speaking Indicator Glow
        if (state != JarvisState.IDLE) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(
                        width = 8.dp * pulse,
                        brush = Brush.radialGradient(
                            colors = listOf(CyanJarvis.copy(alpha = 0.4f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}
