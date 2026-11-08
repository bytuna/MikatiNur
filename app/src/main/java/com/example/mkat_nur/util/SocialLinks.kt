package com.example.mkat_nur.util

/**
 * Sosyal medya bağlantıları. Instagram kullanıcı adı (başında @ olmadan).
 * Boş bırakılırsa kartta Instagram bağlantısı gösterilmez.
 */
object SocialLinks {
    const val INSTAGRAM_HANDLE = "mikati_nur"

    val instagramUrl: String? get() = if (INSTAGRAM_HANDLE.isBlank()) null else "https://www.instagram.com/$INSTAGRAM_HANDLE/"
}
