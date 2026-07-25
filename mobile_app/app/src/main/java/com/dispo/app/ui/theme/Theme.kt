package com.dispo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dispo.app.R

// Palette cirque / Looney Tunes
val Cream = Color(0xFFFFF8E7)
val CircusRed = Color(0xFFE63946)
val CircusRedDark = Color(0xFFB22030)
val SunYellow = Color(0xFFFFD60A)
val CircusOrange = Color(0xFFFF6B35)
val DispoGreen = Color(0xFF2ECC71)
val DispoGreenDark = Color(0xFF1E8E4E)
val CircusPurple = Color(0xFF7B2CBF)
val InkBrown = Color(0xFF3D2B1F)
val LedAmber = Color(0xFFFFB300)
val LedOff = Color(0xFF5A4632)
val Gold = Color(0xFFE8C547)
val GoldDark = Color(0xFFC9A020)

/** Police display cartoon (titres, gros textes). */
val BangersFamily = FontFamily(Font(R.font.bangers))

/** Police "barre LED" / dot-matrix (statuts, animations). */
val LedFamily = FontFamily(Font(R.font.vt323))

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
        fontFamily = BangersFamily,
        fontSize = 52.sp,
        letterSpacing = 3.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = BangersFamily,
        fontSize = 26.sp,
        letterSpacing = 1.5.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = LedFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 2.sp,
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
