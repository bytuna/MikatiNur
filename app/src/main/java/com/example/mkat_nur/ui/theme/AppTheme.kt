package com.example.mkat_nur.ui.theme

import androidx.compose.ui.graphics.Color

data class ThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val cardBorder: Color,
    val drawerBackground: Color,
    val drawerTextColor: Color,
    val gradientColors: List<Color>
)

enum class AppTheme(
    val key: String,
    val title: String,
    val subtitle: String,
    val lightColors: ThemeColors,
    val darkColors: ThemeColors
) {
    KLASIK(
        key = "klasik",
        title = "Mîkat Orijinal (Klasik)",
        subtitle = "Orijinal Pasifik Mavi Gradyan & Altın Sarısı",
        lightColors = ThemeColors(
            background = Color(0xFF023E8A),
            surface = Color.White.copy(alpha = 0.12f),
            primary = Color.White,
            secondary = Color(0xFF0077B6),
            accent = Color(0xFFFFD700),
            textPrimary = Color.White,
            textSecondary = Color.White.copy(alpha = 0.7f),
            cardBorder = Color.White.copy(alpha = 0.2f),
            drawerBackground = Color(0xFF1B263B),
            drawerTextColor = Color.White,
            gradientColors = listOf(Color(0xFF023E8A), Color(0xFF0077B6))
        ),
        darkColors = ThemeColors(
            background = Color(0xFF0D1B2A),
            surface = Color.White.copy(alpha = 0.12f),
            primary = Color.White,
            secondary = Color(0xFF1B263B),
            accent = Color(0xFFFFD700),
            textPrimary = Color.White,
            textSecondary = Color.White.copy(alpha = 0.7f),
            cardBorder = Color(0xFFFFD700).copy(alpha = 0.35f),
            drawerBackground = Color(0xFF0D1B2A),
            drawerTextColor = Color.White,
            gradientColors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))
        )
    ),

    SUKUNET(
        key = "sukunet",
        title = "Sükûnet & Derinlik",
        subtitle = "Zümrüt Yeşili & Sıcak Krem (Dingin & Huzurlu)",
        lightColors = ThemeColors(
            background = Color(0xFFDCE8DE),
            surface = Color(0xFFF0F5F1),
            primary = Color(0xFF0A4426),
            secondary = Color(0xFF1B8A5A),
            accent = Color(0xFFB8860B),
            textPrimary = Color(0xFF082D18),
            textSecondary = Color(0xFF2E5340),
            cardBorder = Color(0xFF0A4426).copy(alpha = 0.25f),
            drawerBackground = Color(0xFF0A3C26),
            drawerTextColor = Color(0xFFFFFFFF),
            gradientColors = listOf(Color(0xFFCDE0D2), Color(0xFFDCE8DE))
        ),
        darkColors = ThemeColors(
            background = Color(0xFF071710),
            surface = Color(0xFF0F2B1D),
            primary = Color(0xFF34D399),
            secondary = Color(0xFF10B981),
            accent = Color(0xFFFBBF24),
            textPrimary = Color(0xFFECFDF5),
            textSecondary = Color(0xFFA7F3D0),
            cardBorder = Color(0xFFFBBF24).copy(alpha = 0.35f),
            drawerBackground = Color(0xFF071710),
            drawerTextColor = Color(0xFFECFDF5),
            gradientColors = listOf(Color(0xFF071710), Color(0xFF0F2B1D))
        )
    ),

    HAKIKAT(
        key = "hakikat",
        title = "Kozmik Gece & Kehribar",
        subtitle = "Gece Mavisi & Sıcak Amber (Modern & Şık)",
        lightColors = ThemeColors(
            background = Color(0xFFD9E2EC),
            surface = Color(0xFFEDF2F7),
            primary = Color(0xFF0F172A),
            secondary = Color(0xFF0284C7),
            accent = Color(0xFFD97706),
            textPrimary = Color(0xFF0B132B),
            textSecondary = Color(0xFF334155),
            cardBorder = Color(0xFF0F172A).copy(alpha = 0.25f),
            drawerBackground = Color(0xFF1E293B),
            drawerTextColor = Color(0xFFFFFFFF),
            gradientColors = listOf(Color(0xFFBCCCDC), Color(0xFFD9E2EC))
        ),
        darkColors = ThemeColors(
            background = Color(0xFF0A0F1D),
            surface = Color(0xFF141C2E),
            primary = Color(0xFF38BDF8),
            secondary = Color(0xFF60A5FA),
            accent = Color(0xFFF59E0B),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFF94A3B8),
            cardBorder = Color(0xFFF59E0B).copy(alpha = 0.35f),
            drawerBackground = Color(0xFF0A0F1D),
            drawerTextColor = Color(0xFFF8FAFC),
            gradientColors = listOf(Color(0xFF0A0F1D), Color(0xFF141C2E))
        )
    ),

    PARSOMEN(
        key = "parsomen",
        title = "Parşömen & Bordo",
        subtitle = "Sıcak Parşömen & Kömür Kahvesi (Göz Dostu E-Kitap)",
        lightColors = ThemeColors(
            background = Color(0xFFE8DCC4),
            surface = Color(0xFFF3EAD8),
            primary = Color(0xFF2C2523),
            secondary = Color(0xFF991B1B),
            accent = Color(0xFF991B1B),
            textPrimary = Color(0xFF211510),
            textSecondary = Color(0xFF5C4339),
            cardBorder = Color(0xFF2C2523).copy(alpha = 0.25f),
            drawerBackground = Color(0xFF3D261D),
            drawerTextColor = Color(0xFFF9F4E8),
            gradientColors = listOf(Color(0xFFDDD0B5), Color(0xFFE8DCC4))
        ),
        darkColors = ThemeColors(
            background = Color(0xFF1A1310),
            surface = Color(0xFF281E19),
            primary = Color(0xFFE7E5E4),
            secondary = Color(0xFFEF4444),
            accent = Color(0xFFF97316),
            textPrimary = Color(0xFFF5F5F4),
            textSecondary = Color(0xFFA8A29E),
            cardBorder = Color(0xFF57392B),
            drawerBackground = Color(0xFF1A1310),
            drawerTextColor = Color(0xFFF5F5F4),
            gradientColors = listOf(Color(0xFF1A1310), Color(0xFF281E19))
        )
    ),

    MONOKROM(
        key = "monokrom",
        title = "Aydınlık Safir & Minimal",
        subtitle = "Açık Kristal Mavi & Derin Okyanus (Ultra Minimal)",
        lightColors = ThemeColors(
            background = Color(0xFFD5E0EA),
            surface = Color(0xFFE8F0F7),
            primary = Color(0xFF092C48),
            secondary = Color(0xFF0077B6),
            accent = Color(0xFF0077B6),
            textPrimary = Color(0xFF051B2E),
            textSecondary = Color(0xFF334E68),
            cardBorder = Color(0xFF092C48).copy(alpha = 0.25f),
            drawerBackground = Color(0xFF0D3B66),
            drawerTextColor = Color(0xFFFFFFFF),
            gradientColors = listOf(Color(0xFFBFCFD9), Color(0xFFD5E0EA))
        ),
        darkColors = ThemeColors(
            background = Color(0xFF081C24),
            surface = Color(0xFF112E38),
            primary = Color(0xFFE2E8F0),
            secondary = Color(0xFF38BDF8),
            accent = Color(0xFF38BDF8),
            textPrimary = Color(0xFFF1F5F9),
            textSecondary = Color(0xFF94A3B8),
            cardBorder = Color(0xFF1E4B5A),
            drawerBackground = Color(0xFF081C24),
            drawerTextColor = Color(0xFFF1F5F9),
            gradientColors = listOf(Color(0xFF081C24), Color(0xFF112E38))
        )
    );

    fun getActiveColors(isDark: Boolean): ThemeColors {
        return if (isDark) darkColors else lightColors
    }

    companion object {
        fun fromKey(key: String): AppTheme {
            return entries.find { it.key == key } ?: KLASIK
        }
    }
}
