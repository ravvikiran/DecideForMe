package com.decideforme.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Favorite/pin toggle for options.
 * Pinned options get a significant weight boost in the decision engine,
 * making them more likely to be suggested.
 */
@Composable
fun FavoritePinButton(
    isPinned: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animateScale by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animateScale) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pin_scale",
        finishedListener = { animateScale = false }
    )

    Icon(
        imageVector = if (isPinned) Icons.Filled.Star else Icons.Outlined.StarOutline,
        contentDescription = if (isPinned) "Unpin" else "Pin as favorite",
        tint = if (isPinned) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .size(24.dp)
            .scale(scale)
            .clickable {
                animateScale = true
                onToggle()
            }
    )
}
