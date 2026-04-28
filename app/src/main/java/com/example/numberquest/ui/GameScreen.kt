package com.example.numberquest.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.numberquest.GameViewModel
import com.example.numberquest.Screen
import com.example.numberquest.model.Player
import com.example.numberquest.model.TurnResult
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(vm: GameViewModel) {
    val players by vm.players.collectAsState()
    val currentIndex by vm.currentTurnIndex.collectAsState()
    val lastResult by vm.lastResult.collectAsState()
    val history by vm.history.collectAsState()
    val hapticsEnabled by vm.hapticsEnabled.collectAsState()
    val soundEnabled by vm.soundEnabled.collectAsState()

    if (players.isEmpty()) return
    val player = players[currentIndex.coerceIn(0, players.lastIndex)]

    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(player.color)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${player.name}'s turn",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { vm.resetGame() }) {
                        Icon(Icons.Default.Close, contentDescription = "Exit game")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.goTo(Screen.History) }) {
                        BadgedBox(badge = {
                            if (history.isNotEmpty()) {
                                Badge { Text("${history.size}") }
                            }
                        }) {
                            Icon(Icons.Default.History, contentDescription = "History")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlayerTurnDots(
                players = players,
                currentIndex = currentIndex,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = lastResult,
                    label = "reveal_state",
                    transitionSpec = {
                        if (targetState != null) {
                            // Idle -> Revealed: a quick fade with a tiny scale-up
                            (fadeIn(tween(180)) + scaleIn(
                                initialScale = 0.85f,
                                animationSpec = tween(220, easing = FastOutSlowInEasing)
                            )) togetherWith
                                (fadeOut(tween(120)) + scaleOut(targetScale = 1.05f))
                        } else {
                            // Revealed -> Idle (next player)
                            (fadeIn(tween(160)) + scaleIn(initialScale = 0.95f)) togetherWith
                                (fadeOut(tween(120)) + scaleOut(targetScale = 0.9f))
                        }
                    }
                ) { result ->
                    if (result == null) {
                        IdleCapsule(
                            color = player.color,
                            onTap = {
                                if (hapticsEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                if (soundEnabled) {
                                    // Soft, system-friendly click — not casino-like.
                                    view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                                }
                                vm.drawNumbers()
                            }
                        )
                    } else {
                        RevealedNumbers(
                            first = result.first,
                            second = result.second,
                            color = result.playerColor
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = lastResult != null,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val nextPlayer = players[(currentIndex + 1) % players.size]
                    Text(
                        text = "Next: ${nextPlayer.name}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (hapticsEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            vm.nextTurn()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = nextPlayer.color,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Pass to ${nextPlayer.name}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/* ---------------------------------------------------------------------------
   Idle capsule — a colored "mystery box" that breathes gently to invite a tap.
   Deliberately NOT a cube and NOT a wheel. Simple rounded pill with sparkle.
   --------------------------------------------------------------------------- */

@Composable
private fun IdleCapsule(color: Color, onTap: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "idle_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(240.dp)
            .scale(pulse)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(48.dp))
            .clip(RoundedCornerShape(48.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        color,
                        color.darken(0.15f)
                    )
                )
            )
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Tap to Open",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/* ---------------------------------------------------------------------------
   Revealed numbers — two cards bounce in side-by-side. The second card lags
   slightly so the reveal feels playful rather than synchronized.
   --------------------------------------------------------------------------- */

@Composable
private fun RevealedNumbers(first: Int, second: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberCard(value = first, color = color, delayMillis = 0)
            NumberCard(value = second, color = color, delayMillis = 90)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Total: ${first + second}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NumberCard(value: Int, color: Color, delayMillis: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        visible = false
        delay(delayMillis.toLong())
        visible = true
    }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    Box(
        modifier = Modifier
            .size(width = 110.dp, height = 150.dp)
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(color, color.darken(0.18f))
                )
            )
            .border(3.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            color = Color.White,
            fontSize = 80.sp,
            fontWeight = FontWeight.Black
        )
    }
}

/* ---------------------------------------------------------------------------
   Small turn indicator at the top: one dot per player, current one enlarged.
   --------------------------------------------------------------------------- */

@Composable
private fun PlayerTurnDots(
    players: List<Player>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        players.forEachIndexed { idx, p ->
            val isCurrent = idx == currentIndex
            val targetSize by animateFloatAsState(
                targetValue = if (isCurrent) 1f else 0.6f,
                animationSpec = tween(220),
                label = "dot_size"
            )
            Box(
                Modifier
                    .size(if (isCurrent) 16.dp else 12.dp)
                    .scale(targetSize / (if (isCurrent) 1f else 0.6f))
                    .clip(CircleShape)
                    .background(
                        if (isCurrent) p.color else p.color.copy(alpha = 0.35f)
                    )
            )
        }
    }
}

/* ---------------------------------------------------------------------------
   Helper: darken a Color by mixing it toward black.
   --------------------------------------------------------------------------- */
private fun Color.darken(fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = red * (1 - f),
        green = green * (1 - f),
        blue = blue * (1 - f),
        alpha = alpha
    )
}
