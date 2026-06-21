package com.decideforme.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OceanLightScheme = lightColorScheme(
    primary = OceanPrimary,
    secondary = OceanSecondary,
    tertiary = OceanTertiary
)
private val OceanDarkScheme = darkColorScheme(
    primary = OceanPrimaryDark,
    secondary = OceanSecondaryDark,
    tertiary = OceanTertiaryDark
)

private val SunsetLightScheme = lightColorScheme(
    primary = SunsetPrimary,
    secondary = SunsetSecondary,
    tertiary = SunsetTertiary
)
private val SunsetDarkScheme = darkColorScheme(
    primary = SunsetPrimaryDark,
    secondary = SunsetSecondaryDark,
    tertiary = SunsetTertiaryDark
)

private val ForestLightScheme = lightColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary
)
private val ForestDarkScheme = darkColorScheme(
    primary = ForestPrimaryDark,
    secondary = ForestSecondaryDark,
    tertiary = ForestTertiaryDark
)

private val LavenderLightScheme = lightColorScheme(
    primary = LavenderPrimary,
    secondary = LavenderSecondary,
    tertiary = LavenderTertiary
)
private val LavenderDarkScheme = darkColorScheme(
    primary = LavenderPrimaryDark,
    secondary = LavenderSecondaryDark,
    tertiary = LavenderTertiaryDark
)

@Composable
fun DecideForMeTheme(
    themeMode: String = "system",
    colorPalette: String = "dynamic",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark", "amoled" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        colorPalette == "dynamic" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        colorPalette == "ocean" -> if (darkTheme) OceanDarkScheme else OceanLightScheme
        colorPalette == "sunset" -> if (darkTheme) SunsetDarkScheme else SunsetLightScheme
        colorPalette == "forest" -> if (darkTheme) ForestDarkScheme else ForestLightScheme
        colorPalette == "lavender" -> if (darkTheme) LavenderDarkScheme else LavenderLightScheme
        else -> if (darkTheme) LavenderDarkScheme else LavenderLightScheme
    }

    // AMOLED black override
    val finalScheme = if (themeMode == "amoled") {
        colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF1A1A1A)
        )
    } else colorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = finalScheme,
        typography = DecideForMeTypography,
        content = content
    )
}
