package com.dispo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Palette cirque / Looney Tunes
val Cream = Color(0xFFFFF8E7)
val CircusRed = Color(0xFFE63946)
val SunYellow = Color(0xFFFFD60A)
val CircusOrange = Color(0xFFFF6B35)
val DispoGreen = Color(0xFF2ECC71)
val DispoGreenDark = Color(0xFF1E8E4E)
val CircusPurple = Color(0xFF7B2CBF)
val InkBrown = Color(0xFF3D2B1F)

private val CircusColorScheme = lightColorScheme(
    primary = CircusRed,
    onPrimary = Color.White,
    secondary = SunYellow,
    onSecondary = InkBrown,
    tertiary = CircusPurple,
    background = Cream,
    onBackground = InkBrown,
    surface = Color.White,
    onSurface = InkBrown,
)

private val CircusTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Cursive,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
    ),
)

@Composable
fun DispoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CircusColorScheme,
        typography = CircusTypography,
        content = content,
    )
}
