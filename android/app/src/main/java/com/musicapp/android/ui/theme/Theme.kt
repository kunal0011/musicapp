package com.musicapp.android.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SpotifyDarkScheme = darkColorScheme(
    primary = Brand,
    onPrimary = Color.Black,
    primaryContainer = BrandDark,
    onPrimaryContainer = Color.White,
    secondary = BrandLight,
    onSecondary = Color.Black,
    secondaryContainer = SurfaceRaised,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentAmber,
    onTertiary = Color.Black,
    background = BackgroundBottom,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHighest = SurfaceHighlight,
    outline = SurfaceStroke,
    outlineVariant = SurfaceHighlight,
    error = AccentRed,
    onError = Color.White,
    inverseSurface = TextPrimary,
    inverseOnSurface = SurfaceBlack,
    inversePrimary = BrandDark,
    scrim = Color.Black.copy(alpha = 0.6f),
)

@Composable
fun MusicAppTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: transparent bars for immersive look
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = SpotifyDarkScheme,
        typography = Typography,
        content = content
    )
}
