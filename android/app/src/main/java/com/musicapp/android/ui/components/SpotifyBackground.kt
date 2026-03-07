package com.musicapp.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.musicapp.android.ui.theme.BackgroundBottom
import com.musicapp.android.ui.theme.BackgroundTop
import com.musicapp.android.ui.theme.HeroGradientEnd
import com.musicapp.android.ui.theme.HeroGradientStart

@Composable
fun SpotifyBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundTop, BackgroundBottom)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(HeroGradientStart.copy(alpha = 0.25f), HeroGradientEnd.copy(alpha = 0f))
                    )
                )
        )
        content()
    }
}
