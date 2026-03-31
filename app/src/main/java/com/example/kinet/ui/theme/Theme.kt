package com.example.kinet.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DefaultDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val DefaultLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanBlue80,
    secondary = OceanTeal80,
    tertiary = OceanCyan80
)

private val OceanLightColorScheme = lightColorScheme(
    primary = OceanBlue40,
    secondary = OceanTeal40,
    tertiary = OceanCyan40
)

private val ForestDarkColorScheme = darkColorScheme(
    primary = ForestGreen80,
    secondary = ForestEarth80,
    tertiary = ForestMoss80
)

private val ForestLightColorScheme = lightColorScheme(
    primary = ForestGreen40,
    secondary = ForestEarth40,
    tertiary = ForestMoss40
)

private val SunsetDarkColorScheme = darkColorScheme(
    primary = SunsetOrange80,
    secondary = SunsetRose80,
    tertiary = SunsetAmber80
)

private val SunsetLightColorScheme = lightColorScheme(
    primary = SunsetOrange40,
    secondary = SunsetRose40,
    tertiary = SunsetAmber40
)

@Composable
fun KinetTheme(
    appTheme: AppTheme = AppTheme.DYNAMIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when (appTheme) {
        AppTheme.DYNAMIC -> when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            darkTheme -> DefaultDarkColorScheme
            else -> DefaultLightColorScheme
        }
        AppTheme.OCEAN   -> if (darkTheme) OceanDarkColorScheme   else OceanLightColorScheme
        AppTheme.FOREST  -> if (darkTheme) ForestDarkColorScheme  else ForestLightColorScheme
        AppTheme.SUNSET  -> if (darkTheme) SunsetDarkColorScheme  else SunsetLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
