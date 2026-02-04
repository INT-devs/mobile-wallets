// Copyright (c) 2024-2026 The INTcoin Core developers
// Distributed under the MIT software license

package org.intcoin.wallet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/**
 * INTcoin brand colors - dark theme matching Qt desktop wallet
 * WCAG 2.1 AA compliant with proper contrast ratios
 */
object INTcoinColors {
    // Primary Backgrounds
    val Background = Color(0xFF0A0F1E)           // Deep navy - main background
    val BackgroundSecondary = Color(0xFF141B2E)  // Slightly lighter - cards
    val BackgroundTertiary = Color(0xFF1E2842)   // Input fields, buttons
    val Surface = Color(0xFF141B2E)
    val SurfaceVariant = Color(0xFF1E2842)

    // Accent Colors
    val Primary = Color(0xFF3399FF)              // INTcoin blue - primary actions
    val PrimaryHover = Color(0xFF66B3FF)         // Blue hover state
    val Accent = Color(0xFF00D4FF)               // Cyan - highlights, selected states
    val Secondary = Color(0xFF00D4FF)

    // Semantic Colors
    val Success = Color(0xFF10B75F)              // Green - received, confirmed
    val Warning = Color(0xFFF59E0B)              // Amber - pending, unconfirmed
    val Error = Color(0xFFEF4444)                // Red - errors, failed

    // Text Colors
    val TextPrimary = Color(0xFFF8FAFC)          // White - primary text
    val TextSecondary = Color(0xFF94A3B8)        // Gray - secondary text
    val TextTertiary = Color(0xFF64748B)         // Darker gray - hints
    val OnPrimary = Color(0xFF0A0F1E)            // Text on primary color

    // Border Colors
    val Border = Color(0xFF334155)               // Border, dividers
    val BorderFocused = Color(0xFF3399FF)        // Focus ring
}

/**
 * INTcoin dark color scheme for Material 3
 */
private val INTcoinDarkColorScheme = darkColorScheme(
    primary = INTcoinColors.Primary,
    onPrimary = INTcoinColors.OnPrimary,
    primaryContainer = INTcoinColors.Primary.copy(alpha = 0.2f),
    onPrimaryContainer = INTcoinColors.Primary,
    secondary = INTcoinColors.Secondary,
    onSecondary = INTcoinColors.OnPrimary,
    secondaryContainer = INTcoinColors.Secondary.copy(alpha = 0.2f),
    onSecondaryContainer = INTcoinColors.Secondary,
    tertiary = INTcoinColors.Accent,
    onTertiary = INTcoinColors.OnPrimary,
    background = INTcoinColors.Background,
    onBackground = INTcoinColors.TextPrimary,
    surface = INTcoinColors.Surface,
    onSurface = INTcoinColors.TextPrimary,
    surfaceVariant = INTcoinColors.SurfaceVariant,
    onSurfaceVariant = INTcoinColors.TextSecondary,
    error = INTcoinColors.Error,
    onError = Color.White,
    outline = INTcoinColors.Border,
    outlineVariant = INTcoinColors.Border.copy(alpha = 0.5f)
)

/**
 * INTcoin typography
 */
val INTcoinTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

/**
 * INTcoin shapes
 */
val INTcoinShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * INTcoin design constants
 */
object INTcoinDesign {
    val CornerRadiusSmall = 8.dp
    val CornerRadiusMedium = 12.dp
    val CornerRadiusLarge = 16.dp
    val CornerRadiusXL = 24.dp

    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp
    val PaddingLarge = 24.dp
    val PaddingXL = 32.dp

    val MinTouchTarget = 44.dp
    val ButtonHeight = 52.dp
    val IconSizeMedium = 24.dp
    val IconSizeLarge = 32.dp
    val IconSizeXL = 48.dp
}

/**
 * INTcoin theme composable
 */
@Composable
fun INTcoinTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = INTcoinDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = INTcoinTypography,
        shapes = INTcoinShapes,
        content = content
    )
}
