package com.soulscop.mostusedwidget.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MostUsedWidgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
    } else {
        lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
