package com.boolder.boolder.view.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Composable
fun BoolderTheme(
    content: @Composable () -> Unit
) {
    val boolderColorSchemeLight = MaterialTheme.colorScheme.copy(
        primary = Color(0xFF65C466),
        onPrimary = Color.White,
        secondary = Color(0xFF65C466),
        onSecondary = Color(0xFF018786),
        background = Color(0xFFEBEBEB),
        onBackground = Color(0xFF4D4D4D),
        surface = Color.White,
        onSurface = Color.Black,
        onSurfaceVariant = Color(0xFFA5A5A5),
        outline = Color(0xFFE5E5E5),
        secondaryContainer = Color(0xFF65C466),
        onSecondaryContainer = Color.White
    )

    val boolderColorSchemeDark = MaterialTheme.colorScheme.copy(
        primary = Color(0xFF65C466),
        onPrimary = Color.White,
        secondary = Color(0xFF65C466),
        onSecondary = Color(0xFF018786),
        background = Color(0xFF131313),
        onBackground = Color(0xFFADADAD),
        surface = Color.Black,
        onSurface = Color.White,
        onSurfaceVariant = Color(0xFF888888),
        outline = Color(0xFF161616),
        secondaryContainer = Color(0xFF65C466),
        onSecondaryContainer = Color.White
    )

    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) {
            boolderColorSchemeDark
        } else {
            boolderColorSchemeLight
        },
        content = content
    )
}

@Immutable
object BoolderRippleTheme : RippleTheme {

    @Composable
    override fun defaultColor(): Color {
        val darkTheme = isSystemInDarkTheme()

        return RippleTheme.defaultRippleColor(
            contentColor = if (darkTheme) Color.White else Color.Black,
            lightTheme = !darkTheme
        )
    }

    @Composable
    override fun rippleAlpha(): RippleAlpha {
        val darkTheme = isSystemInDarkTheme()

        return RippleTheme.defaultRippleAlpha(
            contentColor = if (darkTheme) Color.White else Color.Black,
            lightTheme = !darkTheme
        )
    }
}

@Stable
val Color.Companion.BoolderOrange get() = Color(red = 255, green = 149, blue = 0)

@Stable
val Color.Companion.BoolderYellow get() = Color(red = 255, green = 204, blue = 0)

@Stable
val Color.Companion.BoolderTeal get() = Color(red = 48, green = 176, blue = 199)

@Stable
val Color.Companion.BoolderBlue get() = Color(red = 0, green = 122, blue = 255)
