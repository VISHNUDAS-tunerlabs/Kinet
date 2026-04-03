package com.example.kinet.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DefaultLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = AppBackground,
    surface = AppBackground
)

private val OceanLightColorScheme = lightColorScheme(
    primary = OceanBlue40,
    secondary = OceanTeal40,
    tertiary = OceanCyan40,
    background = AppBackground,
    surface = AppBackground
)

private val ForestLightColorScheme = lightColorScheme(
    primary = ForestGreen40,
    secondary = ForestEarth40,
    tertiary = ForestMoss40,
    background = AppBackground,
    surface = AppBackground
)

private val SunsetLightColorScheme = lightColorScheme(
    primary = SunsetOrange40,
    secondary = SunsetRose40,
    tertiary = SunsetAmber40,
    background = AppBackground,
    surface = AppBackground
)

@Composable
fun KinetTheme(
    appTheme: AppTheme = AppTheme.DYNAMIC,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // App is light-mode only — dark scheme variants removed intentionally
    val colorScheme = when (appTheme) {
        AppTheme.DYNAMIC ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                dynamicLightColorScheme(context).copy(background = AppBackground, surface = AppBackground)
            else
                DefaultLightColorScheme
        AppTheme.OCEAN   -> OceanLightColorScheme
        AppTheme.FOREST  -> ForestLightColorScheme
        AppTheme.SUNSET  -> SunsetLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
