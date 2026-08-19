package com.decideforme.presentation.home

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.decideforme.data.model.Category
import com.decideforme.domain.ShakeDetector
import com.decideforme.presentation.components.ConfettiAnimation
import com.decideforme.presentation.components.ContextBadge
import com.decideforme.presentation.components.MoodSelector
import com.decideforme.presentation.components.SlotMachineAnimation

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showSlotMachine by remember { mutableStateOf(false) }
    var slotMachineComplete by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    // Shake-to-decide (respects user setting)
    val shakeEnabled = uiState.shakeToDecideEnabled

    // Use a ref for the shake callback to avoid capturing stale state
    val currentUiState = rememberUpdatedState(uiState)
    val shakeDetector = remember {
        ShakeDetector(context) {
            val state = currentUiState.value
            if (!state.showResult && state.categories.isNotEmpty()) {
                showSlotMachine = true
                viewModel.decide()
            }
        }
    }

    // Lifecycle-aware shake detection
    DisposableEffect(lifecycleOwner, shakeEnabled) {
        val observer = if (shakeEnabled) {
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> shakeDetector.start()
                    Lifecycle.Event.ON_PAUSE -> shakeDetector.stop()
                    else -> {}
                }
            }.also { lifecycleOwner.lifecycle.addObserver(it) }
        } else {
            shakeDetector.stop()
            null
        }
        onDispose {
            observer?.let { lifecycleOwner.lifecycle.removeObserver(it) }
            shakeDetector.stop()
        }
    }

    // Confetti on milestone streaks
    LaunchedEffect(uiState.currentStreak) {
        if (uiState.currentStreak > 0 && uiState.currentStreak % 7 == 0) {
            showConfetti = true
            kotlinx.coroutines.delay(4000)
            showConfetti = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Greeting & Streak
            GreetingSection(
                greeting = uiState.greeting,
                streak = uiState.currentStreak,
                todayDecisions = uiState.todayDecisions
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Context badge (time of day, weather)
            ContextBadge(
                timeOfDay = com.decideforme.domain.DecisionEngine.getCurrentTimeOfDay().name.lowercase(),
                weather = "any", // from settings
                dayType = com.decideforme.domain.DecisionEngine.getCurrentDayType().name.lowercase()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mood selector
            MoodSelector(
                currentMood = uiState.currentMood,
                onMoodSelected = viewModel::setMood
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category selector
            if (uiState.categories.isNotEmpty() && !uiState.showResult) {
                CategorySelector(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategory,
                    onSelect = viewModel::selectCategory
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Slot machine or decide button or result
            AnimatedContent(
                targetState = Triple(uiState.showResult, showSlotMachine, slotMachineComplete),
                transitionSpec = {
                    scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn() togetherWith
                            scaleOut() + fadeOut()
                },
                label = "decide_content"
            ) { (showResult, slotting, _) ->
                when {
                    slotting && !showResult -> {
                        // Slot machine animation
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎰 Shaking things up...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SlotMachineAnimation(
                                options = uiState.selectedCategory?.options?.map { it.name }
                                    ?: uiState.categories.flatMap { it.options.map { o -> o.name } },
                                finalChoice = uiState.currentDecision?.name ?: "???",
                                isAnimating = true,
                                onAnimationComplete = {
                                    showSlotMachine = false
                                    slotMachineComplete = true
                                }
                            )
                        }
                    }
                    showResult && uiState.currentDecision != null -> {
                        DecisionResult(
                            optionName = uiState.currentDecision!!.name,
                            categoryName = uiState.selectedCategory?.name ?: "",
                            onAccept = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                triggerHaptic(context)
                                viewModel.acceptDecision()
                                slotMachineComplete = false
                            },
                            onReject = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.rejectAndGetAnother()
                            },
                            onDismiss = {
                                viewModel.dismissResult()
                                slotMachineComplete = false
                            }
                        )
                    }
                    else -> {
                        DecideButton(
                            enabled = uiState.categories.isNotEmpty(),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                triggerHaptic(context)
                                viewModel.decide()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Quick stats + shake hint
            if (!uiState.showResult && !showSlotMachine) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    QuickStats(
                        streak = uiState.currentStreak,
                        todayCount = uiState.todayDecisions
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📱 Shake your phone for a surprise decision",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Confetti overlay
        ConfettiAnimation(
            isPlaying = showConfetti,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun triggerHaptic(context: android.content.Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    } catch (_: Exception) { }
}

@Composable
private fun GreetingSection(
    greeting: String,
    streak: Int,
    todayDecisions: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (todayDecisions > 0) "$todayDecisions decisions made today"
                else "Ready to stop overthinking?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (streak > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streak",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySelector(
    categories: List<Category>,
    selectedCategory: Category?,
    onSelect: (Category) -> Unit
) {
    Column {
        Text(
            text = "What do you need help with?",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category.id == selectedCategory?.id
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(category) },
                    label = { Text(category.name) },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(category.icon),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DecideButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(if (enabled) pulseScale else 1f)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .clickable(enabled = enabled) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DECIDE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (enabled) "Tap to let me decide" else "Add options to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun DecisionResult(
    optionName: String,
    categoryName: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    val enterAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterAnimation.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(enterAnimation.value),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = optionName,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reject button
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Try another")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nah")
                }

                // Accept button
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Accept")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Let's go!")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun QuickStats(
    streak: Int,
    todayCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatChip(
            icon = Icons.Default.LocalFireDepartment,
            value = "$streak days",
            label = "Streak"
        )
        StatChip(
            icon = Icons.Default.CheckCircle,
            value = "$todayCount",
            label = "Today"
        )
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "restaurant" -> Icons.Default.Restaurant
        "fitness_center" -> Icons.Default.FitnessCenter
        "checkroom" -> Icons.Default.Checkroom
        "movie" -> Icons.Default.Movie
        "favorite" -> Icons.Default.Favorite
        "weekend" -> Icons.Default.Weekend
        else -> Icons.Default.Category
    }
}
