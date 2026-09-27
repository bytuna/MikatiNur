package com.example.mkat_nur.util

import androidx.compose.ui.graphics.Color

enum class TajweedRule(val title: String, val color: Color) {
    MADD_LAZIM("Zorunlu Uzatmalar (6 Hareke)", Color(0xFFB71C1C)),
    MADD_MUTTASIL_MUNFASIL("Uzatmalar (4-5 Hareke)", Color(0xFFE53935)),
    MADD_OPTIONAL("Tercihe Bağlı Kısa Uzatmalar", Color(0xFFFB8C00)),
    MADD_TABII("Normal Uzatma (2 Hareke)", Color(0xFFFDD835)),
    GHUNNAH_IHFA("İhfa ve Ğunne (Geniz Sesi)", Color(0xFF4CAF50)),
    QALQALAH("Kalkale (Vurgulu Harfler)", Color(0xFF29B6F6)),
    HEAVY_RA("Kalın Râ Vurguları", Color(0xFF1E88E5)),
    SILENT("Okunmayan / Geçiş Harfleri", Color(0xFF9E9E9E))
}
