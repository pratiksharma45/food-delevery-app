package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DilliBiteLightColorScheme = lightColorScheme(
    primary = TandoorCrimson,
    onPrimary = PureWhiteSurface,
    primaryContainer = TandoorSoftContainer,
    onPrimaryContainer = OnTandoorSoftContainer,
    secondary = SaffronGold,
    onSecondary = PureWhiteSurface,
    secondaryContainer = SaffronSoftContainer,
    onSecondaryContainer = OnSaffronSoftContainer,
    tertiary = CorianderVegGreen,
    onTertiary = PureWhiteSurface,
    tertiaryContainer = CorianderSoftContainer,
    onTertiaryContainer = CorianderVegGreen,
    background = CreamBackground,
    onBackground = EspressoInk,
    surface = PureWhiteSurface,
    onSurface = EspressoInk,
    surfaceVariant = WarmSandSurface,
    onSurfaceVariant = CharcoalMuted,
    surfaceContainerLowest = PureWhiteSurface,
    surfaceContainerLow = CreamBackground,
    surfaceContainer = WarmSandSurface,
    surfaceContainerHigh = WarmSandElevated,
    outline = WarmBorder,
    outlineVariant = WarmBorder,
    error = NonVegRed,
    onError = PureWhiteSurface
)

val DilliBiteShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DilliBiteTheme(
    content: @Composable () -> Unit
) {
    // Strictly light-mode palette with high-contrast dark fonts as requested
    MaterialTheme(
        colorScheme = DilliBiteLightColorScheme,
        typography = DilliBiteTypography,
        shapes = DilliBiteShapes,
        content = content
    )
}
