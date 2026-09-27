package com.example.mkat_nur.util

object QuranUtils {
    // Cüzlerin başlangıç sayfaları (Diyanet/Hafız Osman hattı)
    private val juzStartPages = listOf(
        1, 21, 41, 61, 81, 101, 121, 141, 161, 181,
        201, 221, 241, 261, 281, 301, 321, 341, 361, 381,
        401, 421, 441, 461, 481, 501, 521, 541, 561, 581
    )

    private val juzSurahMap = mapOf(
        1 to listOf(1, 2),
        2 to listOf(2),
        3 to listOf(2, 3),
        4 to listOf(3, 4),
        5 to listOf(4),
        6 to listOf(4, 5),
        7 to listOf(5, 6),
        8 to listOf(6, 7),
        9 to listOf(7, 8),
        10 to listOf(8, 9),
        11 to listOf(9, 10, 11),
        12 to listOf(11, 12),
        13 to listOf(12, 13, 14),
        14 to listOf(15, 16),
        15 to listOf(17, 18),
        16 to listOf(18, 19, 20),
        17 to listOf(21, 22),
        18 to listOf(23, 24, 25),
        19 to listOf(25, 26, 27),
        20 to listOf(27, 28, 29),
        21 to listOf(29, 30, 31, 32, 33),
        22 to listOf(33, 34, 35, 36),
        23 to listOf(36, 37, 38, 39),
        24 to listOf(39, 40, 41),
        25 to listOf(41, 42, 43, 44, 45),
        26 to listOf(46, 47, 48, 49, 50, 51),
        27 to listOf(51, 52, 53, 54, 55, 56, 57),
        28 to (58..66).toList(),
        29 to (67..77).toList(),
        30 to (78..114).toList()
    )

    private val surahStartPages = mapOf(
        1 to 1, 2 to 2, 3 to 50, 4 to 77, 5 to 106, 6 to 128, 7 to 151, 8 to 177, 9 to 187, 10 to 208,
        11 to 221, 12 to 235, 13 to 249, 14 to 255, 15 to 262, 16 to 267, 17 to 282, 18 to 293, 19 to 305, 20 to 312,
        21 to 322, 22 to 332, 23 to 342, 24 to 350, 25 to 359, 26 to 367, 27 to 377, 28 to 385, 29 to 396, 30 to 404,
        31 to 411, 32 to 415, 33 to 418, 34 to 428, 35 to 434, 36 to 440, 37 to 446, 38 to 453, 39 to 458, 40 to 467,
        41 to 477, 42 to 483, 43 to 489, 44 to 496, 45 to 499, 46 to 502, 47 to 507, 48 to 511, 49 to 515, 50 to 518,
        51 to 520, 52 to 523, 53 to 526, 54 to 528, 55 to 531, 56 to 534, 57 to 537, 58 to 542, 59 to 545, 60 to 549,
        61 to 551, 62 to 553, 63 to 554, 64 to 556, 65 to 558, 66 to 560, 67 to 562, 68 to 564, 69 to 566, 70 to 568,
        71 to 570, 72 to 572, 73 to 574, 74 to 575, 75 to 577, 76 to 578, 77 to 580, 78 to 582, 79 to 583, 80 to 585,
        81 to 586, 82 to 587, 83 to 587, 84 to 589, 85 to 590, 86 to 591, 87 to 591, 88 to 592, 89 to 593, 90 to 594,
        91 to 595, 92 to 595, 93 to 596, 94 to 596, 95 to 597, 96 to 597, 97 to 598, 98 to 598, 99 to 599, 100 to 599,
        101 to 600, 102 to 600, 103 to 601, 104 to 601, 105 to 601, 106 to 602, 107 to 602, 108 to 602, 109 to 603, 110 to 603,
        111 to 603, 112 to 604, 113 to 604, 114 to 604
    )

    fun normalizeForSearch(text: String?): String {
        if (text == null) return ""
        return text.lowercase(java.util.Locale("tr", "TR"))
            .replace('â', 'a')
            .replace('î', 'i')
            .replace('û', 'u')
            .replace('Â', 'a')
            .replace('Î', 'i')
            .replace('Û', 'u')
            .replace("'", "")
            .replace("’", "")
            .replace("`", "")
            .replace("-", "")
            .replace(" ", "")
    }

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

    fun getSurahIdsForJuz(juzNumber: Int): List<Int> {
        return juzSurahMap[juzNumber] ?: listOf(1)
    }

    fun getStartPageForSurah(surahId: Int): Int {
        return surahStartPages[surahId] ?: 1
    }
}
