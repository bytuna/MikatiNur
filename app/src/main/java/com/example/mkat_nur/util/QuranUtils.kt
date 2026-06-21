package com.example.mkat_nur.util

object QuranUtils {
    // Cüzlerin başlangıç sayfaları (Diyanet/Hafız Osman hattı)
    private val juzStartPages = listOf(
        1, 21, 41, 61, 81, 101, 121, 141, 161, 181,
        201, 221, 241, 261, 281, 301, 321, 341, 361, 381,
        401, 421, 441, 461, 481, 501, 521, 541, 561, 581
    )

    fun getJuzPageRange(juzNumber: Int): Pair<Int, Int> {
        val start = juzStartPages.getOrNull(juzNumber - 1) ?: 1
        val end = if (juzNumber == 30) 604 else (juzStartPages.getOrNull(juzNumber) ?: 605) - 1
        return start to end
    }

    fun getJuzByPage(pageNumber: Int): Int {
        for (i in juzStartPages.indices.reversed()) {
            if (pageNumber >= juzStartPages[i]) return i + 1
        }
        return 1
    }
}
